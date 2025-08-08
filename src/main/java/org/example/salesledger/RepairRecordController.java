package org.example.salesledger;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.salesledger.database.DataBase;

import java.io.IOException;
import java.sql.*;

public class RepairRecordController {

    @FXML private Label nameLabel;
    @FXML private Label phoneLabel;

    @FXML private TableView<RepairRecordInfo> repairTable;
    @FXML private TableColumn<RepairRecordInfo, Integer> colIndex;
    @FXML private TableColumn<RepairRecordInfo, String> colBikeNumber;
    @FXML private TableColumn<RepairRecordInfo, String> colContent;     // "수리 내역" 컬럼
    @FXML private TableColumn<RepairRecordInfo, String> colMileage;
    @FXML private TableColumn<RepairRecordInfo, String> colCost;
    @FXML private TableColumn<RepairRecordInfo, String> colRepairDate;

    private final ObservableList<RepairRecordInfo> records = FXCollections.observableArrayList();
    private int customerId = -1;

    @FXML
    public void initialize() {
        colIndex.setCellValueFactory(new PropertyValueFactory<>("index"));
        colBikeNumber.setCellValueFactory(new PropertyValueFactory<>("bikeNumber"));
        colContent.setCellValueFactory(new PropertyValueFactory<>("description"));
        colMileage.setCellValueFactory(new PropertyValueFactory<>("mileage"));
        colCost.setCellValueFactory(new PropertyValueFactory<>("cost"));
        colRepairDate.setCellValueFactory(new PropertyValueFactory<>("repairDate"));

        repairTable.setItems(records);
    }

    /** CustomerListController에서 호출 */
    public void init(int customerId, String name, String phone) {
        this.customerId = customerId;
        nameLabel.setText(name);
        phoneLabel.setText(phone);
        loadRecords();
    }

    private void loadRecords() {
        records.clear();
        if (customerId < 0) return;

        String sql = """
            SELECT r.id AS rid,
                   m.bike_number,
                   r.description,
                   r.mileage,
                   r.cost,
                   r.repair_date
            FROM repair r
            JOIN motorbike m ON r.motorbike_id = m.id
            WHERE m.customer_id = ?
            ORDER BY r.repair_date DESC, r.id DESC
        """;

        try (Connection conn = DataBase.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, customerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                int idx = 1;
                while (rs.next()) {
                    String mileage = rs.getString("mileage");
                    String cost = rs.getString("cost");
                    String date = rs.getString("repair_date");

                    records.add(new RepairRecordInfo(
                            idx++,
                            rs.getString("bike_number"),
                            rs.getString("description"),
                            mileage == null ? "" : mileage,
                            cost == null ? "" : cost,
                            date == null ? "" : date
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onBackClicked() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/salesledger/CustomerListView.fxml"));
            Parent root = loader.load();
            repairTable.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
