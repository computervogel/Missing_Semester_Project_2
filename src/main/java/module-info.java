module Project {
    requires javafx.controls;

    requires javafx.fxml;
    requires java.desktop;
    requires com.google.gson;
    requires guide;
    requires okhttp3;

    opens main;
}