--------------------------------------------------------------------------------
-- TaskProject �ɮפW�ǥ\�� - SQL Server ����e�m�@�~�}��
-- �����Gv4.0
-- �\��G�]�t��ҳ]�w�B��Ʈw�]�w�BFileTable �P���p������إ߬y�{�C
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
-- �Ĥ@�����G��Ҽh�ų]�w (Instance-Level Configuration)
-- *****************************************************************************
-- ** ĵ�i�G���槹���϶���A������ʭ��s�Ұ� SQL Server �A�ȡI **
-- *****************************************************************************
--------------------------------------------------------------------------------

-- �B�J 1.1: �ҥζi���ﶵ
-- �o�O���F����]�w 'filestream access level' �o�Ӷi���ﶵ�C
EXEC sp_configure 'show advanced options', 1;
RECONFIGURE;
GO

-- �B�J 1.2: �]�w FILESTREAM �s���h��
-- 2 �N��ҥ� Transact-SQL �M Win32 ��y�s���A�o�O�̧��㪺�v�����šC
EXEC sp_configure 'filestream access level', 2;
RECONFIGURE;
GO

PRINT '�Ĥ@�����w�����C�ХߧY�e���uSQL Server �պA�޲z���v��ʭ��s�Ұʱz�� SQL Server �A�ȡA�M��A�������}���C';
GO

--------------------------------------------------------------------------------
-- �ĤG�����G��Ʈw�h�ų]�w (Database-Level Configuration)
-- ** �нT�{ SQL Server �A�Ȥw���ҫ�A���榹���� **
--------------------------------------------------------------------------------

-- �B�J 2.1: ���w�n�ϥΪ���Ʈw
USE [taskdb];
GO

-- �B�J 2.2: ����Ʈw�s�W FILESTREAM �ɮ׸s��
ALTER DATABASE [taskdb]
ADD FILEGROUP [FileStreamGroup] CONTAINS FILESTREAM;
GO

-- �B�J 2.3: ���ɮ׸s�շs�W�����������ɮרt�Υؿ�
-- �`�N�G'D:\8_database' �o�Ӹ��|�����w�s�b�C
ALTER DATABASE [taskdb]
ADD FILE (
    NAME = N'TaskProjectFileStream',
    FILENAME = N'D:\8_database\FileStreamData' -- �ϥΪ̴��Ѫ����|
)
TO FILEGROUP [FileStreamGroup];
GO

-- �B�J 2.4: �]�w��Ʈw�h�Ū� FILESTREAM �ڥؿ��W��
-- �o�O�إ� FileTable �����n�e�m�B�J�C
ALTER DATABASE [taskdb]
SET FILESTREAM ( DIRECTORY_NAME = N'TaskProjectFiles' );
GO

--------------------------------------------------------------------------------
-- �ĤT�����G����إ� (Object Creation)
--------------------------------------------------------------------------------

-- �B�J 3.1: (�w���_��) �M�z�¦�����ƪ�
-- �T�O�}���i�H���ư���C�������R�������p�� TaskAttachmentLinks�C
IF OBJECT_ID('dbo.TaskAttachmentLinks', 'U') IS NOT NULL
   DROP TABLE dbo.TaskAttachmentLinks;
GO
IF OBJECT_ID('dbo.TaskAttachments', 'U') IS NOT NULL
   DROP TABLE dbo.TaskAttachments;
GO

-- �B�J 3.2: �إ� FileTable (�ɮ��x�s��)
-- ����ȥΩ��x�s�ɮ׹���P�t���ݩʡC
CREATE TABLE dbo.TaskAttachments AS FILETABLE
WITH
(
    FileTable_Directory = 'TaskAttachments',
    FileTable_Collate_Filename = database_default
);
GO

-- �B�J 3.3: �إ� Linking Table (���p��)
-- ����Ω�إߡu�ɮסv�P�u���ȡv���������p�C
CREATE TABLE dbo.TaskAttachmentLinks (
    AttachmentStreamId UNIQUEIDENTIFIER NOT NULL, -- �������ɮת� stream_id
    TaskId BIGINT NOT NULL,                         -- ��������Ȫ� id
    
    PRIMARY KEY (AttachmentStreamId, TaskId), -- �ƦX�D��A��������p

    -- �~��������A�T�O��Ƨ����
    CONSTRAINT FK_AttachmentLinks_To_Attachments FOREIGN KEY (AttachmentStreamId) 
        REFERENCES dbo.TaskAttachments(stream_id)
        ON DELETE CASCADE, -- ���ɮ׳Q�R���ɡA���p�����]�@�֧R��

    CONSTRAINT FK_AttachmentLinks_To_Tasks FOREIGN KEY (TaskId) 
        REFERENCES dbo.Tasks(id)
        ON DELETE CASCADE -- ����ȳQ�R���ɡA��������p�����]�@�֧R��
);
GO

--------------------------------------------------------------------------------
-- �ĥ|�����G����
--------------------------------------------------------------------------------
PRINT '�Ҧ���Ʈw�]�w�P��ƪ�إߤw���\�����I';
GO