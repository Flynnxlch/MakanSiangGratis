package com.example.makansianggratis.Model;

public class CC {
    private String Name;
    private String email;
    private String phone;

    public CC(String name, String email, String phone) {
        this.Name = name;
        this.email = email;
        this.phone = phone;
    }

    public String getName() {
        return Name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }
}
