package com.example.countdownapp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Updater {
    private static final String VERSION_FILE = "/version.txt"; // Vendosni versionin lokal
    private static final String GITHUB_API_URL = "https://api.github.com/repos/SignorTynam/CountdownApp/releases/latest";

    public static boolean checkForUpdates() {
        try {
            // Lexoni versionin lokal
            String localVersion = getLocalVersion();

            // Merrni versionin më të fundit nga GitHub
            String latestVersion = getLatestVersion();

            if (latestVersion != null && !localVersion.equals(latestVersion)) {
                System.out.println("Përditësimi i ri është i disponueshëm: " + latestVersion);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static String getLocalVersion() throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                Updater.class.getResourceAsStream(VERSION_FILE)))) {
            return reader.readLine().trim();
        }
    }

    private static String getLatestVersion() throws Exception {
        URL url = new URL(GITHUB_API_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/vnd.github.v3+json");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            // Ekstrakto versionin nga JSON
            String json = response.toString();
            int versionIndex = json.indexOf("\"tag_name\":\"") + 11;
            return json.substring(versionIndex, json.indexOf("\"", versionIndex));
        }
    }
}

