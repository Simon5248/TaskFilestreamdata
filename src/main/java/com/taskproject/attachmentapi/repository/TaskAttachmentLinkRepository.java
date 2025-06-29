// --- 檔案：TaskAttachmentLinkRepository.java ---
// 負責操作 TaskAttachmentLinks (關聯表) 資料表。

package com.taskproject.attachmentapi.repository;

import com.taskproject.attachmentapi.model.TaskAttachmentLink;
import com.taskproject.attachmentapi.model.TaskAttachmentLinkId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskAttachmentLinkRepository extends JpaRepository<TaskAttachmentLink, TaskAttachmentLinkId> {

    /**
     * 根據任務 ID (taskId) 查詢所有相關的附件關聯紀錄。
     * Spring Data JPA 會根據這個方法名稱，自動為我們產生對應的查詢。
     * @param taskId 任務的 ID
     * @return 該任務的所有附件關聯紀錄列表
     */
    List<TaskAttachmentLink> findById_TaskId(Long taskId);

    /**
     * 根據檔案的 streamId 查詢關聯紀錄。
     * 這在刪除檔案前確認其歸屬時可能會用到。
     * @param attachmentStreamId 檔案的 stream_id
     * @return 該檔案的關聯紀錄
     */
    TaskAttachmentLink findById_AttachmentStreamId(UUID attachmentStreamId);
}