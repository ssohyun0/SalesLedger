package org.example.salesledger.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
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

    public static void initializeTables() {
        String createCustomerTable = """
            CREATE TABLE IF NOT EXISTS customer (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                phone TEXT NOT NULL
            );
            """;

        String createMotorbikeTable = """
            CREATE TABLE IF NOT EXISTS motorbike (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                customer_id INTEGER NOT NULL,
                bike_number TEXT NOT NULL,
                bike_model TEXT,
                FOREIGN KEY (customer_id) REFERENCES customer(id) ON DELETE CASCADE
            );
            """;

        String createRepairTable = """
            CREATE TABLE IF NOT EXISTS repair (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                motorbike_id INTEGER NOT NULL,
                mileage INTEGER,
                description TEXT,
                repair_date TEXT DEFAULT (DATE('now', 'localtime')),
                cost INTEGER,
                FOREIGN KEY (motorbike_id) REFERENCES motorbike(id) ON DELETE CASCADE
            );
            """;

        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.execute(createCustomerTable);
            stmt.execute(createMotorbikeTable);
            stmt.execute(createRepairTable);
            System.out.println("✅ 모든 테이블이 준비되었습니다.");
        } catch (SQLException e) {
            System.err.println("❌ 테이블 생성 실패: " + e.getMessage());
        }
    }
}
