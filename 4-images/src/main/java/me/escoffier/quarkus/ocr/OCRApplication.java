package me.escoffier.quarkus.ocr;

import dev.langchain4j.data.image.Image;
import io.quarkus.logging.Log;
import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import jakarta.inject.Inject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;

@QuarkusMain
public class OCRApplication implements QuarkusApplication {

    @Inject
    OCR ocr;

    public static String encodeFileToBase64(File file) throws IOException {
        var content = Files.readAllBytes(file.toPath());
        return Base64.getEncoder().encodeToString(content);
    }

    @Override
    public int run(String... args) throws Exception {
        File file = new File("text.jpg");
        Log.infof("Converting image...");
        var image = Image.builder().base64Data(encodeFileToBase64(file))
                .mimeType("image/jpeg").build();
        Log.info("Processing image... ");

        System.out.println("----");
        System.out.println(ocr.process(image));
        System.out.println("----");

        return 0;
    }


    public static void main(String[] args) {
        Quarkus.run(OCRApplication.class, args);
    }
}
