package com.ibm.developer.stormtracker;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/storms")
public class StormTrackerController {

	@GetMapping
	public ResponseEntity<String> helloWorld(){
		return ResponseEntity.ok("Hello World");
	}
}
