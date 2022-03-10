package kumbhakarna.Response;

import com.google.gson.Gson;
import kumbhakarna.model.CronJob;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;
import java.util.List;

@AllArgsConstructor
@Getter
public class GetCronsResponse implements Serializable {
    private static final Gson gson = new Gson();
    private final List<CronJob> cronJobs;

    @Override
    public String toString() {
        return gson.toJson(this);
    }
}