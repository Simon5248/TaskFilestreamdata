package com.taskproject.attachmentapi.service;

import com.taskproject.attachmentapi.exception.FileStorageException;
import com.taskproject.attachmentapi.model.TaskAttachment;
import com.taskproject.attachmentapi.model.TaskAttachmentLink;
import com.taskproject.attachmentapi.model.TaskAttachmentLinkId;
import com.taskproject.attachmentapi.repository.TaskAttachmentLinkRepository;
import com.taskproject.attachmentapi.repository.TaskAttachmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FileStorageServiceTest {

    private TaskAttachmentRepository attachmentRepository;
    private TaskAttachmentLinkRepository linkRepository;
    private JdbcTemplate jdbcTemplate;
    private FileStorageService fileStorageService;

    @BeforeEach
    void setUp() {
        attachmentRepository = mock(TaskAttachmentRepository.class);
        linkRepository = mock(TaskAttachmentLinkRepository.class);
        jdbcTemplate = mock(JdbcTemplate.class);
        fileStorageService = new FileStorageService(attachmentRepository, linkRepository, jdbcTemplate);
    }

    @Test
    void storeFile_success() throws IOException {
        // Arrange
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("test.txt");
        when(file.getBytes()).thenReturn("hello".getBytes());

        UUID streamId = UUID.randomUUID();
        when(jdbcTemplate.queryForObject(anyString(), any(Object[].class), eq(UUID.class))).thenReturn(streamId);

        TaskAttachment attachment = new TaskAttachment();
        when(attachmentRepository.findById(streamId)).thenReturn(Optional.of(attachment));

        // Act
        TaskAttachment result = fileStorageService.storeFile(file, 123L);

        // Assert
        assertEquals(attachment, result);
        verify(linkRepository).save(any(TaskAttachmentLink.class));
    }

    @Test
    void storeFile_invalidFileName() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("../evil.txt");

        FileStorageException ex = assertThrows(FileStorageException.class,
                () -> fileStorageService.storeFile(file, 123L));
        assertTrue(ex.getMessage().contains("檔名包含無效的路徑序列"));
    }
}