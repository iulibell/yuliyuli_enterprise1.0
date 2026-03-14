package com.yuliyuli.util;

import com.yuliyuli.entity.VideoDelivery;
import com.yuliyuli.exception.GlobalExceptionHandler.BusinessException;
import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/** 转换工具类 */
@Component
@Slf4j
public class TransferUtil {

    /**
     * 视频上传目录
     */
    @Value("${video.upload.videopath}")
    private String TARGETDIR;

    /**
     * 封面上传目录
     */
    @Value("${video.upload.coverPath}")
    private String COVERDIR;

    // 允许的视频格式
    private static final String[] ALLOWED_VIDEO_EXTENSIONS = {"mp4", "flv", "avi", "mov"};
    // 最大文件大小：100MB
    private static final long MAX_VIDEO_SIZE = 100 * 1024 * 1024;

    @PostConstruct
    public void init() {
        // 初始化就创建目录
        createDirectoryIfNotExists();
    }

    private void createDirectoryIfNotExists() {
        File directory = new File(TARGETDIR);
        if (!directory.exists()) {
            boolean success = directory.mkdirs();
            if (!success) {
                log.error("创建视频上传目录失败: {}", TARGETDIR);
                throw new BusinessException("视频上传目录创建失败");
            }
        }
    }

    /**
     * 处理头像路径，确保路径格式正确
     */
    public String processAvatarPath(String avatarPath) {
        if (avatarPath == null || avatarPath.isEmpty()) {
            return avatarPath;
        }
        avatarPath = avatarPath.replace("\\", "/");
        if (avatarPath.startsWith("./") || avatarPath.startsWith("../")) {
            avatarPath = avatarPath.replaceFirst("[./]+", "");
        }
        return avatarPath.startsWith("/") ? avatarPath : "/" + avatarPath;
    }

    /**
     * 保存MultipartFile（你上传视频真正用的方法！）
     */
    public String saveMultipartFile(MultipartFile file) {
        return saveMultipartFile(file, TARGETDIR);
    }

    /**
     * 保存MultipartFile到指定目录
     */
    public String saveMultipartFile(MultipartFile file, String directory) {
        if (file == null || file.isEmpty()) {
            log.error("文件为空");
            throw new BusinessException("文件不能为空");
        }

        // 大小检查
        if (file.getSize() > MAX_VIDEO_SIZE) {
            throw new BusinessException("文件超过100MB限制");
        }

        // 后缀
        String originalFilename = file.getOriginalFilename();
        String ext = getFileExtension(originalFilename);

        // 唯一文件名
        String fileName = UUID.randomUUID() + "_" + System.currentTimeMillis();
        if (StringUtils.hasText(ext)) {
            fileName += "." + ext;
        }

        // 确保目录存在
        File dir = new File(directory);
        if (!dir.exists()) {
            boolean success = dir.mkdirs();
            if (!success) {
                log.error("创建上传目录失败: {}", directory);
                throw new BusinessException("上传目录创建失败");
            }
        }

        // 绝对路径目标文件
        File targetFile = new File(directory, fileName);

        try {
            // 直接保存（绝对路径，不会被Tomcat带偏）
            file.transferTo(targetFile);
            log.info("文件保存成功: {}", targetFile.getAbsolutePath());
            return targetFile.getAbsolutePath(); // 返回绝对路径
        } catch (IOException e) {
            log.error("文件保存失败", e);
            throw new BusinessException("文件保存失败");
        }
    }

    /**
     * 旧方法（保留兼容）
     */
    public String saveVideoToDirectory(VideoDelivery videoDilivery) {
        if (videoDilivery == null || videoDilivery.getVideoFile() == null) {
            throw new BusinessException("视频文件不能为空");
        }
        if (!(videoDilivery.getVideoFile() instanceof File)) {
            throw new BusinessException("文件类型不支持");
        }

        File videoFile = (File) videoDilivery.getVideoFile();
        String videoUrl = videoDilivery.getVideo().getUrl();

        if (!videoFile.exists()) throw new BusinessException("视频文件不存在");
        if (videoFile.length() > MAX_VIDEO_SIZE) throw new BusinessException("视频超过100MB");

        String extension = getFileExtension(videoUrl);
        if (!isAllowedExtension(extension)) throw new BusinessException("不支持的格式：" + extension);

        String fileName = System.currentTimeMillis() + "_" + videoUrl;
        File targetFile = new File(TARGETDIR, fileName);

        try {
            Files.copy(videoFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return targetFile.getAbsolutePath();
        } catch (Exception e) {
            log.error("保存视频失败", e);
            throw new BusinessException("视频保存失败");
        }
    }

    private String getFileExtension(String fileName) {
        if (!StringUtils.hasText(fileName) || !fileName.contains(".")) return "";
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }

    private boolean isAllowedExtension(String extension) {
        for (String allowed : ALLOWED_VIDEO_EXTENSIONS) {
            if (allowed.equals(extension)) return true;
        }
        return false;
    }
}
