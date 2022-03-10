package kumbhakarna.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

@AllArgsConstructor
@Getter
public class SummaryRequest implements Serializable {
    @JsonProperty(value = "startDate")
    private String startDate;
    @JsonProperty(value = "endDate")
    private String endDate;
    @JsonProperty(value = "password")
    private String password;
    @JsonProperty(value = "getCurrentRooms")
    private Boolean getCurrentRooms;
}
