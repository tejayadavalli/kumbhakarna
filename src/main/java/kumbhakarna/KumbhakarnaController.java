package kumbhakarna;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import com.google.gson.JsonObject;
import kumbhakarna.Response.*;
import kumbhakarna.dao.InventoryDao;
import kumbhakarna.model.*;
import kumbhakarna.requests.*;
import kumbhakarna.utils.TextLocalSmsSender;
import kumbhakarna.utils.Utils;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.*;

import static java.util.concurrent.CompletableFuture.completedFuture;

@RestController
public class   KumbhakarnaController {
    private final InventoryDao inventoryDao;
    private final String slotsFile;
    private final String summaryFile;
    private final String billsFile;
    private final String cashOutFile;
    private final S3Uploader s3Uploader;
    private static final Map<String, String> emptyMap = ImmutableMap.of();

    @Autowired
    public KumbhakarnaController(InventoryDao inventoryDao) throws IOException {
        this.summaryFile = this.readResource("html/summary.html", Charsets.UTF_8);
        this.slotsFile = this.readResource("html/kumbhakarna.html", Charsets.UTF_8);
        this.billsFile = this.readResource("html/bills.html", Charsets.UTF_8);
        this.cashOutFile = this.readResource("html/cashout.html", Charsets.UTF_8);
        this.inventoryDao = inventoryDao;
        this.s3Uploader = new S3Uploader();

        //Read File Content
    }

    private String readResource(final String fileName, Charset charset) throws IOException {
        return Resources.toString(Resources.getResource(fileName), charset);
    }

    @RequestMapping(value = "/kumbhakarna", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String getHtmlPage() {
        return this.slotsFile;
    }

    @RequestMapping(value = "/kumbhakarna/cashout", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String getCashOutPage() {
        return this.cashOutFile;
    }

    @RequestMapping(value = "/kumbhakarna/summary", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String getSummaryPage() {
        return this.summaryFile;
    }

    @RequestMapping(value = "/preSignedUrl", method = RequestMethod.POST, produces = "application/json")
    public GetPreSignedUrlResponse getPreSignedUrl(@RequestBody GetPreSignedUrlRequest request) {
        String url = this.s3Uploader.generatePreSignedUrl(request.getFileName(), request.getType()).toString();
        return new GetPreSignedUrlResponse(url);
    }


    @RequestMapping(value = "/kumbhakarna/bills", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String getBillsPage() {
        return this.billsFile;
    }

    @RequestMapping(value = "/get-status", method = RequestMethod.GET, produces = "application/json")
    public GetHotelStatusResponse getRoomsStatus() {
        Map<String, RoomData> roomStatus = inventoryDao.getAllRoomStatus();
        return new GetHotelStatusResponse(roomStatus);
    }

    @RequestMapping(value = "/upsert-bill", method = RequestMethod.POST, produces = "application/json")
    public String upsertBill(@RequestBody  UpsertCheckInBill upsertCheckInBill) {
        String id = upsertCheckInBill.getId();
        String gstNumber = upsertCheckInBill.getGstNumber();
        String s3BillKey = upsertCheckInBill.getS3BillKey();
        String billNumber = upsertCheckInBill.getBillNumber();
        inventoryDao.upsertCheckInBill(id, gstNumber, s3BillKey, billNumber);
        return "{}";
    }

    @RequestMapping(value = "/toggle-checkin", method = RequestMethod.POST, produces = "application/json")
    public String toggleCheckIn(@RequestBody  ToggleCheckIn toggleCheckIn) {
        String id = toggleCheckIn.getId();
        boolean deleted = toggleCheckIn.isDeleted();
        inventoryDao.toggleCheckIn(id, deleted);
        return "{}";
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
        String requestName = updateRoomStatusRequest.getName();
        String name = (requestName != null) ? requestName .replace(",", " "): requestName;
        String plan = updateRoomStatusRequest.getPlan();
        Integer tariff = updateRoomStatusRequest.getTariff();
        Integer days = updateRoomStatusRequest.getDays();
        Integer roomBill = updateRoomStatusRequest.getRoomBill();
        Integer foodBill = updateRoomStatusRequest.getFoodBill();
        Integer bottles = updateRoomStatusRequest.getBottles();
        Integer combos = updateRoomStatusRequest.getCombos();
        Integer cooldrinks = updateRoomStatusRequest.getCooldrinks();
        Integer cash = updateRoomStatusRequest.getCash();
        Integer card = updateRoomStatusRequest.getCard();
        Integer upi = updateRoomStatusRequest.getUpi();
        Integer ingommt = updateRoomStatusRequest.getIngommt();
        String checkOutTime = updateRoomStatusRequest.getCheckOutTime();
        List<int[]> list = new ArrayList<>();

        switch (stateChange) {
            case "AVAILABLE#MAINTENANCE":
                inventoryDao.updateRoomStatus(room, RoomStatus.MAINTENANCE, null, emptyMap);
                break;
            case "AVAILABLE#BLOCKED":
                inventoryDao.updateRoomStatus(room, RoomStatus.BLOCKED, null, ImmutableMap.of("name", name.replace(" ", "&nbsp")));
                break;
            case "MAINTENANCE#ROOMFIXED":
            case "BLOCKED#UNBLOCKED":
                inventoryDao.updateRoomStatus(room, RoomStatus.AVAILABLE, null, emptyMap);
                break;
            case "AVAILABLE#OCCUPIED":
                String checkInId = inventoryDao
                        .checkIn(phone, name, room, guestCount, extraBed, plan, tariff, checkInTime, remark);
                completedFuture(null)
                        .thenRunAsync(() -> {
                            try {
                                Guest guestDetails = inventoryDao.getGuestDetails(phone);
                                int checkInCount = inventoryDao.checkInCount(phone);
                                sendMessage("Check-In", room, name, phone,
                                        guestCount, plan, tariff, extraBed, remark, checkInCount);
                                long currentTime = System.currentTimeMillis();
                                Long checkInSmsTime = guestDetails.getCheckInSmsTime();
                                if(checkInSmsTime == null || currentTime - checkInSmsTime > 1000l*60*60*24*2){
                                    TextLocalSmsSender.sendSms("CheckIn", phone);
                                    inventoryDao.updateGuestCheckInSms(phone, currentTime);
                                }

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                inventoryDao.updateRoomStatus(room, updateRoomStatusRequest.getRoomStatus(), checkInId, emptyMap);
                break;
            case "OCCUPIED#OCCUPIED":
            case "CHECKOUT#CHECKOUT":
                String linkedCheckIn = roomInfoEntry.getLinkedCheckIn();
                inventoryDao.updateCheckin(linkedCheckIn, phone, guestCount, extraBed, plan, tariff, checkInTime,
                        checkOutTime, days, roomBill, foodBill, bottles, combos, cooldrinks, cash, card, upi, ingommt, remark);
                break;
            case "OCCUPIED#CHECKOUT":
                inventoryDao.checkout(phone, name, room, guestCount, extraBed, plan, tariff, checkInTime,
                        days, roomBill, foodBill, bottles, combos, cooldrinks, cash, card, upi, ingommt, remark);
                completedFuture(null)
                        .thenRunAsync(() -> {
                            try {
                                Guest guestDetails = inventoryDao.getGuestDetails(phone);
                                sendMessageCheckout("Check-Out", room, name, phone,
                                        guestCount, plan, tariff, extraBed, days, roomBill, foodBill, remark);
                                long currentTime = System.currentTimeMillis();
                                Long checkOutSmsTime = guestDetails.getCheckOutSmsTime();
                                Long checkInSmsTime = guestDetails.getCheckInSmsTime();
//                                if((currentTime - checkInSmsTime  > 1000l*60*60*3) && (checkOutSmsTime == null || currentTime - checkOutSmsTime > 1000l*60*60*24*30)){
//                                    String checkOut = TextLocalSmsSender.sendSms("CheckOut", phone);
//                                    inventoryDao.updateGuestCheckOutSms(phone, currentTime);
//                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                break;
            case "CHECKOUT#AVAILABLE":
                inventoryDao.updateRoomStatus(room, RoomStatus.AVAILABLE, null, emptyMap);
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
        String password = summaryRequest.getPassword();
        Boolean getCurrentRooms = summaryRequest.getGetCurrentRooms();
        if (Boolean.TRUE.equals(getCurrentRooms)) {
            return new SummaryResponse(inventoryDao.getCurrentOccupiedRooms(), new ArrayList<>(), false);
        }
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        String generatedPassword = "vamsi" + new Date().getDate() + "" + (new Date().getMonth() + 1);
        boolean isPasswordCorrect = generatedPassword.equals(password);
        if(isPasswordCorrect ) {
            return new SummaryResponse(inventoryDao.getCheckIns(startDate, endDate), inventoryDao.getCashOut(startDate, endDate), isPasswordCorrect);
        } else{
            throw new InternalError("Unauthorized");
        }
    }

    @RequestMapping(value = "/bills", method = RequestMethod.POST, consumes = "application/json")
    public BillResponse getBillsSummary(@RequestBody SummaryRequest summaryRequest)
            throws SQLException {
        String startDate = summaryRequest.getStartDate();
        String endDate = summaryRequest.getEndDate();
        String password = summaryRequest.getPassword();
        Boolean getCurrentRooms = summaryRequest.getGetCurrentRooms();
        if(!"fo123".equals(password)) throw new InternalError("Unauthorized");
        return new BillResponse(inventoryDao.getBills(startDate, endDate));
    }


    @RequestMapping(value = "/bookings", method = RequestMethod.GET, produces = "application/json")
    public List<Booking> getBookings()
            throws SQLException {
        return inventoryDao.getBookings();
    }

    @RequestMapping(value = "/upsert-booking", method = RequestMethod.POST, produces = "application/json")
    public String updateRoomStatus(@RequestBody UpsertBooking upsertBooking) throws IOException {
        inventoryDao.upsertBooking(upsertBooking);
        if(upsertBooking.getId()  ==  null){
            completedFuture(null)
                    .thenRunAsync(() -> {
                        try {
                            sendNewBookingMessage(upsertBooking);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        }
        return "{}";
    }

    @RequestMapping(value = "/delete-booking", method = RequestMethod.POST, produces = "application/json")
    public String deleteBooking(@RequestBody DeleteBooking deleteBooking) throws IOException {
        inventoryDao.deleteBooking(deleteBooking.getId());
        return "{}";
    }

    private void sendNewBookingMessage(UpsertBooking upsertBooking) throws IOException {
        String query = "https://api.flock.com/hooks/sendMessage/d3f4ca07-847f-4b01-a5c3-ff58d6de79cf";
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("flockml", "<flockml>" +
                "<b> New Booking </b><br/>" +
                ((upsertBooking.getDeluxeRooms() != null) ? "<b>Deluxe: </b>" + upsertBooking.getDeluxeRooms() + ": " + upsertBooking.getDeluxeTariff() + "<br/>": "") +
                ((upsertBooking.getExecutiveRooms() != null) ? "<b>Executive: </b>" + upsertBooking.getExecutiveRooms() + ": " + upsertBooking.getExecutiveTariff() + "<br/>": "") +
                ((upsertBooking.getSuiteRooms() != null) ? "<b>Suite: </b>" + upsertBooking.getSuiteRooms() + ": " + upsertBooking.getSuiteTariff() + "<br/>": "") +
                ((upsertBooking.getBusinessRooms() != null) ? "<b>Bussiness: </b>" + upsertBooking.getBusinessRooms() + ": " + upsertBooking.getBusinessTariff() + "<br/>": "") +
                ((upsertBooking.getSuperiorRooms() != null) ? "<b>Superior: </b>" + upsertBooking.getSuperiorRooms() + ": " + upsertBooking.getSuperiorTariff() + "<br/>": "") +
                "<b>Plan: </b>" + upsertBooking.getPlan() + "<br/>" +
                "<b>Days: </b>" + upsertBooking.getDays() + "<br/>" +
                "<b>Mode: </b>" + upsertBooking.getMode() + "<br/>" +
                "<b>Name: </b>" + upsertBooking.getName() + "<br/>" +
                "<b>Phone: </b>" + upsertBooking.getPhone() + "<br/>" +
                "<b>Extra Bed: </b>" + (upsertBooking.getExtraBed() ? "Yes" : "No") + "<br/>" +
                "<b>CheckIn: </b>" + upsertBooking.getCheckInTime() + "<br/>" +
                "<b>Remark: </b>" + upsertBooking.getRemark() + "<br/>" +
                "</flockml>");
        Utils.sendMessage(query, jsonObject);
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
        Utils.sendMessage(query, jsonObject);
    }

    private void sendMessage(String checkIn,
                             String room,
                             String name,
                             String phone,
                             Integer guestCount,
                             String plan,
                             Integer tariff,
                             Boolean extraBed,
                             String remark,
                             int checkInCount) throws IOException {
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
                "<b>Visits: </b>" + checkInCount + "<br/>" +
                "</flockml>");
        Utils.sendMessage(query, jsonObject);
    }

    @RequestMapping(value = "/add-cashout", method = RequestMethod.POST, consumes = "application/json")
    public String addCashOut(@RequestBody CashOut cashOut) {
        cashOut.setId(String.valueOf(System.currentTimeMillis()));
        inventoryDao.addCashOut(cashOut);
        return "{}";
    }

    @RequestMapping(value = "/delete-cashout", method = RequestMethod.PUT, consumes = "application/json")
    public String deleteCashOut(@RequestBody CashOut cashOut) {
        inventoryDao.deleteCashOut(cashOut);
        return "{}";
    }

    @RequestMapping(value = "/get-cashout", method = RequestMethod.GET, produces = "application/json")
    public GetCashOutResponse getCashOut() throws SQLException {
        List<CashOut> cashOut = inventoryDao.getCashOut();
        return new GetCashOutResponse(cashOut);
    }

}