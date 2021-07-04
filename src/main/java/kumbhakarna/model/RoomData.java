package kumbhakarna.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class RoomData {
    private static final Gson gson = new Gson();
    @JsonProperty(value ="roomStatus")
    private final RoomStatus roomStatus;
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
    @JsonProperty(value ="remark")
    private final String remark;

    @Override
    public String toString() {
        return gson.toJson(this);
    }
}
