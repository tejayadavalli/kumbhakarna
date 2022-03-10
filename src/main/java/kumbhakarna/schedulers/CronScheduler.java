package kumbhakarna.schedulers;

import com.google.common.collect.ImmutableMap;
import kumbhakarna.dao.InventoryDao;
import kumbhakarna.model.CronJob;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import static org.quartz.JobBuilder.newJob;

public class CronScheduler {
    private final Scheduler scheduler;

    public CronScheduler() throws SchedulerException {
        SchedulerFactory schedulerFactory = new StdSchedulerFactory();
        this.scheduler = schedulerFactory.getScheduler();
    }

    public void addJob(CronJob cronJob, InventoryDao inventoryDao) throws SchedulerException {
        JobDetail job = newJob(TaskCreator.class)
                .withIdentity(cronJob.getId(), cronJob.getId())
                .setJobData(new JobDataMap(ImmutableMap.of("cronJob", cronJob, "dao", inventoryDao)))
                .build();
        Trigger trigger = TriggerBuilder
                .newTrigger()
                .withIdentity(cronJob.getId(),cronJob.getId())
                .withSchedule(CronScheduleBuilder.cronSchedule(cronJob.getCronExpression()))
                .build();
        scheduler.scheduleJob(job, trigger);
    }

    public void startScheduler() throws SchedulerException {
        scheduler.start();
    }

    public void deleteJob(CronJob cronJob) throws SchedulerException {
        JobDetail job = newJob(TaskCreator.class).withIdentity(cronJob.getId(), cronJob.getId()).build();
        scheduler.deleteJob(job.getKey());
    }
}
