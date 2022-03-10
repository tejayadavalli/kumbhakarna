package kumbhakarna.Response;

import com.google.gson.Gson;
import kumbhakarna.model.CashOut;
import kumbhakarna.model.Task;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;
import java.util.List;

@AllArgsConstructor
@Getter
public class GetCashOutResponse implements Serializable {
    private static final Gson gson = new Gson();
    private final List<CashOut> cashOuts;

    @Override
    public String toString() {
        return gson.toJson(this);
    }
}