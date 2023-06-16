package com.example.veridion;

import com.example.veridion.model.ExtractedData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class VeridionController {

    private final DataExtractorService dataExtractorService;

    @Autowired
    public VeridionController(DataExtractorService dataExtractorService) {
        this.dataExtractorService = dataExtractorService;
    }

    @GetMapping("/data")
    public List<ExtractedData> index(@RequestParam("all") boolean all) {
        return dataExtractorService.extractData(all);
    }
}
