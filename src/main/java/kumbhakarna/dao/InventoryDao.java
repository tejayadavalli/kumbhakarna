package kumbhakarna.dao;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import kumbhakarna.model.*;
import kumbhakarna.requests.UpsertBooking;
import kumbhakarna.utils.Utils;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class InventoryDao {
    private static final JsonObject EMPTY_JSON = new JsonObject();


    @Getter
    public static class Pair {

        public Booking key;
        public String value;

        public Pair(Booking booking, String value) {
            this.key = booking;
            this.value = value;
        }

    }

    private Connection connection;

    private static final Gson gson = new Gson();

    public InventoryDao() {
        try {
            this.connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/smaash", "postgres", "postgres");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Map<String, RoomData> getAllRoomStatus() {
        Map<String, RoomData> roomDataMap = new HashMap<>();
        try {
            PreparedStatement st = connection.prepareStatement("select * from (select * from room_status " +
                    "r left join check_in c on  r.linked_check_in = c.id) a left join guest g " +
                    "on a.phone = g.phone");
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                String room = rs.getString("room");
                Map<String, String> meta = ImmutableMap.of();
                if (rs.getObject("meta") != null) {
                    meta = gson.fromJson(rs.getString("meta").replace(',', ' '), new TypeToken<HashMap<String, String>>() {
                    }.getType());
                }
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
                Integer bottles = rs.getObject("water_bottles") == null ? null :
                        rs.getInt("water_bottles");
                Integer combos = rs.getObject("combos") == null ? null :
                        rs.getInt("combos");
                Integer cooldrinks = rs.getObject("cooldrinks") == null ? null :
                        rs.getInt("cooldrinks");
                Integer cash = rs.getObject("cash") == null ? null :
                        rs.getInt("cash");
                Integer upi = rs.getObject("upi") == null ? null :
                        rs.getInt("upi");
                Integer card = rs.getObject("card") == null ? null :
                        rs.getInt("card");
                Integer ingommt = rs.getObject("ingommt") == null ? null :
                        rs.getInt("ingommt");
                RoomData roomData = RoomData.builder()
                        .roomStatus(RoomStatus.valueOf(status))
                        .meta(meta)
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
                        .bottles(bottles)
                        .combos(combos)
                        .cooldrinks(cooldrinks)
                        .cash(cash)
                        .card(card)
                        .upi(upi)
                        .ingommt(ingommt)
                        .remark(remark)
                        .build();
                roomDataMap.put(room, roomData);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return roomDataMap;

    }

    public RoomInfoEntry getRoomStatus(String room) {
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

    public Guest getGuestDetails(String phoneNumber) {
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

    public int checkInCount(String phone){
        try {
            PreparedStatement st = connection.prepareStatement("SELECT count(*) FROM check_in WHERE phone = ?");
            st.setString(1, phone);
            ResultSet rs = st.executeQuery();
            rs.next();
            return rs.getInt(1);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1;
    }

    public String checkIn(String phone,
                          String name,
                          String room,
                          Integer guestCount,
                          Boolean extraBed,
                          String plan,
                          Integer tariff,
                          String checkInTime,
                          String remark) {
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
                          String name) {
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
                                 String checkInId,
                                 Map<String, String> meta) {
        String query = "INSERT INTO room_status(room, status, linked_check_in)" +
                "VALUES(?, ?, ?) on conflict (room) do update set" +
                " status = ?, linked_check_in = ?, meta = ?";
        try {
            PreparedStatement pst = connection.prepareStatement(query);
            pst.setString(1, room);
            pst.setString(2, roomStatus.name());
            pst.setString(3, checkInId);
            pst.setString(4, roomStatus.name());
            pst.setString(5, checkInId);
            pst.setString(6, meta.toString());
            pst.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return;
    }

    public void upsertCheckInBill(String id,
                                  String gstNumber,
                                  String s3BillKey,
                                  String billNumber) {
        String query = "update check_in set gst_number = ?, s3_bill_key = ?, bill_number = ? where id = ?";
        try {
            PreparedStatement pst = connection.prepareStatement(query);
            pst.setObject(1, gstNumber);
            pst.setObject(2, s3BillKey);
            pst.setObject(3, billNumber);
            pst.setObject(4, id);
            pst.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void toggleCheckIn(String id, boolean deleted) {
        String query = "update check_in set is_deleted = ? where id = ?";
        try {
            PreparedStatement pst = connection.prepareStatement(query);
            pst.setBoolean(1, deleted);
            pst.setObject(2, id);
            pst.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
                         Integer bottles,
                         Integer combos,
                         Integer cooldrinks,
                         Integer cash,
                         Integer card,
                         Integer upi,
                         Integer ingommt,
                         String remark) {
        RoomInfoEntry roomStatus = getRoomStatus(room);
        DateFormat dtf = new SimpleDateFormat("dd/MM/yyy hh:mm aa");
        dtf.setTimeZone(TimeZone.getTimeZone("Asia/Calcutta"));
        String checkout = dtf.format(new Date());
        addUser(phone, name);
        String id = roomStatus.getLinkedCheckIn();
        updateCheckin(id, phone, guestCount, extraBed, plan, tariff, checkInTime,
                checkout, days, roomBill, foodBill, bottles, combos, cooldrinks,
                cash, card, upi, ingommt, remark);
        updateRoomStatus(room, RoomStatus.CHECKOUT, id, ImmutableMap.of());
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
                              Integer bottles,
                              Integer combos,
                              Integer cooldrinks,
                              Integer cash,
                              Integer card,
                              Integer upi,
                              Integer ingommt,
                              String remark) {
        checkInTime = Utils.convertToDBDate(checkInTime);
        checkout = Utils.convertToDBDate(checkout);
        String query = "INSERT INTO check_in(id) VALUES(?) on conflict(id) do update set " +
                "phone = ?, guest_count= ?, extra_bed = ?, plan = ?, tariff = ?, check_in_time = ?, " +
                "check_out_time = ?, days = ?, room_bill = ?, food_bill = ?, water_bottles = ?, " +
                "combos = ? , cooldrinks = ?, remark = ?, cash = ?, card = ?, upi = ?, ingommt = ?";
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
            pst.setObject(12, bottles);
            pst.setObject(13, combos);
            pst.setObject(14, cooldrinks);
            pst.setString(15, remark);
            pst.setObject(16, cash);
            pst.setObject(17, card);
            pst.setObject(18, upi);
            pst.setObject(19, ingommt);

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
        startDate = startDate + " " + "00:00 AM";
        endDate = endDate + " " + "99:99 PM";
        String query = "select * from check_in c join guest g on c.phone = g.phone where " +
                "c.check_in_time >= ? and c.check_in_time <= ? order by c.id";
        PreparedStatement pst = connection.prepareStatement(query);
        pst.setString(1, startDate);
        pst.setString(2, endDate);
        return getCheckInSummaries(pst);
    }

    public List<BillSummary> getBills(String startDate, String endDate) throws SQLException {
        startDate = startDate + " " + "00:00 AM";
        endDate = endDate + " " + "99:99 PM";
        long currentTime = System.currentTimeMillis() - 24*60*60*3*1000;
        String query = "select * from check_in c join guest g on c.phone = g.phone where " +
                "c.check_in_time >= ? and c.check_in_time <= ? and c.check_out_time is not null " +
                "and is_deleted = false and (bill_number is not null or (card is not null and card > 0) or " +
                "(ingommt is not null and ingommt > 0) or id > ?) " +
                "order by c.check_out_time";
        PreparedStatement pst = connection.prepareStatement(query);
        pst.setString(1, startDate);
        pst.setString(2, endDate);
        pst.setString(3, currentTime + "");

        return getBillSummaries(pst);
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
            Integer bottles = (rs.getObject("water_bottles") == null) ? null : rs.getInt("water_bottles");
            Integer combos = (rs.getObject("combos") == null) ? null : rs.getInt("combos");
            Integer cooldrinks = (rs.getObject("cooldrinks") == null) ? null : rs.getInt("cooldrinks");

            Integer cash = (rs.getObject("cash") == null) ? null : rs.getInt("cash");
            Integer card = (rs.getObject("card") == null) ? null : rs.getInt("card");
            Integer upi = (rs.getObject("upi") == null) ? null : rs.getInt("upi");
            Integer ingommt = (rs.getObject("ingommt") == null) ? null : rs.getInt("ingommt");
            String gstNumber = rs.getString("gst_number");
            String s3BillKey = rs.getString("s3_bill_key");
            String id = rs.getString("id");

            Boolean isDeleted = rs.getBoolean("is_deleted");

            CheckInSummary checkInSummary = new CheckInSummary(id, name, room, tariff, plan, check_in_time,
                    checkOuttime, guestCount, extra_bed, days, roomBill, foodBill, bottles, combos, cooldrinks,
                    cash, upi, card, ingommt, remark, gstNumber, s3BillKey, isDeleted);
            list.add(checkInSummary);
        }

        return list;
    }

    private List<BillSummary> getBillSummaries(PreparedStatement pst) throws SQLException {
        List<BillSummary> list = new ArrayList<>();
        ResultSet rs = pst.executeQuery();
        while (rs.next()) {
            String id = rs.getString("id");
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
            Integer cash = (rs.getObject("cash") == null) ? null : rs.getInt("cash");
            Integer card = (rs.getObject("card") == null) ? null : rs.getInt("card");
            Integer upi = (rs.getObject("upi") == null) ? null : rs.getInt("upi");
            Integer ingommt = (rs.getObject("ingommt") == null) ? null : rs.getInt("ingommt");
            String gstNumber = rs.getString("gst_number");
            String s3BillKey = rs.getString("s3_bill_key");
            String billNumber = rs.getString("bill_number");
            Boolean isDeleted = rs.getBoolean("is_deleted");

            BillSummary billSummary = new BillSummary(id, name, room, tariff, plan, check_in_time,
                    checkOuttime, guestCount, extra_bed, days, roomBill, foodBill, cash,
                    upi, card, ingommt, remark, gstNumber, s3BillKey, billNumber);
            list.add(billSummary);
        }

        return list;
    }

    public List<Booking> getBookings() throws SQLException {
        DateFormat dtf = new SimpleDateFormat("yyy/MM/dd hh:mm aa");
        dtf.setTimeZone(TimeZone.getTimeZone("Asia/Calcutta"));
        String yesterdaysDate = dtf.format(new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000));
        String query = "select * from bookings where " +
                "check_in_time >= ? and deleted is false order by id";
        PreparedStatement pst = connection.prepareStatement(query);
        pst.setString(1, yesterdaysDate);
        List<Booking> list = new ArrayList<>();
        ResultSet rs = pst.executeQuery();
        while (rs.next()) {
            String id = rs.getString("id");
            String name = rs.getString("name");
            String phone = rs.getString("phone");
            Integer deluxeRooms = (Integer) rs.getObject("d_rooms");
            Integer deluxeTariff = (Integer) rs.getObject("d_tariff");
            Integer executiveRooms = (Integer) rs.getObject("e_rooms");
            Integer executiveTariff = (Integer) rs.getObject("e_tariff");
            Integer superiorRooms = (Integer) rs.getObject("sp_rooms");
            Integer superiorTariff = (Integer) rs.getObject("sp_tariff");
            Integer businessRooms = (Integer) rs.getObject("b_rooms");
            Integer businessTariff = (Integer) rs.getObject("b_tariff");
            Integer suiteRooms = (Integer) rs.getObject("su_rooms");
            Integer suiteTariff = (Integer) rs.getObject("su_tariff");
            Integer days = (Integer) rs.getObject("days");
            String plan = rs.getString("plan");
            Boolean extra_bed = rs.getBoolean("extra_bed");
            String mode = rs.getString("mode");
            String checkInTime = Utils.convertToHumanReadableDate(rs.getString("check_in_time"));
            Integer advance = (Integer) rs.getObject("advance");
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
        return list.stream().map(x -> new Pair(x, Utils.convertToDBDate(x.getCheckInTime())))
                .sorted(Comparator.comparing(x -> {
                    String time = x.getValue().toString();
                    String[] parts = time.split(" ");
                    return parts[0] + " " + parts[2] + " " + parts[1];
                })).map(x1 -> (Booking) x1.getKey())
                .collect(Collectors.toList());
    }

    public void deleteBooking(String bookingId) {
        String query = "Update bookings set deleted = true where id = ?";
        try {
            PreparedStatement pst = connection.prepareStatement(query);
            pst.setObject(1, bookingId);
            pst.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
            pst.setObject(1, (u.getId() == null) ? System.currentTimeMillis() : u.getId());
            pst.setObject(2, u.getPhone());
            pst.setObject(3, u.getName());
            pst.setObject(4, u.getDeluxeRooms());
            pst.setObject(5, u.getDeluxeTariff());
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
            pst.setObject(24, u.getDeluxeTariff());
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

    public List<CronJob> getAllCrons() throws SQLException {
        String query = "select * from cron_job order by id";
        PreparedStatement pst = connection.prepareStatement(query);
        List<CronJob> list = new ArrayList<>();
        ResultSet rs = pst.executeQuery();
        while (rs.next()) {
            String id = rs.getString("id");
            String cronExpression = rs.getString("cron_expression");
            String assigned_to = rs.getString("assigned_to");
            String flock_message = rs.getString("flock_message");
            String portal_message = rs.getString("portal_message");
            boolean differentiate_rooms = rs.getBoolean("differentiate_rooms");

            CronJob cronJob = new CronJob(id, cronExpression, assigned_to, flock_message,
                    portal_message, differentiate_rooms);
            list.add(cronJob);
        }
        return list;
    }

    public void addCron(CronJob cronJob) {
        String query = "insert into cron_job(id,  cron_expression,  assigned_to, flock_message,  portal_message,  differentiate_rooms)"
                + " values(?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement pst = connection.prepareStatement(query);
            pst.setString(1, cronJob.getId());
            pst.setString(2, cronJob.getCronExpression());
            pst.setString(3, cronJob.getAssignedTo());
            pst.setString(4, cronJob.getFlockMessage());
            pst.setString(5, cronJob.getPortalMessage());
            pst.setBoolean(6, cronJob.getDifferentiateRooms());
            pst.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteCron(CronJob cronJob) {
        String query = "delete from cron_job where id = ?";
        try {
            PreparedStatement pst = connection.prepareStatement(query);
            pst.setObject(1, cronJob.getId());
            pst.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Task> getAllPendingTasks() throws SQLException {
        String query = "select * from tasks where task_status = 'CREATED' order by created_at";
        PreparedStatement pst = connection.prepareStatement(query);
        List<Task> list = new ArrayList<>();
        ResultSet rs = pst.executeQuery();
        while (rs.next()) {
            String id = rs.getString("id");
            String task_info = rs.getString("task_info");
            Long created_at = rs.getLong("created_at");
            String assigned_to = rs.getString("assigned_to");
            String remark = rs.getString("remark");
            Long updated_at = rs.getLong("updated_at");

            Task task = new Task(id, task_info, created_at, Task.TaskStatus.CREATED,
                    assigned_to, remark, updated_at);
            list.add(task);
        }
        return list;
    }

    public void updateTaskStatus(String id, Task.TaskStatus taskStatus) {
        String query = "update tasks set task_status = ?, updated_at = ? where id = ?";
        long currentTimeMillis = System.currentTimeMillis();
        try {
            PreparedStatement pst = connection.prepareStatement(query);
            pst.setObject(1, taskStatus.name());
            pst.setObject(2, currentTimeMillis);
            pst.setObject(3, id);
            pst.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addTask(Task task) {
        String query = "insert into tasks(id,  task_info,  created_at, task_status,  assigned_to)"
                + " values(?, ?, ?, ?, ?)";
        try {
            PreparedStatement pst = connection.prepareStatement(query);
            pst.setString(1, task.getId());
            pst.setString(2, task.getTaskInfo());
            pst.setLong(3, task.getCreatedAt());
            pst.setString(4, task.getTaskStatus().name());
            pst.setString(5, task.getAssignedTo());
            pst.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Task getTask(String id) {
        try {
            PreparedStatement st = connection.prepareStatement("SELECT * FROM tasks WHERE id = ?");
            st.setString(1, id);
            ResultSet rs = st.executeQuery();
            rs.next();
            String task_info = rs.getString("task_info");
            Long created_at = rs.getLong("created_at");
            String assigned_to = rs.getString("assigned_to");
            String remark = rs.getString("remark");
            Long updated_at = rs.getLong("updated_at");
            return new Task(id, task_info, created_at, Task.TaskStatus.CREATED,
                    assigned_to, remark, updated_at);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void addCashOut(CashOut cashOut) {
        String query = "insert into cash_out(id,  date,  amount)"
                + " values(?, ?, ?)";
        try {
            PreparedStatement pst = connection.prepareStatement(query);
            pst.setString(1, cashOut.getId());
            pst.setString(2, cashOut.getDate());
            pst.setInt(3, cashOut.getAmount());
            pst.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteCashOut(CashOut cashOut) {
        String query = "delete from cash_out where id = ?";
        try {
            PreparedStatement pst = connection.prepareStatement(query);
            pst.setObject(1, cashOut.getId());
            pst.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<CashOut> getCashOut() throws SQLException {
        Long time = System.currentTimeMillis() - 1000 * 30 * 24 * 60 * 60L;
        String query = "select * from cash_out where id >= ? order by date desc";
        PreparedStatement pst = connection.prepareStatement(query);
        pst.setObject(1, time.toString());
        List<CashOut> list = new ArrayList<>();
        ResultSet rs = pst.executeQuery();
        while (rs.next()) {
            String id = rs.getString("id");
            String date = rs.getString("date");
            Integer amount = rs.getInt("amount");

            CashOut cashOut = new CashOut(id, date, amount);
            list.add(cashOut);
        }
        return list;
    }


    public List<CashOut> getCashOut(String startDate, String endDate) throws SQLException {
        Long time = System.currentTimeMillis() - 1000 * 30 * 24 * 60 * 60L;
        String query = "select * from cash_out where date >= ? and date <= ? order by date";
        PreparedStatement pst = connection.prepareStatement(query);
        pst.setObject(1, startDate);
        pst.setObject(2, endDate);
        List<CashOut> list = new ArrayList<>();
        ResultSet rs = pst.executeQuery();
        while (rs.next()) {
            String id = rs.getString("id");
            String date = rs.getString("date");
            Integer amount = rs.getInt("amount");

            CashOut cashOut = new CashOut(id, date, amount);
            list.add(cashOut);
        }
        return list;
    }
}
