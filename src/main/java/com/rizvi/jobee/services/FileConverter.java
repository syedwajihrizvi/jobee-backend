package com.rizvi.jobee.services;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import org.docx4j.Docx4J;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.springframework.stereotype.Service;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class FileConverter {

    public byte[] convertDocxToPdf(InputStream docxInputStream) throws Exception {
        WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(docxInputStream);

        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            Docx4J.toPDF(wordMLPackage, os);
            return os.toByteArray();
        }
    }
}