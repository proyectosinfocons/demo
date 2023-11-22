package com.mitocode.exceptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.mitocode.dto.ErrorResponse;
import com.mitocode.dto.RestResponse;

import reactor.core.publisher.Mono;

@Component
@Order(-1)
public class GlobalErrorWebExceptionHandler extends AbstractErrorWebExceptionHandler {

	public GlobalErrorWebExceptionHandler(ErrorAttributes errorAttributes, ResourceProperties resourceProperties,
			ApplicationContext applicationContext, ServerCodecConfigurer configurer) {
		super(errorAttributes, resourceProperties, applicationContext);
		this.setMessageWriters(configurer.getWriters());
	}
	
	@Override
	protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
		return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse); //req -> this.renderErrorResponse(req)
	}
	
	private Mono<ServerResponse> renderErrorResponse(ServerRequest request) {
		Map<String, Object> errorGeneral = getErrorAttributes(request, ErrorAttributeOptions.defaults());
		Map<String, Object> mapException = new HashMap<>();		
				
		//request.getBindingResult().getTarget()

		var httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
		String statusCode = String.valueOf(errorGeneral.get("status"));
		
		switch(statusCode) {
			case "500":
				mapException.put("code", "500");
				mapException.put("excepcion", "Error general del backend");
				httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
				break;
				
			case "400":
				Map<String, Object> mapAtributos = request.exchange().getAttributes();
								
				try {
					//Para obtener los mensajes del @Valid - INI				
					/*WebExchangeBindException valorMapError = (WebExchangeBindException) mapAtributos.get("org.springframework.boot.web.reactive.error.DefaultErrorAttributes.ERROR");
					BindingResult result =  valorMapError.getBindingResult();
					List<ObjectError> listaErrores = result.getAllErrors(); 
		
					List<String> mensajesError = listaErrores.stream()
							.map(er -> er.getDefaultMessage())
							.collect(Collectors.toList());
					//Para obtener los mensajes del @Valid - FIN*/
					mapException.put("code", "400");
					mapException.put("excepcion", "Peticion incorrecta");					
					httpStatus = HttpStatus.BAD_REQUEST;
				}catch(Exception e) {										
					mapException.put("error", "500");
					mapException.put("excepcion", "Error general del backend");
					httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;					
				}				
				break;
			case "406":
				mapException.put("code", "406");
				mapException.put("excepcion", "Archivo no subido correctamente");
				httpStatus = HttpStatus.NOT_ACCEPTABLE;
				break;			
			default:
				mapException.put("code", "900");
				mapException.put("excepcion", errorGeneral.get("error"));
				httpStatus = HttpStatus.CONFLICT;
				break;
		}
		
		RestResponse rr = new RestResponse();
		rr.setContent(new ArrayList<>());
		rr.setErrors(Arrays.asList(new ErrorResponse(mapException)));	
		
		return ServerResponse.status(httpStatus)
				.contentType(MediaType.APPLICATION_JSON)
				.body(BodyInserters.fromValue(rr)); //mapException
	}

}
