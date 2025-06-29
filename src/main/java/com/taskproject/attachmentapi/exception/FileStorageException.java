// --- 檔案：exception/FileStorageException.java ---
// 一個自訂的 RuntimeException，專門用於檔案儲存相關的錯誤。

package com.taskproject.attachmentapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) // 如果此例外未被捕獲，預設回傳 500 錯誤
public class FileStorageException extends RuntimeException {

    public FileStorageException(String message) {
        super(message);
    }

    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}