package org.example.salesledger.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.File;

public class DataBase {

    // DB 파일 경로 (프로젝트 루트 기준)
    private static final String DB_PATH = "database/DataBase.db";
    private static final String DB_URL = "jdbc:sqlite:" + DB_PATH;

    // DB 연결
    public static Connection connect() {
        try {
            File dbFile = new File(DB_PATH);

            // DB 파일 존재 여부 확인
            if (!dbFile.exists()) {
                System.err.println("❌ DB 파일을 찾을 수 없습니다: " + dbFile.getAbsolutePath());
                return null;
            }

            Connection conn = DriverManager.getConnection(DB_URL);
            System.out.println("✅ SQLite 연결 성공: " + dbFile.getAbsolutePath());
            return conn;
        } catch (SQLException e) {
            System.err.println("❌ SQLite 연결 실패: " + e.getMessage());
            return null;
        }
    }
}
