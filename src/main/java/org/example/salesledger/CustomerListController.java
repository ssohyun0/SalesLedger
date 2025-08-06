package org.example.salesledger;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.example.salesledger.database.DataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.scene.layout.HBox;

public class CustomerListController {

    @FXML private TextField searchField;
    @FXML private TableView<CustomerInfo> customerTable;
    @FXML private TableColumn<CustomerInfo, String> nameColumn;
    @FXML private TableColumn<CustomerInfo, String> phoneColumn;
    @FXML private TableColumn<CustomerInfo, Void> actionColumn;


    private final ObservableList<CustomerInfo> customerData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // 컬럼과 데이터 매핑
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));

        addActionButtonsToTable();
        // 초기 데이터 로드
        loadCustomerList("");
    }

    @FXML
    private void onSearchClicked() {
        String keyword = searchField.getText().trim();
        loadCustomerList(keyword);
    }

    @FXML
    private void onBackClicked() {
        Stage stage = (Stage) searchField.getScene().getWindow();
        stage.close();
    }

    private void loadCustomerList(String searchName) {
        customerData.clear();

        String sql = """
            SELECT DISTINCT c.name, c.phone
            FROM customer c
            WHERE c.name LIKE ?
            ORDER BY c.name ASC, c.phone ASC
            """;

        try (Connection conn = DataBase.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + searchName + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                customerData.add(new CustomerInfo(
                        rs.getString("name"),
                        rs.getString("phone")
                ));
            }

            customerTable.setItems(customerData);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // buttons
    private void addActionButtonsToTable() {
        actionColumn.setCellFactory(col -> new TableCell<CustomerInfo, Void>() {
            private final Button detailButton = new Button("상세 보기");
            private final Button deleteButton = new Button("회원 삭제");
            {
                detailButton.setOnAction(e -> {
                    CustomerInfo customer = getTableView().getItems().get(getIndex());
                    System.out.println("상세 보기 클릭됨: " + customer.getName());
                    // TODO: 상세보기 기능 구현
                });

                deleteButton.setOnAction(e -> {
                    CustomerInfo customer = getTableView().getItems().get(getIndex());
                    deleteCustomerFromDatabase(customer.getPhone());
                    loadCustomerList(searchField.getText().trim()); // 새로고침
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(5, detailButton, deleteButton);
                    setGraphic(box);
                }
            }
        });
    }

    private void deleteCustomerFromDatabase(String phone) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("회원 삭제 확인");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("정말 이 회원을 삭제하시겠습니까? 관련 오토바이 및 수리 내역도 삭제됩니다.");

        if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        String deleteSql = "DELETE FROM customer WHERE phone = ?";

        try (Connection conn = DataBase.connect();
             PreparedStatement pstmt = conn.prepareStatement(deleteSql)) {

            pstmt.setString(1, phone);
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                System.out.println("✅ 회원 삭제 성공: " + phone);
            } else {
                System.out.println("⚠️ 회원 삭제 실패: " + phone);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
