package org.example.salesledger;

import javafx.fxml.FXML;
import javafx.stage.Stage;

public class MainController {
    @FXML
    private void onCustomerRegisterClicked() {
        System.out.println("고객 등록 버튼 클릭됨");

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