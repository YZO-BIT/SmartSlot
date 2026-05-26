package com.timetable.backend.model;

public enum RoomType {
    CR("Classroom"),
    LAB("Laboratory"),
    LT("Lecture Hall"),
    NEW_AUDI("New Auditorium");

    private final String description;

    RoomType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
