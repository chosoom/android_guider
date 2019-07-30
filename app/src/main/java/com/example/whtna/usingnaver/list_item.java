package com.example.whtna.usingnaver;

public class list_item {
    private String address;
    private String info;

    public list_item(String address, String info) {
        this.address = address;
        this.info = info;
    }

    public String getAddress() {
        return address;
    }

    public String getInfo() {
        return info;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setInfo(String info) {
        this.info = info;
    }
}
