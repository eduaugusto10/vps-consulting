package com.vps.ordermanagement.service;

import com.vps.ordermanagement.model.Order;

public interface NotificationService {
    
    void notifyOrderStatusChange(Order order);
} 