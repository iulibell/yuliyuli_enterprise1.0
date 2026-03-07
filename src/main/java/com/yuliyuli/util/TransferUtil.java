package com.yuliyuli.util;

import lombok.extern.slf4j.Slf4j;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.yuliyuli.entity.VideoDelivery;
import com.yuliyuli.exception.GlobalExceptionHandler.BusinessException;

import jakarta.annotation.PostConstruct;

/**
 * 转换工具类
 */
@Slf4j
@Component
public class TransferUtil {

    @Value("${video.upload.path:./static/videoUrl}")
    private String TARGETDIR;

    // 允许的视频格式
    private static final String[] ALLOWED_VIDEO_EXTENSIONS = {"mp4", "flv", "avi", "mov"};
    // 最大文件大小：100MB
    private static final long MAX_VIDEO_SIZE = 100 * 1024 * 1024;

    @PostConstruct
    public void init() {
        // 确保目标目录存在
        File directory = new File(TARGETDIR);
        if (!directory.exists() && !directory.mkdirs()) {
            log.error("创建视频上传目录失败: {}", TARGETDIR);
            throw new BusinessException("视频上传目录创建失败");
        }
    }

    /**
     * 处理头像路径，确保路径格式正确
     * @param avatarPath 原始头像路径
     * @return 处理后的头像路径
     */
    public String processAvatarPath(String avatarPath) {
        if (avatarPath != null && !avatarPath.isEmpty()) {
            avatarPath = avatarPath.replace("\\", "/");
            if (avatarPath.startsWith(".\\") || avatarPath.startsWith("./")) {
                avatarPath = avatarPath.substring(2);
            }
            if (!avatarPath.startsWith("/")) {
                avatarPath = "/" + avatarPath;
            }
        }
        return avatarPath;
    }

    /**
     * 将视频存到指定目录
     * @param videoFile 视频文件
     * @param targetDir 目标目录
     * @return 保存后的视频文件路径
     */
    public String saveVideoToDirectory(VideoDelivery videoDilivery) {
        // 1. 参数校验
        if (videoDilivery == null || videoDilivery.getFile() == null) {
            log.error("视频文件为空");
            throw new BusinessException("视频文件不能为空");
        }
        if (!(videoDilivery.getFile() instanceof File)) {
            log.error("文件类型错误,仅支持本地File类型");
            throw new BusinessException("文件类型不支持");
        }

        File videoFile = (File) videoDilivery.getFile();
        String videoUrl = videoDilivery.getVideo().getUrl();

        // 2. 文件存在性+大小+格式校验
        if (!videoFile.exists()) {
            throw new BusinessException("视频文件不存在");
        }
        if (videoFile.length() > MAX_VIDEO_SIZE) {
            throw new BusinessException("视频文件超过100MB限制");
        }
        String extension = getFileExtension(videoUrl);
        if (!isAllowedExtension(extension)) {
            throw new BusinessException("不支持的视频格式：" + extension);
        }

        // 3. 创建目录（仅初始化一次，优化性能）
        File directory = new File(TARGETDIR);

        // 4. 生成唯一文件名（避免覆盖）
        String fileName = System.currentTimeMillis() + "_" + videoUrl;
        File targetFile = new File(directory, fileName);

        // 5. 保存文件
        try {
            Files.copy(videoFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            log.info("视频文件保存成功: {}", targetFile.getAbsolutePath());
            return targetFile.getAbsolutePath();
        } catch (Exception e) {
            log.error("保存视频文件失败", e);
            throw new BusinessException("视频文件保存失败");
        }
    }

    // 辅助方法：获取文件扩展名
    private String getFileExtension(String fileName) {
        if (!StringUtils.hasText(fileName) || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }

    // 辅助方法：校验文件格式
    private boolean isAllowedExtension(String extension) {
        for (String allowed : ALLOWED_VIDEO_EXTENSIONS) {
            if (allowed.equals(extension)) {
                return true;
            }
        }
        return false;
    }

}
