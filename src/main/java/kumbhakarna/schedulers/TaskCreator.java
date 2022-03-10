package kumbhakarna.schedulers;

import com.google.gson.JsonObject;
import kumbhakarna.dao.InventoryDao;
import kumbhakarna.model.CronJob;
import kumbhakarna.model.Task;
import kumbhakarna.utils.Utils;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.io.IOException;
import java.util.UUID;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static kumbhakarna.model.Task.TaskStatus.CREATED;

public class TaskCreator implements Job {
    private static final String[] rooms = new String[]{
            "101", "102", "103", "104",
            "201", "202", "203", "204", "205",
            "301", "302", "303", "304", "305"
    };
    public void execute(JobExecutionContext context)
            throws JobExecutionException {
        JobDataMap jobDataMap = context.getMergedJobDataMap();
        CronJob cronJob = (CronJob) jobDataMap.get("cronJob");
        InventoryDao dao = (InventoryDao) jobDataMap.get("dao");

        String assignedTo = cronJob.getAssignedTo();
        String flockMessage = cronJob.getFlockMessage();
        String portalMessage = cronJob.getPortalMessage();
        Boolean differentiateRooms = cronJob.getDifferentiateRooms();
        long currentTimeMillis = System.currentTimeMillis();
        if(!differentiateRooms) {
            String id = UUID.randomUUID().toString();
            Task task = new Task(id, portalMessage, currentTimeMillis, CREATED, assignedTo, null, null);
            dao.addTask(task);
        } else{
           for(String room : rooms){
               String id = UUID.randomUUID().toString();
               Task task = new Task(id, room + " : " + portalMessage, currentTimeMillis, CREATED, assignedTo, null, null);
               dao.addTask(task);
           }
        }
        sendFlockMessage(flockMessage, assignedTo);
    }

    private void sendFlockMessage(String flockMessage, String assignedTo) {
        completedFuture(null)
                .thenRunAsync(() -> {
                            try {
                                Thread.sleep(60000);
                                String flockUrl = "https://api.flock.com/hooks/sendMessage/3c5b3cea-08b5-4837-80a2-ed62fe42bfd7";
                                JsonObject data = new JsonObject();
                                data.addProperty("flockml", "<flockml>" +
                                        "<b>New Task</b><br/><br/>" +
                                        "<b>Assigned To: </b>" + assignedTo + "<br/>" +
                                        "<b>Task: </b>" + flockMessage + "<br/>" +
                                        "</flockml>");
                                Utils.sendMessage(flockUrl, data);
                            } catch (InterruptedException | IOException e) {
                                e.printStackTrace();
                            }
                        }
                );
    }
}
