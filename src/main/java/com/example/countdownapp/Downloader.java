package com.example.countdownapp;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.net.URL;

public class Downloader {
    public static void downloadUpdate(String downloadUrl, String savePath) {
        try (BufferedInputStream in = new BufferedInputStream(new URL(downloadUrl).openStream());
             FileOutputStream fileOutputStream = new FileOutputStream(savePath)) {
            byte[] dataBuffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }
            System.out.println("Shkarkimi përfundoi: " + savePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

