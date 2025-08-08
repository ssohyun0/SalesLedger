package org.example.salesledger;

public class CustomerInfo {
    private final int id;
    private final String name;
    private final String phone;

    public CustomerInfo(int id, String name, String phone) {
        this.id = id;
        this.name = name;
        this.phone = phone;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
}
