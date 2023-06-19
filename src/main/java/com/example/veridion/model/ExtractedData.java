package com.example.veridion.model;

import java.util.List;

public record ExtractedData(
        String domain,
        List<String> phoneNumbers,
        List<String> socialMediaLinks,
        List<String> addresses
) {}
