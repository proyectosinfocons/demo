package com.mitocode.dto;

import java.util.List;

public class RestResponse {

	private List<?> content;
	private List<ErrorResponse> errors;

	public List<?> getContent() {
		return content;
	}

	public void setContent(List<?> content) {
		this.content = content;
	}

	public List<ErrorResponse> getErrors() {
		return errors;
	}

	public void setErrors(List<ErrorResponse> errors) {
		this.errors = errors;
	}

}
