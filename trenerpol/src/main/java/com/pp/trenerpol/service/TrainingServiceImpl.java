package com.pp.trenerpol.service;

import com.pp.trenerpol.exception.EntityNotFoundException;
import com.pp.trenerpol.model.DTO.ProtegeeMeasurementDTO;
import com.pp.trenerpol.model.DTO.SetExerciseDTO;
import com.pp.trenerpol.model.DTO.TrainingPlanDTO;
import com.pp.trenerpol.model.SetExercise;
import com.pp.trenerpol.model.Training;
import com.pp.trenerpol.model.TrainingSetExercise;
import com.pp.trenerpol.repository.ExerciseRepository;
import com.pp.trenerpol.repository.SetExerciseRepository;
import com.pp.trenerpol.repository.TrainingRepository;
import com.pp.trenerpol.repository.TrainingSetExerciseRepository;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class TrainingServiceImpl implements TrainingService {
	@Autowired
	private TrainingRepository trainingRepository;

	@Autowired
	private SetExerciseRepository setExerciseRepository;

	@Autowired
	private ExerciseRepository exerciseRepository;

	@Autowired
	private TrainingSetExerciseRepository trainingsetExerciseRepository;

	@Autowired
	private UserService userService;

	@Override
	public Training createTraining(Training training) {
		trainingRepository.save(training);
		return training;
	}

	@Override
	public Training updateTraining(Training training) {
		trainingRepository.save(training);
		return training;
	}

	@Override
	public void cancelTraining(Long trainingId) {
		trainingRepository.deleteById(trainingId);
	}

	@Override
	public Training getTrainingById(Long trainingId) {
		Optional<Training> training = trainingRepository.findById(trainingId);
		if (training.isEmpty()) {
			throw new EntityNotFoundException("Training with given id not found!");
		}
		return training.get();
	}

	@Override
	public Training getTrainingInfoByTrainerIdAndDate(Long trainerId, Date date) {
		return trainingRepository.findByTrainerIdAndTrainingStartDate(trainerId, date);
	}

	@Override
	public Training getTrainingInfoByProtegeeIdAndDate(Long protegeeId, Date date) {
		return trainingRepository.findByProtegeIdAndTrainingStartDate(protegeeId, date);
	}

	@Override
	public List<Training> getTrainingsByTrainerId(Long id) {
		return trainingRepository.findAllByTrainerId(id);
	}

	@Override
	public List<Training> getTrainingsByTrainerIdBetweenDates(Long id, Date from, Date to) {
		return trainingRepository.findAllByTrainerIdAndTrainingStartDateIsGreaterThanEqualAndTrainingStartDateIsLessThanEqual(id, from, to);
	}

	@Override
	public List<Training> getTrainingsByTrainerIdFromDay(Long id, Date date) {
		return trainingRepository.findAllByTrainerIdAndTrainingStartDateContaining(id, date);
	}

	@Override
	public List<Training> getTrainingsByProtegeeId(Long id) {
		return trainingRepository.findAllByProtegeId(id);
	}

	@Override
	public void saveProtegeeMetricsPerTraining(ProtegeeMeasurementDTO dto) {
		Training training = getTrainingById(dto.getTrainingId());
		userService.saveProtegeeMeasurements(training.getProtegeId(), dto);
	}

	@Override
	public ByteArrayInputStream generateTrainingPlanAsExcelFile(Long trainingId, String fileName) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		List<TrainingSetExercise> sets = getTrainingPlan(trainingId);
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet("training plan");
		HSSFFont headerFont = wb.createFont();
		headerFont.setBold(true);
		headerFont.setColor(IndexedColors.BLACK.getIndex());
		CellStyle headerCellStyle = wb.createCellStyle();
		headerCellStyle.setFont(headerFont);
		headerCellStyle.setFillBackgroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
		String[] columns = {"ORDER", "EXERCISE", "LOAD[KG]", "REPETITION", "DESCRIPTION"};
		HSSFRow headerRow = sheet.createRow(0);

		int index = 1;
		for (TrainingSetExercise set : sets) {
			HSSFRow row = sheet.createRow(index);
			row.createCell(0).setCellValue(set.getOrder());
			row.createCell(1).setCellValue(set.getExercise().getExercise().getName());
			row.createCell(2).setCellValue(set.getExercise().getLoad());
			row.createCell(3).setCellValue(set.getExercise().getRepetition());
			row.createCell(4).setCellValue(set.getExercise().getExercise().getDescr());
			index++;
		}

		for (int col = 0; col < columns.length; col++) {
			Cell cell = headerRow.createCell(col);
			cell.setCellValue(columns[col]);
			cell.setCellStyle(headerCellStyle);
			sheet.autoSizeColumn(col);
			if (col == 4) {
				CellStyle cs = wb.createCellStyle();
				cs.setWrapText(true);
				cs.setFont(headerFont);
				cell.setCellStyle(cs);
			}
		}

		Training training = getTrainingById(trainingId);
		if (training != null) {
			wb.write(out);
			byte[] arr = out.toByteArray();
			out.flush();
			out.close();
			return new ByteArrayInputStream(arr);
		}
		return null;
	}

	@Override
	public void createTrainingPlan(TrainingPlanDTO dto) {
		Training training = getTrainingById(dto.getTrainingId());
		int order = 1;
		for (SetExerciseDTO setExercise : dto.getSetExerciseList()) {
			SetExercise set = new SetExercise();
			set.setExercise(exerciseRepository.getById(setExercise.getExerciseId()));
			set.setLoad(setExercise.getLoad());
			set.setRepetition(setExercise.getRepetition());
			set = setExerciseRepository.save(set);
			TrainingSetExercise trainingSetExercise = new TrainingSetExercise();
			trainingSetExercise.setSetExerciseId(set.getId());
			trainingSetExercise.setTrainingId(training.getId());
			trainingSetExercise.setOrder(order);
			trainingsetExerciseRepository.save(trainingSetExercise);
			order++;
		}
	}

	@Override
	public List<TrainingSetExercise> getTrainingPlan(Long trainingId) {
		Training training = getTrainingById(trainingId);
		if (training == null) {
			return null;
		}
		return trainingsetExerciseRepository.findAllByTrainingId(trainingId);
	}

	@Override
	public List<TrainingSetExercise> getTrainingPlansByProtegeeId(Long protegeeId) {
		return trainingsetExerciseRepository.findAllByTrainingIdForProtegee(protegeeId);
	}

	@Override
	public List<TrainingSetExercise> getTrainingPlansByTrainerId(Long trainerId) {
		return trainingsetExerciseRepository.findAllByTrainingIdForTrainer(trainerId);
	}

	@Override
	public void deleteTrainingPlan(Long trainingId) {
		List<TrainingSetExercise> plan = getTrainingPlan(trainingId);
		for (TrainingSetExercise set : plan) {
			setExerciseRepository.deleteById(set.getSetExerciseId());
			trainingsetExerciseRepository.delete(set);
		}

	}

	@Override
	public void updateTrainingPlan(Long trainingId, TrainingPlanDTO dto) {
		deleteTrainingPlan(trainingId);
		createTrainingPlan(dto);
	}

}
