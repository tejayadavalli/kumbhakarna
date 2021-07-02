package kumbhakarna.Response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.Getter;
import kumbhakarna.model.RevenuInfoEntry;

import java.io.Serializable;
import java.util.List;

@AllArgsConstructor
@Getter
public class SummaryResponse implements Serializable {
    private static final Gson gson = new Gson();
    private final List<SlotSummary> slotSummaries;

    public static class SlotSummary{
        @JsonProperty(value ="date")
        private String date;
        @JsonProperty(value ="timing")
        private String timing;
        @JsonProperty("court")
        private String court;
        @JsonProperty(value ="revenue")
        private int revenue;
        @JsonProperty(value ="remark")
        private String remark;
        @JsonProperty(value ="paymentMode")
        private String paymentMode;
        public SlotSummary(String date, String timing, String court, RevenuInfoEntry.SlotInfo slotInfo){
            this.date = date;
            this.timing = timing;
            this.court = court;
            this.revenue = slotInfo.getRevenue();
            this.remark = slotInfo.getRemark();
            this.paymentMode = slotInfo.getPaymentMode();
        }
        @Override
        public String toString() {
            return gson.toJson(this);
        }
    };

}
