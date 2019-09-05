package com.ibm.developer.stormtracker;

import org.springframework.data.repository.CrudRepository;

public interface StormRepo extends CrudRepository<Storm, Long> {
	public Iterable<Storm> findByStartLocation(String startLocation);
}
