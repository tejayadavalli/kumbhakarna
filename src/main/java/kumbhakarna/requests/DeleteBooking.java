package kumbhakarna.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeleteBooking implements Serializable {
    private static final Gson gson = new Gson();
    @JsonProperty(value ="id")
    private String id;

    @Override
    public String toString() {
        return gson.toJson(this);
    }
}
