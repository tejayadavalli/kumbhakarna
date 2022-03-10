package kumbhakarna.Response;

import kumbhakarna.model.BillSummary;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;
import java.util.List;

@AllArgsConstructor
@Getter
public class BillResponse implements Serializable {
    private final List<BillSummary> billSummaries;
}
