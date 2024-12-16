package me.escoffier.quarkus.ocr;

import dev.langchain4j.data.image.Image;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.ImageUrl;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.ApplicationScoped;

@RegisterAiService
@ApplicationScoped
public interface OCR {

    @UserMessage("""
            You take an image in and output the 
            text extracted from the image.
            """)
    String process(Image image);

}
