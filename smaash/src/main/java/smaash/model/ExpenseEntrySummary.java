package smaash.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.Gson;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ExpenseEntrySummary{
    private static final Gson gson = new Gson();
    @JsonProperty(value ="date")
    private String date;
    @JsonProperty(value ="info")
    private String info;
    @JsonProperty(value ="expense")
    private int expense;
    @Override
    public String toString() {
        return gson.toJson(this);
    }
}
