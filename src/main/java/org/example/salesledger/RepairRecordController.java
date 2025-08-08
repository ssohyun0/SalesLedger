package org.example.salesledger;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
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
    @FXML private TableColumn<RepairRecordInfo, Void>   colActions;

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

        // 수정/삭제 버튼 추가
        addActionButtons();

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
                            rs.getInt("rid"),
                            rs.getString("bike_number"),
                            rs.getString("description"),
                            mileage == null ? "" : mileage,
                            cost    == null ? "" : cost,
                            date    == null ? "" : date
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("조회 실패", e.getMessage());
        }
    }

    private void addActionButtons() {
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("수정");
            private final Button delBtn  = new Button("삭제");

            {
                editBtn.setOnAction(e -> openEditDialog(getTableView().getItems().get(getIndex())));
                delBtn.setOnAction(e -> {
                    RepairRecordInfo rec = getTableView().getItems().get(getIndex());
                    deleteRepair(rec.getRepairId());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : new HBox(8, editBtn, delBtn));
            }
        });
    }

    private void openEditDialog(RepairRecordInfo rec) {
        Dialog<RepairRecordInfo> dialog = new Dialog<>();
        dialog.setTitle("수리 내역 수정");

        ButtonType okType = new ButtonType("저장", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okType, ButtonType.CANCEL);

        TextField mileageField = new TextField(rec.getMileage());
        TextField costField    = new TextField(rec.getCost());
        TextArea  descArea     = new TextArea(rec.getDescription());
        descArea.setPrefRowCount(3);

        GridPane gp = new GridPane();
        gp.setHgap(10); gp.setVgap(10); gp.setPadding(new Insets(10));
        gp.addRow(0, new Label("주행거리"), mileageField);
        gp.addRow(1, new Label("수리 금액"), costField);
        gp.addRow(2, new Label("수리 내역"), descArea);

        dialog.getDialogPane().setContent(gp);

        dialog.setResultConverter(btn -> {
            if (btn == okType) {
                return new RepairRecordInfo(
                        rec.getIndex(),
                        rec.getRepairId(),
                        rec.getBikeNumber(),
                        descArea.getText().trim(),
                        mileageField.getText().trim(),
                        costField.getText().trim(),
                        rec.getRepairDate()
                );
            }
            return null;
        });

        dialog.showAndWait().ifPresent(updated -> {
            updateRepair(updated);
            loadRecords();
        });
    }

    private void updateRepair(RepairRecordInfo updated) {
        String sql = "UPDATE repair SET mileage = ?, description = ?, cost = ? WHERE id = ?";
        try (Connection conn = DataBase.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // INTEGER 컬럼: 빈 문자열은 NULL로 저장
            if (isBlank(updated.getMileage())) ps.setNull(1, Types.INTEGER);
            else ps.setInt(1, Integer.parseInt(updated.getMileage()));

            ps.setString(2, updated.getDescription());

            if (isBlank(updated.getCost())) ps.setNull(3, Types.INTEGER);
            else ps.setInt(3, Integer.parseInt(updated.getCost()));

            ps.setInt(4, updated.getRepairId());

            ps.executeUpdate();

        } catch (NumberFormatException nfe) {
            showError("입력 오류", "주행거리/금액은 숫자만 입력하세요.");
        } catch (SQLException ex) {
            ex.printStackTrace();
            showError("수정 실패", ex.getMessage());
        }
    }

    private void deleteRepair(int repairId) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "정말 삭제하시겠습니까?", ButtonType.OK, ButtonType.CANCEL);
        confirm.setHeaderText(null);
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        String sql = "DELETE FROM repair WHERE id = ?";
        try (Connection conn = DataBase.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, repairId);
            ps.executeUpdate();
            loadRecords();
        } catch (SQLException ex) {
            ex.printStackTrace();
            showError("삭제 실패", ex.getMessage());
        }
    }

    private boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }

    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.setHeaderText(title);
        a.showAndWait();
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
