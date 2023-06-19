package com.example.veridion.service;

import com.example.veridion.model.ExtractedData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Slf4j
@Service
public class HttpService {

    @Value("${my.elasticsearch-service.host}")
    private String host;
    @Value("${my.elasticsearch-service.port}")
    private String port;

    private final WebClient webClient;

    @Autowired
    public HttpService(WebClient webClient) {
        this.webClient = webClient;
    }

    public void updateCompanyData(List<ExtractedData> extractedDataList) {
        log.info("Calling /update endpoint");
        webClient.put()
                .uri("http://" + host + ":" + port+ "/update")
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(extractedDataList)
                .retrieve()
                .toBodilessEntity()
                .block();
    }
}
