package kumbhakarna.Response;

import com.google.gson.Gson;
import kumbhakarna.model.RoomData;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;
import java.util.Map;

@AllArgsConstructor
@Getter
public class GetHotelStatusResponse implements Serializable {
    private static final Gson gson = new Gson();
    private final Map<String, RoomData> roomsData;

    @Override
    public String toString() {
        return gson.toJson(this);
    }
}