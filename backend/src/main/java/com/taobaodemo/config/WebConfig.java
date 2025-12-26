package com.taobaodemo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Value("${server.public.ip:localhost}")
    private String serverPublicIp;
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 构建允许的源列表
        List<String> allowedOrigins = new ArrayList<>();
        allowedOrigins.add("http://localhost:5173");
        allowedOrigins.add("http://localhost:3000");
        
        // 如果配置了公网IP，添加相关域名
        if (serverPublicIp != null && !serverPublicIp.isEmpty() && !serverPublicIp.equals("localhost")) {
            allowedOrigins.add("http://" + serverPublicIp);
            allowedOrigins.add("http://" + serverPublicIp + ":80");
            allowedOrigins.add("http://" + serverPublicIp + ":5173");
            allowedOrigins.add("https://" + serverPublicIp);
            allowedOrigins.add("https://" + serverPublicIp + ":443");
        }
        
        registry.addMapping("/**")
                .allowedOrigins(allowedOrigins.toArray(new String[0]))
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置静态资源访问，使上传的图片可以通过URL访问
        String uploadPath = System.getProperty("user.dir") + File.separator + "uploads";
        // 确保路径格式正确（Windows需要转义，Linux不需要）
        String filePath = uploadPath.replace("\\", "/");
        if (!filePath.endsWith("/")) {
            filePath += "/";
        }
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + filePath);
    }
}
