package com.app.fitrack.config;

import com.mysql.cj.jdbc.AbandonedConnectionCleanupThread;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.sql.DriverManager;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Enumeration;

// Adding SLF4J imports, assuming it's available via Spring Boot (spring-boot-starter-logging)
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebListener
public class MysqlLeakFixListener implements ServletContextListener {

    private static final Logger log = LoggerFactory.getLogger(MysqlLeakFixListener.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        log.info("Initializing context and MySQL cleanup listener.");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        log.info("Destroying context. Attempting to shutdown MySQL AbandonedConnectionCleanupThread...");
        try {
            AbandonedConnectionCleanupThread.checkedShutdown();
            log.info("MySQL AbandonedConnectionCleanupThread shutdown successful.");
        } catch (Throwable t) {
            // Catching Throwable to be safe, though InterruptedException is what is typically documented
            log.error("Failed to shutdown MySQL AbandonedConnectionCleanupThread.", t);
        }

        log.info("Deregistering JDBC drivers loaded by this web application...");
        // Get the webapp's ClassLoader
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        // Loop through all drivers
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            // Check if the driver was loaded by this web application's classloader
            if (driver.getClass().getClassLoader() == cl) {
                // This driver was registered by the webapp's ClassLoader, so deregister it:
                try {
                    DriverManager.deregisterDriver(driver);
                    log.info("Deregistered JDBC driver: {}", driver);
                } catch (SQLException ex) {
                    log.error("Error deregistering JDBC driver: {}", driver, ex);
                }
            } else {
                // Driver was not registered by the webapp's ClassLoader and may be in use elsewhere
                log.trace("Not deregistering JDBC driver {} as it does not belong to this webapp's ClassLoader.", driver);
            }
        }
    }
} 