package com.abao.gmall.manageweb.controller;


import org.apache.commons.lang3.StringUtils;
import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@CrossOrigin
public class FileUploadController {


    @Value("${fileServer.url}")
    String fileUrl;



    @RequestMapping("fileUpload")
    public String fileUpload(@RequestParam("file") MultipartFile file) throws IOException, MyException {

        String retUrl = fileUrl;

        if(file != null){

            String conf = this.getClass().getResource("/tracker.conf").getFile();
            ClientGlobal.init(conf);

            TrackerClient trackerClient=new TrackerClient();
            TrackerServer trackerServer=trackerClient.getConnection();
            StorageClient storageClient=new StorageClient(trackerServer,null);

            String orginalFilename=file.getOriginalFilename();

            String[] upload_file = storageClient.upload_file(file.getBytes(), StringUtils.substringAfterLast(orginalFilename,"."),null);


            for (int i = 0; i < upload_file.length; i++) {
                String s = upload_file[i];
                retUrl += "/" + s;
            }



        }

        System.out.println("retUrl = " + retUrl);

        return retUrl;
    }




}
