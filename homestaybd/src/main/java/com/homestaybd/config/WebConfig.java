package com.homestaybd.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // এটা দিয়ে /uploads/** → src/main/resources/static/uploads/ থেকে সার্ভ হবে
        registry.addResourceHandler("/uploads/**")   // ← এটা ঠিক আছে
                .addResourceLocations("file:///C:/Users/Abdullah/Downloads/homestaybd/uploads/");  // ← এখানে শেষে / আছে, ঠিক আছে

        // placeholder এর জন্য (যদি images/ ফোল্ডারে থাকে)
        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/images/");
    }
}