package kumbhakarna.dao;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import kumbhakarna.model.CheckInSummary;
import kumbhakarna.model.RoomData;
import kumbhakarna.model.RoomInfoEntry;
import kumbhakarna.model.RoomStatus;
import kumbhakarna.utils.Utils;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

@Component
public class InventoryDao {
    private Connection connection;
    private static final Gson gson = new Gson();
    private static final JsonParser jsonParser = new JsonParser();
    public InventoryDao(){
        try {
            this.connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/smaash", "postgres", "postgres");
        } catch (SQLException e) {
            e.printStackTrace();
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
                String check_in_time = Utils
                        .convertToHumanReadableDate(rs.getString("check_in_time"));
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
        checkInTime = Utils.convertToDBDate(checkInTime);
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
                                 String checkInId) {
        String query = "INSERT INTO room_status(room, status, linked_check_in)" +
                "VALUES(?, ?, ?) on conflict (room) do update set" +
                " status = ?, linked_check_in = ?";
        try {
            PreparedStatement pst = connection.prepareStatement(query);
            pst.setString(1, room);
            pst.setString(2, roomStatus.name());
            pst.setString(3, checkInId);
            pst.setString(4, roomStatus.name());
            pst.setString(5, checkInId);
            pst.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return;
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
        checkInTime = Utils.convertToDBDate(checkInTime);
        checkout = Utils.convertToDBDate(checkout);
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

    public List<CheckInSummary> getCurrentOccupiedRooms() throws SQLException {
        String query = "select * from check_in c join guest g on " +
                "c.phone = g.phone where c.check_out_time is null order by c.check_in_time";
        PreparedStatement pst = connection.prepareStatement(query);
        return getCheckInSummaries(pst);
    }

    public List<CheckInSummary> getCheckIns(String startDate, String endDate) throws SQLException {
        String query = "select * from check_in c join guest g on c.phone = g.phone where " +
                "c.check_in_time >= ? and c.check_in_time <= ? order by c.check_in_time";
        PreparedStatement pst = connection.prepareStatement(query);
        pst.setString(1, startDate);
        pst.setString(2, endDate);
        return getCheckInSummaries(pst);
    }

    private List<CheckInSummary> getCheckInSummaries(PreparedStatement pst) throws SQLException {
        List<CheckInSummary> list = new ArrayList<>();
        ResultSet rs = pst.executeQuery();
        while (rs.next()) {
            String room = rs.getString("room");
            String name = rs.getString("name");
            Integer guestCount = rs.getInt("guest_count");
            Boolean extra_bed = rs.getBoolean("extra_bed");
            String plan = rs.getString("plan");
            Integer tariff = rs.getInt("tariff");
            String check_in_time = Utils
                    .convertToHumanReadableDate(rs.getString("check_in_time"));
            String checkOuttime = Utils
                    .convertToHumanReadableDate(rs.getString("check_out_time"));
            String remark = rs.getString("remark");

            CheckInSummary checkInSummary = new CheckInSummary(name, room, tariff, plan, check_in_time,
                    checkOuttime, guestCount, extra_bed, remark);
            list.add(checkInSummary);
        }

        return list;
    }

}
