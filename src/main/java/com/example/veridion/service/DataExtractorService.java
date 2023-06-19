package com.example.veridion.service;

import com.example.veridion.model.ExtractedData;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DataExtractorService {

    private static final String PHONE_NUMBER_REGEX = "^\\(?\\d{3}\\)?[- ]?\\d{3}[- ]?\\d{4}$";
    private static final String USA_ADDRESS_REGEX = "^(\\d{1,}) [a-zA-Z0-9\\s]+(\\,)? [a-zA-Z]+(\\,)? [A-Z]{2} [0-9]{5,6}$";

    private final HelperService helperService;

    public DataExtractorService(HelperService helperService) {
        this.helperService = helperService;
    }

    public List<ExtractedData> extractData(int index) {
        List<ExtractedData> extractedDataList = new ArrayList<>();

        List<String> domains = helperService.getDomains();
        if (domains.isEmpty()) {
            return extractedDataList;
        }
        int startIndex = 10 * index;
        int stopIndex = Math.min(10 * (index + 1), domains.size());
        domains = domains.subList(startIndex, stopIndex);
        log.info("{} domains were read from the config file", domains.size());
        log.info("Processing domains: {}", String.join(", ", domains));

        domains.forEach(domain -> {
            try {
                Document document = Jsoup.connect("https://" + domain).get();

                extractedDataList.add(new ExtractedData(domain,
                        new ArrayList<>(extractPhoneNumbers(document)),
                        new ArrayList<>(extractSocialMediaLinks(document)),
                        new ArrayList<>(extractAddresses(document))));
            } catch (IOException e) {
                log.warn("Failed to extract data for {}", domain);
            }
        });
        return extractedDataList;
    }

    public Set<String> extractPhoneNumbers(Document document) {
        Elements elementsByRegex = document.getElementsMatchingOwnText(PHONE_NUMBER_REGEX);
        Set<String> phoneNumbers = elementsByRegex.stream().map(Element::text).collect(Collectors.toSet());
        Elements elementsByCss = document.select(".tel, .telephone, .phone");
        phoneNumbers.addAll(elementsByCss.stream().map(Element::text).toList());
        return phoneNumbers;
    }

    public Set<String> extractSocialMediaLinks(Document document) {
        Elements elements = document.select("a[href*=facebook.com], a[href*=twitter.com], a[href*=instagram.com], a[href*=linkedin.com], a[href*=youtube.com], a[href*=github.com], a[href*=gitlab.com]");
        return elements.stream().map(element -> element.attr("href")).collect(Collectors.toSet());
    }

    public Set<String> extractAddresses(Document document) {
        Elements elementsByRegex = document.getElementsMatchingOwnText(USA_ADDRESS_REGEX);
        Set<String> addresses = elementsByRegex.stream().map(Element::text).collect(Collectors.toSet());
        Elements elementsByCss = document.select(".adr, .address");
        addresses.addAll(elementsByCss.stream().map(Element::text).toList());
        return addresses;
    }
}
