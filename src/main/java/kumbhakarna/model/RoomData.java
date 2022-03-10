package kumbhakarna.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@AllArgsConstructor
@Getter
@Builder
public class RoomData {
    private static final Gson gson = new Gson();
    @JsonProperty(value ="roomStatus")
    private final RoomStatus roomStatus;
    @JsonProperty(value = "meta")
    private final Map<String, String> meta;
    @JsonProperty(value ="phone")
    private final String phone;
    @JsonProperty(value ="name")
    private final String name;
    @JsonProperty(value ="guestCount")
    private final Integer guestCount;
    @JsonProperty(value ="extraBed")
    private final Boolean extraBed;
    @JsonProperty(value ="linkedCheckin")
    private final String linkedCheckin;
    @JsonProperty(value ="plan")
    private final String plan;
    @JsonProperty(value ="tariff")
    private final Integer tariff;
    @JsonProperty(value ="checkInTime")
    private final String checkInTime;
    @JsonProperty(value ="checkOutTime")
    private final String checkOutTime;
    @JsonProperty(value ="days")
    private final Integer days;
    @JsonProperty(value ="roomBill")
    private final Integer roomBill;
    @JsonProperty(value ="foodBill")
    private final Integer foodBill;
    @JsonProperty(value ="bottles")
    private final Integer bottles;
    @JsonProperty(value ="combos")
    private final Integer combos;
    @JsonProperty(value ="cooldrinks")
    private final Integer cooldrinks;
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


    @Override
    public String toString() {
        return gson.toJson(this);
    }
}
