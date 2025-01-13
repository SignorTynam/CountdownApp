package com.example.countdownapp;

public class Main {
    public static void main(String[] args) {
        if (Updater.checkForUpdates()) {
            Downloader.downloadUpdate(
                    "https://github.com/SignorTynam/CountdownApp/releases/download/v2.0/CountdownApp.jar",
                    "CountdownApp-Updated.jar"
            );
            System.out.println("Aplikacioni është përditësuar!");
        } else {
            System.out.println("Nuk ka përditësime të reja.");
        }

        CountdownApp.main(args);
    }
}
