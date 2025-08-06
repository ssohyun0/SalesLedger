package org.example.salesledger;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.salesledger.database.DataBase;

import java.io.File;
import java.sql.*;

public class CustomerRegisterController {

    @FXML private TextField nameField;
    @FXML private TextField phoneField;
    @FXML private TextField bikeNumberField;
    @FXML private TextField bikeModelField;

    @FXML
    public void initialize() {
        // 📌 전화번호 입력 시 자동 하이픈 처리
        phoneField.textProperty().addListener((obs, oldValue, newValue) -> {
            // 숫자만 추출
            String digits = newValue.replaceAll("[^0-9]", "");
            if (digits.length() > 11) {
                digits = digits.substring(0, 11); // 최대 11자리 제한
            }

            StringBuilder sb = new StringBuilder();
            if (digits.length() > 3) {
                sb.append(digits, 0, 3).append("-");
                if (digits.length() > 7) {
                    sb.append(digits, 3, 7).append("-").append(digits.substring(7));
                } else {
                    sb.append(digits.substring(3));
                }
            } else {
                sb.append(digits);
            }

            if (!newValue.equals(sb.toString())) {
                phoneField.setText(sb.toString());
            }
        });
    }

    @FXML
    private void onRegisterClicked() {
        String name = nameField.getText().trim();
        String phone = phoneField.getText().trim();
        String bikeNumber = bikeNumberField.getText().trim();
        String bikeModel = bikeModelField.getText().trim();

        // 필수 입력값 체크
        if (name.isEmpty() || phone.isEmpty() || bikeNumber.isEmpty()) {
            showAlert("입력 오류", "이름, 전화번호, 오토바이 번호는 필수 입력입니다.");
            return;
        }

        // 전화번호 형식 검증
        if (!phone.matches("^\\d{3}-\\d{4}-\\d{4}$")) {
            showAlert("입력 오류", "전화번호는 000-0000-0000 형식으로 입력해주세요.");
            return;
        }

        try (Connection conn = DataBase.connect()) {
            // DB 연결 체크
            if (conn == null) {
                System.out.println("❌ DB 연결 실패");
                showAlert("DB 오류", "데이터베이스에 연결할 수 없습니다.");
                return;
            }

            // DB 경로 확인 로그
            String dbPath = new File("database/DataBase.db").getAbsolutePath();
            System.out.println("✅ DB 연결 성공 (" + dbPath + ")");

            // 오토커밋 켜기 (테스트 용)
            conn.setAutoCommit(true);

            // 1. 동일 고객 존재 여부 확인
            String findCustomerSql = "SELECT id FROM customer WHERE name = ? AND phone = ?";
            int customerId = -1;

            try (PreparedStatement pstmt = conn.prepareStatement(findCustomerSql)) {
                pstmt.setString(1, name);
                pstmt.setString(2, phone);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    customerId = rs.getInt("id");
                    System.out.println("ℹ️ 기존 고객 발견 (ID: " + customerId + ")");
                }
            }

            // 2. 고객 없으면 새로 등록
            if (customerId == -1) {
                String insertCustomerSql = "INSERT INTO customer (name, phone) VALUES (?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(insertCustomerSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                    pstmt.setString(1, name);
                    pstmt.setString(2, phone);
                    int rows = pstmt.executeUpdate();
                    System.out.println("👤 고객 INSERT 영향받은 행: " + rows);

                    ResultSet keys = pstmt.getGeneratedKeys();
                    if (keys.next()) {
                        customerId = keys.getInt(1);
                        System.out.println("🆕 새 고객 ID: " + customerId);
                    }
                }
            }

            // 3. 오토바이 등록
            String insertBikeSql = "INSERT INTO motorbike (customer_id, bike_number, bike_model) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(insertBikeSql)) {
                pstmt.setInt(1, customerId);
                pstmt.setString(2, bikeNumber);
                pstmt.setString(3, bikeModel);
                int rows = pstmt.executeUpdate();
                System.out.println("🏍 오토바이 INSERT 영향받은 행: " + rows);
            }

            showAlert("등록 완료", "고객 정보가 저장되었습니다.");
            System.out.println("✅ 고객 등록 완료");

            // 창 닫기
            Stage stage = (Stage) nameField.getScene().getWindow();
            stage.close();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("DB 오류", "데이터 저장 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @FXML
    private void onCancelClicked() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
