package com.test.dogs.api.rest.endpoint;

import org.springframework.web.bind.annotation.*;
import com.test.dogs.api.rest.model.DogDTO;
import com.test.dogs.api.rest.service.DogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.util.List;

@RestController
@RequestMapping("/")
public class DogEndpoint {

    private final DogService dogService;

    @Autowired
    public DogEndpoint(DogService dogService) {
        this.dogService = dogService;
    }

	/**
	 * Handles GET requests to /api/dogs and returns a list of all dogs.
	 */
	@GetMapping
	public ResponseEntity<List<DogDTO>> getAllDogsList(@RequestParam(name = "filter", required = false) String filter) {
		return ResponseEntity.ok(dogService.getAllDogsList(filter));
	}

	/**
	 * Handles GET requests to /api/dogs/{id} and returns the record for that ID.
	 */
    @GetMapping("/{id}")
    public ResponseEntity<DogDTO> getDogById(@PathVariable Integer id) {
		DogDTO dog = dogService.getDogById(id);
        return ResponseEntity.ok(dog);
    }

	/**
	 * Handles GET requests to /api/dogs/dogs and returns a list of all records, including any deleted records.
	 */
    @GetMapping("/dogs")
    public ResponseEntity<List<DogDTO>> getAllDogs() {
	    return ResponseEntity.ok(dogService.getAllDogsIncludingDeleted());
    }

	/**
	 * Handles PUT requests to /api/dogs/{id} and returns the updated record.
	 */
    @PutMapping("/{id}")
    public ResponseEntity<DogDTO> updateDogById(@PathVariable Integer id, @RequestBody DogDTO dogDTO) {
		DogDTO updatedRecord = dogService.updateDog(id, dogDTO);
        return ResponseEntity.ok(updatedRecord);
    }

	/**
	 * Handles POST requests to /api/dogs and returns a newly created record.
	 */
	@PostMapping
	public ResponseEntity<DogDTO> create(@RequestBody DogDTO dogDTO) {
		DogDTO newRecord = dogService.saveNewDog(dogDTO);
		return ResponseEntity.ok(newRecord);
	}

	/**
	 * Handles DELETE requests to /api/dogs/{id} and returns a confirmation message.
	 */
	@DeleteMapping("/{id}")
	public ResponseEntity<String> delete(@PathVariable Integer id) {
		dogService.markDogAsDeleted(id);
		return ResponseEntity.ok("Dog with ID " + id + " has been successfully deleted");
	}
}
