package com.example.ai_bsrs.notification_module;

public class Notification {

    private String breadName;
    private String dateKeep;
    private String dateIn;
    private String dateOut;
    private String timeKeep;
    private String notificationId;
    private String notes;
    private long notificationTimeStamp;

    public Notification(){

    }

    public Notification(String notificationId, String breadName, String dateIn, String dateKeep, String dateOut, String timeKeep, String notes, long notificationTimeStamp) {
        this.notificationId = notificationId;
        this.breadName = breadName;
        this.dateKeep = dateKeep;
        this.dateIn = dateIn;
        this.dateOut = dateOut;
        this.timeKeep = timeKeep;
        this.notes = notes;
        this.notificationTimeStamp = notificationTimeStamp;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public long getNotificationTimeStamp() {
        return notificationTimeStamp;
    }

    public void setNotificationTimeStamp(long notificationTimeStamp) {
        this.notificationTimeStamp = notificationTimeStamp;
    }

    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public String getBreadName() {
        return breadName;
    }

    public void setBreadName(String breadName) {
        this.breadName = breadName;
    }

    public String getDateKeep() {
        return dateKeep;
    }

    public void setDateKeep(String dateKeep) {
        this.dateKeep = dateKeep;
    }

    public String getDateIn() {
        return dateIn;
    }

    public void setDateIn(String dateIn) {
        this.dateIn = dateIn;
    }

    public String getDateOut() {
        return dateOut;
    }

    public void setDateOut(String dateOut) {
        this.dateOut = dateOut;
    }

    public String getTimeKeep() {
        return timeKeep;
    }

    public void setTimeKeep(String timeKeep) {
        this.timeKeep = timeKeep;
    }
}
