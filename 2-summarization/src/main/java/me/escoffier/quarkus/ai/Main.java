package me.escoffier.quarkus.ai;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@QuarkusMain
public class Main implements QuarkusApplication {
    @Inject
    SummarizationService ai;

    @Override
    public int run(String... args) throws IOException {
        var article = Files.readString(new File("./2-summarization/load-shedding.txt").toPath());
        System.out.println(ai.summarize(article));
        return 0;
    }

    @RegisterAiService
    @ApplicationScoped
    @SystemMessage("""
            You are an expert content summarizer. You take content in and output a Markdown formatted summary using the format below.

            # OUTPUT SECTIONS
            - Combine all of your understanding of the content into a single, 20-word sentence in a section called ONE SENTENCE SUMMARY:.
            - Output the 10 most important points of the content as a list with no more than 15 words per point into a section called MAIN POINTS:.            
            - Output a list of the 5 best takeaways from the content in a section called TAKEAWAYS:.
            
            # OUTPUT INSTRUCTIONS            
            - Create the output using the formatting above.
            - You only output human readable Markdown.
            - Output numbered lists, not bullets.
            - Do not output warnings or notes—just the requested sections.
            - Do not repeat items in the output sections.
            - Do not start items with the same opening words.
        """)
    public interface SummarizationService {

        @UserMessage("Input: {input}")
        String summarize(String input);
    }


    public static void main(String[] args) {
        Quarkus.run(Main.class, args);
    }
}
