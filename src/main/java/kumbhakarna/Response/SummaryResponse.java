package kumbhakarna.Response;

import kumbhakarna.model.CashOut;
import kumbhakarna.model.CheckInSummary;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;
import java.util.List;

@AllArgsConstructor
@Getter
public class SummaryResponse implements Serializable {
    private final List<CheckInSummary> checkInSummaries;
    private final List<CashOut> cashOuts;
    private final boolean shouldComputeSummary;

}
