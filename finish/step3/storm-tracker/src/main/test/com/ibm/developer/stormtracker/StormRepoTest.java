package com.ibm.developer.stormtracker;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.text.ParseException;

import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringJUnitConfig
@ContextConfiguration(classes = { StormTrackerApplication.class }, initializers = StormRepoTest.Initializer.class)
@TestMethodOrder(OrderAnnotation.class)
public class StormRepoTest {
	private static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>();

	static {
        postgres.start();
    }
	
	public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
		@Override
		public void initialize(ConfigurableApplicationContext applicationContext) {
			TestPropertyValues.of("spring.datasource.url=" + postgres.getJdbcUrl(), //
					"spring.datasource.username=" + postgres.getUsername(), //
					"spring.datasource.password=" + postgres.getPassword(),
					"spring.datasource.initialization-mode=ALWAYS",
					"spring.datasource.data=classpath:data.sql",
					"spring.datasource.schema=classpath:schema.sql",
					"spring.jpa.database-platform=org.hibernate.dialect.PostgreSQL95Dialect") //
					.applyTo(applicationContext);
		}
	}

	@Autowired
	private StormRepo repo;

	@Test
	@Order(1)
	public void testCountNumberOfStormInDB() {
		assertEquals(2, repo.count());
	}

	@Test
	public void testRetrieveStormFromDatabase() {

		Storm storm = repo.findAll().iterator().next();

		assertEquals(10, storm.getId());
	}

	@Test
	public void testStormToDB() throws ParseException {
		Storm storm = new Storm("25-08-2019", "05-09-2019", "Coast of Africa", "North Carolina", "Hurricane", 5);

		repo.save(storm);

		assertEquals(3, repo.count());
	}
}