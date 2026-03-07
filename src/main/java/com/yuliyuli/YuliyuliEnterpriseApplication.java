package com.yuliyuli;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan("com.yuliyuli.mapper")
@EnableScheduling
public class YuliyuliEnterpriseApplication {
    public static void main(String[] args) {
        SpringApplication.run(YuliyuliEnterpriseApplication.class, args);
    }
}
