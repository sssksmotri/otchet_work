package com.example.otchet_work.Models;

public class ReportModel {
    private String action;
    private String apartment;
    private String comments;
    private String datetime;
    private String firstName;
    private String lastName;
    private String objectName;
    private String photoUri; // Change to String to match the database storage format
    private String userId;
    private String ReportID;

    public ReportModel() {
        // Empty constructor needed for Firebase
    }

    public ReportModel(String action, String apartment, String comments, String datetime, String firstName, String lastName, String objectName, String photoUri, String userId) {
        this.action = action;
        this.apartment = apartment;
        this.comments = comments;
        this.datetime = datetime;
        this.firstName = firstName;
        this.lastName = lastName;
        this.objectName = objectName;
        this.photoUri = photoUri; // Change to String to match the database storage format
        this.userId = userId;
    }

    public String getReportID() {
        return ReportID;
    }

    public void setReportID(String reportID) {
        ReportID = reportID;
    }

    // Getter and setter methods
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getApartment() { return apartment; }
    public void setApartment(String apartment) { this.apartment = apartment; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }

    public String getDatetime() { return datetime; }
    public void setDatetime(String datetime) { this.datetime = datetime; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getObjectName() { return objectName; }
    public void setObjectName(String objectName) { this.objectName = objectName; }

    public String getPhotoUri() { return photoUri; } // Change to String
    public void setPhotoUri(String photoUri) { this.photoUri = photoUri; } // Change to String

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}
