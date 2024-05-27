package com.hangandkai.areyouhungry;

import java.io.Serializable;

public class User implements Serializable {
    private String id;
    private String phone;
    // 其他用户信息

    public User(String id,String phone) {
        this.id = id;
        this.phone = phone;
        // 初始化其他用户信息
    }

    public User() {

    }

    // getter 和 setter 方法
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }



    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

}
