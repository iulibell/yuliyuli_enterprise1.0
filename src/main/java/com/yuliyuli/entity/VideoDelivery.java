package com.yuliyuli.entity;

import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.Data;

@Data
public class VideoDelivery {
    @Parameter(name = "视频文件")
    private MultipartFile file;
    @Parameter(name = "视频信息")
    private Video video;
}
