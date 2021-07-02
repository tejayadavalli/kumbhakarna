package smaash.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;
import java.util.List;

@AllArgsConstructor
@Getter
public class SummaryRequest implements Serializable {
    @JsonProperty(value = "startDate")
    private String startDate;
    @JsonProperty(value = "endDate")
    private String endDate;
    @JsonProperty(value = "slotsSelected")
    private List<String> slotsSelected;
}
