package uk.ac.surrey.vmrecsys;

import org.apache.mahout.cf.taste.common.TasteException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@SpringBootApplication
@ComponentScan
public class MainApplication {

    public static void main(String[] args) throws TasteException, IOException {
        SpringApplication.run(MainApplication.class, args);
    }
}