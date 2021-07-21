package kumbhakarna.utils;

import com.google.common.collect.ImmutableMap;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class TextLocalSmsSender {
    private static Map<String, String> smsString = ImmutableMap.of(
            "CheckIn" , "Dear%20Guest%2C%20%0AThank%20you%20for%20choosing%20to%20stay%20at%20Hotel%20Kumbhakarna.%20We%20are%20delighted%20to%20have%20you%20Checked-In.%20Some%20pointers%20while%20you%20stay%20at%20the%20hotel%0A1.%20TV%20has%20access%20to%20platforms%20like%20Netflix%2C%20Youtube%2C%20Prime%20Video%0A2.%20Password%20for%20the%20wifi%3A%20netflix%40kumbhakarna112%0A3.%2024hrs%20hot%20water%20supply%20is%20available%0A4.%20Dial%209%20on%20the%20landline%20to%20reach%20reception%0AWe%20would%20love%20to%20provide%20you%20a%20pleasant%20stay.%20Don%27t%20hesitate%20to%20reach%20out%20to%20reception%20for%20any%20assistance%20required",
            "CheckOut", "We%20hope%20you%20had%20a%20pleasant%20stay%20at%20Hotel%20Kumbhakarna.%20We%20are%20pleased%20to%20serve%20you%3B%20We%20need%20your%20support%20in%20reaching%20a%20wider%20audience.%20Would%20you%20please%20review%20us%20on%20Google%3A%20https%3A%2F%2Fg.page%2Fhotel-kumbhakarna%2Freview%20%3F"
    );
    public static String sendSms(String identifier, String phoneNumber) {
        try {
            // Construct data
            String apiKey = "apikey=" + "NjE3Mjc4NmYzNDU2NDU3ODUyNTA2OTRlNmU1YTUwNmE=\t";
            String message = "&message=" + smsString.get(identifier);
            String sender = "&sender=" + "HTLKUM";
            String numbers = "&numbers=" + phoneNumber;

            // Send data
            HttpURLConnection conn = (HttpURLConnection) new URL("https://api.textlocal.in/send/?").openConnection();
            String data = apiKey + numbers + message + sender;
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Length", Integer.toString(data.length()));
            conn.getOutputStream().write(data.getBytes("UTF-8"));
            final BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            final StringBuffer stringBuffer = new StringBuffer();
            String line;
            while ((line = rd.readLine()) != null) {
                stringBuffer.append(line);
            }
            rd.close();

            return stringBuffer.toString();
        } catch (Exception e) {
            System.out.println("Error SMS "+e);
            return "Error "+e;
        }
    }
}
