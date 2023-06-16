package com.example.veridion.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExtractedData {
    String url;
    List<String> phoneNumbers;
    List<String> socialMediaLinks;
    List<String> addresses;
}
