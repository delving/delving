package eu.europeana.dashboard.server;

import eu.europeana.core.database.incoming.ImportRepository;
import eu.europeana.dashboard.client.DashboardService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class HostedModeServiceLoader {
    private static final String[] CONTEXTS = {"dashboard-application-context.xml"};
    private static ApplicationContext applicationContext;
    private static DashboardService dashboardService;
    private static ImportRepository normalizedImportRepository;
    private static ImportRepository sandboxImportRepository;

    public static DashboardService getDashboardService() {
        if (dashboardService == null) {
            dashboardService = (DashboardService) context().getBean("dashboardService");
        }
        return dashboardService;
    }

    public static ImportRepository getNormalizedImportRepository() {
        if (normalizedImportRepository == null) {
            normalizedImportRepository = (ImportRepository) context().getBean("normalizedImportRepository");
        }
        return normalizedImportRepository;
    }

    public static ImportRepository getSandboxImportRepository() {
        if (sandboxImportRepository == null) {
            sandboxImportRepository = (ImportRepository) context().getBean("sandboxImportRepository");
        }
        return sandboxImportRepository;
    }

    private static synchronized ApplicationContext context() {
        if (applicationContext == null) {
            applicationContext = new ClassPathXmlApplicationContext(CONTEXTS);
        }
        return applicationContext;
    }
}