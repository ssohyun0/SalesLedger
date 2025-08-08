module org.example.salesledger {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.controlsfx.controls;
    requires org.xerial.sqlitejdbc;

    opens org.example.salesledger to javafx.fxml;

    exports org.example.salesledger;
}
