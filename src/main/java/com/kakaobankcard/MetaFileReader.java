package com.kakaobankcard;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MetaFileReader {

    static final String path = "/Users/kakaobank/Desktop/";
    private final String fileName;

    public MetaFileReader(String fileName) {
        this.fileName = fileName;
    }

    public Map<String, String> readMetaCsv() {
        String line;
        String csvSplitBy = ",";
        Map<String, String> dataMap = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(path + fileName + ".csv"))) {
            while ((line = br.readLine()) != null) {
                String[] data = line.split(csvSplitBy);
                if (data.length >= 2) {
                    String key = data[0];
                    String value = data[1];
                    dataMap.put(key, value);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dataMap;
    }

}
