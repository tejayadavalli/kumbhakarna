package kumbhakarna.dao;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import kumbhakarna.model.*;
import kumbhakarna.requests.UpsertBooking;
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
                String checkOuttime = Utils
                        .convertToHumanReadableDate(rs.getString("check_out_time"));
                String remark = rs.getString("remark");
                Integer days = rs.getObject("days") == null ? null :
                        rs.getInt("days");
                Integer roomBill = rs.getObject("room_bill") == null ? null :
                        rs.getInt("room_bill");
                Integer foodBill = rs.getObject("food_bill") == null ? null :
                        rs.getInt("food_bill");

                RoomData roomData = RoomData.builder()
                        .roomStatus(RoomStatus.valueOf(status))
                        .phone(phone)
                        .name(name)
                        .guestCount(guestCount)
                        .extraBed(extra_bed)
                        .linkedCheckin(linkedCheckInId)
                        .plan(plan)
                        .tariff(tariff)
                        .checkInTime(check_in_time)
                        .checkOutTime(checkOuttime)
                        .days(days)
                        .roomBill(roomBill)
                        .foodBill(foodBill)
                        .remark(remark)
                        .build();
                roomDataMap.put(room, roomData);
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


    public Guest getGuestDetails(String phoneNumber){
        try {
            PreparedStatement st = connection.prepareStatement("SELECT * FROM guest WHERE phone = ?");
            st.setString(1, phoneNumber);
            ResultSet rs = st.executeQuery();
            rs.next();
            String name = rs.getString(2);
            Long checkInSmsTime = (Long) rs.getObject(3);
            Long checkOutSmsTime = (Long) rs.getObject(4);
            return new Guest(phoneNumber, name, checkInSmsTime, checkOutSmsTime);

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
                         Integer days,
                         Integer roomBill,
                         Integer foodBill,
                         String remark){
        RoomInfoEntry roomStatus = getRoomStatus(room);
        DateFormat dtf = new SimpleDateFormat("dd/MM/yyy hh:mm aa");
        dtf.setTimeZone(TimeZone.getTimeZone("Asia/Calcutta"));
        String checkout = dtf.format(new Date());
        addUser(phone, name);
        String id = roomStatus.getLinkedCheckIn();
        updateCheckin(id, phone, guestCount, extraBed, plan, tariff, checkInTime,
                checkout, days, roomBill, foodBill, remark);
        updateRoomStatus(room, RoomStatus.CHECKOUT, id);
    }

    public void updateCheckin(String id,
                              String phone,
                              Integer guestCount,
                              Boolean extraBed,
                              String plan,
                              Integer tariff,
                              String checkInTime,
                              String checkout,
                              Integer days,
                              Integer roomBill,
                              Integer foodBill,
                              String remark) {
        checkInTime = Utils.convertToDBDate(checkInTime);
        checkout = Utils.convertToDBDate(checkout);
        String query = "INSERT INTO check_in(id) VALUES(?) on conflict(id) do update set " +
                "phone = ?, guest_count= ?, extra_bed = ?, plan = ?, tariff = ?, check_in_time = ?, " +
                "check_out_time = ?, days = ?, room_bill = ?, food_bill = ?, remark = ?";
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
            pst.setObject(9, days);
            pst.setObject(10, roomBill);
            pst.setObject(11, foodBill);
            pst.setString(12, remark);

            pst.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<CheckInSummary> getCurrentOccupiedRooms() throws SQLException {
        String query = "select * from check_in c join guest g on " +
                "c.phone = g.phone where c.check_out_time is null order by c.id";
        PreparedStatement pst = connection.prepareStatement(query);
        return getCheckInSummaries(pst);
    }

    public List<CheckInSummary> getCheckIns(String startDate, String endDate) throws SQLException {
        String query = "select * from check_in c join guest g on c.phone = g.phone where " +
                "c.check_in_time >= ? and c.check_in_time <= ? order by c.id";
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
            Integer days = (rs.getObject("days") == null) ? null : rs.getInt("days");
            Integer roomBill = (rs.getObject("room_bill") == null) ? null : rs.getInt("room_bill");
            Integer foodBill = (rs.getObject("food_bill") == null) ? null : rs.getInt("food_bill");
            CheckInSummary checkInSummary = new CheckInSummary(name, room, tariff, plan, check_in_time,
                    checkOuttime, guestCount, extra_bed, days, roomBill, foodBill, remark);
            list.add(checkInSummary);
        }

        return list;
    }

    public List<Booking> getBookings() throws SQLException {
        DateFormat dtf = new SimpleDateFormat("yyy/MM/dd hh:mm aa");
        dtf.setTimeZone(TimeZone.getTimeZone("Asia/Calcutta"));
        String yesterdaysDate = dtf.format(new Date(System.currentTimeMillis()-24*60*60*1000));
        String query = "select * from bookings where " +
                "check_in_time >= ? order by id";
        PreparedStatement pst = connection.prepareStatement(query);
        pst.setString(1, yesterdaysDate);
        List<Booking> list = new ArrayList<>();
        ResultSet rs = pst.executeQuery();
        while (rs.next()) {
            String id = rs.getString("id");
            String name = rs.getString("name");
            String phone = rs.getString("phone");
            Integer deluxeRooms = (Integer)rs.getObject("d_rooms");
            Integer deluxeTariff = (Integer)rs.getObject("d_tariff");
            Integer executiveRooms = (Integer)rs.getObject("e_rooms");
            Integer executiveTariff = (Integer)rs.getObject("e_tariff");
            Integer superiorRooms = (Integer)rs.getObject("sp_rooms");
            Integer superiorTariff = (Integer)rs.getObject("sp_tariff");
            Integer businessRooms = (Integer)rs.getObject("b_rooms");
            Integer businessTariff = (Integer)rs.getObject("b_tariff");
            Integer suiteRooms = (Integer)rs.getObject("su_rooms");
            Integer suiteTariff = (Integer)rs.getObject("su_tariff");
            Integer days = (Integer)rs.getObject("days");
            String plan = rs.getString("plan");
            Boolean extra_bed = rs.getBoolean("extra_bed");
            String mode = rs.getString("mode");
            String checkInTime = Utils.convertToHumanReadableDate(rs.getString("check_in_time"));
            Integer advance = (Integer)rs.getObject("advance");
            String remark = rs.getString("remark");
            Booking booking = Booking.builder()
                    .id(id)
                    .phone(phone)
                    .name(name)
                    .deluxeRooms(deluxeRooms)
                    .deluxeTariff(deluxeTariff)
                    .executiveRooms(executiveRooms)
                    .executiveTariff(executiveTariff)
                    .superiorRooms(superiorRooms)
                    .superiorTariff(superiorTariff)
                    .businessRooms(businessRooms)
                    .businessTariff(businessTariff)
                    .suiteRooms(suiteRooms)
                    .suiteTariff(suiteTariff)
                    .days(days)
                    .plan(plan)
                    .extraBed(extra_bed)
                    .mode(mode)
                    .checkInTime(checkInTime)
                    .advance(advance)
                    .remark(remark).build();
            list.add(booking);

        }
        return list;
    }

    public void upsertBooking(UpsertBooking u) {

        String query = "insert into bookings(id,  phone,  name,  d_rooms,  d_tariff,  " +
                "e_rooms,  e_tariff,  sp_rooms,  sp_tariff,  b_rooms,  b_tariff,  su_rooms,  " +
                "su_tariff, days,  plan,  extra_bed,  mode,  check_in_time,  advance,  remark) " +
                "values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) on conflict(id) " +
                "do update set phone=?, name=?,   d_rooms=?,   d_tariff=?,   e_rooms=?,   e_tariff=?,   sp_rooms=?,   " +
                "sp_tariff=?,   b_rooms=?,   b_tariff=?,   su_rooms=?,   su_tariff=?, days=?,   plan=?,   " +
                "extra_bed=?,   mode=?,   check_in_time=?,   advance=?,   remark =?";
        try {
            PreparedStatement pst = connection.prepareStatement(query);
            pst.setObject(1, (u.getId() == null)? System.currentTimeMillis() : u.getId());
            pst.setObject(2, u.getPhone());
            pst.setObject(3, u.getName());
            pst.setObject(4, u.getDeluxeRooms());
            pst.setObject(5,  u.getDeluxeTariff());
            pst.setObject(6, u.getExecutiveRooms());
            pst.setObject(7, u.getExecutiveTariff());
            pst.setObject(8, u.getSuperiorRooms());
            pst.setObject(9, u.getSuperiorTariff());
            pst.setObject(10, u.getBusinessRooms());
            pst.setObject(11, u.getBusinessTariff());
            pst.setObject(12, u.getSuiteRooms());
            pst.setObject(13, u.getSuiteTariff());
            pst.setObject(14, u.getDays());
            pst.setObject(15, u.getPlan());
            pst.setObject(16, u.getExtraBed());
            pst.setObject(17, u.getMode());
            pst.setObject(18, Utils.convertToDBDate(u.getCheckInTime()));
            pst.setObject(19, u.getAdvance());
            pst.setObject(20, u.getRemark());
            pst.setObject(21, u.getPhone());
            pst.setObject(22, u.getName());
            pst.setObject(23, u.getDeluxeRooms());
            pst.setObject(24,  u.getDeluxeTariff());
            pst.setObject(25, u.getExecutiveRooms());
            pst.setObject(26, u.getExecutiveTariff());
            pst.setObject(27, u.getSuperiorRooms());
            pst.setObject(28, u.getSuperiorTariff());
            pst.setObject(29, u.getBusinessRooms());
            pst.setObject(30, u.getBusinessTariff());
            pst.setObject(31, u.getSuiteRooms());
            pst.setObject(32, u.getSuiteTariff());
            pst.setObject(33, u.getDays());
            pst.setObject(34, u.getPlan());
            pst.setObject(35, u.getExtraBed());
            pst.setObject(36, u.getMode());
            pst.setObject(37, Utils.convertToDBDate(u.getCheckInTime()));
            pst.setObject(38, u.getAdvance());
            pst.setObject(39, u.getRemark());
            pst.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateGuestCheckInSms(String phone, long currentTime) {
        String query = "update guest set check_in_sms = ? where phone = ?";
        try {
            PreparedStatement pst = connection.prepareStatement(query);
            pst.setObject(1, currentTime);
            pst.setObject(2, phone);
            pst.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateGuestCheckOutSms(String phone, long currentTime) {
        String query = "update guest set check_out_sms = ? where phone = ?";
        try {
            PreparedStatement pst = connection.prepareStatement(query);
            pst.setObject(1, currentTime);
            pst.setObject(2, phone);
            pst.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
