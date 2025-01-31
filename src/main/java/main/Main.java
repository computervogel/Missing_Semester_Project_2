package main;

import javafx.application.Application;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.awt.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


public class Main extends Application {
    private static final String ACCOUNTS_FILE = "accounts.txt";
    private static final String PASSWORDS_FILE = "passwords.txt";
    private final List<main.PasswordEntry> entries = new ArrayList<>();
    private String loggedInUser = null;

    private static TableColumn<PasswordEntry, String> getPasswordEntryStringTableColumn() {
        TableColumn<PasswordEntry, String> passwordColumn = new TableColumn<>("Password");
        passwordColumn.setCellValueFactory(cellData -> cellData.getValue().passwordProperty());
        passwordColumn.setCellFactory(_ -> new TableCell<>() {
            private final Label label = new Label();

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    label.setText("******");
                    setGraphic(label);
                    label.setOnMouseClicked(_ -> label.setText(label.getText().equals("******") ? item : "******"));
                }
            }
        });
        return passwordColumn;
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        showLoginScreen(primaryStage);
    }

    private void showLoginScreen(Stage stage) {
        VBox loginLayout = new VBox(10);
        loginLayout.setPadding(new Insets(20));
        loginLayout.setStyle("-fx-alignment: center;");

        Label titleLabel = new Label("Password manager - Login");
        titleLabel.setStyle("-fx-font-size: 20px;");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        Label messageLabel = new Label();
        messageLabel.setStyle("-fx-text-fill: red;");

        Button loginButton = new Button("Login with existing account");
        loginButton.setOnAction(_ -> {
            if (login(usernameField.getText(), passwordField.getText())) {
                showPasswordManager(stage);
            } else {
                messageLabel.setText("Login failed. Username or Password incorrect or missing.");
            }
        });

        Button createAccountButton = getCreateAccountButton(usernameField, passwordField, messageLabel);

        Button clearAllDataButton = getClearAllDataButton(messageLabel);

        Button exitButton = new Button("Exit");
        exitButton.setOnAction(_ -> System.exit(0));


        loginLayout.getChildren().addAll(titleLabel, usernameField, passwordField, loginButton, createAccountButton, clearAllDataButton, exitButton, messageLabel);

        Scene loginScene = new Scene(loginLayout, 400, 300);
        stage.setScene(loginScene);
        stage.setTitle("Password manager - Login");
        stage.show();
    }

    private Button getClearAllDataButton(Label messageLabel) {
        Button clearAllDataButton = new Button("Delete all data");
        clearAllDataButton.setOnAction(_ -> {
            Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION,
                    "Do you really want to delete all your data?", ButtonType.YES, ButtonType.NO);
            confirmationAlert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) {
                    if (clearAllData()) {
                        messageLabel.setText("All data has been successfully deleted!");
                    } else {
                        messageLabel.setText("Error when deleting all data.");
                    }
                }
            });
        });
        return clearAllDataButton;
    }

    private Button getCreateAccountButton(TextField usernameField, PasswordField passwordField, Label messageLabel) {
        Button createAccountButton = new Button("Register new user");
        createAccountButton.setOnAction(_ -> {
            if (createAccount(usernameField.getText(), passwordField.getText())) {
                messageLabel.setText("Account successfully created!");
            } else {
                messageLabel.setText("Error: User name or password invalid or already taken.");
            }
        });
        return createAccountButton;
    }

    private boolean login(String username, String password) {
        try (BufferedReader reader = new BufferedReader(new FileReader(ACCOUNTS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2 && parts[0].equals(username) && parts[1].equals(password)) {
                    loggedInUser = username;
                    loadEntries();
                    return true;
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading the account file.");
        }
        return false;
    }

    private boolean createAccount(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) return false;

        if (isUsernameTaken(username)) {
            System.out.println("Username is already taken.");
            return false;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ACCOUNTS_FILE, true))) {
            writer.write(username + "," + password);
            writer.newLine();
            return true;
        } catch (IOException e) {
            System.out.println("Error saving the account.");
        }
        return false;
    }

    private void showPasswordManager(Stage stage) {

        ImageGenerator imageGenerator = new ImageGenerator();

        VBox mainLayout = new VBox(10);
        mainLayout.setPadding(new Insets(20));

        HBox addEntryLayout = new HBox(10);
        TextField websiteField = new TextField();
        websiteField.setPromptText("Website");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        Button addButton = new Button("Add");

        addEntryLayout.getChildren().addAll(websiteField, passwordField, addButton);

        TableView<main.PasswordEntry> tableView = new TableView<>();
        TableColumn<PasswordEntry, String> websiteColumn = new TableColumn<>("Website");
        websiteColumn.setCellValueFactory(cellData -> cellData.getValue().websiteProperty());
        websiteColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        websiteColumn.setOnEditCommit(e -> e.getRowValue().setWebsite(e.getNewValue()));

        TableColumn<PasswordEntry, String> passwordColumn = getPasswordEntryStringTableColumn();

        TableColumn<PasswordEntry, String> dateColumn = new TableColumn<>("Date/Time");
        dateColumn.setCellValueFactory(cellData -> cellData.getValue().dateProperty());

        TableColumn<PasswordEntry, ImageView> pictureColumn = new TableColumn<>("Gedächtnisbild");
        pictureColumn.setCellValueFactory(new PropertyValueFactory<>("imageView"));

        //noinspection unchecked
        tableView.getColumns().addAll(websiteColumn, passwordColumn, dateColumn, pictureColumn);
        tableView.getItems().addAll(entries);
        tableView.setEditable(true);

        addButton.setOnAction(_ -> {
            String website = websiteField.getText();
            String password = passwordField.getText();
            if (!website.isEmpty() && !password.isEmpty()) {
                String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
                // generate image
                String filename = loggedInUser + "_" + website + ".png";
                String imagePath = imageGenerator.generateImage(password, filename);

                if (imagePath == null) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Could not generate image", ButtonType.OK);
                    alert.setTitle("Error");
                    alert.setHeaderText("Image Generation Failed");
                    alert.showAndWait();
                    imagePath = "None";
                }

                PasswordEntry entry = new PasswordEntry(website, password, date, imagePath);
                entries.add(entry);
                tableView.getItems().add(entry);
                saveEntries();
                websiteField.clear();
                passwordField.clear();
            }
        });

        Button deleteButton = new Button("Delete");
        deleteButton.setOnAction(_ -> {
            PasswordEntry selectedEntry = tableView.getSelectionModel().getSelectedItem();
            if (selectedEntry != null) {
                entries.remove(selectedEntry);
                tableView.getItems().remove(selectedEntry);
                saveEntries();
            }
        });

        Button logoutButton = new Button("Log out");
        logoutButton.setOnAction(_ -> {
            loggedInUser = null;
            entries.clear();
            showLoginScreen(stage);
        });

        mainLayout.getChildren().addAll(addEntryLayout, tableView, deleteButton, logoutButton);

        Scene mainScene = new Scene(mainLayout, 600, 400);
        stage.setScene(mainScene);
        stage.setTitle("Password manager");
        stage.show();
    }

    private void loadEntries() {
        entries.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(PASSWORDS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 5 && parts[0].equals(loggedInUser)) {
                    // website, password, date, image
                    entries.add(new PasswordEntry(parts[1], parts[2], parts[3], parts[4]));
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading the entries.");
        }
    }

    private void saveEntries() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(PASSWORDS_FILE))) {
            for (PasswordEntry entry : entries) {
                writer.write(loggedInUser + "," + entry.toCsvString());
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error when saving the entries.");
        }
    }

    private boolean clearAllData() {
        try {
            deleteLocalFile("accounts.txt");
            deleteLocalFile("passwords.txt");
            return true;
        } catch (Exception e) {
            System.out.println("Error when deleting all data: " + e.getMessage());
        }
        return false;
    }

    private void deleteLocalFile(String fileName) {
        File file = new File(fileName);
        if (file.exists()) {
            if (file.delete()) {
                System.out.println("File " + fileName + " deleted successfully.");
            } else {
                System.out.println("Error when deleting the file " + fileName + ".");
            }
        } else {
            System.out.println("File " + fileName + " does not exist.");
        }
    }

    private boolean isUsernameTaken(String username) {
        try (BufferedReader reader = new BufferedReader(new FileReader(ACCOUNTS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length > 0 && parts[0].equals(username)) {
                    return true;
                }
            }
        } catch (IOException e) {
            System.out.println("Error while checking username.");
        }
        return false;
    }
}