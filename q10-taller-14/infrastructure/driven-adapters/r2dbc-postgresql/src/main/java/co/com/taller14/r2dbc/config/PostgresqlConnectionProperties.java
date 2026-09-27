package co.com.taller14.r2dbc.config;


import org.springframework.boot.context.properties.ConfigurationProperties;

//@ConfigurationProperties(prefix = "spring.r2dbc")
@ConfigurationProperties(prefix = "adapter.r2dbc")
public record PostgresqlConnectionProperties(
        String host,
        Integer port,
        String database,
        String schema,
        String username,
        String password) {
}
