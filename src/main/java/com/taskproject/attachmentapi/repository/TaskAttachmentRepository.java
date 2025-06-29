// --- 檔案：TaskAttachmentRepository.java ---
// 負責操作 TaskAttachments (FileTable) 資料表。

package com.taskproject.attachmentapi.repository;

import com.taskproject.attachmentapi.model.TaskAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TaskAttachmentRepository extends JpaRepository<TaskAttachment, UUID> {

    /**
     * 這是一個特殊的原生 SQL 查詢，專門用來讀取 FileTable 中的檔案內容 (file_stream)。
     * 因為檔案內容可能很大，我們只在需要下載檔案時才呼叫這個方法。
     * @param streamId 檔案的唯一識別碼 (stream_id)
     * @return 檔案內容的 byte 陣列
     */
    @Query(value = "SELECT file_stream FROM TaskAttachments WHERE stream_id = :streamId", nativeQuery = true)
    byte[] findFileStreamById(@Param("streamId") UUID streamId);
}