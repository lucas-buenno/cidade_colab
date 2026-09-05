package tech.cidade.colab;

import io.mongock.runner.springboot.EnableMongock;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
@EnableMongock
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
