package dev.langchain4j.quarkus.workshop;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.CosineSimilarity;
import io.quarkiverse.langchain4j.response.AiResponseAugmenter;
import io.quarkiverse.langchain4j.response.ResponseAugmenterParams;
import io.smallrye.mutiny.Multi;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static dev.langchain4j.quarkus.workshop.RagIngestion.FILE_KEY;

@ApplicationScoped
public class SourceAugmenter implements AiResponseAugmenter<String> {

    @Inject
    EmbeddingModel embeddingModel;

    record SourceEmbedding(TextSegment textSegment, String file, Embedding embedding) {

    }

    @Override
    public Multi<String> augment(Multi<String> stream, ResponseAugmenterParams params) {
        var full = new StringBuilder();
        return stream
                .invoke(full::append)
                .onCompletion().continueWith(() -> List.of(" (Sources: "
                        + String.join(", ", getSources(full.toString(), params)) + ")")
                );
    }


    @Override
    public String augment(String response, ResponseAugmenterParams params) {
        if (params.augmentationResult().contents().isEmpty()) {
            return response;
        }
        Set<String> sources = getSources(response, params);
        return response + " (Sources: "
                + String.join(", ", sources) + ")";
    }

    private Set<String> getSources(String response, ResponseAugmenterParams params) {
        if (response == null || response.isBlank() || params.augmentationResult().contents().isEmpty()) {
            return Set.of();
        }
        var embeddingOfTheResponse = embeddingModel.embed(response).content();
        List<SourceEmbedding> sources = params.augmentationResult().contents().stream().map(c -> {
            var embedding = embeddingModel.embed(c.textSegment().text()).content();
            return new SourceEmbedding(c.textSegment(), c.textSegment().metadata().getString(FILE_KEY), embedding);
        }).toList();

        Set<SourceEmbedding> filtered = filter(embeddingOfTheResponse, sources);

        Set<String> names = new LinkedHashSet<>();
        for (var source : filtered) {
            names.add(source.file());
        }
        return names;
    }

    private Set<SourceEmbedding> filter(Embedding embeddingOfTheResponse, List<SourceEmbedding> contents) {
        Set<SourceEmbedding> filtered = new LinkedHashSet<>();
        for (SourceEmbedding content : contents) {
            double similarity = CosineSimilarity.between(embeddingOfTheResponse, content.embedding());
            if (similarity > 0.85) {
                System.out.println("Similarity: " + similarity + " : " + content.textSegment().text());
                filtered.add(content);
            } else {
                System.out.println("Similarity too low: " + similarity + " : " + content.textSegment().text());
            }
        }

        return filtered;
    }
}
