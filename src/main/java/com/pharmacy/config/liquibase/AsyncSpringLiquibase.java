package com.pharmacy.config.liquibase;

import com.pharmacy.config.Constants;
import liquibase.exception.LiquibaseException;
import liquibase.integration.spring.SpringLiquibase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.core.task.TaskExecutor;
import org.springframework.util.StopWatch;

import javax.inject.Inject;

public class AsyncSpringLiquibase extends SpringLiquibase {

    private final Logger log = LoggerFactory.getLogger(AsyncSpringLiquibase.class);

    @Inject
    @Qualifier("taskExecutor")
    private TaskExecutor taskExecutor;

    @Inject
    private Environment env;

    @Override
    public void afterPropertiesSet() throws LiquibaseException {
        log.debug("Starting Liquibase synchronously");
        initDb();
    }

    protected void initDb() throws LiquibaseException {
        StopWatch watch = new StopWatch();
        watch.start();
        super.afterPropertiesSet();
        watch.stop();
        log.debug("Started Liquibase in {} ms", watch.getTotalTimeMillis());
    }
}
