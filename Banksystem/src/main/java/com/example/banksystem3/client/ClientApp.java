package com.example.banksystem3.client;

import com.example.banksystem3.client.utils.ViewManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientApp extends Application {

    private static final Logger logger = Logger.getLogger(ClientApp.class.getName());

    @Override
    public void start(Stage primaryStage) {
        logger.info("🚀 Starting Banking System Client...");
        printStartupInstructions();

        try {
            ViewManager.setPrimaryStage(primaryStage);
            ViewManager.switchScene(ViewManager.LOGIN, "🏦 Banking System - Login");
            logger.info("✅ Client started successfully!");

        } catch (IOException e) {
            logger.log(Level.SEVERE, "FATAL: Could not load the main login screen. The application cannot start.", e);
            showErrorDialog("Fatal Error", "Could not load the main login screen.", e);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "FATAL: An unexpected error occurred during application start.", e);
            showErrorDialog("Fatal Error", "An unexpected error occurred during startup.", e);
        }
    }

    /**
     * Displays a simple error window. This can be called when a critical
     * loading error prevents the main UI from appearing. This is now improved
     * to use a standard Alert dialog with expandable content for the stack trace.
     *
     * @param title   The title for the alert dialog.
     * @param header  The main header message for the alert.
     * @param ex      The exception to display in an expandable section.
     */
    private void showErrorDialog(String title, String header, Exception ex) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(ex.getMessage());

            // Create expandable Exception details
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            String exceptionText = sw.toString();

            TextArea textArea = new TextArea(exceptionText);
            textArea.setEditable(false);
            textArea.setWrapText(true);

            textArea.setMaxWidth(Double.MAX_VALUE);
            textArea.setMaxHeight(Double.MAX_VALUE);
            GridPane.setVgrow(textArea, Priority.ALWAYS);
            GridPane.setHgrow(textArea, Priority.ALWAYS);

            GridPane expContent = new GridPane();
            expContent.setMaxWidth(Double.MAX_VALUE);
            expContent.add(new Label("The exception stacktrace was:"), 0, 0);
            expContent.add(textArea, 0, 1);

            // Set expandable Exception into the dialog pane.
            alert.getDialogPane().setExpandableContent(expContent);

            alert.showAndWait();
        });
    }

    private void printStartupInstructions() {
        System.out.println("╔════════════════════════════════════════╗");
        System.out.println("║        BANKING SYSTEM CLIENT           ║");
        System.out.println("╚════════════════════════════════════════╝");
        System.out.println("\n📋 Before starting the client:");
        System.out.println("  1. Start the server first: Run ServerMain");
        System.out.println("  2. Make sure RMI registry is running on port 1099");
        System.out.println("─────────────────────────────────────");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
