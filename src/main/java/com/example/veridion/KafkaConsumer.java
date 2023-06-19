package com.example.veridion;

import com.example.veridion.model.ExtractedData;
import com.example.veridion.service.DataExtractorService;
import com.example.veridion.service.HttpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class KafkaConsumer {

    @Value("${HOSTNAME}")
    private String hostname;

    private final DataExtractorService dataExtractorService;
    private final HttpService httpService;

    @Autowired
    public KafkaConsumer(DataExtractorService dataExtractorService, HttpService httpService) {
        this.dataExtractorService = dataExtractorService;
        this.httpService = httpService;
    }

    @KafkaListener(id = "${HOSTNAME}-consumer", groupId = "${HOSTNAME}-group", topics = "${kafka.topic.name}")
    public void listen(String message) {
        log.info("Start message `{}` received by pod: {}", message, hostname);

        List<ExtractedData> dataList = this.dataExtractorService.extractData(getPodIndex());
        log.info("Extracted data: {}", dataList.stream().map(ExtractedData::toString).collect(Collectors.joining(", ")));
        this.httpService.updateCompanyData(dataList);
    }

    public int getPodIndex() {
        try {
            return Integer.parseInt(this.hostname.split("-")[2]);
        } catch (Exception ex) {
            log.warn("Exception thrown while trying to extract the pod index: {}", ex.getMessage());
            return 0;
        }
    }
}
