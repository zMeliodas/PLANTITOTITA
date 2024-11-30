package com.meliodas.plantitotita.mainmodule;

import java.util.ArrayList;
import java.util.List;

public class Reminder {
    private String id;
    private String title;
    private String task;
    private String plant;
    private String image;
    private long timeInMillis;
    private boolean repeat;
    private List<String> repeatDays;

    public Reminder() {
        // Default constructor for Firebase
        repeatDays = new ArrayList<>(); // Initialize with an empty list
    }

    public Reminder(String id, String title, String task, String plant, String image,
                    long timeInMillis, boolean repeat, List<String> repeatDays) {
        this.id = id;
        this.title = title;
        this.task = task;
        this.plant = plant;
        this.timeInMillis = timeInMillis;
        this.repeat = repeat;
        this.image = image;
        this.repeatDays = repeatDays != null ? repeatDays : new ArrayList<>();
    }

    // Add getter and setter for repeatDays
    public List<String> getRepeatDays() {
        return repeatDays;
    }

    public void setRepeatDays(List<String> repeatDays) {
        this.repeatDays = repeatDays;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public String getPlant() {
        return plant;
    }

    public void setPlant(String plant) {
        this.plant = plant;
    }

    public long getTimeInMillis() {
        return timeInMillis;
    }

    public void setTimeInMillis(long timeInMillis) {
        this.timeInMillis = timeInMillis;
    }

    public boolean isRepeat() {
        return repeat;
    }

    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}