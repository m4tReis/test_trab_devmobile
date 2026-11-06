package br.com.santosschein.complexo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ComplexoSantosScheinBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(ComplexoSantosScheinBackendApplication.class, args);
    }
}
