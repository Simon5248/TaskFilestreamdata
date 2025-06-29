--------------------------------------------------------------------------------
-- TaskProject 檔案上傳功能 - SQL Server 完整前置作業腳本
-- 版本：v4.0
-- 功能：包含實例設定、資料庫設定、FileTable 與關聯表的完整建立流程。
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
-- 第一部分：實例層級設定 (Instance-Level Configuration)
-- *****************************************************************************
-- ** 警告：執行完此區塊後，必須手動重新啟動 SQL Server 服務！ **
-- *****************************************************************************
--------------------------------------------------------------------------------

-- 步驟 1.1: 啟用進階選項
-- 這是為了能夠設定 'filestream access level' 這個進階選項。
EXEC sp_configure 'show advanced options', 1;
RECONFIGURE;
GO

-- 步驟 1.2: 設定 FILESTREAM 存取層級
-- 2 代表啟用 Transact-SQL 和 Win32 串流存取，這是最完整的權限等級。
EXEC sp_configure 'filestream access level', 2;
RECONFIGURE;
GO

PRINT '第一部分已完成。請立即前往「SQL Server 組態管理員」手動重新啟動您的 SQL Server 服務，然後再執行後續腳本。';
GO

--------------------------------------------------------------------------------
-- 第二部分：資料庫層級設定 (Database-Level Configuration)
-- ** 請確認 SQL Server 服務已重啟後再執行此部分 **
--------------------------------------------------------------------------------

-- 步驟 2.1: 指定要使用的資料庫
USE [taskdb];
GO

-- 步驟 2.2: 為資料庫新增 FILESTREAM 檔案群組
ALTER DATABASE [taskdb]
ADD FILEGROUP [FileStreamGroup] CONTAINS FILESTREAM;
GO

-- 步驟 2.3: 為檔案群組新增對應的實體檔案系統目錄
-- 注意：'D:\8_database' 這個路徑必須已存在。
ALTER DATABASE [taskdb]
ADD FILE (
    NAME = N'TaskProjectFileStream',
    FILENAME = N'D:\8_database\FileStreamData' -- 使用者提供的路徑
)
TO FILEGROUP [FileStreamGroup];
GO

-- 步驟 2.4: 設定資料庫層級的 FILESTREAM 根目錄名稱
-- 這是建立 FileTable 的必要前置步驟。
ALTER DATABASE [taskdb]
SET FILESTREAM ( DIRECTORY_NAME = N'TaskProjectFiles' );
GO

--------------------------------------------------------------------------------
-- 第三部分：物件建立 (Object Creation)
--------------------------------------------------------------------------------

-- 步驟 3.1: (安全起見) 清理舊有的資料表
-- 確保腳本可以重複執行。必須先刪除有關聯的 TaskAttachmentLinks。
IF OBJECT_ID('dbo.TaskAttachmentLinks', 'U') IS NOT NULL
   DROP TABLE dbo.TaskAttachmentLinks;
GO
IF OBJECT_ID('dbo.TaskAttachments', 'U') IS NOT NULL
   DROP TABLE dbo.TaskAttachments;
GO

-- 步驟 3.2: 建立 FileTable (檔案儲存表)
-- 此表僅用於儲存檔案實體與系統屬性。
CREATE TABLE dbo.TaskAttachments AS FILETABLE
WITH
(
    FileTable_Directory = 'TaskAttachments',
    FileTable_Collate_Filename = database_default
);
GO

-- 步驟 3.3: 建立 Linking Table (關聯表)
-- 此表用於建立「檔案」與「任務」之間的關聯。
CREATE TABLE dbo.TaskAttachmentLinks (
    AttachmentStreamId UNIQUEIDENTIFIER NOT NULL, -- 對應到檔案的 stream_id
    TaskId BIGINT NOT NULL,                         -- 對應到任務的 id
    
    PRIMARY KEY (AttachmentStreamId, TaskId), -- 複合主鍵，防止重複關聯

    -- 外部索引鍵，確保資料完整性
    CONSTRAINT FK_AttachmentLinks_To_Attachments FOREIGN KEY (AttachmentStreamId) 
        REFERENCES dbo.TaskAttachments(stream_id)
        ON DELETE CASCADE, -- 當檔案被刪除時，關聯紀錄也一併刪除

    CONSTRAINT FK_AttachmentLinks_To_Tasks FOREIGN KEY (TaskId) 
        REFERENCES dbo.Tasks(id)
        ON DELETE CASCADE -- 當任務被刪除時，其附件的關聯紀錄也一併刪除
);
GO

--------------------------------------------------------------------------------
-- 第四部分：完成
--------------------------------------------------------------------------------
PRINT '所有資料庫設定與資料表建立已成功完成！';
GO