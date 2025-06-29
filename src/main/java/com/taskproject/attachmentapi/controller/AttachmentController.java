package com.taskproject.attachmentapi.controller;

import com.taskproject.attachmentapi.client.TaskApiClient;
import com.taskproject.attachmentapi.dto.AttachmentInfoDTO;
import com.taskproject.attachmentapi.model.TaskAttachment;
import com.taskproject.attachmentapi.service.FileStorageService;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api") // 所有此 Controller 的 API 都以 /api 開頭
public class AttachmentController {

    private final FileStorageService fileStorageService;
    private final TaskApiClient taskApiClient;

    @Autowired
    public AttachmentController(FileStorageService fileStorageService, TaskApiClient taskApiClient) {
        this.fileStorageService = fileStorageService;
        this.taskApiClient = taskApiClient;
    }

    /**
     * 為指定的任務上傳一個檔案。
     * @param taskId 任務 ID
     * @param file 上傳的檔案
     * @param bearerToken JWT 授權標頭
     * @return 包含新附件資訊的 DTO
     */
    @PostMapping("/tasks/{taskId}/attachments")
    public ResponseEntity<?> uploadFile(@PathVariable Long taskId,
                                        @RequestParam("file") MultipartFile file,
                                        @RequestHeader("Authorization") String bearerToken) {
        try {
            // 步驟 1: 透過 Feign Client 呼叫 TaskApi，檢查任務是否存在且屬於該用戶
            taskApiClient.checkTaskExists(taskId, bearerToken);

            // 步驟 2: 如果檢查通過，則儲存檔案並建立關聯
            TaskAttachment attachment = fileStorageService.storeFile(file, taskId);
            
            // 步驟 3: 將 Entity 轉換為 DTO 並回傳給前端
            return new ResponseEntity<>(AttachmentInfoDTO.fromEntity(attachment), HttpStatus.CREATED);

        } catch (FeignException e) {
            // 如果 Feign 呼叫失敗 (例如 404 或 403)，則回傳對應的錯誤訊息
            return new ResponseEntity<>("任務不存在或您沒有權限訪問。", HttpStatus.valueOf(e.status()));
        } catch (Exception e) {
            return new ResponseEntity<>("檔案上傳失敗：" + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 取得指定任務的所有附件清單。
     * @param taskId 任務 ID
     * @return 附件資訊 DTO 列表
     */
    @GetMapping("/tasks/{taskId}/attachments")
    public ResponseEntity<List<AttachmentInfoDTO>> getAttachmentsForTask(@PathVariable Long taskId) {
        List<AttachmentInfoDTO> dtos = fileStorageService.getAttachmentsForTask(taskId)
                .stream()
                .map(AttachmentInfoDTO::fromEntity) // 將每個 Entity 轉換為 DTO
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * 下載指定的附件檔案。
     * @param fileId 檔案的 stream_id
     * @return 檔案資源
     */
    @GetMapping("/attachments/{fileId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable UUID fileId) {
        TaskAttachment attachment = fileStorageService.getAttachmentInfo(fileId);
        if (attachment == null) {
            return ResponseEntity.notFound().build();
        }

        byte[] fileContent = fileStorageService.getFileContent(fileId);
        ByteArrayResource resource = new ByteArrayResource(fileContent);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + attachment.getName() + "\"")
                .body(resource);
    }

    /**
     * 刪除指定的附件。
     * @param fileId 檔案的 stream_id
     * @return 空的回應
     */
    @DeleteMapping("/attachments/{fileId}")
    public ResponseEntity<Void> deleteFile(@PathVariable UUID fileId) {
        // 在刪除前，可以增加權限檢查邏輯 (例如，再次呼叫 TaskApi 確認操作者身份)
        // 此處為簡化，直接執行刪除
        fileStorageService.deleteFile(fileId);
        return ResponseEntity.noContent().build();
    }
}