package com.example.banksystem3.client.controller.admin;

import com.example.banksystem3.client.controller.Navigable;
import javafx.fxml.FXML;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.net.URL;

public class ExchangeRatesController implements Navigable {

    @FXML
    private WebView exchangeWebView;

    private AdminDashboardController parentController;

    @FXML
    public void initialize() {
        WebEngine webEngine = exchangeWebView.getEngine();
        // Load the local HTML file for the dashboard
        URL url = getClass().getResource("/com/example/banksystem3/view/admin/ETBExchangeDashboard.html");
        if (url != null) {
            webEngine.load(url.toExternalForm());
        }
    }

    @Override
    public void setParentController(AdminDashboardController controller) {
        this.parentController = controller;
    }
}