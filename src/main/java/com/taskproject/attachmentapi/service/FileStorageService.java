// --- 檔案：service/FileStorageService.java ---
// 封裝所有與檔案附件相關的業務邏輯。

package com.taskproject.attachmentapi.service;

import com.taskproject.attachmentapi.exception.FileStorageException;
import com.taskproject.attachmentapi.model.TaskAttachment;
import com.taskproject.attachmentapi.model.TaskAttachmentLink;
import com.taskproject.attachmentapi.model.TaskAttachmentLinkId;
import com.taskproject.attachmentapi.repository.TaskAttachmentLinkRepository;
import com.taskproject.attachmentapi.repository.TaskAttachmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FileStorageService {

    private final TaskAttachmentRepository attachmentRepository;
    private final TaskAttachmentLinkRepository linkRepository;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public FileStorageService(TaskAttachmentRepository attachmentRepository, 
                              TaskAttachmentLinkRepository linkRepository, 
                              JdbcTemplate jdbcTemplate) {
        this.attachmentRepository = attachmentRepository;
        this.linkRepository = linkRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 儲存上傳的檔案，並將其與一個任務關聯。
     * @param file 前端上傳的 MultipartFile 物件
     * @param taskId 要關聯的任務 ID
     * @return 儲存後的檔案中繼資料
     */
    @Transactional // 確保整個方法在同一個資料庫交易中執行
    public TaskAttachment storeFile(MultipartFile file, Long taskId) {
        // 清理並標準化檔名
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            if (fileName.contains("..")) {
                throw new FileStorageException("檔名包含無效的路徑序列：" + fileName);
            }

            // 使用原生 SQL INSERT 並利用 OUTPUT 子句取回新產生的 stream_id
            // 這是操作 FileTable 中 file_stream 欄位的可靠方式
            String sql = "INSERT INTO TaskAttachments (name, file_stream, is_directory) " +
                         "OUTPUT inserted.stream_id " +
                         "VALUES (?, ?, 0)";

            UUID streamId = jdbcTemplate.queryForObject(sql, 
                new Object[]{fileName, file.getBytes()}, 
                UUID.class
            );

            if (streamId == null) {
                throw new FileStorageException("無法儲存檔案並獲取 stream_id：" + fileName);
            }

            // 建立並儲存關聯紀錄
            TaskAttachmentLinkId linkId = new TaskAttachmentLinkId(streamId, taskId);
            TaskAttachmentLink link = new TaskAttachmentLink(linkId, null);
            linkRepository.save(link);

            // 根據剛取得的 streamId，查詢並回傳完整的檔案中繼資料
            return attachmentRepository.findById(streamId)
                .orElseThrow(() -> new FileStorageException("儲存後無法找到檔案中繼資料：" + fileName));

        } catch (IOException ex) {
            throw new FileStorageException("無法儲存檔案 " + fileName + "，請稍後再試！", ex);
        }
    }

    /**
     * 根據檔案的 streamId 取得檔案內容。
     * @param fileId 檔案的 stream_id
     * @return 檔案的 byte 陣列
     */
    public byte[] getFileContent(UUID fileId) {
        return attachmentRepository.findFileStreamById(fileId);
    }

    /**
     * 根據任務 ID 取得所有附件的中繼資料列表。
     * @param taskId 任務的 ID
     * @return 附件中繼資料列表
     */
    public List<TaskAttachment> getAttachmentsForTask(Long taskId) {
        return linkRepository.findById_TaskId(taskId)
                .stream()
                .map(TaskAttachmentLink::getAttachment) // 從關聯紀錄中取出對應的檔案實體
                .collect(Collectors.toList());
    }
    
    /**
     * 根據檔案的 streamId 取得單一附件的中繼資料。
     * @param fileId 檔案的 stream_id
     * @return TaskAttachment 物件
     */
    public TaskAttachment getAttachmentInfo(UUID fileId) {
        return attachmentRepository.findById(fileId).orElse(null);
    }

    /**
     * 根據檔案的 streamId 刪除檔案及其關聯。
     * @param fileId 檔案的 stream_id
     */
    public void deleteFile(UUID fileId) {
        // 因為我們在資料庫中設定了 ON DELETE CASCADE，
        // 所以只需要刪除 TaskAttachments 中的紀錄，
        // TaskAttachmentLinks 中的對應關聯就會被自動刪除。
        attachmentRepository.deleteById(fileId);
    }
}