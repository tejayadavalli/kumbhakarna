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
public class GetPreSignedUrlRequest implements Serializable {
    private static final Gson gson = new Gson();
    @JsonProperty(value ="fileName")
    private String fileName;

    @JsonProperty(value = "type")
    private String type;

    @Override
    public String toString() {
        return gson.toJson(this);
    }
}
