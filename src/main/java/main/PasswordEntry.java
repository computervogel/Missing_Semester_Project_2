package main;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.File;

public class PasswordEntry {
    private final StringProperty website;
    private final StringProperty password;
    private final StringProperty date;
    private final ObjectProperty<ImageView> imageView;
    private final String imagePath;

    public PasswordEntry(String website, String password, String date, String imagePath) {
        this.website = new SimpleStringProperty(website);
        this.password = new SimpleStringProperty(password);
        this.date = new SimpleStringProperty(date);
        this.imagePath = imagePath;
        // Initialize ImageView if imagePath is valid
        ImageView iv = null;
        if (imagePath != null && !imagePath.trim().isEmpty()) {
            File file = new File(imagePath);
            if (file.exists()) {
                iv = new ImageView(new Image(file.toURI().toString()));
                // optional: set sizing constraints
                iv.setFitWidth(200);
                iv.setFitHeight(200);
                iv.setPreserveRatio(true);
            }
        }
        this.imageView = new SimpleObjectProperty<>(iv);
    }

    public StringProperty websiteProperty() {
        return website;
    }

    public String getWebsite() {
        return website.get();
    }

    public void setWebsite(String website) {
        this.website.set(website);
    }

    public StringProperty passwordProperty() {
        return password;
    }

    public String getPassword() {
        return password.get();
    }

    public StringProperty dateProperty() {
        return date;
    }

    public String getDate() {
        return date.get();
    }

    public ImageView getImageView() {
        return imageView.get();
    }

    public void setImageView(ImageView iv) {
        imageView.set(iv);
    }

    public ObjectProperty<ImageView> imageViewProperty() {
        return imageView;
    }

    public String getImagePath() {
        return imagePath;
    }


    public String toCsvString() {
        return getWebsite() + "," + getPassword() + "," + getDate() + "," + getImagePath();
    }
}
