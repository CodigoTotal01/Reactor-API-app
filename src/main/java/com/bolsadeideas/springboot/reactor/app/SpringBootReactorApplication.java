package com.bolsadeideas.springboot.reactor.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
//Clase de arranque - las configuraciones que realicemos en el pom afectaran mucho al programa
@SpringBootApplication
public class SpringBootReactorApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootReactorApplication.class, args);
    }

}
