package kumbhakarna.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class BillSummary {
    private static final Gson gson = new Gson();
    @JsonProperty(value ="id")
    private final String id;
    @JsonProperty(value ="name")
    private final String name;
    @JsonProperty(value ="room")
    private final String room;
    @JsonProperty(value ="tariff")
    private final Integer tariff;
    @JsonProperty(value ="plan")
    private final String plan;
    @JsonProperty(value ="checkInTime")
    private final String checkInTime;
    @JsonProperty(value ="checkOutTime")
    private final String checkOutTime;
    @JsonProperty(value ="guestCount")
    private final Integer guestCount;
    @JsonProperty(value ="extraBed")
    private final Boolean extraBed;
    @JsonProperty(value ="days")
    private final Integer days;
    @JsonProperty(value ="roomBill")
    private final Integer roomBill;
    @JsonProperty(value ="foodBill")
    private final Integer foodBill;
    @JsonProperty(value ="cash")
    private final Integer cash;
    @JsonProperty(value ="upi")
    private final Integer upi;
    @JsonProperty(value ="card")
    private final Integer card;
    @JsonProperty(value ="ingommt")
    private final Integer ingommt;
    @JsonProperty(value ="remark")
    private final String remark;
    @JsonProperty(value ="gstNumber")
    private final String gstNumber;
    @JsonProperty(value ="s3BillKey")
    private final String s3BillKey;
    @JsonProperty(value ="billNumber")
    private final String billNumber;

    @Override
    public String toString() {
        return gson.toJson(this);
    }
}
