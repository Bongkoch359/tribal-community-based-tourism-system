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

         // ── Paymrnt images ──────────────────────────────────────────────
        String PaymentSlipPath = System.getProperty("user.dir") + "/uploads/slips/";
        System.out.println("📁 payment resource path: " + PaymentSlipPath);
         registry.addResourceHandler("/uploads/slips/**")
            .addResourceLocations("file:" + PaymentSlipPath );

        //--Review
        String reviewPath = System.getProperty("user.dir") + "/uploads/reviews/";
        System.out.println("📁 Review resource path: " + reviewPath);
        registry.addResourceHandler("/uploads/reviews/**")
                .addResourceLocations("file:" + reviewPath);

        // ── Activity post images ───────────────────────────────────────────
        String postPath = System.getProperty("user.dir") + "/uploads/posts/";
        System.out.println("📁 Posts resource path: " + postPath);
        registry.addResourceHandler("/uploads/posts/**")
                .addResourceLocations("file:" + postPath);

    }
}