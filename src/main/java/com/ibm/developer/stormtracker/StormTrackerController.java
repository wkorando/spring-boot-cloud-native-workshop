package com.ibm.developer.stormtracker;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/storms")
public class StormTrackerController {

	@GetMapping
	public ResponseEntity<String> helloWorld(@RequestParam(defaultValue = "World") String name){
		System.out.println("Passed in value: " + name);
		return ResponseEntity.ok("Hello " + name);
	}
}
