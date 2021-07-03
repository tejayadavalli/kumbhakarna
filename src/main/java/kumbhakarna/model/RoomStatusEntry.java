package kumbhakarna.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

@AllArgsConstructor
@Getter
public class RoomStatusEntry implements Serializable {
    @JsonProperty(value = "room")
    private String room;
    @JsonProperty(value ="roomStatus")
    private String roomStatus;
    @JsonProperty(value ="linkedCheckin")
    private String linkedCheckin;

    public RoomStatus getRoomStatus(){
        return RoomStatus.valueOf(roomStatus);
    }
}
