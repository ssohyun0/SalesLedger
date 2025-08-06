package org.example.salesledger;

import java.sql.Connection;
import org.sqlite.JDBC; // 빨간줄 없어졌는지 확인

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.sql.Connection; // JDBC 연결 클래스
import org.example.salesledger.database.DataBase;

import java.io.IOException;

public class MainApplication extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        // DB Check
        // DB 연결 확인 (프로그램 시작 시 1회)
        // DB 연결 확인
        try (Connection conn = DataBase.connect()) {
            if (conn == null) {
                System.err.println("❌ DB 연결 실패! 프로그램 종료.");
                System.exit(1);
            }
        }

        // DB 연결 및 테이블 초기화
        DataBase.initializeTables();

        // UI Load
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("MainView.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 700, 500);
        //Scene scene = new Scene(fxmlLoader.load());

        stage.setTitle("고객 장부 프로그램");
        stage.setScene(scene);

        //stage.setMaximized(true);

        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}