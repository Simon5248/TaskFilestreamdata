// --- 檔案：TaskAttachment.java ---
// 對應到 FileTable: dbo.TaskAttachments
// 這個類別只用來讀取檔案的中繼資料(metadata)，例如檔名和大小。

package com.taskproject.attachmentapi.model;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "TaskAttachments")
@Data // Lombok: 自動產生 getter, setter, toString 等方法
public class TaskAttachment {

    /**
     * 檔案的唯一識別碼，對應 FileTable 的 stream_id。
     * 這是由 SQL Server 自動產生的 GUID，我們設定為不可更新。
     */
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "stream_id", updatable = false, nullable = false)
    private UUID streamId;

    /**
     * 檔案名稱，對應 FileTable 的 name。
     */
    @Column(name = "name")
    private String name;

    /**
     * 檔案類型 (副檔名)，對應 FileTable 的 file_type。
     */
    @Column(name = "file_type")
    private String fileType;

    /**
     * 檔案大小 (bytes)，對應 FileTable 的 cached_file_size。
     */
    @Column(name = "cached_file_size")
    private Long size;

    // 我們不直接對應 file_stream (檔案內容) 欄位，因為它可能很大。
    // 後續我們將透過特定的原生 SQL 查詢來讀寫檔案內容，以獲得更好的效能。
}