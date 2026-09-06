package com.ecommerce.project.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FIleServiceImpl implements FileService{

    @Override
    public String uploadImage(String path, MultipartFile file) throws IOException {
        // file names of current / original file
        String originalFileName = file.getOriginalFilename();
        // generate unique file name
        String randomId = UUID.randomUUID().toString();
        String filename = randomId.concat(originalFileName.substring(originalFileName.lastIndexOf('.')));
        String filePath = path + File.separator + filename;


        // check if path exist and create
        File folder = new File(path);
        if(!folder.exists())
            folder.mkdir();


        // upload to server
        Files.copy(file.getInputStream(), Paths.get(filePath));
        // return file name
        return filename;

    }
}
