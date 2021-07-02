package smaash.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Getter
public class RevenuInfoEntry implements Serializable {
    @JsonProperty(value = "id")
    private String id;
    @JsonProperty(value ="courtOne")
    private CourtRevenueInfo courtOne;
    @JsonProperty(value = "courtTwo")
    private CourtRevenueInfo courtTwo;
    @JsonProperty(value = "expenses")
    private List<ExpenseEntry> expenses;

    @JsonProperty(value = "sendUpdate")
    private boolean sendUpdate;
    @JsonProperty(value = "messageSlot")
    private String messageSlot;
    @JsonProperty(value = "messageRevenue")
    private String messageRevenue;
    @JsonProperty(value = "messageRemark")
    private String messageRemark;
    @JsonProperty(value = "messageDate")
    private String messageDate;
    @JsonProperty(value = "messagePaymentModes")
    private Map<String, String> messagePaymentModes;

    public SlotInfo getRequestedSlot(String court, String slotTime){
        CourtRevenueInfo courtRevenueInfo;
        if(court.equals("court1")){
            courtRevenueInfo = courtOne;
        } else{
            courtRevenueInfo = courtTwo;
        }

        switch (slotTime){
            case "s4amTo5am":
                return courtRevenueInfo.getS4amTo5am();
            case "s5amTo6am":
                return courtRevenueInfo.getS5amTo6am();
            case "s6amTo7am":
                return courtRevenueInfo.getS6amTo7am();
            case "s7amTo8am":
                return courtRevenueInfo.getS7amTo8am();
            case "s8amTo9am":
                return courtRevenueInfo.getS8amTo9am();
            case "s9amTo10am":
                return courtRevenueInfo.getS9amTo10am();
            case "s10amTo11am":
                return courtRevenueInfo.getS10amTo11am();
            case "s11amTo12pm":
                return courtRevenueInfo.getS11amTo12pm();
            case "s12pmTo1pm":
                return courtRevenueInfo.getS12pmTo1pm();
            case "s1pmTo2pm":
                return courtRevenueInfo.getS1pmTo2pm();
            case "s2pmTo3pm":
                return courtRevenueInfo.getS2pmTo3pm();
            case "s3pmTo4pm":
                return courtRevenueInfo.getS3pmTo4pm();
            case "s4pmTo5pm":
                return courtRevenueInfo.getS4pmTo5pm();
            case "s5pmTo6pm":
                return courtRevenueInfo.getS5pmTo6pm();
            case "s6pmTo7pm":
                return courtRevenueInfo.getS6pmTo7pm();
            case "s7pmTo8pm":
                return courtRevenueInfo.getS7pmTo8pm();
            case "s8pmTo9pm":
                return courtRevenueInfo.getS8pmTo9pm();
            case "s9pmTo10pm":
                return courtRevenueInfo.getS9pmTo10pm();
            default:
                return courtRevenueInfo.getS10pmTo11pm();
        }
    }

    private static final Gson gson = new Gson();

    @Override
    @JsonValue
    public String toString() {
        return gson.toJson(this);
    }

    @AllArgsConstructor
    @Getter
    public static class ExpenseEntry{
        @JsonProperty(value ="info")
        private String info;
        @JsonProperty(value ="expense")
        private int expense;
        @Override
        public String toString() {
            return gson.toJson(this);
        }
    }

    @AllArgsConstructor
    @Getter
    public static class SlotInfo{
        @JsonProperty(value ="revenue")
        private int revenue;
        @JsonProperty(value ="remark")
        private String remark;
        @JsonProperty(value ="paymentMode")
        private String paymentMode;
        @JsonProperty(value ="paymentModes")
        private Map<String, Integer> paymentModes;
        @Override
        public String toString() {
            return gson.toJson(this);
        }
    }

    @Getter
    @AllArgsConstructor
    public static class CourtRevenueInfo {
        @JsonProperty(value ="s4amTo5am")
        private SlotInfo s4amTo5am;
        @JsonProperty(value ="s5amTo6am")
        private SlotInfo s5amTo6am;
        @JsonProperty(value ="s6amTo7am")
        private SlotInfo s6amTo7am;
        @JsonProperty(value ="s7amTo8am")
        private SlotInfo s7amTo8am;
        @JsonProperty(value ="s8amTo9am")
        private SlotInfo s8amTo9am;
        @JsonProperty(value ="s9amTo10am")
        private SlotInfo s9amTo10am;
        @JsonProperty(value ="s10amTo11am")
        private SlotInfo s10amTo11am;
        @JsonProperty(value ="s11amTo12pm")
        private SlotInfo s11amTo12pm;
        @JsonProperty(value ="s12pmTo1pm")
        private SlotInfo s12pmTo1pm;
        @JsonProperty(value ="s1pmTo2pm")
        private SlotInfo s1pmTo2pm;
        @JsonProperty(value ="s2pmTo3pm")
        private SlotInfo s2pmTo3pm;
        @JsonProperty(value ="s3pmTo4pm")
        private SlotInfo s3pmTo4pm;
        @JsonProperty(value ="s4pmTo5pm")
        private SlotInfo s4pmTo5pm;
        @JsonProperty(value ="s5pmTo6pm")
        private SlotInfo s5pmTo6pm;
        @JsonProperty(value ="s6pmTo7pm")
        private SlotInfo s6pmTo7pm;
        @JsonProperty(value ="s7pmTo8pm")
        private SlotInfo s7pmTo8pm;
        @JsonProperty(value ="s8pmTo9pm")
        private SlotInfo s8pmTo9pm;
        @JsonProperty(value ="s9pmTo10pm")
        private SlotInfo s9pmTo10pm;
        @JsonProperty(value ="s10pmTo11pm")
        private SlotInfo s10pmTo11pm;
        @JsonProperty(value ="s11pmTo12am")
        private SlotInfo s11pmTo12am;

        @Override
        public String toString() {
            return gson.toJson(this);
        }
    }

    public enum Slot {
        s4amTo5am,
        s5amTo6am,
        s6amTo7am,
        s7amTo8am,
        s8amTo9am,
        s9amTo10am,
        s10amTo11am,
        s11amTo12pm,
        s12pmTo1pm,
        s1pmTo2pm,
        s2pmTo3pm,
        s3pmTo4pm,
        s4pmTo5pm,
        s5pmTo6pm,
        s6pmTo7pm,
        s7pmTo8pm,
        s8pmTo9pm,
        s9pmTo10pm,
        s10pmTo11pm,
        s11pmTo12am
    }
}
