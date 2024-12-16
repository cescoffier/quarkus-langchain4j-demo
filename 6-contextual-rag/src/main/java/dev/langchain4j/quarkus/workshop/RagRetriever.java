package dev.langchain4j.quarkus.workshop;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.query.transformer.CompressingQueryTransformer;
import dev.langchain4j.store.embedding.EmbeddingStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

import static dev.langchain4j.quarkus.workshop.RagIngestion.EXTENDED_CONTENT_KEY;

public class RagRetriever {

    @Produces
    @ApplicationScoped
    public RetrievalAugmentor createContextualizedRetriever(EmbeddingStore store, EmbeddingModel model, ChatLanguageModel chatModel) {
        var contentRetriever = getExtendedContentRetriever(store, model);

        return DefaultRetrievalAugmentor.builder()
                .queryTransformer(new CompressingQueryTransformer(chatModel))
                .contentRetriever(contentRetriever)
                .build();
    }

    ContentRetriever getExtendedContentRetriever(EmbeddingStore store, EmbeddingModel model) {
        EmbeddingStoreContentRetriever delegate = EmbeddingStoreContentRetriever.builder()
                .embeddingModel(model)
                .embeddingStore(store)
                .maxResults(3)
                .build();
        return query -> delegate.retrieve(query)
                .stream().map(c -> Content.from(getExtendedContent(c.textSegment())))
                .toList();
    }

    private TextSegment getExtendedContent(TextSegment segment) {
        var content = segment.metadata().getString(EXTENDED_CONTENT_KEY);
        var metadata = segment.metadata();
        return TextSegment.from(content, metadata.remove(EXTENDED_CONTENT_KEY));
    }
}
