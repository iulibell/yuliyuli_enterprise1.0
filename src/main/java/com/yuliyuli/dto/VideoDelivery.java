package com.yuliyuli.dto;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class VideoDelivery {
    private MultipartFile file;
    private Video video;
}
