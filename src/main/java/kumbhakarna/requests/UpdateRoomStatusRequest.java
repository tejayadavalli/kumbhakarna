package kumbhakarna.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import kumbhakarna.model.RoomStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

@AllArgsConstructor
@Getter
public class UpdateRoomStatusRequest implements Serializable {
    @JsonProperty(value = "room")
    private String room;
    @JsonProperty(value = "roomStatus")
    private String roomStatus;
    @JsonProperty(value = "name")
    private String name;
    @JsonProperty(value = "phone")
    private String phone;
    @JsonProperty(value = "guestCount")
    private Integer guestCount;
    @JsonProperty(value = "extraBed")
    private Boolean extraBed;
    @JsonProperty(value = "plan")
    private String plan;
    @JsonProperty(value = "tariff")
    private Integer tariff;
    @JsonProperty(value = "checkInTime")
    private String checkInTime;
    @JsonProperty(value = "checkOutTime")
    private String checkOutTime;
    @JsonProperty(value = "remark")
    private String remark;
    @JsonProperty(value = "days")
    private Integer days;
    @JsonProperty(value = "roomBill")
    private Integer roomBill;
    @JsonProperty(value = "foodBill")
    private Integer foodBill;

    public RoomStatus getRoomStatus(){
        return RoomStatus.valueOf(roomStatus);
    }
}
