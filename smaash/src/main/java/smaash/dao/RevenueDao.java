package smaash.dao;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import org.springframework.stereotype.Component;
import smaash.model.RevenuInfoEntry;
import smaash.model.RevenuInfoEntry.CourtRevenueInfo;

import java.lang.reflect.Type;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static smaash.model.RevenuInfoEntry.ExpenseEntry;
import static smaash.model.RevenuInfoEntry.SlotInfo;

@Component
public class RevenueDao {
    private Connection connection;
    private static CourtRevenueInfo DEFAULT_COURT_REVENUE =  getDefaultCourtRevenue();
    private static final Gson gson = new Gson();
    private static final JsonParser jsonParser = new JsonParser();
    public RevenueDao(){
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
            CourtRevenueInfo courtOneRevenueInfo = gson.fromJson(rs.getString(2), CourtRevenueInfo.class);
            CourtRevenueInfo courtTwoRevenueInfo = gson.fromJson(rs.getString(3), CourtRevenueInfo.class);
            Type listType = new TypeToken<List<ExpenseEntry>>(){}.getType();

            List<ExpenseEntry> expenses = gson.fromJson(rs.getString(4), listType);

            return new RevenuInfoEntry(date, courtOneRevenueInfo, courtTwoRevenueInfo, expenses, false,
                    null, null, null, null, null);
        } catch (SQLException e) {
            return new RevenuInfoEntry(date, DEFAULT_COURT_REVENUE, DEFAULT_COURT_REVENUE, new ArrayList<>(), false,
                    null, null, null, null, null);
        }
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
                CourtRevenueInfo courtOneRevenueInfo = gson.fromJson(rs.getString(2), CourtRevenueInfo.class);
                CourtRevenueInfo courtTwoRevenueInfo = gson.fromJson(rs.getString(3), CourtRevenueInfo.class);
                Type listType = new TypeToken<List<ExpenseEntry>>() {
                }.getType();

                List<ExpenseEntry> expenses = gson.fromJson(rs.getString(4), listType);

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

    private static CourtRevenueInfo getDefaultCourtRevenue() {
        SlotInfo slotInfo = new SlotInfo(0, "", null, new HashMap<>());
        return new CourtRevenueInfo(slotInfo,slotInfo,slotInfo,slotInfo,slotInfo,slotInfo,slotInfo,slotInfo,slotInfo,slotInfo,slotInfo,slotInfo,
                slotInfo,slotInfo,slotInfo,slotInfo,slotInfo,slotInfo,slotInfo,slotInfo);
    }
}
