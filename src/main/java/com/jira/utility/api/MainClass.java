package com.jira.utility.api;

import javafx.application.Application;
import javafx.stage.Stage;

public class MainClass extends Application {
    @Override
    public void start(Stage primaryStage) {
            new LoginStage();
    }
    public static void main(String[] args) {
    	System.out.println(System.getProperty("java.class.path"));
        launch(args);
    }
}