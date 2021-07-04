package kumbhakarna;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.gson.JsonObject;
import kumbhakarna.Response.GetHotelStatusResponse;
import kumbhakarna.Response.SummaryResponse;
import kumbhakarna.dao.InventoryDao;
import kumbhakarna.model.RevenuInfoEntry;
import kumbhakarna.model.RevenuInfoEntry.SlotInfo;
import kumbhakarna.model.RoomData;
import kumbhakarna.model.RoomInfoEntry;
import kumbhakarna.model.RoomStatus;
import kumbhakarna.requests.SummaryRequest;
import kumbhakarna.requests.UpdateRoomStatusRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
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
    @RequestMapping(value = "/kumbhakarna", method = RequestMethod.GET, produces= MediaType.TEXT_HTML_VALUE)
    public String getHtmlPage() {
        return this.slotsFile;
    }

    @RequestMapping(value = "/kumbhakarna/summary", method = RequestMethod.GET, produces= MediaType.TEXT_HTML_VALUE)
    public String getSummaryPage() {
        return this.summaryFile;
    }

    @RequestMapping(value = "/get-status", method = RequestMethod.GET, produces="application/json")
    public GetHotelStatusResponse getRoomsStatus() {
        Map<String, RoomData> roomStatus = inventoryDao.getAllRoomStatus();
        return new GetHotelStatusResponse(roomStatus);
    }

    @RequestMapping(value = "/update-status", method = RequestMethod.POST, produces="application/json")
    public String updateRoomStatus(@RequestBody UpdateRoomStatusRequest updateRoomStatusRequest) {
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
        switch (stateChange){
            case "AVAILABLE#OCCUPIED":
                String checkInId = inventoryDao
                        .checkIn(phone, name, room, guestCount, extraBed, plan, tariff, checkInTime, remark);
                inventoryDao.updateRoomStatus(room, updateRoomStatusRequest.getRoomStatus(), checkInId);
                break;
            case "OCCUPIED#OCCUPIED":
                String linkedCheckIn = roomInfoEntry.getLinkedCheckIn();
                inventoryDao.updateCheckin(linkedCheckIn, phone, guestCount, extraBed, plan, tariff, checkInTime,
                        null,remark);
                break;
            case "OCCUPIED#CHECKOUT":
                inventoryDao.checkout(phone, name, room, guestCount, extraBed, plan, tariff, checkInTime, remark);
                break;
            case "CHECKOUT#AVAILABLE":
                inventoryDao.updateRoomStatus(room, RoomStatus.AVAILABLE, null);
                break;
            default:
                break;

        }
        return "{}";
    }

    @RequestMapping(value = "/summary",  method = RequestMethod.POST, consumes = "application/json")
    public List<SummaryResponse.SlotSummary> getSummary(@RequestBody SummaryRequest summaryRequest) {
        String startDate = summaryRequest.getStartDate();
        String endDate = summaryRequest.getEndDate();
        List<SummaryResponse.SlotSummary> slotSummaries = new ArrayList<>();
        List<RevenuInfoEntry> revenueInfoEntries = inventoryDao.getRevenueInfoEntries(startDate, endDate);
        revenueInfoEntries.forEach(
                revenueInfoEntry -> {
                    String date = revenueInfoEntry.getId();
                    for(String slotTime : summaryRequest.getSlotsSelected()){
                        SlotInfo courtOneSlotInfo = revenueInfoEntry.getRequestedSlot("court1", slotTime);
                        SlotInfo courtTwoSlotInfo = revenueInfoEntry.getRequestedSlot("court2", slotTime);
                        if(courtOneSlotInfo.getRevenue() != 0){
                            slotSummaries.add(new SummaryResponse.SlotSummary(date, slotTime, "Court 1", courtOneSlotInfo));
                        }

                        if(courtTwoSlotInfo.getRevenue() != 0){
                            slotSummaries.add(new SummaryResponse.SlotSummary(date, slotTime, "Court 2", courtTwoSlotInfo));
                        }
                    }
                }
        );
        return slotSummaries;
    }

    @RequestMapping(value = "/revenue/{date}",  method = RequestMethod.POST, consumes = "application/json")
    public String revenueInfo(@PathVariable("date") String date,
                        @RequestBody RevenuInfoEntry revenuInfoEntry) {
        inventoryDao.updateRevenueInfoEntry(date, revenuInfoEntry);
        completedFuture(null)
                .thenRun(()->{
                    try {
                        if(!revenuInfoEntry.isSendUpdate()) return;
                        makePostRequest(revenuInfoEntry.getMessageSlot(),
                                revenuInfoEntry.getMessageRevenue(), revenuInfoEntry.getMessageRemark(),
                                revenuInfoEntry.getMessageDate(),
                                revenuInfoEntry.getMessagePaymentMode());
                    } catch (IOException e) {

                    }
                });

        return "{}";

    }


    public static void makePostRequest(String slotInfo,
                                String revenue,
                                String remark,
                                String date, String paymentMode) throws IOException {
        String query = "https://api1.flock.com/hooks/sendMessage/816c2b51-5b23-44ed-bd56-ac664f68acde";
        JsonObject jsonObject = new JsonObject();
        String[] time = slotInfo.split("To");
        jsonObject.addProperty("flockml" , "<flockml>" +
                "<b>Date: </b>" +  date + "<br/>" +
                "<b>Slot: </b>" + time[0].substring(1).toUpperCase() + " - " + time[1].toUpperCase() + "<br/>" +
                "<b>Revenue: </b>" + revenue + "<br/>" +
                "<b>Payment Mode: </b>" + paymentMode + "<br/>" +
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