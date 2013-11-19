package com.ashman.fivehundredpx.enums;


public enum GalleryCategory {
    UNCATEGORIZED (0, "Uncategorized"),
    ABSTRACT (10, "Abstract"),
    ANIMALS (11, "Animals"),
    BLACK_AND_WHITE (5, "Black and White"),
    CELEBRITIES (1, "Celebrities"),
    CITY_AND_ARCHITECTURE (9, "City and Architecture"),
    COMMERCIAL (15, "Commercial"),
    CONCERT (16, "Concert"),
    FAMILY (20, "Family"),
    FASHION (14, "Fashion"),
    FILM (2, "Film"),
    FINE_ART (24, "Fine Art"),
    FOOD (23, "Food"),
    JOURNALISM (3, "Journalism"),
    LANDSCAPES (8, "Landscapes"),
    MACRO (12, "Macro"),
    NATURE (18, "Nature"),
    NUDE (4, "Nude"),
    PEOPLE (7, "People"),
    PERFORMING_ARTS (19, "Performing Arts"),
    SPORT (17, "Sport"),
    STILL_LIFE (6, "Still Life"),
    STREET (21, "Street"),
    TRANSPORTATION (26, "Transportation"),
    TRAVEL (13, "Travel"),
    UNDERWATER (22, "Underwater"),
    URBAN_EXPLORATION (27, "Urban Exploration"),
    WEDDING (25, "Wedding");

    private int value;
    private final String name;

    GalleryCategory(int value, String name) {
        this.value = value;
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public String getName() {
        return name;
    }

    public int getValue() {
        return value;
    }
}
