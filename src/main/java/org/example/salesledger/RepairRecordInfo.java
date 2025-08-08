package org.example.salesledger;

public class RepairRecordInfo {
    private final int index;
    private final String bikeNumber;
    private final String mileage;
    private final String cost;
    private final String repairDate;
    private final String description;

    public RepairRecordInfo(int index, String bikeNumber, String description,
                            String mileage, String cost, String repairDate) {
        this.index = index;
        this.bikeNumber = bikeNumber;
        this.description = description;
        this.mileage = mileage;
        this.cost = cost;
        this.repairDate = repairDate;
    }

    public int getIndex() { return index; }
    public String getBikeNumber() { return bikeNumber; }
    public String getDescription() { return description; }
    public String getMileage() { return mileage; }
    public String getCost() { return cost; }
    public String getRepairDate() { return repairDate; }
}
