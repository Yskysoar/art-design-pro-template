package com.template;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 后端模板启动类。
 */
@MapperScan("com.template.**.mapper")
@SpringBootApplication
public class BackendTemplateApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendTemplateApplication.class, args);
    }
}
