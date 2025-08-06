package org.example.salesledger;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainController {
    @FXML
    private void onCustomerRegisterClicked() {
        System.out.println("고객 등록 버튼 클릭됨");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("CustomerRegisterView.fxml"));
            Stage stage = new Stage();
            stage.setTitle("고객 등록");
            stage.setScene(new Scene(loader.load()));
            stage.showAndWait(); // 등록 창 닫힐 때까지 대기
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onRepairRegisterClicked() {
        System.out.println("수리 등록 버튼 클릭됨");

    }

    @FXML
    private void onCustomerListClicked() {
        System.out.println("고객 리스트 버튼 클릭됨");

    }

    @FXML
    private void onExitClicked() {
        javafx.application.Platform.exit();
    }

}