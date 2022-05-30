package com.dannyhurley.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;

public class JSONParser {
    public static Map<String, String> parse(String jsonString) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();

        try {
            String json = "{\"hi\": \"hello\"}";

            return mapper.readValue(json, Map.class);
        } catch (IOException e) {
            System.out.println("::: failed to parse JSON");
            throw e;
        }
    }
}
