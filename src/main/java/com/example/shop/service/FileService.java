package com.example.shop.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.util.UUID;

@Service
@Slf4j
public class FileService {
    
    public String uploadFile(String uploadPath, String originalFilename, 
                             byte[] fileData) throws Exception {
        UUID uuid = UUID.randomUUID(); // 중복 방지를 위한 파일명 생성

        // sampletest.jpg
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));

        log.info("originalFileName.substring : {}", extension);

        String savedFileName = uuid.toString() + extension;

        String fileUploadPath = uploadPath + "/" + savedFileName;

        FileOutputStream fos = new FileOutputStream(fileUploadPath);
        fos.write(fileData);
        fos.close();
        return savedFileName;
    }

    public void deleteFile(String filePath) throws Exception {

        File deletedFile = new File(filePath);

        if(deletedFile.exists()) {
            deletedFile.delete();
            log.info("파일을 삭제하였습니다. : {}", filePath);
        }else{
            log.info("파일이 존재하지 않습니다. : {}", filePath);
        }
    }
}
