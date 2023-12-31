package com.sky.admin.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Hello world!
 *
 */
@SpringBootApplication
@EnableDiscoveryClient
public class AdminGatewayApplication
{
    public static void main( String[] args )
    {
        SpringApplication.run(AdminGatewayApplication.class,args);
    }
}
