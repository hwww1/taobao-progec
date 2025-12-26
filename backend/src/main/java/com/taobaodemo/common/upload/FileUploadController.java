package com.taobaodemo.common.upload;

import com.taobaodemo.common.util.ResponseUtil;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * 文件上传控制器
 * 处理图片上传功能
 */
@RestController
@RequestMapping("/api/upload")
public class FileUploadController {
    private static final String UPLOAD_DIR = "uploads/images/products";

    /**
     * 上传图片文件
     */
    @PostMapping("/image")
    public Object uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                return ResponseUtil.error("文件不能为空");
            }

            // 简单验证：只检查文件大小（5MB）
            if (file.getSize() > 5 * 1024 * 1024) {
                return ResponseUtil.error("文件大小不能超过5MB");
            }

            // 创建上传目录（兼容Windows和Linux）
            String basePath = System.getProperty("user.dir");
            String uploadPath = basePath + File.separator + UPLOAD_DIR.replace("/", File.separator);
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                boolean created = uploadDir.mkdirs();
                if (!created) {
                    return ResponseUtil.error("无法创建上传目录，请检查权限");
                }
            }
            // 确保目录可写
            if (!uploadDir.canWrite()) {
                return ResponseUtil.error("上传目录不可写，请检查权限");
            }

            // 生成唯一文件名并保存
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                : ".jpg";
            String uniqueFileName = UUID.randomUUID().toString() + extension;
            Files.copy(file.getInputStream(), Paths.get(uploadPath, uniqueFileName));

            // 返回文件路径
            String filePath = "/uploads/images/products/" + uniqueFileName;
            return ResponseUtil.success(filePath);
        } catch (Exception e) {
            return ResponseUtil.error("上传失败");
        }
    }
}

