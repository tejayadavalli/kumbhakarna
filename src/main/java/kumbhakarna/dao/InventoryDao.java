package kumbhakarna.dao;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import kumbhakarna.model.RevenuInfoEntry;
import kumbhakarna.model.RoomData;
import kumbhakarna.model.RoomStatus;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class InventoryDao {
    private Connection connection;
    private static RevenuInfoEntry.CourtRevenueInfo DEFAULT_COURT_REVENUE =  getDefaultCourtRevenue();
    private static final Gson gson = new Gson();
    private static final JsonParser jsonParser = new JsonParser();
    public InventoryDao(){
        try {
            this.connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/smaash", "postgres", "postgres");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public RevenuInfoEntry getRevenueInfo(String date){
        try {
            PreparedStatement st = connection.prepareStatement("SELECT * FROM revenueInfo WHERE id = ?::date");
            st.setString(1, date);
            ResultSet rs = st.executeQuery();
            rs.next();
            RevenuInfoEntry.CourtRevenueInfo courtOneRevenueInfo = gson.fromJson(rs.getString(2), RevenuInfoEntry.CourtRevenueInfo.class);
            RevenuInfoEntry.CourtRevenueInfo courtTwoRevenueInfo = gson.fromJson(rs.getString(3), RevenuInfoEntry.CourtRevenueInfo.class);
            Type listType = new TypeToken<List<RevenuInfoEntry.ExpenseEntry>>(){}.getType();

            List<RevenuInfoEntry.ExpenseEntry> expenses = gson.fromJson(rs.getString(4), listType);

            return new RevenuInfoEntry(date, courtOneRevenueInfo, courtTwoRevenueInfo, expenses, false,
                    null, null, null, null, null);
        } catch (SQLException e) {
            return new RevenuInfoEntry(date, DEFAULT_COURT_REVENUE, DEFAULT_COURT_REVENUE, new ArrayList<>(), false,
                    null, null, null, null, null);
        }
    }


    public Map<String, RoomData> getRoomStatus(){
        Map<String, RoomData> roomDataMap = new HashMap<>();
        try {
            PreparedStatement st = connection.prepareStatement("SELECT * FROM room_status");
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                String room = rs.getString(1);
                String status = rs.getString(2);
                roomDataMap.put(room, new RoomData(RoomStatus.valueOf(status), null
                        , null, null, null, null, null, null, null));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return roomDataMap;

    }

    public List<RevenuInfoEntry> getRevenueInfoEntries(String startDate, String endDate){
        try {
            PreparedStatement st = connection.prepareStatement("SELECT * FROM revenueInfo WHERE id >= ?::date and id <= ?::date");
            st.setString(1, startDate);
            st.setString(2, endDate);
            ResultSet rs = st.executeQuery();
            List<RevenuInfoEntry> revenuInfoEntries = new ArrayList<>();
            while (rs.next()) {
                String date = rs.getString(1);
                RevenuInfoEntry.CourtRevenueInfo courtOneRevenueInfo = gson.fromJson(rs.getString(2), RevenuInfoEntry.CourtRevenueInfo.class);
                RevenuInfoEntry.CourtRevenueInfo courtTwoRevenueInfo = gson.fromJson(rs.getString(3), RevenuInfoEntry.CourtRevenueInfo.class);
                Type listType = new TypeToken<List<RevenuInfoEntry.ExpenseEntry>>() {
                }.getType();

                List<RevenuInfoEntry.ExpenseEntry> expenses = gson.fromJson(rs.getString(4), listType);

                revenuInfoEntries.add(new RevenuInfoEntry(date, courtOneRevenueInfo, courtTwoRevenueInfo, expenses, false,
                        null, null, null, null, null));
            }
            return revenuInfoEntries.stream().sorted((e1, e2)->{
                return e1.getId().compareTo(e2.getId());
            }).collect(Collectors.toList());
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public void updateRevenueInfoEntry(String date, RevenuInfoEntry revenuInfoEntry)  {
        String query = "INSERT INTO revenueInfo(id, courtOne, courtTwo, expenses) VALUES(?::date, ?::jsonb, ?::jsonb, ?::jsonb) on conflict (id) do update set" +
                " courtOne = ?::jsonb , courtTwo = ?::jsonb, expenses = ?::jsonb";
        try {
            PreparedStatement pst = connection.prepareStatement(query);
            pst.setString(1, date);
            pst.setString(2, revenuInfoEntry.getCourtOne().toString());
            pst.setString(3, revenuInfoEntry.getCourtTwo().toString());
            pst.setString(4, revenuInfoEntry.getExpenses().toString());

            pst.setString(5, revenuInfoEntry.getCourtOne().toString());
            pst.setString(6, revenuInfoEntry.getCourtTwo().toString());
            pst.setString(7, revenuInfoEntry.getExpenses().toString());
            pst.execute();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static RevenuInfoEntry.CourtRevenueInfo getDefaultCourtRevenue() {
        RevenuInfoEntry.SlotInfo slotInfo = new RevenuInfoEntry.SlotInfo(0, "", "");
        return new RevenuInfoEntry.CourtRevenueInfo(slotInfo,slotInfo,slotInfo,slotInfo,slotInfo,slotInfo,slotInfo,slotInfo,slotInfo,slotInfo,slotInfo,slotInfo,
                slotInfo,slotInfo,slotInfo,slotInfo,slotInfo,slotInfo,slotInfo,slotInfo);
    }
}
