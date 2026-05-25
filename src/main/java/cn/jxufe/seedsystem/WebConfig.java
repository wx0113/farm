package cn.jxufe.seedsystem;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 让 SpringBoot 识别 /images/ 开头的图片
        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/images/");
    }
}