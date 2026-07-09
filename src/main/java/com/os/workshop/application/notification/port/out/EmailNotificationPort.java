package com.os.workshop.application.notification.port.out;

public interface EmailNotificationPort {

    void sendStatusUpdate(String toEmail, String clientName, String vehicleInfo, String newStatus);
}
