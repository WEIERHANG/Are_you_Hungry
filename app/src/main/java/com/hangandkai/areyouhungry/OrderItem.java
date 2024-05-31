package com.hangandkai.areyouhungry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderItem {
    private String order_id;
    private int status;
    private long order_time;
    private int amount;
    private String remark;
    private String phone;
    private String address;
    private String consignee;
    private String name;
    private String image;
    private int number;
    private List<OrderItem> subOrders;
    private boolean isExpanded;

    // Getters and Setters for all fields
    public String getOrderId() {
        return order_id;
    }

    public void setOrderId(String order_id) {
        this.order_id = order_id;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getOrderTime() {
        return order_time;
    }

    public void setOrderTime(long order_time) {
        this.order_time = order_time;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getConsignee() {
        return consignee;
    }

    public void setConsignee(String consignee) {
        this.consignee = consignee;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public List<OrderItem> getSubOrders() {
        return subOrders;
    }

    public void setSubOrders(List<OrderItem> subOrders) {
        this.subOrders = subOrders;
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }

    public void addSubOrder(OrderItem subOrder) {
        if (subOrders == null) {
            subOrders = new ArrayList<>();
        }
        subOrders.add(subOrder);
    }

    public int getTotalAmount() {
        int totalAmount = this.amount;
        if (subOrders != null) {
            for (OrderItem subOrder : subOrders) {
                totalAmount += subOrder.getAmount();
            }
        }
        return totalAmount;
    }

    public Map<String, Integer> getProductQuantities() {
        Map<String, Integer> productQuantities = new HashMap<>();
        if (subOrders != null) {
            for (OrderItem subOrder : subOrders) {
                productQuantities.put(subOrder.getName(),
                        productQuantities.getOrDefault(subOrder.getName(), 0) + subOrder.getNumber());
            }
        }
        return productQuantities;
    }

    public String getStatusString() {
        switch (status) {
            case 1:
                return "待付款";
            case 2:
                return "待派送";
            case 3:
                return "已派送";
            case 4:
                return "已完成";
            case 5:
                return "已取消";
            default:
                return "未知";
        }
    }
}
