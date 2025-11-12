module org.example.demo_project_fx {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    opens org.example.demo_project_fx to javafx.fxml;
}
