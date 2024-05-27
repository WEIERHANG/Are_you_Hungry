package com.hangandkai.areyouhungry;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 购物车
 */

public class ShoppingCart implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    //名称
    private String name;

    //用户id
    private String userId;

    //菜品id
    private Long dishId;

    //套餐id
    private Long setmealId;

    //数量
    private Integer number;

    //金额
    private BigDecimal amount;

    //图片
    private String image;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Long getDishId() {
        return dishId;
    }

    public void setDishId(Long dishId) {
        this.dishId = dishId;
    }

    public Long getSetmealId() {
        return setmealId;
    }

    public void setSetmealId(Long setmealId) {
        this.setmealId = setmealId;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getImage() {
        return image;
    }

    public ShoppingCart(Long id, String name, String userId, Long dishId, Long setmealId, Integer number, BigDecimal amount, String image) {
        this.id = id;
        this.name = name;
        this.userId = String.valueOf(userId);
        this.dishId = dishId;
        this.setmealId = setmealId;
        this.number = number;
        this.amount = amount;
        this.image = image;
    }

    public void setImage(String image) {
        this.image = image;
    }
    @Override
    public String toString() {
        return "ShoppingCart{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", userId=" + userId +
                ", dishId=" + dishId +
                ", setmealId=" + setmealId +
                ", number=" + number +
                ", amount=" + amount +
                ", image='" + image + '\'' +
                '}';
    }


}
