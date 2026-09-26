package co.com.taller14.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

/**
 * Beans transversales de infraestructura: operador transaccional reactivo y cliente HTTP
 * usado para llamar a los servicios externos simulados bajo /external/**.
 */
@Configuration
public class BeanConfiguration {

    @Bean
    public TransactionalOperator transactionalOperator(ReactiveTransactionManager transactionManager) {
        return TransactionalOperator.create(transactionManager);
    }

    @Bean
    public WebClient externalWebClient(org.springframework.core.env.Environment env) {
        String port = env.getProperty("server.port", "8081");
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(5));
        return WebClient.builder()
                .baseUrl("http://localhost:" + port)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
