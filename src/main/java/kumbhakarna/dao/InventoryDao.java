package kumbhakarna.dao;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import kumbhakarna.model.RevenuInfoEntry;
import kumbhakarna.model.RoomData;
import kumbhakarna.model.RoomInfoEntry;
import kumbhakarna.model.RoomStatus;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.*;
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


    public Map<String, RoomData> getAllRoomStatus(){
        Map<String, RoomData> roomDataMap = new HashMap<>();
        try {
            PreparedStatement st = connection.prepareStatement("select * from (select * from room_status " +
                    "r left join check_in c on  r.linked_check_in = c.id) a left join guest g " +
                    "on a.phone = g.phone");
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                String room = rs.getString("room");
                String status = rs.getString("status");
                String linkedCheckInId = rs.getString("linked_check_in");
                String phone = rs.getString("phone");
                String name = rs.getString("name");
                Integer guestCount = rs.getInt("guest_count");
                Boolean extra_bed = rs.getBoolean("extra_bed");
                String plan = rs.getString("plan");
                Integer tariff = rs.getInt("tariff");
                String check_in_time = rs.getString("check_in_time");
                String checkOuttime = rs.getString("check_out_time");
                String remark = rs.getString("remark");


                roomDataMap.put(room, new RoomData(RoomStatus.valueOf(status), phone
                        , name, guestCount, extra_bed, linkedCheckInId, plan, tariff,
                        check_in_time, checkOuttime, remark));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return roomDataMap;

    }

    public RoomInfoEntry getRoomStatus(String room){
        try {
            PreparedStatement st = connection.prepareStatement("SELECT * FROM room_status WHERE room = ?");
            st.setString(1, room);
            ResultSet rs = st.executeQuery();
            rs.next();
            String status = rs.getString(2);
            String linkedCheckIn = rs.getString(3);
            return new RoomInfoEntry(RoomStatus.valueOf(status), linkedCheckIn);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String checkIn(String phone,
                          String name,
                          String room,
                          Integer guestCount,
                          Boolean extraBed,
                          String plan,
                          Integer tariff,
                          String checkInTime,
                          String remark){
        addUser(phone, name);
        String id = String.valueOf(System.currentTimeMillis());
        String query = "INSERT INTO check_in(id, phone, room, guest_count, extra_bed, plan, tariff, check_in_time, remark) " +
                "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement pst = connection.prepareStatement(query);
            pst.setString(1, id);
            pst.setString(2, phone);
            pst.setString(3, room);
            pst.setInt(4, guestCount);
            pst.setBoolean(5, extraBed);
            pst.setString(6, plan);
            pst.setInt(7, tariff);
            pst.setString(8, checkInTime);
            pst.setString(9, remark);

            pst.execute();
            return id;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String addUser(String phone,
                          String name){
        String id = UUID.randomUUID().toString();
        String query = "INSERT INTO guest(phone, name)" +
                "VALUES(?, ?) on conflict (phone) do update set" +
        " name = ?";
        try {
            PreparedStatement pst = connection.prepareStatement(query);
            pst.setString(1, phone);
            pst.setString(2, name);
            pst.setString(3, name);
            pst.execute();
            return id;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updateRoomStatus(String room,
                                 RoomStatus roomStatus,
                                 String checkIn) {
        String query = "INSERT INTO room_status(room, status, linked_check_in)" +
                "VALUES(?, ?, ?) on conflict (room) do update set" +
                " status = ?, linked_check_in = ?";
        try {
            PreparedStatement pst = connection.prepareStatement(query);
            pst.setString(1, room);
            pst.setString(2, roomStatus.name());
            pst.setString(3, checkIn);
            pst.setString(4, roomStatus.name());
            pst.setString(5, checkIn);
            pst.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return;
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

    public void checkout(String phone,
                         String name,
                         String room,
                         Integer guestCount,
                         Boolean extraBed,
                         String plan,
                         Integer tariff,
                         String checkInTime,
                         String remark){
        RoomInfoEntry roomStatus = getRoomStatus(room);
        DateFormat dtf = new SimpleDateFormat("dd/MM/yyy hh:mm aa");
        String checkout = dtf.format(new Date());
        addUser(phone, name);
        String id = roomStatus.getLinkedCheckIn();
        updateCheckin(id, phone, guestCount, extraBed, plan, tariff, checkInTime, checkout, remark);
        updateRoomStatus(room, RoomStatus.CHECKOUT, id);
    }

    public void updateCheckin(String id, String phone, Integer guestCount,
                              Boolean extraBed, String plan, Integer tariff,
                              String checkInTime, String checkout, String remark) {
        String query = "INSERT INTO check_in(id) VALUES(?) on conflict(id) do update set " +
                "phone = ?, guest_count= ?, extra_bed = ?, plan = ?, tariff = ?, check_in_time = ?, " +
                "check_out_time = ?, remark = ?";
        try {
            PreparedStatement pst = connection.prepareStatement(query);
            pst.setString(1, id);
            pst.setString(2, phone);
            pst.setInt(3, guestCount);
            pst.setBoolean(4, extraBed);
            pst.setString(5, plan);
            pst.setInt(6, tariff);
            pst.setString(7, checkInTime);
            pst.setString(8, checkout);
            pst.setString(9, remark);

            pst.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
