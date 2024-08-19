package projects.currencyexchangeapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class CurrencyExchangeApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(CurrencyExchangeApiApplication.class, args);
    }

}
