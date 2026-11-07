package com.example.chieuthu3_ketthucmonhoc.domain.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public enum TasteAttribute {
    SPICY("cay", Arrays.asList("cay", "muối ớt", "muoi ot", "sốt thái", "sot thai")),
    SWEET("ngọt", Arrays.asList("ngọt", "ngot", "mật ong", "mat ong")),
    CRISPY("giòn", Arrays.asList("giòn", "gion", "chiên xù", "chien xu", "sần sật")),
    SAVORY("đậm đà", Arrays.asList("đậm đà", "dam da", "đậm vị", "dam vi", "mặn mà")),
    SOUR("chua", Arrays.asList("chua", "sả tắc", "sa tac", "lá giang", "la giang")),
    REFRESHING("thanh mát", Arrays.asList("thanh mát", "thanh mat", "mát lạnh", "mat lanh", "sảng khoái", "giải ngấy")),
    GRILLED("nướng", Arrays.asList("nướng", "nuong", "quay")),
    FRIED("chiên", Arrays.asList("chiên", "chien"));

    private final String tag;
    private final List<String> keywords;

    TasteAttribute(String tag, List<String> keywords) {
        this.tag = tag;
        this.keywords = keywords;
    }

    public String getTag() {
        return tag;
    }

    public String getDisplayName() {
        return tag;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public static List<TasteAttribute> parseFromText(String text) {
        List<TasteAttribute> result = new ArrayList<>();
        if (text == null || text.trim().isEmpty()) {
            return result;
        }
        String lower = text.toLowerCase(Locale.ROOT);
        for (TasteAttribute attr : values()) {
            if (matchesAttribute(lower, attr)) {
                result.add(attr);
            }
        }
        return result;
    }

    private static boolean matchesAttribute(String lowerText, TasteAttribute attr) {
        if (lowerText.contains(attr.tag)) {
            return true;
        }
        for (String kw : attr.keywords) {
            if (kw.equals("chua")) {
                continue;
            }
            if (containsWordOrPhrase(lowerText, kw)) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsWordOrPhrase(String text, String phrase) {
        int index = text.indexOf(phrase);
        while (index != -1) {
            boolean startOk = (index == 0) || !Character.isLetterOrDigit(text.charAt(index - 1));
            int end = index + phrase.length();
            boolean endOk = (end == text.length()) || !Character.isLetterOrDigit(text.charAt(end));
            if (startOk && endOk) {
                return true;
            }
            index = text.indexOf(phrase, index + 1);
        }
        return false;
    }
}
