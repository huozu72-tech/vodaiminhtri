package com.example.chieuthu3_ketthucmonhoc.domain.models;

public class AIRecommendation {
    private final MenuItem menuItem;
    private final double score;
    private final String reason;

    public AIRecommendation(MenuItem menuItem, double score, String reason) {
        this.menuItem = menuItem;
        this.score = score;
        this.reason = reason;
    }

    public MenuItem getMenuItem() {
        return menuItem;
    }

    public double getScore() {
        return score;
    }

    public String getReason() {
        return reason;
    }
}
