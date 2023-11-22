package com.upc.demo;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;


@EnableEurekaClient
@SpringBootApplication
public class DemoApplication {


    private static Logger log = LoggerFactory.getLogger(DemoApplication.class);




    public static void main(String[] args) {

        SpringApplication.run(DemoApplication.class, args);
    }




}
