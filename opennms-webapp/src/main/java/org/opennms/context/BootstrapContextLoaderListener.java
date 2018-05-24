package org.opennms.context;

import javax.servlet.ServletContext;

import org.hyperic.hq.context.Bootstrap;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.ContextLoaderListener;

public class BootstrapContextLoaderListener extends ContextLoaderListener {

    @Override
    protected void customizeContext(ServletContext servletContext, ConfigurableWebApplicationContext applicationContext) {
        Bootstrap.setAppContext(applicationContext);
    }

}
