package dev.langchain4j.quarkus.workshop;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.splitter.DocumentBySentenceSplitter;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import io.quarkus.logging.Log;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static dev.langchain4j.data.document.Metadata.metadata;
import static io.smallrye.mutiny.unchecked.Unchecked.function;

@ApplicationScoped
public class RagIngestion {

    public static final String EXTENDED_CONTENT_KEY = "extended_content";
    public static final String FILE_KEY = "file";

    public void ingest(@Observes StartupEvent ev,
                       EmbeddingStore<TextSegment> store, EmbeddingModel embeddingModel,
                       @ConfigProperty(name = "rag.location") Path documents) throws IOException {
        store.removeAll(); // cleanup the store to start fresh (just for demo purposes)

        // Read the documents from the file system (and add the "file" metadata)
        List<Document> docs = readDocuments(documents);

        // Split by sentence, but also collect a context of 2 sentences before and after
        DocumentBySentenceSplitter splitter = new DocumentBySentenceSplitter(200, 20);
        List<TextSegment> segments = splitter.splitAll(docs);
        List<SegmentAndExtendedContext> segmentsWithContext = collectTextSegmentAndExtendedContent(segments, 2, 2);

        // Embed the segments and store them
        List<TextSegment> embeddedSegments = segmentsWithContext.stream()
                .map(SegmentAndExtendedContext::segment)
                .toList();
        List<Embedding> embeddings = embeddingModel.embedAll(embeddedSegments).content();
        store.addAll(embeddings, embeddedSegments);

        Log.info("Documents ingested successfully");
    }

    public record SegmentAndExtendedContext(TextSegment segment, String context) {

    }

    private static List<SegmentAndExtendedContext> collectTextSegmentAndExtendedContent(List<TextSegment> input,
                                                                                        int before, int after) {
        return IntStream.range(0, input.size())
                .mapToObj(i -> {
                    TextSegment textSegment = input.get(i);
                    String content = IntStream.rangeClosed(i - before, i + after)
                            .filter(j -> j >= 0 && j < input.size())
                            .mapToObj(j -> input.get(j).text())
                            .collect(Collectors.joining(" "));
                    textSegment.metadata()
                            .put(EXTENDED_CONTENT_KEY, content);
                    return new SegmentAndExtendedContext(textSegment, content);
                })
                .collect(Collectors.toList());
    }

    private static List<Document> readDocuments(Path documents) throws IOException {
        return Files.list(documents)
                .map(function(p -> {
                    String content = Files.readString(p);
                    return Document.document(content, metadata(FILE_KEY, p.getFileName().toString()));
                })).toList();
    }

}
