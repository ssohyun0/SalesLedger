package org.example.salesledger;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.salesledger.database.DataBase;

import java.sql.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;
import javafx.util.StringConverter;
import javafx.util.converter.NumberStringConverter;

public class RepairRegisterController {

    @FXML private TextField nameField, phoneField;
    @FXML private Label mileageLabel, descLabel, costLabel;
    @FXML private TextField mileageField, costField;
    @FXML private TextArea descField;
    @FXML private Button registerButton;
    @FXML private VBox bikeToggleContainer;

    private int motorbikeId = -1;

    @FXML
    public void initialize() {
        setCommaFormatter(mileageField);
        setCommaFormatter(costField);
    }

    private void setCommaFormatter(TextField textField) {
        DecimalFormat format = new DecimalFormat("#,###");

        textField.textProperty().addListener((obs, oldVal, newVal) -> {
            String digits = newVal.replaceAll(",", "").replaceAll("[^\\d]", "");
            if (digits.isEmpty()) {
                textField.setText("");
                return;
            }

            try {
                long number = Long.parseLong(digits);
                String formatted = format.format(number);
                int caretPos = textField.getCaretPosition();
                textField.setText(formatted);
                textField.positionCaret(caretPos + (formatted.length() - newVal.length()));
            } catch (NumberFormatException e) {
                // 무시
            }
        });

        textField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (isNowFocused && textField.getText().equals("0")) {
                textField.clear();
            }
        });
    }

    @FXML
    private void onCheckCustomer() {
        String name = nameField.getText().trim();
        String phoneSuffix = phoneField.getText().trim();

        if (name.isEmpty() || phoneSuffix.isEmpty()) {
            showAlert("입력 오류", "이름과 전화번호 뒷자리를 입력해주세요.");
            return;
        }

        try (Connection conn = DataBase.connect()) {
            String sql = """
                SELECT m.id, m.bike_number 
                FROM customer c
                JOIN motorbike m ON c.id = m.customer_id
                WHERE c.name = ? AND substr(replace(c.phone, '-', ''), -4) = ?
            """;

            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, name);
            pstmt.setString(2, phoneSuffix);
            ResultSet rs = pstmt.executeQuery();

            List<Integer> motorbikeIds = new ArrayList<>();
            List<String> bikeNumbers = new ArrayList<>();

            while (rs.next()) {
                motorbikeIds.add(rs.getInt("id"));
                bikeNumbers.add(rs.getString("bike_number"));
            }

            if (motorbikeIds.isEmpty()) {
                showAlert("고객 없음", "입력한 정보와 일치하는 고객이 없습니다.");
            } else if (motorbikeIds.size() == 1) {
                motorbikeId = motorbikeIds.get(0);
                showRepairInputFields();
            } else {
                showBikeToggleUI(motorbikeIds, bikeNumbers);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("DB 오류", "고객 확인 중 오류 발생: " + e.getMessage());
        }
    }

    private void showBikeToggleUI(List<Integer> ids, List<String> numbers) {
        ToggleGroup group = new ToggleGroup();
        bikeToggleContainer.getChildren().clear();

        for (int i = 0; i < ids.size(); i++) {
            RadioButton rb = new RadioButton(numbers.get(i));
            int selectedId = ids.get(i);
            rb.setToggleGroup(group);
            rb.setOnAction(e -> {
                motorbikeId = selectedId;
                showRepairInputFields();
            });
            bikeToggleContainer.getChildren().add(rb);
        }

        bikeToggleContainer.setVisible(true);
        bikeToggleContainer.setManaged(true);
    }

    private void showRepairInputFields() {
        mileageLabel.setVisible(true); mileageLabel.setManaged(true);
        mileageField.setVisible(true); mileageField.setManaged(true);

        descLabel.setVisible(true); descLabel.setManaged(true);
        descField.setVisible(true); descField.setManaged(true);

        costLabel.setVisible(true); costLabel.setManaged(true);
        costField.setVisible(true); costField.setManaged(true);

        registerButton.setVisible(true); registerButton.setManaged(true);
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
