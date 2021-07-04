package kumbhakarna.Response;

import kumbhakarna.model.CheckInSummary;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;
import java.util.List;

@AllArgsConstructor
@Getter
public class SummaryResponse implements Serializable {
    private final List<CheckInSummary> checkInSummaries;
}
