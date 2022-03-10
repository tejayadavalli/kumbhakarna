package kumbhakarna.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@AllArgsConstructor
@Getter
@Builder
@Setter
public class CashOut implements Serializable {
    private static final Gson gson = new Gson();
    @JsonProperty(value ="id")
    private String id;
    @JsonProperty(value ="date")
    private final String date;
    @JsonProperty(value ="amount")
    private final Integer amount;

    @Override
    public String toString() {
        return gson.toJson(this);
    }
}
