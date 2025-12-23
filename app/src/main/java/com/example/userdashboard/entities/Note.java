package com.example.userdashboard.entities;

import java.time.LocalDate;

public class Note {
    private int id;
    private String title;
    private long date;
    private String body;

    public Note(int id, String title, long date, String body) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.body = body;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
