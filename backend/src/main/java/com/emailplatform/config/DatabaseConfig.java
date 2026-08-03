package com.emailplatform.config;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.net.URI;
import java.util.Map;

/**
 * DataSource factory that understands platform-native connection strings
 * (e.g. "postgres://user:pass@host:5432/dbname?sslmode=require" used by
 * Render, Neon and Railway) and rewrites them into JDBC URLs.
 *
 * When DATABASE_URL is present it wins; otherwise the standard Spring
 * datasource properties (DB_URL / DB_USERNAME / DB_PASSWORD) are used.
 */
@Configuration
public class DatabaseConfig {

    private static final Map<String, String> JDBC_PREFIX = Map.of(
            "postgres", "jdbc:postgresql://",
            "postgresql", "jdbc:postgresql://",
            "mysql", "jdbc:mysql://"
    );

    @Bean
    public DataSource dataSource(DataSourceProperties properties) {
        String databaseUrl = System.getenv("DATABASE_URL");
        if (!StringUtils.hasText(databaseUrl)) {
            return properties.initializeDataSourceBuilder().build();
        }

        URI uri = URI.create(databaseUrl.trim());
        String prefix = JDBC_PREFIX.getOrDefault(uri.getScheme(), null);
        if (prefix == null) {
            return properties.initializeDataSourceBuilder().build();
        }

        StringBuilder jdbc = new StringBuilder(prefix);
        jdbc.append(uri.getHost());
        if (uri.getPort() > 0) {
            jdbc.append(':').append(uri.getPort());
        }
        jdbc.append(uri.getPath());

        if (StringUtils.hasText(uri.getQuery())) {
            jdbc.append('?').append(uri.getQuery());
        }

        String[] userInfo = uri.getUserInfo() == null ? new String[0] : uri.getUserInfo().split(":", 2);
        String username = System.getenv("DATABASE_USERNAME");
        if (!StringUtils.hasText(username)) {
            username = userInfo.length > 0 ? userInfo[0] : "";
        }
        String password = System.getenv("DATABASE_PASSWORD");
        if (!StringUtils.hasText(password)) {
            password = userInfo.length > 1 ? userInfo[1] : "";
        }

        return DataSourceBuilder.create()
                .driverClassName(properties.getDriverClassName())
                .url(jdbc.toString())
                .username(username)
                .password(password)
                .build();
    }
}
