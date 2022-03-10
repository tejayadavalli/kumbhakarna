package kumbhakarna.utils;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.google.gson.JsonObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Utils {
    private static JsonMapper mapper = new JsonMapper();

    public static String convertToDBDate(String date) {
        if (date == null || date.trim().equals("")) return null;
        String[] parts = date.split(" ");
        String[] dateParts = parts[0].split("/");
        StringBuilder reverseDateString = new StringBuilder();
        for (int i = 2; i >= 0; i--) {
            reverseDateString.append(dateParts[i]).append("/");
        }
        return reverseDateString.substring(0, reverseDateString.length() - 1) + " " +
                parts[1] + " " + parts[2];
    }

    public static String convertToHumanReadableDate(String date) {
        return convertToDBDate(date);
    }

    public static void main(String[] args) {
        String date = "04/07/2021 10:26 AM";
        String machineDate = convertToDBDate(date);
        String humanReadableDate = convertToHumanReadableDate(machineDate);
        System.out.println(machineDate);
        System.out.println(humanReadableDate);
    }

    public static void sendMessage(String flockUrl,
                                   JsonObject jsonObject) throws IOException {
        URL url = new URL(flockUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(5000);
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setRequestMethod("POST");

        OutputStream os = conn.getOutputStream();
        os.write(jsonObject.toString().getBytes("UTF-8"));
        os.close();

        // read the response
        InputStream in = new BufferedInputStream(conn.getInputStream());
        String result = org.apache.commons.io.IOUtils.toString(in, "UTF-8");

        in.close();
        conn.disconnect();
    }
}
