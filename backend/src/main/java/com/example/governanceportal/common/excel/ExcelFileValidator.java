package com.example.governanceportal.common.excel;

import com.example.governanceportal.common.error.BusinessException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class ExcelFileValidator {

    public void validateXlsx(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw BusinessException.badRequest("Excel upload file is empty.");
        }

        String filename = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        if (!filename.toLowerCase(Locale.ROOT).endsWith(".xlsx")) {
            throw BusinessException.badRequest("Only .xlsx Excel files are allowed.");
        }

        try (InputStream inputStream = file.getInputStream()) {
            byte[] signature = inputStream.readNBytes(4);
            if (signature.length < 4 || signature[0] != 'P' || signature[1] != 'K') {
                throw BusinessException.badRequest("Invalid .xlsx file signature.");
            }
        } catch (IOException error) {
            throw new BusinessException(
                org.springframework.http.HttpStatus.BAD_REQUEST,
                com.example.governanceportal.common.error.ErrorCode.BAD_REQUEST,
                "Could not read Excel upload file.",
                error
            );
        }
    }
}
