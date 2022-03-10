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
public class CronJob implements Serializable {
    private static final Gson gson = new Gson();
    @JsonProperty(value ="id")
    private String id;
    @JsonProperty(value ="cron_expression")
    private final String cronExpression;
    @JsonProperty(value ="assigned_to")
    private final String assignedTo;
    @JsonProperty(value ="flock_message")
    private final String flockMessage;
    @JsonProperty(value ="portal_message")
    private final String portalMessage;
    @JsonProperty(value ="differentiate_rooms")
    private final Boolean differentiateRooms;

    @Override
    public String toString() {
        return gson.toJson(this);
    }
}
