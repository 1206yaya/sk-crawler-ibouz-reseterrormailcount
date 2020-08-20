package sk.crawler.ibouz.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@SpringBootApplication(scanBasePackages = "sk.crawler.ibouz")
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
