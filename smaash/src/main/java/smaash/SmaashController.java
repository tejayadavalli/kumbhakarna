package smaash;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import smaash.Response.SummaryResponse;
import smaash.Response.SummaryResponse.SlotSummary;
import smaash.dao.RevenueDao;
import smaash.model.ExpenseEntrySummary;
import smaash.model.RevenuInfoEntry;
import smaash.model.RevenuInfoEntry.SlotInfo;
import smaash.requests.SummaryRequest;

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
import java.util.stream.Collectors;

import static java.util.concurrent.CompletableFuture.completedFuture;

@RestController
public class SmaashController {
    private final RevenueDao revenueDao;
    private final String slotsFile;
    private final String summaryFile;

    @Autowired
    public SmaashController(RevenueDao revenueDao) throws IOException {
        this.slotsFile = this.readResource("kumbhakarna.html", Charsets.UTF_8);
        this.summaryFile = this.readResource("summary.html", Charsets.UTF_8);

        this.revenueDao = revenueDao;

        //Read File Content
    }

    private String readResource(final String fileName, Charset charset) throws IOException {
        return Resources.toString(Resources.getResource(fileName), charset);
    }
    @RequestMapping(value = "/smaash", method = RequestMethod.GET, produces= MediaType.TEXT_HTML_VALUE)
    public String getHtmlPage() {
        return this.slotsFile;
    }

    @RequestMapping(value = "/smaash/summary", method = RequestMethod.GET, produces= MediaType.TEXT_HTML_VALUE)
    public String getSummaryPage() {
        return this.summaryFile;
    }

    @RequestMapping(value = "/revenue/{date}", method = RequestMethod.GET, produces="application/json")
    public String revenueInfo(@PathVariable("date") String date) {
        return revenueDao.getRevenueInfo(date).toString();
    }

    @RequestMapping(value = "/summary",  method = RequestMethod.POST, consumes = "application/json")
    public SummaryResponse getSummary(@RequestBody SummaryRequest summaryRequest) {
        String startDate = summaryRequest.getStartDate();
        String endDate = summaryRequest.getEndDate();
        List<SlotSummary> slotSummaries = new ArrayList<>();
        List<ExpenseEntrySummary> expenseEntries = new ArrayList<>();
        List<RevenuInfoEntry> revenueInfoEntries = revenueDao.getRevenueInfoEntries(startDate, endDate);
        revenueInfoEntries.forEach(
                revenueInfoEntry -> {
                    String date = revenueInfoEntry.getId();
                    for(String slotTime : summaryRequest.getSlotsSelected()){
                        SlotInfo courtOneSlotInfo = revenueInfoEntry.getRequestedSlot("court1", slotTime);
                        SlotInfo courtTwoSlotInfo = revenueInfoEntry.getRequestedSlot("court2", slotTime);
                        if(courtOneSlotInfo.getRevenue() != 0){
                            slotSummaries.add(new SlotSummary(date, slotTime, "Court 1", courtOneSlotInfo));
                        }

                        if(courtTwoSlotInfo.getRevenue() != 0){
                            slotSummaries.add(new SlotSummary(date, slotTime, "Court 2", courtTwoSlotInfo));
                        }
                    }
                    revenueInfoEntry.getExpenses().forEach(r->{
                        expenseEntries.add(new ExpenseEntrySummary(date, r.getInfo(), r.getExpense()));
                    });
                }

        );
        return new SummaryResponse(slotSummaries, expenseEntries);
    }

    @RequestMapping(value = "/revenue/{date}",  method = RequestMethod.POST, consumes = "application/json")
    public String revenueInfo(@PathVariable("date") String date,
                        @RequestBody RevenuInfoEntry revenuInfoEntry) {
        revenueDao.updateRevenueInfoEntry(date, revenuInfoEntry);
        completedFuture(null)
                .thenRun(()->{
                    try {
                        if(!revenuInfoEntry.isSendUpdate()) return;
                        Map<String, String> messagePaymentModes = revenuInfoEntry.getMessagePaymentModes();
                        String paymentMode = messagePaymentModes.entrySet()
                                .stream()
                                .filter(entry-> Integer.valueOf(entry.getValue()) != 0)
                                .map(entry -> entry.getKey() + ":" + entry.getValue())
                                .collect(Collectors.joining(", "));
                        makePostRequest(revenuInfoEntry.getMessageSlot(),
                                revenuInfoEntry.getMessageRevenue(), revenuInfoEntry.getMessageRemark(),
                                revenuInfoEntry.getMessageDate(),
                                paymentMode);
                    } catch (IOException e) {

                    }
                });

        return "{}";

    }


    public static void makePostRequest(String slotInfo,
                                String revenue,
                                String remark,
                                String date, String paymentMode) throws IOException {
        String query = "https://api.flock.com/hooks/sendMessage/816c2b51-5b23-44ed-bd56-ac664f68acde";
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