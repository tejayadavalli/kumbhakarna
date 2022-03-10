package kumbhakarna;

import com.google.common.io.Resources;
import kumbhakarna.Response.GetCronsResponse;
import kumbhakarna.dao.InventoryDao;
import kumbhakarna.model.CronJob;
import kumbhakarna.schedulers.CronScheduler;
import org.apache.commons.io.Charsets;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.List;

@RestController
public class CronController {
    private final InventoryDao inventoryDao;
    private final String cronsFile;
    private final CronScheduler cronScheduler;

    @Autowired
    public CronController(InventoryDao inventoryDao) throws IOException, SchedulerException, SQLException {
        this.inventoryDao = inventoryDao;
        this.cronsFile = readResource("html/crons.html", Charsets.UTF_8);
        this.cronScheduler = new CronScheduler();
        inventoryDao.getAllCrons().forEach(
                cronJob -> {
                    try {
                        cronScheduler.addJob(cronJob, inventoryDao);
                    } catch (SchedulerException e) {
                        e.printStackTrace();
                    }
                });
        cronScheduler.startScheduler();
    }

    private String readResource(final String fileName, Charset charset) throws IOException {
        return Resources.toString(Resources.getResource(fileName), charset);
    }

    @RequestMapping(value = "kumbhakarna/crons", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String getHtmlPage() {
        return this.cronsFile;
    }

    @RequestMapping(value = "/get-crons", method = RequestMethod.GET, produces = "application/json")
    public GetCronsResponse getRoomsStatus() throws SQLException {
        List<CronJob> cronJobs = inventoryDao.getAllCrons();
        return new GetCronsResponse(cronJobs);
    }

    @RequestMapping(value = "/add-cron", method = RequestMethod.POST, consumes = "application/json")
    public String addCron(@RequestBody CronJob cronJob)
            throws SQLException, SchedulerException {
        if (!org.quartz.CronExpression.isValidExpression(cronJob.getCronExpression()))
            throw new InternalError("Invalid Cron Expression");
        cronJob.setId(String.valueOf(System.currentTimeMillis()));
        inventoryDao.addCron(cronJob);
        cronScheduler.addJob(cronJob, inventoryDao);
        return "{}";
    }

    @RequestMapping(value = "/delete-cron", method = RequestMethod.PUT, produces = "application/json")
    public String deleteCron(@RequestBody CronJob cronJob)
            throws SQLException, SchedulerException {
        inventoryDao.deleteCron(cronJob);
        cronScheduler.deleteJob(cronJob);
        return "{}";
    }

    public static void main(String[] args) {
        System.out.println(new Integer(1000000000).hashCode());
        System.out.println(new Integer(1000000000).hashCode());
    }
}