package kumbhakarna;

import com.google.gson.JsonObject;
import kumbhakarna.Response.GetTasksResponse;
import kumbhakarna.dao.InventoryDao;
import kumbhakarna.model.Task;
import kumbhakarna.utils.Utils;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import static java.util.concurrent.CompletableFuture.completedFuture;

@RestController
public class TaskController {
    private final InventoryDao inventoryDao;

    @Autowired
    public TaskController(InventoryDao inventoryDao) throws IOException, SchedulerException, SQLException {
        this.inventoryDao = inventoryDao;
    }

    @RequestMapping(value = "/get-tasks", method = RequestMethod.GET, produces = "application/json")
    public GetTasksResponse getAllTasks() throws SQLException {
        List<Task> tasks = inventoryDao.getAllPendingTasks();
        return new GetTasksResponse(tasks);
    }

    @RequestMapping(value = "/update-task", method = RequestMethod.PUT, consumes = "application/json")
    public String updateTask(@RequestBody Task task)
            throws SQLException, SchedulerException {
        inventoryDao.updateTaskStatus(task.getId(), task.getTaskStatus());
        Task fetchedTask = inventoryDao.getTask(task.getId());
        sendFlockMessage(fetchedTask.getTaskInfo(), fetchedTask.getAssignedTo(), task.getTaskStatus());
        return "{}";
    }

    private void sendFlockMessage(String taskInfo, String assignedTo, Task.TaskStatus taskStatus) {
        completedFuture(null)
                .thenRunAsync(() -> {
                            try {
                                String flockUrl = "https://api.flock.com/hooks/sendMessage/3c5b3cea-08b5-4837-80a2-ed62fe42bfd7";
                                JsonObject data = new JsonObject();
                                data.addProperty("flockml", "<flockml>" +
                                        "<b>Task Updated</b><br/><br/>" +
                                        "<b>Assigned To: </b>" + assignedTo + "<br/>" +
                                        "<b>Task: </b>" + taskInfo + "<br/>" +
                                        "<b>Status: </b>" + taskStatus.name().replace("_", " ") + "<br/>" +
                                        "</flockml>");
                                Utils.sendMessage(flockUrl, data);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                );
    }
}