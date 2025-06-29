package com.taskproject.attachmentapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * TaskAttachmentApi 的主應用程式進入點
 */
@SpringBootApplication
@EnableFeignClients // ** 關鍵：啟用 OpenFeign 功能 **
public class AttachmentApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(AttachmentApiApplication.class, args);
    }

}
// ** 關鍵：這個類別是 Spring Boot 應用程式的入口點，
// ** 啟動時會自動掃描並註冊所有的 Spring Bean