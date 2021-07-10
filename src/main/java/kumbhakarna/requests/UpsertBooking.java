package kumbhakarna.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

@AllArgsConstructor
@Getter
public class UpsertBooking implements Serializable {
    private static final Gson gson = new Gson();
    @JsonProperty(value ="id")
    private final String id;
    @JsonProperty(value ="name")
    private final String name;
    @JsonProperty(value ="phone")
    private final String phone;
    @JsonProperty(value ="deluxeRooms")
    private final Integer deluxeRooms;
    @JsonProperty(value ="deluxeTariff")
    private final Integer deluxeTariff;
    @JsonProperty(value ="executiveRooms")
    private final Integer executiveRooms;
    @JsonProperty(value ="executiveTariff")
    private final Integer executiveTariff;
    @JsonProperty(value ="superiorRooms")
    private final Integer superiorRooms;
    @JsonProperty(value ="superiorTariff")
    private final Integer superiorTariff;
    @JsonProperty(value ="businessRooms")
    private final Integer businessRooms;
    @JsonProperty(value ="businessTariff")
    private final Integer businessTariff;
    @JsonProperty(value ="suiteRooms")
    private final Integer suiteRooms;
    @JsonProperty(value ="suiteTariff")
    private final Integer suiteTariff;
    @JsonProperty(value = "days")
    private final Integer days;
    @JsonProperty(value ="extraBed")
    private final Boolean extraBed;
    @JsonProperty(value ="plan")
    private final String plan;
    @JsonProperty(value ="advance")
    private final Integer advance;
    @JsonProperty(value ="checkInTime")
    private final String checkInTime;
    @JsonProperty(value ="mode")
    private final String mode;
    @JsonProperty(value ="remark")
    private final String remark;

    @Override
    public String toString() {
        return gson.toJson(this);
    }
}
