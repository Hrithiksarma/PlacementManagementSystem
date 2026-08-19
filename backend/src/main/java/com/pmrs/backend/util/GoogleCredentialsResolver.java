package com.pmrs.backend.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Resolves the Google service-account credential shared by GoogleFormSyncService
 * and StudentFormSyncService. The key file itself can't travel through git (it's
 * correctly gitignored — see backend/.gitignore), so a deployed environment has
 * no way to place it on the classpath the way local dev does. This prefers the
 * raw JSON content from the GOOGLE_SERVICE_ACCOUNT_JSON env var when set, and
 * falls back to the classpath/file-path behavior local dev already relies on.
 */
@Component
public class GoogleCredentialsResolver {

    private static final String CLASSPATH_PREFIX = "classpath:";

    @Value("${google.service-account-json:}")
    private String credentialsJson;

    public InputStream open(String credentialsPath) throws Exception {
        if (credentialsJson != null && !credentialsJson.isBlank()) {
            return new ByteArrayInputStream(credentialsJson.getBytes(StandardCharsets.UTF_8));
        }
        if (credentialsPath.startsWith(CLASSPATH_PREFIX)) {
            String resource = credentialsPath.substring(CLASSPATH_PREFIX.length());
            InputStream in = getClass().getClassLoader().getResourceAsStream(resource);
            if (in == null) {
                throw new IllegalStateException(
                        "Service-account key not found on classpath: " + resource
                        + " — put the JSON key in src/main/resources, set the credentials-path "
                        + "property to point elsewhere, or set GOOGLE_SERVICE_ACCOUNT_JSON.");
            }
            return in;
        }
        return new FileInputStream(credentialsPath);
    }
}
