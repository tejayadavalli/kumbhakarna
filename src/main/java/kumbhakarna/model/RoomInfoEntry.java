package kumbhakarna.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class RoomInfoEntry {
    private static final Gson gson = new Gson();
    @JsonProperty(value ="roomStatus")
    private final RoomStatus roomStatus;
    @JsonProperty(value ="linkedCheckIn")
    private final String linkedCheckIn;

    @Override
    public String toString() {
        return gson.toJson(this);
    }
}
