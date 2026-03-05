package com.fintech.Config;

import com.fintech.Service.InvoiceService;
import com.fintech.Service.QrService;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.File;

@WebListener
public class AppContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        DataSourceConfig.init();
        sce.getServletContext().setAttribute("dataSource", DataSourceConfig.getDataSource());
        LiquidBaseConfig.runMigration(DataSourceConfig.getDataSource());

        String qrPath = sce.getServletContext()
                .getRealPath("/qr");

        File dir = new File(qrPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

//        new File(qrPath).mkdirs();

        QrService qrService = new QrService(qrPath);
        sce.getServletContext().setAttribute("qrPath",qrPath);
        sce.getServletContext().setAttribute("qrService", qrService);

        InvoiceService invoiceService=new InvoiceService(DataSourceConfig.getDataSource(),qrService, qrPath);
        new InvoiceEmailConsumer(new EmailService(),invoiceService).start();

        System.out.println("Application Started Successfully");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        DataSourceConfig.shutdown();
        System.out.println("Application Stopped");
    }
}