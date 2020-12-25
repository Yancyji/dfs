package com.dfs.controller;

import com.dfs.util.FileDfsUtil;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

/**
 * @author 13698
 */
@RestController
public class FileController {


    /**
     *
     * 上传保存路径
    * */
    @Value("${lee.path}")
    private String path;

     @Resource
     private FileDfsUtil fileDfsUtil ;
     /**
     * 文件上传
      */
     @ApiOperation(value="上传文件", notes="测试FastDFS文件上传")
     @RequestMapping(value = "/uploadFile",headers="content-type=multipart/form-data", method = RequestMethod.POST)
     public ResponseEntity<String> uploadFile (@RequestParam("file") MultipartFile file){
         String result ;
          try{
               String path = fileDfsUtil.upload(file) ;
                if (!StringUtils.isEmpty(path)){
                       result = path ;
                } else {
                      result = "上传失败" ;
                }
          } catch (Exception e){
               e.printStackTrace() ;
                result = "服务异常" ;
          }
          return ResponseEntity.ok(result);
     }
     /**
     * 文件删除
     */
     @RequestMapping(value = "/deleteByPath", method = RequestMethod.GET)
     public ResponseEntity<String> deleteByPath (){
          String filePathName = "group1/M00/00/00/wKhIgl0n4AKABxQEABhlMYw_3Lo825.png" ;fileDfsUtil.deleteFile(filePathName);
          return ResponseEntity.ok("SUCCESS") ;
     }

    @ApiOperation(value="下载文件", notes="测试FastDFS文件下载")
    @GetMapping("/downloadFile")
    public ResponseEntity<String> downloadFile(String name){

        //String g = "group1/M00/00/00/wKiFg13Plc6AVz_hAB6IzmlNPeY944.png";
        StringBuilder dir = new StringBuilder();
        dir.append(path).append(name);
        byte[] bytes = fileDfsUtil.downloadFile(dir.toString());
        System.out.println(bytes);
        return ResponseEntity.ok("SUCCESS");
    }
 }
