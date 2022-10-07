package com.tgin.esiaauthservice.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import java.io.IOException;
import java.util.ArrayList;

public class JsonHelper {

    private static ObjectMapper mapper = new ObjectMapper();

    public static String getJsonValue(String json, String key) throws JsonProcessingException {
        JsonNode node = parseJson(json);
        return node.get(key).asText();
    }

    public static String getJsonValue(String json, String key, int index) throws JsonProcessingException {
        JsonNode node = parseJson(json);
        return node.get(key).get(index).asText();
    }

    public static ArrayList<String> getJsonValues(String json, String key) throws IOException {
        JsonNode node = parseJson(json).get(key);
        ObjectReader reader = mapper.readerFor(new TypeReference<ArrayList<String>>() {});
        return reader.readValue(node);
    }

    public static JsonNode parseJson(String json) throws JsonProcessingException {
        return mapper.readTree(json);
    }

    public static String toJsonNode(ArrayList<String> list) throws JsonProcessingException {
        return mapper.writeValueAsString(list);
    }
}
