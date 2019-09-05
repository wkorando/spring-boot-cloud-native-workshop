# Cloud native Integration Testing

In this section we will look at some new tools for handling the intergration testing needs of Cloud Native applications. A key to going fast in the modern world is having fast, portable, reliable integration tests. 

## Services Integration Testing with TestContainers

1. Open your **pom.xml** and add the following:

	```
	<dependency>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-test</artifactId>
	</dependency>
	<dependency>
		<groupId>org.testcontainers</groupId>
		<artifactId>postgresql</artifactId>
		<version>1.10.6</version>
	</dependency>
	<dependency>
		<groupId>org.junit.jupiter</groupId>
		<artifactId>junit-jupiter</artifactId>
	</dependency>
			
	```
	```
	<dependencyManagement>
		<dependencies>
			<dependency>
				<groupId>org.junit</groupId>
				<artifactId>junit-bom</artifactId>
				<version>5.5.0</version>
				<type>pom</type>
				<scope>import</scope>
			</dependency>
		</dependencies>
	</dependencyManagement>
	```
2. Create the source folder **/src/main/test**
3. Create the file **StormRepoTest** under `com.ibm.developer.stormtracker`
4. In **StormRepoTest** add the following:

	```
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
	```
1. Execute the test from the command line by running `mvn test`

In the above test class the tool [TestContainers](https://www.testcontainers.org) is setting up and tearing down a PostgreSQL docker container. By using a local database issues that could cause tests to fail like data missing or network connectivity are reduced. By using a Docker container developers don't have to locally install, configure, and administer a database, making these tests portable.

## API Testing With Spring Cloud Contract

1. Open your **pom.xml** and add the following:

	```
	<dependency>
		<groupId>io.rest-assured</groupId>
		<artifactId>spring-mock-mvc</artifactId>
	</dependency>
	<dependency>
		<groupId>org.springframework.cloud</groupId>
		<artifactId>spring-cloud-starter-contract-verifier</artifactId>
	</dependency>
	```
	```
	<dependencyManagement>
		<dependencies>
			<dependency>
				<groupId>org.springframework.cloud</groupId>
				<artifactId>spring-cloud-dependencies</artifactId>
				<version>Greenwich.SR1</version>
				<type>pom</type>
				<scope>import</scope>
			</dependency>
		</dependencies>
	</dependencyManagement>
	```
	Under **build** add:
	```
	<plugin>
		<groupId>org.springframework.cloud</groupId>
		<artifactId>spring-cloud-contract-maven-plugin</artifactId>
		<version>2.1.1.RELEASE</version>
		<extensions>true</extensions>
		<configuration>
			<baseClassForTests>com.ibm.developer.stormtracker.ContractsBaseClass</baseClassForTests>
		</configuration>
	</plugin>
	```
	
1. Create a new fodler **contracts**  `src/test/resources`
Spring Cloud Contract makes the test of APIs for both producers and consumers super fast and reliable. In the above example we are writing a contract and verifying that our producer service, **storm-tracker** is meeting the requirements of the contract. 

Spring Cloud Contract produces a stubs artifact that consumers (clients) can use to test their service against to see if it can properly consume the produce service. Like with TestContainers above this allows fast and reliable integration testing that avoids issues of data going missing or problems with network connectivity. 

## Generating Documentation From Tests 

1. Open your **pom.xml** and add the following:

	```
	<dependency>
		<groupId>org.springframework.restdocs</groupId>
		<artifactId>spring-restdocs-mockmvc</artifactId>
	</dependency>
	```
	
	Under **build** add:
	
	```
	<plugin>
		<groupId>org.asciidoctor</groupId>
		<artifactId>asciidoctor-maven-plugin</artifactId>
		<version>1.5.3</version>
		<executions>
			<execution>
				<id>generate-docs</id>
				<phase>prepare-package</phase>
				<goals>
					<goal>process-asciidoc</goal>
				</goals>
				<configuration>
					<backend>html</backend>
					<doctype>book</doctype>
				</configuration>
			</execution>
		</executions>
		<dependencies>
			<dependency>
				<groupId>org.springframework.restdocs</groupId>
				<artifactId>spring-restdocs-asciidoctor</artifactId>
				<version>${spring-restdocs.version}</version>
			</dependency>
		</dependencies>
	</plugin>
	```
2. Create the source folder **/src/main/asciidoc**
3. Create the file **index.adoc** and add the following: 
	
	```
	= Storm Tracker Service API Guide
	<your name>;
	:doctype: book
	:icons: font
	:source-highlighter: highlightjs
	:toc: left
	:toclevels: 4
	:sectlinks:
	:operation-curl-request-title: Example request
	:operation-http-response-title: Example response
	
	[[overview]]
	= Overview
	
	API of Storm Tracker Service. Example of using contracts to both test an API to make sure it meets contract requirements and then generate API documentation from supplied contracts. 
	
	Form More Information on Contract Testing checkout:  https://github.com/wkorando/collaborative-contract-testing[Collaborative Contract Testing]
	
	[[resources-tag-retrieve]]
	== Find all storms
	
	A `GET` request to retrieve all produce items
	
	operation::ContractsTest_validate_find_all_storms[snippets='curl-request,response-body,http-request,http-response']
	```
4. Run `mvn install` from your command line
5. Open the **index.html** under `/target/generated/docs`
	
Along with testing your API to ensure it is meeting what is specified in your contracts. Spring Cloud Contract can also generate documentation from your contracts. This makes producing documentation for your services not only easier, but because the documentation is being produced as a part of the test and build process, the doocumentation shuld always be update to date with the current state of the service. 
