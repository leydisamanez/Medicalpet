package com.medicalpet.medicalpet.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class ApisPeruService {

    @Value("${api.apisperu.url}")
    private String apiUrl;

    @Value("${api.apisperu.token}")
    private String apiToken;

    private final RestTemplate restTemplate;

    public ApisPeruService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String consultarRUC(String ruc) {
        String url = apiUrl + ruc;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        headers.set("Authorization", "Bearer " + apiToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        System.out.println("Consultando RUC: " + ruc + ", URL: " + url + ", Token: " + apiToken);
        try {
            String response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class).getBody();
            System.out.println("Respuesta API: " + response);
            if (response == null || !response.trim().startsWith("{")) {
                System.err.println("Respuesta no es JSON válido: " + response);
                return "{\"error\": \"Respuesta inválida de la API\"}";
            }
            return response;
        } catch (HttpClientErrorException e) {
            String errorMsg = "Error HTTP al consultar RUC: " + e.getStatusCode() + ", Respuesta: " + e.getResponseBodyAsString();
            System.err.println(errorMsg);
            return "{\"error\": \"" + errorMsg + "\"}";
        } catch (Exception e) {
            String errorMsg = "Error consultando RUC: " + e.getMessage();
            System.err.println(errorMsg);
            return "{\"error\": \"" + errorMsg + "\"}";
        }
    }
}