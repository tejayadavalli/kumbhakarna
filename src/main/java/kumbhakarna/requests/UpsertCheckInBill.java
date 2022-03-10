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
public class UpsertCheckInBill implements Serializable {
    private static final Gson gson = new Gson();
    @JsonProperty(value ="id")
    private String id;

    @JsonProperty(value ="gst_number")
    private String gstNumber;

    @JsonProperty(value ="s3_bill_key")
    private String s3BillKey;

    @JsonProperty(value ="bill_number")
    private String billNumber;

    @Override
    public String toString() {
        return gson.toJson(this);
    }
}
