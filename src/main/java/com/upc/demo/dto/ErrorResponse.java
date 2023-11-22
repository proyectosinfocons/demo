package com.mitocode.dto;

import java.util.HashMap;
import java.util.Map;

public class ErrorResponse {

	Map<String, Object> error = new HashMap<>();	
	
	public ErrorResponse() {
	}
	
	public ErrorResponse(Map<String, Object> error) {		
		this.error = error;
	}

	public Map<String, Object> getError() {
		return error;
	}

	public void setError(Map<String, Object> error) {
		this.error = error;
	}

}
