package com.ibm.developer.stormtracker;

import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import io.restassured.module.mockmvc.RestAssuredMockMvc;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class ContractsBaseClass {
	@Rule
	public JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation();

	@Rule
	public TestName testName = new TestName();
	
	@Autowired
	private StormTrackerController controller;

	@MockBean
	private StormRepo repo;

	@Before
	public void before() throws Throwable {
		when(repo.findAll()).thenReturn(
				Arrays.asList(new Storm("25-08-2019", "05-09-2019", "Coast of Africa", "North Atlantic", "Hurricane", 5))); 
		RestAssuredMockMvc.standaloneSetup(MockMvcBuilders
				.standaloneSetup(controller)
				.apply(documentationConfiguration(this.restDocumentation))
				.alwaysDo(document(getClass().getSimpleName() + "_" + testName.getMethodName())));
	}
}
