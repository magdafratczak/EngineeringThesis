package com.pp.trenerpol.service;

import com.pp.trenerpol.model.DTO.TrainerDTO;
import com.pp.trenerpol.model.Trainer;
import com.pp.trenerpol.model.TrainerSchedule;
import com.pp.trenerpol.model.TrainerScheduleException;
import com.pp.trenerpol.model.Training;
import com.pp.trenerpol.model.enums.Sex;
import com.pp.trenerpol.repository.TrainerRepository;
import com.pp.trenerpol.repository.TrainerScheduleExceptionRepository;
import com.pp.trenerpol.repository.TrainerScheduleRepository;
import com.pp.trenerpol.util.DateTimeFormatUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TrainerServiceImpl implements TrainerService {
	private static final Logger logger = LoggerFactory.getLogger(TrainerServiceImpl.class);
	private static final String EIGHT_AM = "08.00";
	private static final String FOUR_PM = "16.00";

	@Autowired
	private TrainerRepository trainerRepository;

	@Autowired
	private TrainerScheduleRepository trainerScheduleRepository;

	@Autowired
	private TrainerScheduleExceptionRepository trainerScheduleExceptionRepository;

	@Autowired
	private TrainingService trainingService;

	@Override
	public void initDefaultSchedule(Long id) {
		String[][] schedule = new String[][]{new String[]{EIGHT_AM, FOUR_PM},
				new String[]{EIGHT_AM, FOUR_PM}, new String[]{EIGHT_AM, FOUR_PM},
				new String[]{EIGHT_AM, FOUR_PM}, new String[]{EIGHT_AM, FOUR_PM}, null, null};
		createTrainerSchedule(id, schedule);
	}

	@Override
	public boolean createTrainerSchedule(Long id, String[][] schedule) {
		TrainerSchedule trainerSchedule = new TrainerSchedule();
		trainerSchedule.setMonday(schedule[0]);
		trainerSchedule.setTuesday(schedule[1]);
		trainerSchedule.setWednesday(schedule[2]);
		trainerSchedule.setThursday(schedule[3]);
		trainerSchedule.setFriday(schedule[4]);
		trainerSchedule.setSaturday(schedule[5]);
		trainerSchedule.setSunday(schedule[6]);
		trainerSchedule.setTrainerId(id);
		trainerScheduleRepository.save(trainerSchedule);
		return trainerSchedule.getId() > 0;
	}

	public void updateTrainerSchedule(TrainerSchedule trainerSchedule) {
		trainerScheduleRepository.save(trainerSchedule);
	}

	@Override
	public List<Trainer> getAllTrainers() {
		return trainerRepository.findAll();
	}

	@Override
	public boolean createTrainerScheduleException(Long trainerId, String[][] exception) {
		TrainerScheduleException trainerScheduleException = new TrainerScheduleException();
		trainerScheduleException.setTrainerId(trainerId);
		try {
			trainerScheduleException.setDate(DateTimeFormatUtil.parseStringAsDate(exception[0][0]));
		} catch (ParseException e) {
			logger.error("Error while parsing string: {}", exception[0][0]);
			return false;
		}
		trainerScheduleException.setHour(exception[1]);
		trainerScheduleExceptionRepository.save(trainerScheduleException);
		return trainerScheduleException.getId() > 0;
	}

	@Override
	public Trainer getTrainer(Long id) {
		Optional<Trainer> trainer = trainerRepository.findById(id);
		if (trainer.isPresent()) {
			return trainer.get();
		}
		return null;
	}

	@Override
	public ArrayList<String[]> getTrainerSchedule(Long id, Date from, Date to) {
		TrainerSchedule schedule = trainerScheduleRepository.findByTrainerId(id);
		List<TrainerScheduleException> exceptionList = trainerScheduleExceptionRepository
				.findAllByTrainerIdAndDateIsGreaterThanEqualAndModifiedDateIsLessThanEqual(id, from, to);
		LocalDate fromDate = DateTimeFormatUtil.getLocalDateFromDate(from);
		LocalDate toDate = DateTimeFormatUtil.getLocalDateFromDate(to);
		ArrayList<String[]> result = new ArrayList<>();
		List<LocalDate> dates = (fromDate).datesUntil(toDate.plusDays(1)).collect(Collectors.toList());
		for (LocalDate date : dates) {
			Date d = DateTimeFormatUtil.getDateFromLocalDate(date);
			if (!exceptionList.isEmpty()) {
				//get last modified exception for given date
				Optional<String[]> hours = exceptionList.stream()
						.filter(e -> e.getDate().equals(d)).max(Comparator.comparing(TrainerScheduleException::getModifiedDate)).map(e -> e.getHour());
				if (hours.isPresent()) {
					String[] h = hours.get();
					h = checkTrainings(h, id, d);
					result.add(h);
					continue;
				}
			}
			//get hours from schedule depending on week day
			switch (date.getDayOfWeek()) {
				case MONDAY:
					result.add(checkTrainings(schedule.getMonday(), id, d));
					break;
				case TUESDAY:
					result.add(checkTrainings(schedule.getTuesday(), id, d));
					break;
				case WEDNESDAY:
					result.add(checkTrainings(schedule.getWednesday(), id, d));
					break;
				case THURSDAY:
					result.add(checkTrainings(schedule.getThursday(), id, d));
					break;
				case FRIDAY:
					result.add(checkTrainings(schedule.getFriday(), id, d));
					break;
				case SATURDAY:
					result.add(checkTrainings(schedule.getSaturday(), id, d));
					break;
				case SUNDAY:
					result.add(checkTrainings(schedule.getSunday(), id, d));
					break;
			}
		}
		return result;
	}

	private String[] checkTrainings(String[] hours, Long id, Date date) {
		if (hours[0] == null) {
			return new String[]{null};
		}
		List<String> result = new ArrayList<>(List.of(hours));
		List<Training> trainings = trainingService.getTrainingsByTrainerIdFromDay(id, date).stream().
				sorted(Comparator.comparing(Training::getTrainingStartDate)).collect(Collectors.toList());

		if (!trainings.isEmpty()) {
			for (Training t : trainings) {
				SimpleDateFormat sdf = new SimpleDateFormat("HH.mm");
				String start = sdf.format(t.getTrainingStartDate());
				String end = sdf.format(t.getTrainingEndDate());
				result.add(start);
				result.add(end);
			}
			String[] tmp = result.toArray(String[]::new);
			Arrays.sort(tmp);
			result.clear();
			result.addAll(Arrays.asList(tmp));

			for (int i = 0; i < result.size() - 1; i++) {
				String f = result.get(i);
				String s = result.get(i + 1);
				if (f.equals(s)) {
					result.set(i, f.concat("del"));
					result.set(i + 1, s.concat("del"));
				}
			}
		}
		return result.stream().filter(t -> !t.contains("del")).toArray(String[]::new);
	}

	@Override
	public TrainerSchedule getTrainerSchedule(Long id) {
		return trainerScheduleRepository.findByTrainerId(id);
	}

	@Override
	public boolean isTrainerScheduleCreated(Long id) {
		TrainerSchedule schedule = trainerScheduleRepository.findByTrainerId(id);
		return schedule != null;
	}

	@Override
	public boolean updateTrainerData(Trainer trainer, TrainerDTO dto) {

		if (dto.getExpYears() != 0) {
			trainer.setExpYears(dto.getExpYears());
		}
		if (dto.getProtegesNum() != 0) {
			trainer.setProtegesNum(dto.getProtegesNum());
		}
		if (dto.getBio() != null && dto.getBio().length > 0) {
			trainer.setBio(dto.getBio());
		}
		if (!dto.getCity().isEmpty()) {
			trainer.setCity(dto.getCity());
		}
		if (dto.getPricesFrom() > 0) {
			trainer.setPricesFrom(dto.getPricesFrom());
		}
		if (dto.getPricesTo() > 0) {
			trainer.setPricesTo(dto.getPricesTo());
		}

		if (!dto.getName().isEmpty()) {
			trainer.setName(dto.getName());
		}

		if (!dto.getSurname().isEmpty()) {
			trainer.setSurname(dto.getSurname());
		}

		if (!dto.getPhoneNumber().isEmpty()) {
			String phoneNumber = dto.getPhoneNumber().trim().replaceAll(" ", "").replaceAll("-", "");
			trainer.setPhoneNum(phoneNumber);
		}

		if (!dto.getSex().isEmpty()) {
			String sex = Sex.NONE.name();
			if (dto.getSex().equalsIgnoreCase("woman") || dto.getSex().equalsIgnoreCase("w")) {
				sex = Sex.WOMAN.name();
			}
			if (dto.getSex().equalsIgnoreCase("man") || dto.getSex().equalsIgnoreCase("m")) {
				sex = Sex.MAN.name();
			}
			trainer.setSex(sex);
		}

		trainerRepository.save(trainer);
		return true;
	}


}
