package com.PAWPAW.pawpaw.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        String rootDir = System.getProperty("user.dir");


        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + rootDir + File.separator + "uploads" + File.separator);
    }
}