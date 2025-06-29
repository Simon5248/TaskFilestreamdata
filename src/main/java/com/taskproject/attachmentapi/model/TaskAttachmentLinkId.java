// --- 檔案：TaskAttachmentLinkId.java ---
// 這是一個「嵌入式」類別，用來定義 TaskAttachmentLink 的複合主鍵。

package com.taskproject.attachmentapi.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.UUID;

@Embeddable // 表示這個類別可以被嵌入到另一個 Entity 中。
@Data
@NoArgsConstructor // JPA 要求複合主鍵類別必須有「無參數建構子」。
@AllArgsConstructor // 方便我們建立物件的「全參數建構子」。
public class TaskAttachmentLinkId implements Serializable {

    // 複合主鍵的兩個組成部分
    private UUID attachmentStreamId; // 對應到檔案的 ID
    private Long taskId;             // 對應到任務的 ID

    // JPA 要求複合主鍵類別必須覆寫 equals() 和 hashCode() 方法。
    // Lombok 的 @Data 註解已自動為我們產生這些必要的方法。
}