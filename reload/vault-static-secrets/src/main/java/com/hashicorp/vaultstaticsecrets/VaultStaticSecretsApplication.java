package com.hashicorp.vaultstaticsecrets;

import org.apache.commons.logging.LogFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.sql.DataSource;

@SpringBootApplication
@EnableScheduling
public class VaultStaticSecretsApplication {

	public static void main(String[] args) {
		SpringApplication.run(VaultStaticSecretsApplication.class, args);
	}

	@Bean
	@RefreshScope
	DataSource dataSource(DataSourceProperties properties) {
		var log = LogFactory.getLog(getClass());
		var db = DataSourceBuilder
				.create()
				.url(properties.getUrl())
				.username(properties.getUsername())
				.password(properties.getPassword())
				.build();
		log.info(
				"rebuild data source: " +
						properties.getUsername() +
						',' + properties.getPassword()
		);
		return db;
	}

}

