package org.uasound.bot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(
        scanBasePackages = "org.uasound.bot.telegram"
)
@EnableJpaRepositories(
        basePackages = "org.uasound.data.service.hibernate.exact"
)
@EntityScan(basePackages = "org.uasound.data.entity")
@EnableTransactionManagement
public class Starter {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Starter.class, args);
    }
}
