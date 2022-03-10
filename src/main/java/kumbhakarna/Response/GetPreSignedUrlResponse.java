package kumbhakarna.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

@AllArgsConstructor
@Getter
public class GetPreSignedUrlResponse implements Serializable {
    private final String url;
}
