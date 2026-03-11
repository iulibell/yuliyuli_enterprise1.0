package com.yuliyuli;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.yuliyuli.mapper")
@EnableScheduling
public class YuliyuliEnterpriseApplication {
  public static void main(String[] args) {
    SpringApplication.run(YuliyuliEnterpriseApplication.class, args);
  }
}
