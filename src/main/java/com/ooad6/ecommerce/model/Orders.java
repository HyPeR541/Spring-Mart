package com.ooad6.ecommerce.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "Orders")
public class Orders {
    private int orderId;  // MongoDB auto-generates ID
    private int userId;
    private List<Cart> prodList;
    private String paymentMethod;
    private LocalDateTime timestamp;
    private String status;
    public Orders() {
        this.timestamp = LocalDateTime.now(); // Auto-set order time
        this.status = "Pending"; // Default status
    }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public List<Cart> getProdList() { return prodList; }
    public void setProdList(List<Cart> prodList) { this.prodList = prodList; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }  
    @Override
    public String toString() {
        return "Orders{" +
                "orderId='" + orderId + '\'' +
                ", userId=" + userId +
                ", prodList=" + prodList +
                ", timestamp=" + timestamp +
                ", paymentMethod='" + paymentMethod + '\'' +
                '}';
    }
}