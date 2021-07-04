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
    @JsonProperty(value = "remark")
    private String remark;


    public RoomStatus getRoomStatus(){
        return RoomStatus.valueOf(roomStatus);
    }
}
