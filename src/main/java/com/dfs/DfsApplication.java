package com.dfs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @author 13698
 */
@SpringBootApplication
@EnableSwagger2
public class DfsApplication {

    public static void main(String[] args) {
        SpringApplication.run(DfsApplication.class, args);
    }

}
