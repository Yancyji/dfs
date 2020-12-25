package com.dfs.util;

import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.domain.proto.storage.DownloadByteArray;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author 13698
 */
@Component
public class FileDfsUtil {

    /**
    * 下载保存路径
    * */
    @Value("${lee.save}")
    private String save;


    private static final Logger LOGGER = LoggerFactory.getLogger(FileDfsUtil.class);

    @Resource
    private FastFileStorageClient storageClient ;
    /**
      * 上传文件
      */
    public String upload(MultipartFile file) throws Exception{

        StorePath storePath = storageClient.uploadFile(file.getInputStream(),file.getSize(), FilenameUtils.getExtension(file.getOriginalFilename()),null);
        return storePath.getFullPath() ;
    }
     /**
     * 删除文件
      */
    public void deleteFile(String fileUrl) {
        if (StringUtils.isEmpty(fileUrl)) {
            LOGGER.info("fileUrl == >>文件路径为空...");
             return;
        }
       try {
            StorePath storePath = StorePath.parseFromUrl(fileUrl);
            storageClient.deleteFile(storePath.getGroup(), storePath.getPath());
       } catch (Exception e) {
            LOGGER.info(e.getMessage());
       }
    }

    public byte[] downloadFile(String fileUrl){
        String group = fileUrl.substring(0, fileUrl.indexOf("/"));
        String path = fileUrl.substring(fileUrl.indexOf("/") + 1);
        DownloadByteArray downloadByteArray = new DownloadByteArray();
        byte[] bytes = this.storageClient.downloadFile(group, path, downloadByteArray);
        try {
            //将文件保存到d盘
            FileOutputStream fileOutputStream = new FileOutputStream(save);
            fileOutputStream.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytes;
    }
}
