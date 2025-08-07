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

import java.text.DecimalFormat;
import java.util.function.UnaryOperator;
import javafx.util.StringConverter;
import javafx.util.converter.NumberStringConverter;

public class RepairRegisterController {

    @FXML private TextField nameField, bikeNumberField;
    @FXML private Label mileageLabel, descLabel, costLabel;
    @FXML private TextField mileageField, descField, costField;
    @FXML private Button registerButton;

    private int motorbikeId = -1;

    @FXML
    public void initialize() {
        // 주행거리와 수리금액 필드에 쉼표 포맷터 적용
        setCommaFormatter(mileageField);
        setCommaFormatter(costField);
    }

    private void setCommaFormatter(TextField textField) {
        DecimalFormat format = new DecimalFormat("#,###");

        // 입력 도중 쉼표 반영
        textField.textProperty().addListener((obs, oldVal, newVal) -> {
            String digits = newVal.replaceAll(",", "").replaceAll("[^\\d]", "");
            if (digits.isEmpty()) {
                textField.setText("");
                return;
            }

            try {
                long number = Long.parseLong(digits);
                String formatted = format.format(number);

                // 커서 위치 유지
                int caretPos = textField.getCaretPosition();
                textField.setText(formatted);
                textField.positionCaret(caretPos + (formatted.length() - newVal.length()));
            } catch (NumberFormatException e) {
                // 무시: 숫자가 너무 크거나 비정상일 경우
            }
        });

        // 첫 포커스시 전체 선택 or 비우기
        textField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (isNowFocused && textField.getText().equals("0")) {
                textField.clear();
            }
        });
    }

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
            String mileageRaw = mileageField.getText().replaceAll(",", "");
            String costRaw = costField.getText().replaceAll(",", "");

            String sql = "INSERT INTO repair (motorbike_id, mileage, description, cost) VALUES (?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, motorbikeId);
            pstmt.setInt(2, Integer.parseInt(mileageRaw));
            pstmt.setString(3, descField.getText());
            pstmt.setInt(4, Integer.parseInt(costRaw));
            pstmt.executeUpdate();

            showAlert("등록 완료", "수리 내역이 등록되었습니다.");
            ((Stage) nameField.getScene().getWindow()).close();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("DB 오류", "수리 등록 중 오류 발생: " + e.getMessage());
        } catch (NumberFormatException e) {
            showAlert("입력 오류", "주행 거리와 수리 금액은 숫자로만 입력해주세요.");
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
