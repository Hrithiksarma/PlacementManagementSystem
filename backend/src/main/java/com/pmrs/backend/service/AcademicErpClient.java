package com.pmrs.backend.service;

import com.pmrs.backend.dto.AcademicStudentDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class AcademicErpClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String serviceUsername;
    private final String servicePassword;

    private String cachedToken;

    public AcademicErpClient(RestTemplate restTemplate,
                              @Value("${academic.erp.base-url}")          String baseUrl,
                              @Value("${academic.erp.service.username}")  String serviceUsername,
                              @Value("${academic.erp.service.password}")  String servicePassword) {
        this.restTemplate    = restTemplate;
        this.baseUrl         = baseUrl;
        this.serviceUsername = serviceUsername;
        this.servicePassword = servicePassword;
    }

    public AcademicStudentDTO fetchByRollNo(String rollNo) {
        String url = baseUrl + "/api/students/" + rollNo;
        try {
            return exchange(url, AcademicStudentDTO.class);
        } catch (HttpClientErrorException.NotFound ex) {
            throw new IllegalArgumentException(
                "Roll number " + rollNo + " not found in Academic ERP.");
        } catch (HttpClientErrorException.Unauthorized ex) {
            // Token may have expired — clear and retry once with a fresh token
            cachedToken = null;
            try {
                return exchange(url, AcademicStudentDTO.class);
            } catch (HttpClientErrorException.NotFound ex2) {
                throw new IllegalArgumentException(
                    "Roll number " + rollNo + " not found in Academic ERP.");
            }
        }
    }

    private <T> T exchange(String url, Class<T> responseType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getToken());
        HttpEntity<?> entity = new HttpEntity<>(headers);
        ResponseEntity<T> response = restTemplate.exchange(
            url, HttpMethod.GET, entity, responseType);
        return response.getBody();
    }

    private synchronized String getToken() {
        if (cachedToken != null) return cachedToken;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, String> body = Map.of(
            "username", serviceUsername,
            "password", servicePassword
        );
        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        @SuppressWarnings("unchecked")
        Map<String, Object> response = restTemplate.postForObject(
            baseUrl + "/api/auth/login", request, Map.class);

        if (response == null || response.get("token") == null) {
            throw new IllegalStateException(
                "PRMS could not authenticate with Academic ERP — check service credentials.");
        }
        cachedToken = (String) response.get("token");
        return cachedToken;
    }
}
