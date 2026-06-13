package com.freecanvas;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@MapperScan("com.freecanvas.repository")
public class FreeCanvasApplication {
    public static void main(String[] args) {
        SpringApplication.run(FreeCanvasApplication.class, args);
    }
}
