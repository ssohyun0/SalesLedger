package org.example.salesledger;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.salesledger.database.DataBase;

import java.awt.*;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.control.Button;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class RepairRegisterController {

    @FXML private TextField nameField, bikeNumberField;
    @FXML private Label mileageLabel, descLabel, costLabel;
    @FXML private TextField mileageField, descField, costField;
    @FXML private Button registerButton;

    private int motorbikeId = -1;

    @FXML
    private void onCheckCustomer() {
        String name = nameField.getText().trim();
        String bikeNumber = bikeNumberField.getText().trim();

        if (name.isEmpty() || bikeNumber.isEmpty()) {
            showAlert("입력 오류", "이름과 오토바이 번호를 입력해주세요.");
            return;
        }

        try (Connection conn = DataBase.connect()) {
            String sql = """
                SELECT m.id FROM customer c
                JOIN motorbike m ON c.id = m.customer_id
                WHERE c.name = ? AND m.bike_number = ?
            """;
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, name);
            pstmt.setString(2, bikeNumber);
            ResultSet rs = pstmt.executeQuery();


            if (rs.next()) {
                motorbikeId = rs.getInt("id");

                mileageLabel.setVisible(true);
                mileageField.setVisible(true);
                descLabel.setVisible(true);
                descField.setVisible(true);
                costLabel.setVisible(true);
                costField.setVisible(true);
                registerButton.setVisible(true);
            } else {
                showAlert("고객 없음", "입력한 정보와 일치하는 고객이 없습니다.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("DB 오류", "고객 확인 중 오류 발생: " + e.getMessage());
        }
    }

    @FXML
    private void onRegister() {
        try (Connection conn = DataBase.connect()) {
            String sql = "INSERT INTO repair (motorbike_id, mileage, description, cost) VALUES (?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, motorbikeId);
            pstmt.setInt(2, Integer.parseInt(mileageField.getText()));
            pstmt.setString(3, descField.getText());
            pstmt.setInt(4, Integer.parseInt(costField.getText()));
            pstmt.executeUpdate();

            showAlert("등록 완료", "수리 내역이 등록되었습니다.");
            ((Stage) nameField.getScene().getWindow()).close();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("DB 오류", "수리 등록 중 오류 발생: " + e.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
