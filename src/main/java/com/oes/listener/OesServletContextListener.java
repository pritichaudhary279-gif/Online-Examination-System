package com.oes.listener;

import com.oes.service.AsyncExamNotificationService;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Shuts down background mail workers when the application stops.
 */
public class OesServletContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // nothing
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        AsyncExamNotificationService.shutdown();
    }
}
