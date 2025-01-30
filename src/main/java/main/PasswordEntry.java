package main;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class PasswordEntry {
    private final StringProperty website;
    private final StringProperty password;
    private final StringProperty date;

    public PasswordEntry(String website, String password, String date) {
        this.website = new SimpleStringProperty(website);
        this.password = new SimpleStringProperty(password);
        this.date = new SimpleStringProperty(date);
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

    public String toCsvString() {
        return getWebsite() + "," + getPassword() + "," + getDate();
    }
}
