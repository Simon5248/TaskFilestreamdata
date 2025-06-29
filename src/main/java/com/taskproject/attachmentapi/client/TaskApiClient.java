// --- 檔案：client/TaskApiClient.java ---
// 一個 OpenFeign 客戶端，用來與 TaskApi 服務進行通訊。

package com.taskproject.attachmentapi.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * @FeignClient 註解宣告這是一個 Feign 客戶端。
 * name = "task-api": 這個名稱對應到我們在 application.properties 中設定的 feign.client.config.task-api.url。
 */
@FeignClient(name = "task-api", url = "${feign.client.config.task-api.url}")
public interface TaskApiClient {

    /**
     * 定義一個方法來呼叫 TaskApi 的 GET /tasks/{id} 端點。
     * 這個方法的目的是檢查指定的任務是否存在，並且屬於當前的使用者。
     *
     * @param taskId 要檢查的任務 ID。
     * @param bearerToken 必須傳入原始請求中的 JWT，以便 TaskApi 能夠驗證使用者身份。
     * @return 一個 ResponseEntity。如果任務存在且屬於該用戶，TaskApi 應回傳 200 OK。
     * 如果任務不存在或不屬於該用戶，TaskApi 應回傳 404 Not Found 或 403 Forbidden。
     */
    @GetMapping("/tasks/{taskId}")
    ResponseEntity<Void> checkTaskExists(
            @PathVariable("taskId") Long taskId,
            @RequestHeader("Authorization") String bearerToken
    );

}