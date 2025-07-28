package com.client.util;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.ByteArrayInputStream;
import java.util.Base64;

public class Base64ImageHelper {

    public static Image getImageViewFromBase64(String base64String) {
        if (base64String == null || base64String.isEmpty()) {
            return null;
        }

        String base64Data = base64String;
        if (base64String.startsWith("data:image")) {
            int commaIndex = base64String.indexOf(',');
            if (commaIndex != -1) {
                base64Data = base64String.substring(commaIndex + 1);
            }
        }

        try {
            byte[] decodedBytes = Base64.getDecoder().decode(base64Data);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(decodedBytes);
            return new Image(inputStream);
        } catch (IllegalArgumentException e) {
            System.err.println("Error decoding Base64 string: " + e.getMessage());
            return null;
        }

    }
}
