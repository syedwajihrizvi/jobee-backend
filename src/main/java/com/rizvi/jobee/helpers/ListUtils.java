package com.rizvi.jobee.helpers;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class ListUtils {
    public static String listToJsonArrayString(List<String> list) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            sb.append("\"").append(list.get(i)).append("\"");
            if (i < list.size() - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }
}
