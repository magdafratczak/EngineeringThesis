package com.pp.trenerpol.exception;

import com.pp.trenerpol.handler.ResponseHandler;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;


@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CustomizedResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

	@ExceptionHandler(Exception.class)
	@ResponseBody
	public final ResponseEntity<Object> handleAllExceptions(Exception ex, WebRequest request) {
		return ResponseHandler.generateResponse(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, ex.getCause());
	}

	@ExceptionHandler(EntityNotFoundException.class)
	@ResponseBody
	public final ResponseEntity<Object> handleEntityNotFoundException(EntityNotFoundException ex, WebRequest request) {
		return ResponseHandler.generateResponse(ex.getMessage(), HttpStatus.NOT_FOUND, ex.getCause());
	}

	@ExceptionHandler(UserNotAuthorizedForOperationException.class)
	@ResponseBody
	public final ResponseEntity<Object> handleUserNotAuthorizedForOperationException(UserNotAuthorizedForOperationException ex, WebRequest request) {
		return ResponseHandler.generateResponse(ex.getMessage(), HttpStatus.FORBIDDEN, ex.getCause());
	}

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
	                                                              HttpHeaders headers, HttpStatus status, WebRequest request) {
		return ResponseHandler.generateResponse("Validation Failed", HttpStatus.BAD_REQUEST, ex.getCause());

	}
}