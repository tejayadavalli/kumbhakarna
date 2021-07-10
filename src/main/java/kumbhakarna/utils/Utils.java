package kumbhakarna.utils;

import com.fasterxml.jackson.databind.json.JsonMapper;

public class Utils {
    private static JsonMapper mapper = new JsonMapper();

    public static String convertToDBDate(String date){
        if(date == null || date.trim().equals("")) return null;
        String[] parts = date.split(" ");
        String[] dateParts = parts[0].split("/");
        StringBuilder reverseDateString = new StringBuilder();
        for(int i=2; i>=0; i--){
            reverseDateString.append(dateParts[i]).append("/");
        }
        return reverseDateString.substring(0, reverseDateString.length()-1) +  " " +
                parts[1] + " " + parts[2];
    }

    public static String convertToHumanReadableDate(String date){
        return convertToDBDate(date);
    }

    public static void main(String[] args) {
        String date = "04/07/2021 10:26 AM";
        String machineDate = convertToDBDate(date);
        String humanReadableDate = convertToHumanReadableDate(machineDate);
        System.out.println(machineDate);
        System.out.println(humanReadableDate);
    }
}
