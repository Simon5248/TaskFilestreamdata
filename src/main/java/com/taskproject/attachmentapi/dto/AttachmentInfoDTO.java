// --- 檔案：dto/AttachmentInfoDTO.java ---
// 作為 API 回應的標準資料格式，代表一個附件的公開資訊。

package com.taskproject.attachmentapi.dto;

import com.taskproject.attachmentapi.model.TaskAttachment;
import lombok.Data;

import java.util.UUID;

@Data // Lombok: 自動產生 getter, setter, toString 等方法
public class AttachmentInfoDTO {

    private UUID id;
    private String name;
    private String fileType;
    private Long size;
    private String downloadUrl;

    /**
     * 一個靜態的工廠方法，方便地將 TaskAttachment (Entity) 轉換為 AttachmentInfoDTO。
     * @param attachment 從資料庫查詢出的 TaskAttachment 實體
     * @return 一個填充好資料的 AttachmentInfoDTO 物件
     */
    public static AttachmentInfoDTO fromEntity(TaskAttachment attachment) {
        if (attachment == null) {
            return null;
        }

        AttachmentInfoDTO dto = new AttachmentInfoDTO();
        dto.setId(attachment.getStreamId());
        dto.setName(attachment.getName());
        dto.setFileType(attachment.getFileType());
        dto.setSize(attachment.getSize());
        // 我們在這裡預先組合好完整的下載 URL，讓前端可以直接使用
        dto.setDownloadUrl("/api/attachments/" + attachment.getStreamId());
        
        return dto;
    }
}