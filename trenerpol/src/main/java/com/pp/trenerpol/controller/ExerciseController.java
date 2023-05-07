package com.pp.trenerpol.controller;

import com.pp.trenerpol.exception.EntityNotFoundException;
import com.pp.trenerpol.model.DTO.ExerciseDTO;
import com.pp.trenerpol.model.Exercise;
import com.pp.trenerpol.model.User;
import com.pp.trenerpol.repository.ExerciseRepository;
import com.pp.trenerpol.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = {"http://localhost:3000", "http://trenerpol.s3-website-eu-west-1.amazonaws.com/"})
@RestController
@RequestMapping("/exercise")
public class ExerciseController {
	private static final Logger logger = LoggerFactory.getLogger(ExerciseController.class);

	@Autowired
	private ExerciseRepository exerciseRepository;

	@Autowired
	private UserRepository userRepository;

	/***
	 *
	 * @return user's custom exercised as well as core ones
	 */
	@GetMapping("/all")
	public List<Exercise> retrieveAllExercise() {
		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		String username = userDetails.getUsername();
		User user = userRepository.findByEmail(username);
		if (user == null) {
			logger.error("User not found: " + userDetails.getUsername());
			throw new EntityNotFoundException("User not found: " + userDetails.getUsername());
		}
		List<Exercise> exercises = exerciseRepository.findByUserId(0L);
		exercises.addAll(exerciseRepository.findByUserId(user.getId()));
		return exercises;
	}

	/***
	 *
	 * @param id
	 * @return exercise by id from table exercise
	 */
	@GetMapping("/{id}")
	public Exercise retrieveExercise(@PathVariable Long id) {
		Optional<Exercise> exercise = exerciseRepository.findById(id);
		if (exercise.isEmpty()) {
			logger.error("Exercise not found: id-" + id);
			throw new EntityNotFoundException("Exercise not found: id-" + id);
		}
		return exercise.get();
	}

	/***
	 * delete exercise by id
	 * @param id
	 * @return
	 */
	@DeleteMapping("/{id}")
	public ResponseEntity<?> deleteExercise(@PathVariable Long id) {
		Exercise exercise = exerciseRepository.getById(id);
		if (exercise == null) {
			logger.info("Exercise not found: id-" + id);
			throw new EntityNotFoundException("Exercise not found: id-" + id);
		}
		if (exercise.getUserId() == 0L) {
			return ResponseEntity.badRequest().body("User cannot delete this exercise.");
		}
		exerciseRepository.deleteById(id);
		logger.info("Exercise deleted: id-" + id);
		return ResponseEntity.ok()
				.body("Exercise successfully deleted");
	}

	/***
	 * edit existing exercise
	 * @param exerciseDTO
	 * @return
	 */
	@PutMapping("/edit")
	public ResponseEntity<?> editExerciseByName(@RequestBody ExerciseDTO exerciseDTO) {
		if (exerciseDTO.getId() == null) {
			return ResponseEntity.badRequest()
					.body("Exercise id not given!");
		}
		Exercise exercise = exerciseRepository.getById(exerciseDTO.getId());
		if (exercise == null) {
			logger.info("Exercise not found: name-" + exerciseDTO.getName());
			throw new EntityNotFoundException("Exercise not found: name-" + exerciseDTO.getName());
		}
		if (exercise.getUserId() == 0L) {
			return ResponseEntity.badRequest()
					.body("Core exercise cannot be edited!");
		}
		exercise.setDescr(exerciseDTO.getDescr());
		exerciseRepository.save(exercise);
		logger.info("Exercise successfully edited: name-" + exerciseDTO.getName());
		return ResponseEntity.ok()
				.body("Exercise successfully edited");
	}

	/***
	 * create new exercise
	 * @param exerciseDTO
	 * @return
	 */
	@PostMapping("/new")
	public ResponseEntity<?> createExercise(@RequestBody ExerciseDTO exerciseDTO) {
		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		String username = userDetails.getUsername();
		User user = userRepository.findByEmail(username);
		if (user == null) {
			logger.error("User not found: " + userDetails.getUsername());
			throw new EntityNotFoundException("User not found: " + userDetails.getUsername());
		}
		Exercise exercise = new Exercise();
		exercise.setDescr(exerciseDTO.getDescr());
		exercise.setName(exerciseDTO.getName());
		exercise.setUserId(user.getId());
		exerciseRepository.save(exercise);
		return ResponseEntity.ok()
				.body("Exercise successfully created");
	}
}