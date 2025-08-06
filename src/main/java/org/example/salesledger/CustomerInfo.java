package org.example.salesledger;

public class CustomerInfo {
    private final String name;
    private final String phone;

    public CustomerInfo(String name, String phone) {
        this.name = name;
        this.phone = phone;
    }

    public String getName() { return name; }
    public String getPhone() { return phone; }
}
