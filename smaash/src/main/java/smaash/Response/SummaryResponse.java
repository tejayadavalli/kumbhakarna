package smaash.Response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.Getter;
import smaash.model.ExpenseEntrySummary;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import static smaash.model.RevenuInfoEntry.SlotInfo;

@AllArgsConstructor
@Getter
public class SummaryResponse implements Serializable {
    private static final Gson gson = new Gson();
    private final List<SlotSummary> slotSummaries;
    private final List<ExpenseEntrySummary> expenseEntries;

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
        @JsonProperty(value ="paymentModes")
        private Map<String, Integer> paymentModes;
        public SlotSummary(String date, String timing, String court, SlotInfo slotInfo){
            this.date = date;
            this.timing = timing;
            this.court = court;
            this.revenue = slotInfo.getRevenue();
            this.remark = slotInfo.getRemark();
            if(revenue != 0 && slotInfo.getPaymentModes() == null){
                this.paymentModes = ImmutableMap.of(slotInfo.getPaymentMode(), revenue);
            } else{
                this.paymentModes = slotInfo.getPaymentModes();
            }
        }
        @Override
        public String toString() {
            return gson.toJson(this);
        }
    };

}
