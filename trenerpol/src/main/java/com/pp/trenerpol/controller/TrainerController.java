package com.pp.trenerpol.controller;

import com.pp.trenerpol.exception.EntityNotFoundException;
import com.pp.trenerpol.model.DTO.SignUpUserDTO;
import com.pp.trenerpol.model.DTO.TrainerDTO;
import com.pp.trenerpol.model.Protegee;
import com.pp.trenerpol.model.Trainer;
import com.pp.trenerpol.model.TrainerSchedule;
import com.pp.trenerpol.model.User;
import com.pp.trenerpol.repository.ProtegeeRepository;
import com.pp.trenerpol.service.TrainerServiceImpl;
import com.pp.trenerpol.service.TrainingService;
import com.pp.trenerpol.service.UserService;
import com.pp.trenerpol.util.DateTimeFormatUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@CrossOrigin(origins = {"http://localhost:3000", "http://trenerpol.s3-website-eu-west-1.amazonaws.com/"})
@RestController
@RequestMapping("/trainer")
public class TrainerController {

	private static final Logger logger = LoggerFactory.getLogger(TrainerController.class);

	@Autowired
	private UserService userService;

	@Autowired
	private TrainerServiceImpl trainerService;

	@Autowired
	private ProtegeeRepository protegeeRepository;

	@Autowired
	private TrainingService trainingService;

	@GetMapping("/all")
	public List<Trainer> retrieveAllTrainers() {
		List<Trainer> trainers = trainerService.getAllTrainers();
		return trainers.stream().filter(User::isEnabled).collect(Collectors.toList());
	}

	@GetMapping("/{id}")
	public Trainer retrieveTrainer(@PathVariable Long id) {
		Trainer trainer = trainerService.getTrainer(id);
		if (trainer == null) {
			logger.error("Trainer not found: id-" + id);
			throw new EntityNotFoundException("Trainer not found: id-" + id);
		}

		return trainer;
	}

	/***
	 *
	 * @return all proteges from table protegee for specific trainer
	 */
	@GetMapping("/protegees")
	public List<Protegee> retrieveAllProtegesForTrainer() {
		List<Protegee> protegees = new ArrayList<>();
		Trainer trainer = getTrainer();
		if (trainer != null) {
			List<Long> list = trainingService.getTrainingsByTrainerId(trainer.getId())
					.stream().map(t -> t.getProtegeId()).collect(Collectors.toList());
			for (Long id : list) {
				Optional<Protegee> p = protegeeRepository.findById(id);
				if (p.isPresent()) {
					protegees.add(p.get());
				}
			}
		}
		return protegees.stream().distinct().collect(Collectors.toList());
	}

	/***
	 * save trainer's default schedule. exemplary request:
	 * [ [ 8, 17.30 ] , [ 8, 17.30 ] , [ 8, 17.30 ] , [ 8, 17.30 ], [ 8, 17.30 ], [null], [null]]
	 * @param schedule
	 * @return
	 */
    @PostMapping("/createSchedule")
    public ResponseEntity<?> createSchedule(@RequestBody String[][] schedule) {
        Trainer trainer = getTrainer();
        if (trainer == null) {
            return ResponseEntity.badRequest().body("Trainer not found");
        }
        if (trainerService.isTrainerScheduleCreated(trainer.getId())) {
            return ResponseEntity.badRequest().body("Schedule for this trainer has been already created");
        }
        if (schedule.length < 7) {
            return ResponseEntity.badRequest().body("Please provide schedule for 7 days");
        }
        if (trainerService.createTrainerSchedule(trainer.getId(), schedule)) {
            return ResponseEntity.ok()
                    .body("Trainer schedule created.");
        }
        return ResponseEntity.badRequest().body("Errors while creating trainer schedule");
    }

	@PutMapping("/editSchedule")
	public ResponseEntity<?> editSchedule(@RequestBody String[][] schedule) {
		Trainer trainer = getTrainer();
		if (trainer == null) {
			return ResponseEntity.badRequest().body("Trainer not found");
		}
		TrainerSchedule trainerSchedule = trainerService.getTrainerSchedule(trainer.getId());
		if (trainerSchedule == null) {
			return ResponseEntity.badRequest().body("Schedule not found");
		}
		trainerSchedule.setMonday(schedule[0]);
		trainerSchedule.setTuesday(schedule[1]);
		trainerSchedule.setWednesday(schedule[2]);
		trainerSchedule.setThursday(schedule[3]);
		trainerSchedule.setFriday(schedule[4]);
		trainerSchedule.setSaturday(schedule[5]);
		trainerSchedule.setSunday(schedule[6]);
		trainerService.updateTrainerSchedule(trainerSchedule);
		return ResponseEntity.ok()
				.body("Trainer schedule updated.");
	}


	/***
	 * create exception in trainer's default schedule
	 * exemplary request: [["14/07/2022"], [12, 14, 19, 19.30]]
	 * -> [[date], [hour from, hour to, hour from, hour to]]
	 * @param exception
	 * @return
	 */
	@PostMapping("/createScheduleException")
	public ResponseEntity<?> createScheduleException(@RequestBody String[][] exception) {
		if (exception.length < 2) {
			return ResponseEntity.badRequest().body("Please provide date and hours in order to create exception");
		}
		Trainer trainer = getTrainer();
		if (trainer == null) {
			return ResponseEntity.badRequest().body("Trainer not found");
		}
		if (trainerService.createTrainerScheduleException(trainer.getId(), exception)) {
			return ResponseEntity.ok()
					.body("Exception in trainer's schedule created.");
		}
		return ResponseEntity.badRequest().body("Errors while creating trainer schedule's exception");
	}

	/***
	 * Trainer can send invite to the platform for his protegee. Protegee after clicking on link in email
	 * will set up his password and therefore activate his account
	 * @param signUpUserDTO
	 * @param request
	 * @return
	 */
	@PostMapping("/invite")
	public ResponseEntity<?> addProtegee(@RequestBody @Valid SignUpUserDTO signUpUserDTO, HttpServletRequest request) {
		// add check for email exists in DB
		if (userService.existsByEmail(signUpUserDTO.getEmail())) {
			return ResponseEntity.badRequest()
					.body("Email is already taken!");
		}
		userService.inviteProtegee(signUpUserDTO, request);
		return ResponseEntity.ok()
				.body("User account created. Activation mail has been sent");

	}

	/***
	 * get schedule for x days for specific trainer
	 * exemplary request: [["11/07/2022"], ["17/07/2022"]]
	 * -> [[date from], [date to]]`
	 * @param data
	 * @return
	 */
	@PostMapping("/schedule/{id}")
	public ArrayList<String[]> getTrainerSchedule(@PathVariable Long id, @RequestBody String[][] data) {
		Trainer trainer = trainerService.getTrainer(id);
		if (trainer == null) {
			throw new EntityNotFoundException("Trainer not found");
		}
		Date from = null;
		Date to = null;
		try {
			from = DateTimeFormatUtil.parseStringAsDate(data[0][0]);
			to = DateTimeFormatUtil.parseStringAsDate(data[1][0]);
		} catch (ParseException e) {
			logger.error(String.valueOf(e));
		}

		if (from == null || to == null) {
			throw new EntityNotFoundException("Cannot retrieve dates from request");
		}
		return trainerService.getTrainerSchedule(trainer.getId(), from, to);
	}

	/***
	 * edit trainer's data such as: experience years/protegees number/bio/city/prices
	 * name/surname/sex
	 * @param trainerDTO
	 * @return
	 */
	@PutMapping("/edit")
	public ResponseEntity<?> editTrainerData(@RequestBody @Valid TrainerDTO trainerDTO) {
		Trainer trainer = getTrainer();
		if (trainer == null) {
			return ResponseEntity.badRequest().body("Trainer not found");
		}
		trainerService.updateTrainerData(trainer, trainerDTO);
		return ResponseEntity.ok().body("Trainer data successfully edited.");
	}

	@GetMapping("/weeklySchedule")
	public ArrayList<String[]> getTrainerDefaultSchedule() {
		Trainer trainer = getTrainer();
		ArrayList<String[]> schedule = new ArrayList<>();
		if (trainer == null) {
			return new ArrayList<>();
		}
		TrainerSchedule trainerSchedule = trainerService.getTrainerSchedule(trainer.getId());
		schedule.add(trainerSchedule.getMonday());
		schedule.add(trainerSchedule.getTuesday());
		schedule.add(trainerSchedule.getWednesday());
		schedule.add(trainerSchedule.getThursday());
		schedule.add(trainerSchedule.getFriday());
		schedule.add(trainerSchedule.getSaturday());
		schedule.add(trainerSchedule.getSunday());
		return schedule;
	}


	private Trainer getTrainer() {
		User user = userService.findUserByEmail(
				SecurityContextHolder.getContext().getAuthentication().getName());
		if (user == null || user instanceof Protegee) {
			return null;
		}
		return user instanceof Trainer ? (Trainer) user : null;
	}
}
