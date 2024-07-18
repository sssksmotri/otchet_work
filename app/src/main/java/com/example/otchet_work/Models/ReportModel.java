package com.example.otchet_work.Models;

public class ReportModel {
    private String action;
    private String apartment;
    private String comments;
    private String datetime;
    private String firstName;
    private String lastName;
    private String objectName;
    private String photoUri;
    private String userId;
    private String ReportID;
    private String actionType;
    private String unit;
    private String roomName;  // Новое поле
    private String quantity;  // Новое поле

    public ReportModel() {
        // Empty constructor needed for Firebase
    }

    public ReportModel(String action, String apartment, String comments, String datetime, String firstName, String lastName, String objectName, String photoUri, String userId, String actionType, String ReportID, String unit, String roomName, String quantity) {
        this.action = action;
        this.apartment = apartment;
        this.comments = comments;
        this.datetime = datetime;
        this.firstName = firstName;
        this.lastName = lastName;
        this.objectName = objectName;
        this.photoUri = photoUri; // Change to String to match the database storage format
        this.userId = userId;
        this.actionType = actionType;
        this.ReportID = ReportID;
        this.unit = unit;
        this.roomName = roomName;
        this.quantity = quantity;
    }

    // Getter and setter methods
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }

    public String getReportID() { return ReportID; }
    public void setReportID(String reportID) { ReportID = reportID; }

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

    public String getPhotoUri() { return photoUri; }
    public void setPhotoUri(String photoUri) { this.photoUri = photoUri; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }

    public String getQuantity() { return quantity; }
    public void setQuantity(String quantity) { this.quantity = quantity; }
}
