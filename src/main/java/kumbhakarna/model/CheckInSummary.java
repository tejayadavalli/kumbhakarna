package kumbhakarna.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CheckInSummary {
    private static final Gson gson = new Gson();
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
    @JsonProperty(value ="remark")
    private final String remark;

    @Override
    public String toString() {
        return gson.toJson(this);
    }
}
