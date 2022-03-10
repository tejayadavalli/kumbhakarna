package kumbhakarna.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Task {
    private final String id;
    private final String taskInfo;
    private final Long createdAt;
    private final TaskStatus taskStatus;
    private final String assignedTo;
    private final String remark;
    private final Long updatedAt;

    public enum  TaskStatus{
        CREATED,
        DONE,
        NOT_DONE,
        NOT_NEEDED
    }
}
