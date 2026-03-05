package com.fintech.Config;

import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;

public class TemplateConfig {

    private static Configuration configuration;

    static {
        configuration =new Configuration(Configuration.VERSION_2_3_32);
        configuration.setClassLoaderForTemplateLoading(TemplateConfig.class.getClassLoader(),"templates");
        configuration.setDefaultEncoding("UTF-8");
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    }

    public static Configuration getConfiguration(){
        return  configuration;
    }
}