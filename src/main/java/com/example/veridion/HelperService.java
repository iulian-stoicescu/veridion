package com.example.veridion;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.asm.TypeReference;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class HelperService {
    public List<String> getWebsites() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return Arrays.asList(mapper.readValue(TypeReference.class.getResourceAsStream("/sample-websites.json"), String[].class));
        } catch (IOException e) {
            log.warn("File sample-websites.json could not be read!");
            return List.of();
        }
    }
}
