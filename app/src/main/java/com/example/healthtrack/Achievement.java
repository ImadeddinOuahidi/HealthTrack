package com.example.healthtrack;

public class Achievement {
    public enum BadgeTier {
        BRONZE, SILVER, GOLD, PLATINUM, DIAMOND
    }

    private String id;
    private String title;
    private String description;
    private int iconResId;
    private boolean isUnlocked;
    private BadgeTier badgeTier;

    public Achievement(String id, String title, String description, int iconResId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.iconResId = iconResId;
        this.isUnlocked = false; // By default, achievements are locked
        this.badgeTier = BadgeTier.BRONZE; // Default tier
    }

    public Achievement(String id, String title, String description, int iconResId, BadgeTier badgeTier) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.iconResId = iconResId;
        this.isUnlocked = false;
        this.badgeTier = badgeTier;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getIconResId() {
        return iconResId;
    }

    public boolean isUnlocked() {
        return isUnlocked;
    }

    public void setUnlocked(boolean unlocked) {
        isUnlocked = unlocked;
    }

    public BadgeTier getBadgeTier() {
        return badgeTier;
    }

    public void setBadgeTier(BadgeTier badgeTier) {
        this.badgeTier = badgeTier;
    }
}
