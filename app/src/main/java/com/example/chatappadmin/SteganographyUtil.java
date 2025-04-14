package com.example.chatappadmin;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

public class SteganographyUtil {
    public interface ExtractCallback {
        void onCompleted(String extractedText);
        void onError(Exception e);
    }

    public static void extractDataAsync(Bitmap bitmap, ExtractCallback callback) {
        new AsyncTask<Void, Void, String>() {
            Exception error;

            @Override
            protected String doInBackground(Void... voids) {
                try {
                    return extractData(bitmap);
                } catch (Exception e) {
                    error = e;
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String result) {
                if (error != null) {
                    callback.onError(error);
                } else {
                    callback.onCompleted(result);
                }
            }
        }.execute();
    }

    // Actual extraction
    protected static String extractData(Bitmap bitmap) {
        StringBuilder binaryBuilder = new StringBuilder();
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        // Read ONLY LSB from BLUE channel
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = bitmap.getPixel(x, y);
                int blue = Color.blue(pixel);
                binaryBuilder.append(blue & 1);
            }
        }

        // Convert binary to byte[]
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        for (int i = 0; i + 7 < binaryBuilder.length(); i += 8) {
            String byteStr = binaryBuilder.substring(i, i + 8);
            int byteVal = Integer.parseInt(byteStr, 2);
            if (byteVal == 0) break; // Null terminator
            byteStream.write(byteVal);
        }

        try {
            String allText = new String(byteStream.toByteArray(), StandardCharsets.UTF_8);
            int startIndex = allText.indexOf("#START#");
            if (startIndex == -1) return "❌ No hidden data found.";

            String extracted = allText.substring(startIndex + 7).trim();

            // Only allow readable characters: +, digits, letters, hyphen, underscore, colon, comma, whitespace
            extracted = extracted.replaceAll("[^\\+\\dA-Za-z@,\\-_:\\s]", "").trim();

            return extracted.isEmpty() ? "❌ No hidden data found." : extracted;

        } catch (Exception e) {
            Log.e("SteganographyUtil", "❌ Error decoding UTF-8: " + e.getMessage());
            return "❌ Failed to decode hidden data.";
        }
    }
}
