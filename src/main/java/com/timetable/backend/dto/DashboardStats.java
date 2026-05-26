package com.timetable.backend.dto;

public class DashboardStats {
    private long teachers;
    private long rooms;
    private long sections;
    private long bookings;

    public DashboardStats(long teachers, long rooms, long sections, long bookings) {
        this.teachers = teachers;
        this.rooms = rooms;
        this.sections = sections;
        this.bookings = bookings;
    }

    public long getTeachers() { return teachers; }
    public long getRooms() { return rooms; }
    public long getSections() { return sections; }
    public long getBookings() { return bookings; }
}