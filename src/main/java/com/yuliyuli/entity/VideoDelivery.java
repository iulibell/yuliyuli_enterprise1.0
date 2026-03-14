package com.yuliyuli.entity;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class VideoDelivery {
  @Parameter(name = "视频文件")
  private MultipartFile videoFile;

  @Parameter(name = "视频封面文件")
  private MultipartFile coverFile;

  @Parameter(name = "视频文件路径")
  private String videoPath;

  @Parameter(name = "视频封面路径")
  private String coverPath;

  @Parameter(name = "视频信息")
  private Video video;
}
