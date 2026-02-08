package com.example.fleamarketsystem.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class ContactSchemaInitializer implements ApplicationRunner {

	private final JdbcTemplate jdbcTemplate;

	public ContactSchemaInitializer(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public void run(ApplicationArguments args) {
		jdbcTemplate.execute(
				"create table if not exists contact (" +
						"id bigserial primary key," +
						"user_id bigint not null references users(id)," +
						"subject varchar(255) not null," +
						"message text not null," +
						"created_at timestamp without time zone not null," +
						"read boolean not null default false" +
						")");
	}
}
