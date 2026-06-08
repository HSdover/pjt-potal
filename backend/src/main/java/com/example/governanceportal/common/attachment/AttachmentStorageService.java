package com.example.governanceportal.common.attachment;

import com.example.governanceportal.common.error.BusinessException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Properties;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AttachmentStorageService {

    private static final Pattern ATTACHMENT_ID_PATTERN = Pattern.compile("^[0-9a-fA-F-]{36}$");
    private static final String DEFAULT_FILENAME = "attachment";
    private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

    private final AttachmentStorageProperties properties;

    public AttachmentStorageService(AttachmentStorageProperties properties) {
        this.properties = properties;
    }

    public StoredAttachment store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw BusinessException.badRequest("Attachment file is empty.");
        }

        String attachmentId = UUID.randomUUID().toString();
        String fileName = safeFilename(file.getOriginalFilename());
        String contentType = StringUtils.hasText(file.getContentType()) ? file.getContentType() : DEFAULT_CONTENT_TYPE;
        long size = file.getSize();
        Path filePath = filePath(attachmentId);
        Path metadataPath = metadataPath(attachmentId);

        try {
            Files.createDirectories(storageRoot());
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            }
            writeMetadata(metadataPath, fileName, contentType, size);
            return new StoredAttachment(attachmentId, fileName, contentType, size, filePath);
        } catch (IOException error) {
            cleanupQuietly(filePath);
            cleanupQuietly(metadataPath);
            throw BusinessException.internal("Could not store attachment file.");
        }
    }

    public StoredAttachment findById(String attachmentId) {
        validateAttachmentId(attachmentId);

        Path filePath = filePath(attachmentId);
        Path metadataPath = metadataPath(attachmentId);
        if (!Files.isRegularFile(filePath) || !Files.isRegularFile(metadataPath)) {
            throw BusinessException.notFound("Attachment file was not found.");
        }

        Properties metadata = readMetadata(metadataPath);
        String fileName = metadata.getProperty("fileName", DEFAULT_FILENAME);
        String contentType = metadata.getProperty("contentType", DEFAULT_CONTENT_TYPE);
        long size = parseSize(metadata.getProperty("size"), filePath);
        return new StoredAttachment(attachmentId, fileName, contentType, size, filePath);
    }

    public Path storageRoot() {
        return properties.storageRoot();
    }

    private void writeMetadata(Path metadataPath, String fileName, String contentType, long size) throws IOException {
        Properties metadata = new Properties();
        metadata.setProperty("fileName", fileName);
        metadata.setProperty("contentType", contentType);
        metadata.setProperty("size", String.valueOf(size));

        try (OutputStream outputStream = Files.newOutputStream(metadataPath)) {
            metadata.store(outputStream, "Governance Portal attachment metadata");
        }
    }

    private Properties readMetadata(Path metadataPath) {
        Properties metadata = new Properties();
        try (InputStream inputStream = Files.newInputStream(metadataPath)) {
            metadata.load(inputStream);
            return metadata;
        } catch (IOException error) {
            throw BusinessException.internal("Could not read attachment metadata.");
        }
    }

    private long parseSize(String value, Path filePath) {
        try {
            return Long.parseLong(value);
        } catch (Exception error) {
            try {
                return Files.size(filePath);
            } catch (IOException sizeError) {
                return -1;
            }
        }
    }

    private Path filePath(String attachmentId) {
        return resolveStoragePath(attachmentId + ".bin");
    }

    private Path metadataPath(String attachmentId) {
        return resolveStoragePath(attachmentId + ".properties");
    }

    private Path resolveStoragePath(String filename) {
        Path root = storageRoot();
        Path resolved = root.resolve(filename).normalize();
        if (!resolved.startsWith(root)) {
            throw BusinessException.badRequest("Invalid attachment path.");
        }
        return resolved;
    }

    private void validateAttachmentId(String attachmentId) {
        if (!StringUtils.hasText(attachmentId) || !ATTACHMENT_ID_PATTERN.matcher(attachmentId).matches()) {
            throw BusinessException.badRequest("Invalid attachment id.");
        }
    }

    private String safeFilename(String filename) {
        if (!StringUtils.hasText(filename)) {
            return DEFAULT_FILENAME;
        }

        String normalized = filename.replace('\\', '/');
        String basename = normalized.substring(normalized.lastIndexOf('/') + 1).trim();
        String sanitized = basename.replaceAll("[\\p{Cntrl}\"]", "_");
        return StringUtils.hasText(sanitized) ? sanitized : DEFAULT_FILENAME;
    }

    private void cleanupQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
            // Best-effort cleanup only.
        }
    }
}
