package uk.ac.surrey.vmrecsys;

import org.apache.mahout.cf.taste.common.TasteException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
public class MainApplication {

    public static void main(String[] args) throws TasteException, IOException {
        SpringApplication.run(MainApplication.class, args);
    }
}