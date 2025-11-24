package com.rizvi.jobee.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tika.Tika;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileService {
    public String detectMimeType(URL url, byte[] contentBytes, String originalFileName) {
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.connect();
            String mimeType = connection.getContentType();
            if (mimeType != null && !mimeType.isEmpty() && !mimeType.equals("application/octet-stream")
                    && !mimeType.equals("application/json") && !mimeType.equals("text/html")) {
                return mimeType;
            }

            String contentDisposition = connection.getHeaderField("Content-Disposition");
            if (contentDisposition != null) {
                // pattern: filename="something.pdf" or filename=some.pdf
                Pattern p = Pattern.compile("filename\\*=UTF-8''(.+)|filename=\"?([^\";]+)\"?");
                Matcher m = p.matcher(contentDisposition);
                if (m.find()) {
                    String filename = m.group(1) != null ? m.group(1) : m.group(2);
                    if (filename != null && filename.contains(".")) {
                        String ext = filename.substring(filename.lastIndexOf("."));
                        String probe = probeMimeFromExtension(ext);
                        if (probe != null)
                            return probe;
                    }
                }
            }
        } catch (IOException e) {
            // TODO: handle exception
        }

        // Fallback: detect from original file name extension
        if (originalFileName != null && originalFileName.contains(".")) {
            String ext = originalFileName.substring(originalFileName.lastIndexOf("."));
            String probe = probeMimeFromExtension(ext);
            if (probe != null)
                return probe;
        }

        try {
            Path temp = Files.createTempFile("jobee_mime_detect_", null);
            Files.write(temp, contentBytes);
            String probe = Files.probeContentType(temp);
            Files.deleteIfExists(temp);
            System.out.println("Detected MIME type from file probe: " + probe);
            if (probe != null && !probe.equals("application/octet-stream")) {
                return probe;
            }
        } catch (Exception e) {
            System.out.println("Exception during MIME type probing: " + e.getMessage());
        }

        try {
            Tika tika = new Tika();
            String mimeType = tika.detect(contentBytes);
            if (mimeType != null && !mimeType.equals("application/octet-stream")) {
                return mimeType;
            }
        } catch (Throwable ignored) {
        }
        return "application/octet-stream";
    }

    private String probeMimeFromExtension(String ext) {
        if (ext == null)
            return null;
        ext = ext.toLowerCase();
        switch (ext) {
            case ".pdf":
                return "application/pdf";
            case ".doc":
                return "application/msword";
            case ".docx":
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case ".txt":
                return "text/plain";
            case ".rtf":
                return "application/rtf";
            case ".odt":
                return "application/vnd.oasis.opendocument.text";
            case ".jpg":
            case ".jpeg":
                return "image/jpeg";
            case ".png":
                return "image/png";
            default:
                return null;
        }
    }

    public byte[] convertDocxToPdf(byte[] docxBytes) throws Exception {
        Path tempDocx = Files.createTempFile("jobee_docx_", ".docx");
        Files.write(tempDocx, docxBytes);

        Path outputDir = tempDocx.getParent();
        ProcessBuilder pb = new ProcessBuilder(
                "soffice",
                "--headless",
                "--convert-to", "pdf",
                "--outdir", outputDir.toString(),
                tempDocx.toString());
        pb.redirectErrorStream(true);

        Process process = pb.start();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println("LIBREOFFICE: " + line);
            }
        }
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("LibreOffice failed, exit code: " + exitCode);
        }
        String pdfFileName = tempDocx.getFileName().toString().replace(".docx", ".pdf");
        Path realPdfPath = outputDir.resolve(pdfFileName);

        if (!Files.exists(realPdfPath) || Files.size(realPdfPath) == 0) {
            throw new RuntimeException("LibreOffice did not produce a PDF file");
        }
        byte[] pdfBytes = Files.readAllBytes(realPdfPath);
        Files.deleteIfExists(tempDocx);
        Files.deleteIfExists(realPdfPath);

        return pdfBytes;
    }

    public MultipartFile convertBytesToMultipartFile(byte[] fileBytes, String fileName, String contentType) {
        byte[] finalBytes = fileBytes;
        try {
            finalBytes = convertDocxToPdf(fileBytes);
        } catch (Exception e) {
            System.out.println("Error converting to PDF, returning original bytes: " + e.getMessage());
        }
        return new MockMultipartFile(
                fileName,
                fileName,
                contentType,
                finalBytes);
    }

}
