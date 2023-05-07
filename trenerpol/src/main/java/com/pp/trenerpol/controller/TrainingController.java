package com.pp.trenerpol.controller;

import com.pp.trenerpol.exception.EntityNotFoundException;
import com.pp.trenerpol.exception.UserNotAuthorizedForOperationException;
import com.pp.trenerpol.model.DTO.ProtegeeMeasurementDTO;
import com.pp.trenerpol.model.DTO.TrainingDTO;
import com.pp.trenerpol.model.DTO.TrainingPlanDTO;
import com.pp.trenerpol.model.*;
import com.pp.trenerpol.model.enums.UserType;
import com.pp.trenerpol.repository.ProtegeeRepository;
import com.pp.trenerpol.repository.UserRepository;
import com.pp.trenerpol.service.TrainerService;
import com.pp.trenerpol.service.TrainingService;
import com.pp.trenerpol.service.UserService;
import com.pp.trenerpol.util.DateTimeFormatUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

@CrossOrigin(origins = {"http://localhost:3000", "http://trenerpol.s3-website-eu-west-1.amazonaws.com/"})
@RestController
@RequestMapping("/training")
public class TrainingController {
	private static final Logger logger = LoggerFactory.getLogger(TrainingController.class);

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private TrainingService trainingService;

	@Autowired
	private TrainerService trainerService;

	@Autowired
	private UserService userService;

	@Autowired
	private ProtegeeRepository protegeeRepository;

	/***
	 * @return logged-in user's all trainings
	 */
	@GetMapping("/all")
	public List<Training> retrieveAllTrainings() {
		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		String username = userDetails.getUsername();
		User user = userRepository.findByEmail(username);
		if (user == null) {
			logger.error("User not found: " + userDetails.getUsername());
			throw new EntityNotFoundException("User not found: " + userDetails.getUsername());
		}
		if (user instanceof Protegee) {
			return trainingService.getTrainingsByProtegeeId(user.getId());
		}
		return trainingService.getTrainingsByTrainerId(user.getId());
	}

	/***
	 * @param id
	 * @return training by id
	 */
	@GetMapping("/{id}")
	public Training retrieveTraining(@PathVariable Long id) {
		return trainingService.getTrainingById(id);
	}

	/***
	 * @param trainerId
	 * @return all trainings by trainer id
	 */
	@GetMapping("trainer/{trainerId}")
	public List<Training> retrieveTrainingByTrainerId(@PathVariable Long trainerId) {
		String[] ignorableFieldNames = {"id", "color"};
		List<Training> trainings = trainingService.getTrainingsByTrainerId(trainerId);
		// return ResponseHandler.getFilteredObjects(trainings, "protegeId"); nie dziala na aws/dockerze
		return trainings;
	}

	/***
	 * @param protegeeId
	 * @return all trainings by protegee id
	 */
	@GetMapping("protegee/{protegeeId}")
	public List<Training> retrieveTrainingByProtegeeId(@PathVariable Long protegeeId) {
		return trainingService.getTrainingsByProtegeeId(protegeeId);
	}

	/***
	 * @param trainerId
	 * @return training by trainer id for given date
	 */
	@GetMapping("trainer/{trainerId}/{date}")
	public Training retrieveTrainingByTrainerIdAndDate(@PathVariable Long trainerId,
	                                                   @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date date) {
		return trainingService.getTrainingInfoByTrainerIdAndDate(trainerId, date);
	}

	/***
	 * @param protegeeId
	 * @return training by protegee id for given date
	 */
	@GetMapping("protegee/{protegeeId}/{date}")
	public Training retrieveTrainingByProtegeeIdAndDate(@PathVariable Long protegeeId, @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date date) {
		return trainingService.getTrainingInfoByProtegeeIdAndDate(protegeeId, date);
	}

	/***
	 * delete training by training id
	 * @param id
	 * @return
	 */
	@DeleteMapping("/delete/{id}")
	public ResponseEntity<?> deleteTraining(@PathVariable Long id) {
		Training training = trainingService.getTrainingById(id);
		if (training == null) {
			logger.error("Training not found: id-" + id);
			throw new EntityNotFoundException("Training not found: id-" + id);
		}
		trainingService.cancelTraining(id);
		logger.info("Training deleted: id-" + id);
		return ResponseEntity.ok()
				.body("Training successfully deleted");
	}

	/***
	 * update training date and duration
	 * @param
	 * @return
	 */
	@PutMapping("/edit")
	public ResponseEntity<?> editTraining(@RequestBody @Valid TrainingDTO trainingDTO) {
		Training training = trainingService.getTrainingById(trainingDTO.getId());
		if (training == null) {
			logger.error("Training not found: id-" + trainingDTO.getId());
			throw new EntityNotFoundException("Training not found: id-" + trainingDTO.getId());
		}
		int duration = trainingDTO.getMDuration();
		if (duration == 0) {
			//change only date, get duration from DB
			duration = training.getMDuration();
		}

		Date newDate = DateTimeFormatUtil.getDateFromLocalDateTime(trainingDTO.getTrainingStartDate());
		Date endDate = DateTimeFormatUtil.calculateEndDate(newDate, duration);
		training.setTrainingStartDate(new Timestamp(newDate.getTime()));
		training.setMDuration(duration);
		training.setTrainingEndDate(endDate);
		trainingService.updateTraining(training);
		return ResponseEntity.ok()
				.body("Training date successfully updated.");
	}

	/***
	 * create new training
	 * @param
	 * @return
	 */
	@PostMapping("/create")
	public ResponseEntity<?> createTraining(@RequestBody @Valid TrainingDTO trainingDTO) {
		//get protegee id from logged in user, because so far only protegee can create training
		User user = userService.findUserByEmail(
				SecurityContextHolder.getContext().getAuthentication().getName());
		if (user == null || user instanceof Trainer) {
			throw new UserNotAuthorizedForOperationException("Method available only for protegee");
		}
		Protegee protegee = user instanceof Protegee ? (Protegee) user : null;
		if (protegee == null) {
			return ResponseEntity.badRequest().body("Protegee not found");
		}

		Training newTraining = new Training();
		Date startDate = DateTimeFormatUtil.getDateFromLocalDateTime(trainingDTO.getTrainingStartDate());
		Date endDate = DateTimeFormatUtil.calculateEndDate(startDate, trainingDTO.getMDuration());
		newTraining.setTrainingStartDate(startDate);
		if (trainingDTO.getTrainerId() == 0) {
			return ResponseEntity.badRequest()
					.body("Fill trainer id in order to create training");
		}
		Trainer trainer = trainerService.getTrainer(trainingDTO.getTrainerId());
		if (trainer == null) {
			return ResponseEntity.badRequest()
					.body("Cannot find trainer by this trainer id:" + trainingDTO.getTrainerId());
		}
		newTraining.setProtegeId(protegee.getId());
		newTraining.setTrainerId(trainingDTO.getTrainerId());
		newTraining.setMDuration(trainingDTO.getMDuration());
		newTraining.setTrainingEndDate(endDate);
		if (trainingService.createTraining(newTraining) != null) {
			return ResponseEntity.ok()
					.body("Training successfully created");
		}
		return ResponseEntity.badRequest()
				.body("Could not create training");
	}

	/***
	 * let trainer create protegee metrics per training or update existing one, training id has to be given
	 * @param dto
	 * @return
	 */
	@PostMapping("/metric")
	public ResponseEntity<?> createMetricPerTraining(@RequestBody ProtegeeMeasurementDTO dto) {
		if (dto.getTrainingId() == null) {
			return ResponseEntity.badRequest()
					.body("Training id has to be given");
		}
		trainingService.saveProtegeeMetricsPerTraining(dto);
		return ResponseEntity.ok()
				.body("Protegee metrics per training successfully created");
	}

	/***
	 * create Training plan while editing training - trainingId has to be given
	 * as well as array of exercise - exercise_id, repetition, load
	 * @param dto
	 * @return
	 */
	@PostMapping("/trainingPlan")
	public ResponseEntity<?> createTrainingPlan(@RequestBody @Valid TrainingPlanDTO dto) {
		//only trainer can create training plan
		User user = userService.findUserByEmail(
				SecurityContextHolder.getContext().getAuthentication().getName());
		if (user == null || user instanceof Protegee) {
			throw new UserNotAuthorizedForOperationException("Method available only for trainer");
		}
		if (dto.getTrainingId() == 0) {
			return ResponseEntity.badRequest()
					.body("Please, provide training id");
		}
		if (!trainingService.getTrainingPlan(dto.getTrainingId()).isEmpty()) {
			trainingService.updateTrainingPlan(dto.getTrainingId(), dto);
		} else {
			trainingService.createTrainingPlan(dto);
		}
		return ResponseEntity.ok()
				.body("Training plan successfully created");
	}

	/***
	 *
	 * @param trainingId
	 * @return list of parametrized exercises for given training id
	 */
	@GetMapping("/trainingPlan/{trainingId}")
	public List<TrainingSetExercise> getTrainingPlan(@PathVariable Long trainingId) {
		return trainingService.getTrainingPlan(trainingId);
	}

	/***
	 * get all training plans for user id - depending on user type
	 * @param userId
	 * @return all training list for given user id
	 */
	@GetMapping("/trainingPlan/user/{userId}")
	public List<TrainingSetExercise> getTrainingPlanForUser(@PathVariable Long userId) {
		User user = userRepository.getById(userId);
		if (user.getType().equals(UserType.TRAINER.getDbValue())) {
			return trainingService.getTrainingPlansByTrainerId(userId);
		}
		return trainingService.getTrainingPlansByProtegeeId(userId);
	}

	/***
	 * delete training plan for given training id. Deletes rows from table
	 * training_set_exercise as well as from ser_exercise
	 * @param trainingId
	 * @return
	 */
	@DeleteMapping("/trainingPlan/{trainingId}")
	public ResponseEntity<?> deleteTrainingPlan(@PathVariable Long trainingId) {
		trainingService.deleteTrainingPlan(trainingId);
		return ResponseEntity.ok()
				.body("Training plan successfully deleted");
	}

	/***
	 * edit exisiting training plan
	 * @param dto
	 * @return
	 */
	@PutMapping("/trainingPlan/edit")
	public ResponseEntity<?> editTrainingPlan(@RequestBody @Valid TrainingPlanDTO dto) {
		trainingService.updateTrainingPlan(dto.getTrainingId(), dto);
		return ResponseEntity.ok()
				.body("Training plan successfully updated");
	}

	/***
	 * download exisiting training plan as excel file
	 * @param id
	 * @return
	 * @throws IOException
	 */
	@GetMapping(value = "/trainingPlan/file/{id}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	public ResponseEntity<Object> getFile(@PathVariable Long id) throws IOException {
		Training training = trainingService.getTrainingById(id);
		if (training != null) {
			String fileName = String.format("training-plan.%s", training.getTrainingStartDate().toString()
					.replaceAll(" ", "-").replaceAll(":.", "-").concat(".xls"));
			ByteArrayInputStream file = trainingService.generateTrainingPlanAsExcelFile(id, fileName);
			String header = String.format("attachment; filename=%s", fileName);
			HttpHeaders headers = new HttpHeaders();
			headers.add("Content-Disposition", header);
			headers.setContentType(new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
			return ResponseEntity
					.ok()
					.headers(headers)
					.body(new InputStreamResource(file));
		}
		return ResponseEntity.badRequest()
				.body("Training not found");
	}

}
