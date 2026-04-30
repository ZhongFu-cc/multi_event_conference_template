package tw.com.conference;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@ComponentScan("tw.com.conference")
@EnableCaching
@EnableScheduling
@SpringBootApplication
public class ConferenceApplication {
	public static void main(String[] args) {
		SpringApplication.run(ConferenceApplication.class, args);
	}
}
