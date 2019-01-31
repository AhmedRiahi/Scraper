package com.pp.engine.config;

import java.io.IOException;

import org.quartz.JobDetail;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.spi.JobFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

import com.pp.engine.job.CleanerJob;
import com.pp.engine.job.DescriptorsProcessingJob;


@Configuration
public class SchedulerConfig {

    private static final Logger LOG = LoggerFactory.getLogger(SchedulerConfig.class);

    @Bean
    public JobFactory jobFactory(ApplicationContext applicationContext) {
        AutowiringSpringBeanJobFactory jobFactory = new AutowiringSpringBeanJobFactory();
        jobFactory.setApplicationContext(applicationContext);
        return jobFactory;
    }

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(JobFactory jobFactory, @Qualifier("cleanerTrigger")Trigger cleanerTrigger, @Qualifier("descriptorsProcessingTrigger")Trigger descriptorsProcessingTrigger)
            throws IOException {
        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        factory.setJobFactory(jobFactory);
        //factory.setQuartzProperties(quartzProperties());
        factory.setTriggers(cleanerTrigger,descriptorsProcessingTrigger);
        LOG.info("starting jobs....");
        return factory;
    }

    @Bean
    public SimpleTriggerFactoryBean cleanerTrigger(@Qualifier("cleanerJobDetail") JobDetail jobDetail,@Value("${cleaner.frequency}") long frequency) {
        LOG.info("simpleJobTrigger");
        SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
        factoryBean.setJobDetail(jobDetail);
        factoryBean.setStartDelay(0L);
        factoryBean.setRepeatInterval(frequency);
        factoryBean.setRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY);
        return factoryBean;
    }

   

    @Bean
    public JobDetailFactoryBean cleanerJobDetail() {
        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        factoryBean.setJobClass(CleanerJob.class);
        factoryBean.setDurability(true);
        return factoryBean;
    }
    
    
    @Bean
    public SimpleTriggerFactoryBean descriptorsProcessingTrigger(@Qualifier("descriptorsProcessingJobDetail") JobDetail jobDetail,@Value("${descriptorsProcessing.frequency}") long frequency) {
        LOG.info("simpleJobTrigger");
        SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
        factoryBean.setJobDetail(jobDetail);
        factoryBean.setStartDelay(0L);
        factoryBean.setRepeatInterval(frequency);
        factoryBean.setRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY);
        return factoryBean;
    }
    
    @Bean
    public JobDetailFactoryBean descriptorsProcessingJobDetail() {
        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        factoryBean.setJobClass(DescriptorsProcessingJob.class);
        factoryBean.setDurability(true);
        return factoryBean;
    }
}