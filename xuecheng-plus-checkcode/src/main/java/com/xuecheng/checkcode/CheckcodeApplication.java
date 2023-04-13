package com.xuecheng.checkcode;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class CheckcodeApplication {

    public static void main(String[] args) {
        SpringApplication.run(CheckcodeApplication.class, args);
    }

}
