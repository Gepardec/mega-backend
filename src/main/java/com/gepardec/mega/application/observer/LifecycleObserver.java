package com.gepardec.mega.application.observer;

import com.gepardec.mega.application.configuration.ApplicationConfig;
import com.gepardec.mega.service.api.GmailService;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.runtime.configuration.ConfigUtils;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.event.Observes;
import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * With Quarkus we need to inject in the observer method and not in the bean itself.
 */
@Dependent
public class LifecycleObserver {

    void initApplicationConfig(final @Observes StartupEvent event,
                               final ApplicationConfig applicationConfig) {
        applicationConfig.init();
    }

    void initDatabase(final @Observes StartupEvent event,
                      final DataSource dataSource,
                      final @ConfigProperty(name = "quarkus.liquibase.change-log") String masterChangeLogFile,
                      final Logger logger) {
        try (Connection connection = dataSource.getConnection()) {
            ResourceAccessor resourceAccessor =
                    new ClassLoaderResourceAccessor(LifecycleObserver.class.getClassLoader());

            try (Liquibase liquibase =
                         new Liquibase(masterChangeLogFile, resourceAccessor, new JdbcConnection(connection))) {

                liquibase.update(new Contexts());
                logger.info("Initialized database with liquibase");
            }
        } catch (Exception e) {
            logger.error("Initialization of the database with liquibase failed", e);
        }
    }

    void watchGmailInbox(final @Observes StartupEvent event,
                         final GmailService gmailService,
                         final Logger logger) {
        if (ConfigUtils.isProfileActive("prod")) {
            logger.info("Starting to watch the Gmail inbox. Renewals will be handled by the corresponding scheduled task.");
            gmailService.watchInbox();
        }
    }
}
