package kumbhakarna.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Guest {
    private final String phoneNumber;
    private final String name;
    private final Long checkInSmsTime;
    private final Long checkOutSmsTime;
}
