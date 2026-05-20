package com.example.miniproject.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        // ── Tours ──────────────────────────────────────────────────────────
        String tourPath = System.getProperty("user.dir") + "/uploads/tours/";
        System.out.println("📁 Tours resource path: " + tourPath);
        registry.addResourceHandler("/uploads/tours/**")
                .addResourceLocations("file:" + tourPath);

        // ── Room images ────────────────────────────────────────────────────
        String roomPath = System.getProperty("user.dir") + "/uploads/rooms/";
        System.out.println("📁 Rooms resource path: " + roomPath);
        registry.addResourceHandler("/uploads/rooms/**")
                .addResourceLocations("file:" + roomPath);

         // ── Homestay images ──────────────────────────────────────────────
        String homestayPath = System.getProperty("user.dir") + "/uploads/homestays/";
        System.out.println("📁 Homestay resource path: " + homestayPath);
         registry.addResourceHandler("/uploads/homestays/**")
            .addResourceLocations("file:" + homestayPath);
    }
}