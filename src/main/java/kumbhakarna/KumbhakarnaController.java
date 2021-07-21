package kumbhakarna;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.gson.JsonObject;
import kumbhakarna.Response.GetHotelStatusResponse;
import kumbhakarna.Response.SummaryResponse;
import kumbhakarna.dao.InventoryDao;
import kumbhakarna.model.*;
import kumbhakarna.requests.SummaryRequest;
import kumbhakarna.requests.UpdateRoomStatusRequest;
import kumbhakarna.requests.UpsertBooking;
import kumbhakarna.utils.TextLocalSmsSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static java.util.concurrent.CompletableFuture.completedFuture;

@RestController
public class KumbhakarnaController {
    private final InventoryDao inventoryDao;
    private final String slotsFile;
    private final String summaryFile;

    @Autowired
    public KumbhakarnaController(InventoryDao inventoryDao) throws IOException {
        this.summaryFile = this.readResource("html/summary.html", Charsets.UTF_8);
        this.slotsFile = this.readResource("html/kumbhakarna.html", Charsets.UTF_8);

        this.inventoryDao = inventoryDao;

        //Read File Content
    }

    private String readResource(final String fileName, Charset charset) throws IOException {
        return Resources.toString(Resources.getResource(fileName), charset);
    }

    @RequestMapping(value = "/kumbhakarna", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String getHtmlPage() {
        return this.slotsFile;
    }

    @RequestMapping(value = "/kumbhakarna/summary", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String getSummaryPage() {
        return this.summaryFile;
    }

    @RequestMapping(value = "/get-status", method = RequestMethod.GET, produces = "application/json")
    public GetHotelStatusResponse getRoomsStatus() {
        Map<String, RoomData> roomStatus = inventoryDao.getAllRoomStatus();
        return new GetHotelStatusResponse(roomStatus);
    }

    @RequestMapping(value = "/update-status", method = RequestMethod.POST, produces = "application/json")
    public String updateRoomStatus(@RequestBody UpdateRoomStatusRequest updateRoomStatusRequest) throws IOException {
        String room = updateRoomStatusRequest.getRoom();
        RoomInfoEntry roomInfoEntry = inventoryDao.getRoomStatus(room);
        String stateChange = roomInfoEntry.getRoomStatus().name() + "#" +
                updateRoomStatusRequest.getRoomStatus().name();
        String phone = updateRoomStatusRequest.getPhone();
        String checkInTime = updateRoomStatusRequest.getCheckInTime();
        String remark = updateRoomStatusRequest.getRemark();
        Integer guestCount = updateRoomStatusRequest.getGuestCount();
        Boolean extraBed = updateRoomStatusRequest.getExtraBed();
        String name = updateRoomStatusRequest.getName();
        String plan = updateRoomStatusRequest.getPlan();
        Integer tariff = updateRoomStatusRequest.getTariff();
        Integer days = updateRoomStatusRequest.getDays();
        Integer roomBill = updateRoomStatusRequest.getRoomBill();
        Integer foodBill = updateRoomStatusRequest.getFoodBill();
        String checkOutTime = updateRoomStatusRequest.getCheckOutTime();
        switch (stateChange) {
            case "AVAILABLE#MAINTENANCE":
                inventoryDao.updateRoomStatus(room, RoomStatus.MAINTENANCE, null);
                break;
            case "MAINTENANCE#ROOMFIXED":
                inventoryDao.updateRoomStatus(room, RoomStatus.AVAILABLE, null);
                break;
            case "AVAILABLE#OCCUPIED":
                String checkInId = inventoryDao
                        .checkIn(phone, name, room, guestCount, extraBed, plan, tariff, checkInTime, remark);
                completedFuture(null)
                        .thenRunAsync(() -> {
                            try {
                                Guest guestDetails = inventoryDao.getGuestDetails(phone);
                                long currentTime = System.currentTimeMillis();
                                Long checkInSmsTime = guestDetails.getCheckInSmsTime();
                                if(checkInSmsTime == null || currentTime - checkInSmsTime > 1000*60*60*24*2){
                                    TextLocalSmsSender.sendSms("CheckIn", phone);
                                    inventoryDao.updateGuestCheckInSms(phone, currentTime);
                                }
                                sendMessage("Check-In", room, name, phone,
                                        guestCount, plan, tariff, extraBed, remark);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                inventoryDao.updateRoomStatus(room, updateRoomStatusRequest.getRoomStatus(), checkInId);
                break;
            case "OCCUPIED#OCCUPIED":
            case "CHECKOUT#CHECKOUT":
                String linkedCheckIn = roomInfoEntry.getLinkedCheckIn();
                inventoryDao.updateCheckin(linkedCheckIn, phone, guestCount, extraBed, plan, tariff, checkInTime,
                        checkOutTime, days, roomBill, foodBill, remark);
                break;
            case "OCCUPIED#CHECKOUT":
                inventoryDao.checkout(phone, name, room, guestCount, extraBed, plan, tariff, checkInTime,
                        days, roomBill, foodBill, remark);
                completedFuture(null)
                        .thenRunAsync(() -> {
                            try {
                                Guest guestDetails = inventoryDao.getGuestDetails(phone);
                                long currentTime = System.currentTimeMillis();
                                Long checkOutSmsTime = guestDetails.getCheckOutSmsTime();
                                Long checkInSmsTime = guestDetails.getCheckInSmsTime();
                                if((currentTime - checkInSmsTime  > 1000*60*60*3) && (checkOutSmsTime == null || currentTime - checkOutSmsTime > 1000*60*60*24*30)){
                                    String checkOut = TextLocalSmsSender.sendSms("CheckOut", phone);
                                    inventoryDao.updateGuestCheckOutSms(phone, currentTime);
                                }
                                sendMessageCheckout("Check-Out", room, name, phone,
                                        guestCount, plan, tariff, extraBed, days, roomBill, foodBill, remark);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                break;
            case "CHECKOUT#AVAILABLE":
                inventoryDao.updateRoomStatus(room, RoomStatus.AVAILABLE, null);
                break;
            default:
                break;

        }
        return "{}";
    }

    @RequestMapping(value = "/summary", method = RequestMethod.POST, consumes = "application/json")
    public SummaryResponse getSummary(@RequestBody SummaryRequest summaryRequest)
            throws SQLException {
        String startDate = summaryRequest.getStartDate();
        String endDate = summaryRequest.getEndDate();
        Boolean getCurrentRooms = summaryRequest.getGetCurrentRooms();

        if (Boolean.TRUE.equals(getCurrentRooms)) {
            return new SummaryResponse(inventoryDao.getCurrentOccupiedRooms());
        }
        return new SummaryResponse(inventoryDao.getCheckIns(startDate, endDate));
    }

    @RequestMapping(value = "/bookings", method = RequestMethod.GET, produces = "application/json")
    public List<Booking> getBookings()
            throws SQLException {
        return inventoryDao.getBookings();
    }

    @RequestMapping(value = "/upsert-booking", method = RequestMethod.POST, produces = "application/json")
    public String updateRoomStatus(@RequestBody UpsertBooking upsertBooking) throws IOException {
        inventoryDao.upsertBooking(upsertBooking);
        return "{}";
    }
    private void sendMessageCheckout(String checkIn,
                                     String room,
                                     String name,
                                     String phone,
                                     Integer guestCount,
                                     String plan,
                                     Integer tariff,
                                     Boolean extraBed,
                                     Integer days,
                                     Integer roomBill,
                                     Integer foodBill,
                                     String remark) throws IOException {
        String query = "https://api.flock.com/hooks/sendMessage/d3f4ca07-847f-4b01-a5c3-ff58d6de79cf";
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("flockml", "<flockml>" +
                "<b>" + checkIn + "</b><br/>" +
                "<b>Room: </b>" + room + "<br/>" +
                "<b>Tariff: </b>" + tariff + "<br/>" +
                "<b>Plan: </b>" + plan + "<br/>" +
                "<b>Guest Count: </b>" + guestCount + "<br/>" +
                "<b>Name: </b>" + name + "<br/>" +
                "<b>Phone: </b>" + phone + "<br/>" +
                "<b>Extra Bed: </b>" + (extraBed ? "Yes" : "No") + "<br/>" +
                "<b>Days: </b>" + days + "<br/>" +
                "<b>Room Bill: </b>" + roomBill + "<br/>" +
                "<b>Food Bill: </b>" + foodBill + "<br/>" +
                "<b>Remark: </b>" + remark + "<br/>" +
                "</flockml>");

        URL url = new URL(query);
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

    private void sendMessage(String checkIn,
                             String room,
                             String name,
                             String phone,
                             Integer guestCount,
                             String plan,
                             Integer tariff,
                             Boolean extraBed,
                             String remark) throws IOException {
        String query = "https://api.flock.com/hooks/sendMessage/d3f4ca07-847f-4b01-a5c3-ff58d6de79cf";
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("flockml", "<flockml>" +
                "<b>" + checkIn + "</b><br/>" +
                "<b>Room: </b>" + room + "<br/>" +
                "<b>Tariff: </b>" + tariff + "<br/>" +
                "<b>Plan: </b>" + plan + "<br/>" +
                "<b>Guest Count: </b>" + guestCount + "<br/>" +
                "<b>Name: </b>" + name + "<br/>" +
                "<b>Phone: </b>" + phone + "<br/>" +
                "<b>Extra Bed: </b>" + (extraBed ? "Yes" : "No") + "<br/>" +
                "<b>Remark: </b>" + remark + "<br/>" +
                "</flockml>");

        URL url = new URL(query);
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