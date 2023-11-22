package com.mitocode.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
public class ArchivoException extends RuntimeException{

	public ArchivoException(String mensaje) {
		super(mensaje);
	}
}
