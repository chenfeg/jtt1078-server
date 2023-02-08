package com.cesiumai.jtt1078server.configuration;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class ApplicationContextHelper implements ApplicationContextAware {
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        this.applicationContext = context;
    }

    public Object getBean(String beanName) throws BeansException, IllegalArgumentException {
        return this.applicationContext != null ? this.applicationContext.getBean(beanName) : null;
    }

}
