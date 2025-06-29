// --- 檔案：TaskAttachmentLink.java ---
// 對應到我們的關聯表: dbo.TaskAttachmentLinks

package com.taskproject.attachmentapi.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "TaskAttachmentLinks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskAttachmentLink {

    /**
     * 使用 @EmbeddedId 來指定這個 Entity 使用我們剛定義的複合主鍵類別。
     */
    @EmbeddedId
    private TaskAttachmentLinkId id;

    /**
     * 建立與 TaskAttachment 的多對一關聯。
     * 這讓我們可以透過關聯紀錄，輕易地找到對應的檔案實體資訊。
     *
     * FetchType.LAZY: 表示只有在我們明確需要檔案資訊時，JPA 才會去資料庫查詢。
     * insertable = false, updatable = false: 
     * 這告訴 JPA，這個關聯僅用於「讀取」。我們不會透過這個 attachment 物件
     * 去新增或修改 attachmentStreamId，而是直接操作上面的 id 物件。
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attachmentStreamId", referencedColumnName = "stream_id", insertable = false, updatable = false)
    private TaskAttachment attachment;

}