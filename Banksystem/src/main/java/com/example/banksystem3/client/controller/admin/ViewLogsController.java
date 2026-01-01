package com.example.banksystem3.client.controller.admin;

import com.example.banksystem3.client.controller.Navigable;
import com.example.banksystem3.client.rmi.RMIClient;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.List;

public class ViewLogsController implements Navigable {

    @FXML private TableView<String> logsTable;
    @FXML private TableColumn<String, String> logColumn;

    private AdminDashboardController parentController;

    @FXML
    public void initialize() {
        // Configure column to display the string directly
        logColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()));
        
        loadLogs();
    }

    private void loadLogs() {
        try {
            List<String> logs = RMIClient.getInstance().getBankService().getSystemLogs();
            ObservableList<String> data = FXCollections.observableArrayList(logs);
            logsTable.setItems(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleRefresh() {
        loadLogs();
    }

    @Override
    public void setParentController(AdminDashboardController controller) {
        this.parentController = controller;
    }
}