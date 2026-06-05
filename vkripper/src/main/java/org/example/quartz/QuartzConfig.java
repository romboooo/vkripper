package org.example.quartz;

import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzConfig {
    @Bean
    public JobDetail shoppingCartCleanupJobDetail() {
        return JobBuilder.newJob(ShoppingCartCleanup.class)
                .withIdentity("shoppingCartCleanup")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger shoppingCartCleanupTrigger(JobDetail shoppingCartCleanupJobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(shoppingCartCleanupJobDetail)
                .withIdentity("shoppingCartCleanupTrigger")
                .withSchedule(
                        SimpleScheduleBuilder.simpleSchedule()
                                .withIntervalInMinutes(1)
                                .repeatForever()
                )
                .build();
    }
}