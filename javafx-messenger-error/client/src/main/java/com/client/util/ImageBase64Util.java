package com.client.util;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

public class ImageBase64Util {
    public static String encodeImageToBase64(String imagePath) {
        try {
            byte[] imageBytes = Files.readAllBytes(Paths.get(imagePath));
            return Base64.getEncoder().encodeToString(imageBytes);
        } catch (IOException e) {
            System.err.println("Failed to read image: " + e.getMessage());
            return null;
        }
    }

    public static void main(String[] args) {
        System.out.println(encodeImageToBase64("C:/Users/user/OneDrive/Pictures/IMG_5326.jpg"));
    }
}
