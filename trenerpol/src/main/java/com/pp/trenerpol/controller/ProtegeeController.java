package com.pp.trenerpol.controller;

import com.pp.trenerpol.exception.EntityNotFoundException;
import com.pp.trenerpol.exception.UserNotAuthorizedForOperationException;
import com.pp.trenerpol.model.DTO.ProtegeeDTO;
import com.pp.trenerpol.model.DTO.ProtegeeMeasurementDTO;
import com.pp.trenerpol.model.Protegee;
import com.pp.trenerpol.model.ProtegeeMeasurement;
import com.pp.trenerpol.model.Trainer;
import com.pp.trenerpol.model.User;
import com.pp.trenerpol.repository.ProtegeeMeasurementRepository;
import com.pp.trenerpol.repository.ProtegeeRepository;
import com.pp.trenerpol.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = {"http://localhost:3000", "http://trenerpol.s3-website-eu-west-1.amazonaws.com/"})
@RestController
@RequestMapping("/protegee")
public class ProtegeeController {
	private static final Logger logger = LoggerFactory.getLogger(ProtegeeController.class);

	@Autowired
	private ProtegeeRepository protegeeRepository;

	@Autowired
	private ProtegeeMeasurementRepository protegeeMeasurementRepository;

	@Autowired
	private UserService userService;

	/***
	 *
	 * @return all proteges from table protegee
	 */
	@GetMapping("/all")
	public List<Protegee> retrieveAllProteges() {
		return protegeeRepository.findAll();
	}

	/***
	 *
	 * @param id
	 * @return row from table protegee by protegee_id
	 */
	@GetMapping("/{id}")
	public Protegee retrieveProtegee(@PathVariable Long id) {
		return checkProtegee(id);
	}

	/***
	 * @param id
	 * @return all measurements from table protegee_measurement for specific protegee
	 */
	@GetMapping("/metrics/{id}")
	public List<ProtegeeMeasurement> getProtegeeMetrics(@PathVariable Long id) {
		checkProtegee(id);
		return protegeeMeasurementRepository.findAllByProtegeeId(id);
	}

	/***
	 *
	 * @param id
	 * @param date
	 * @return row from table protegee_measurement by protegee id and given date
	 */
	@GetMapping("/metrics/{id}/{date}")
	public ProtegeeMeasurement getProtegeeMetricsForDate(@PathVariable Long id,
	                                                     @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") Date date) {
		checkProtegee(id);
		return protegeeMeasurementRepository.findByProtegeeIdAndCreationDate(id, date);
	}

	/***
	 * create new protegee measurement for logged-in protegee or update existing one
	 * @param dto
	 * @return
	 */
	@PostMapping("/metric")
	public ResponseEntity<?> createMetric(@RequestBody @Valid ProtegeeMeasurementDTO dto) {
		User user = userService.findUserByEmail(
				SecurityContextHolder.getContext().getAuthentication().getName());
		if (user == null) {
			return ResponseEntity.badRequest()
					.body("Could not retrieve user");
		}
		if (user instanceof Trainer) {
			throw new UserNotAuthorizedForOperationException("Method available only for protegee");
		}
		userService.saveProtegeeMeasurements(user.getId(), dto);
		return ResponseEntity.ok()
				.body("Protege's measurement saved");
	}

	/***
	 * edit protegee's data such as: height, birth date
	 * name/surname/sex
	 * @param protegeeDTO
	 * @return
	 */
	@PutMapping("/edit")
	public ResponseEntity<?> editProtegeeData(@RequestBody @Valid ProtegeeDTO protegeeDTO) {
		User user = userService.findUserByEmail(
				SecurityContextHolder.getContext().getAuthentication().getName());
		if (user == null || user instanceof Trainer) {
			throw new UserNotAuthorizedForOperationException("Method available only for protegee");
		}
		Protegee protegee = user instanceof Protegee ? (Protegee) user : null;
		if (protegee == null) {
			return ResponseEntity.badRequest().body("Protegee not found");
		}
		userService.editProtegeeData(protegee, protegeeDTO);
		return ResponseEntity.ok().body("Protegee data successfully edited.");
	}

	private Protegee checkProtegee(Long id) {
		Optional<Protegee> protegee = protegeeRepository.findById(id);
		if (protegee.isEmpty()) {
			throw new EntityNotFoundException("Protegee not found: id-" + id);
		}
		return protegee.get();
	}
}
