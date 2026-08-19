package com.pmrs.backend.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Downloads a Google-Drive-linked document (resume, job description) and
 * extracts its plain text — the shared building block behind the resume/JD
 * skills-gap analyzer. Only PDF is supported for now (DOCX is best-effort,
 * not yet implemented); callers should treat a null return as "extraction
 * unavailable" rather than an error, since a bad/private link shouldn't break
 * the caller's flow.
 *
 * Reuses the RestTemplate bean from RestTemplateConfig — no Google Drive API
 * credentials involved, this relies on the file being shared as "anyone with
 * the link can view" (the same assumption the existing JD preview iframe in
 * FormSubmissions.jsx already makes).
 */
@Service
public class DocumentTextExtractionService {

    private static final Logger log = LoggerFactory.getLogger(DocumentTextExtractionService.class);

    // Mirrors the file-ID extraction already used for JD preview in
    // frontend/src/pages/FormSubmissions.jsx (driveFileId()) — same three
    // fallback patterns, kept in sync deliberately.
    private static final Pattern BY_PATH  = Pattern.compile("/d/([-\\w]{25,})");
    private static final Pattern BY_QUERY = Pattern.compile("[?&]id=([-\\w]{25,})");
    private static final Pattern LOOSE    = Pattern.compile("[-\\w]{25,}");

    private final RestTemplate restTemplate;

    public DocumentTextExtractionService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Downloads the file at the given Google Drive share URL and extracts its
     * plain text. Returns null (never throws) if the URL can't be parsed, the
     * download fails, or the file isn't a PDF — extraction failures are a
     * normal, expected case (private files, unsupported formats), not a bug.
     */
    public String extractText(String driveShareUrl) {
        String fileId = extractDriveFileId(driveShareUrl);
        if (fileId == null) {
            log.warn("Could not parse a Google Drive file ID out of: {}", driveShareUrl);
            return null;
        }

        byte[] fileBytes;
        try {
            String downloadUrl = "https://drive.google.com/uc?export=download&id=" + fileId;
            fileBytes = restTemplate.getForObject(downloadUrl, byte[].class);
        } catch (Exception e) {
            log.warn("Failed to download Drive file {}: {}", fileId, e.getMessage());
            return null;
        }
        if (fileBytes == null || fileBytes.length == 0) {
            return null;
        }

        try (PDDocument document = Loader.loadPDF(fileBytes)) {
            return new PDFTextStripper().getText(document);
        } catch (Exception e) {
            // Not necessarily a PDF (could be DOCX, which isn't supported yet)
            // — log and let the caller treat this as "no text available."
            log.warn("Failed to extract PDF text from Drive file {}: {}", fileId, e.getMessage());
            return null;
        }
    }

    private static String extractDriveFileId(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }
        Matcher byPath = BY_PATH.matcher(url);
        if (byPath.find()) {
            return byPath.group(1);
        }
        Matcher byQuery = BY_QUERY.matcher(url);
        if (byQuery.find()) {
            return byQuery.group(1);
        }
        Matcher loose = LOOSE.matcher(url);
        return loose.find() ? loose.group() : null;
    }
}
