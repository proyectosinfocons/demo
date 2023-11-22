package com.mitocode.handler;


import static org.springframework.web.reactive.function.BodyInserters.fromValue;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.util.Map;

import org.cloudinary.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.Part;
import org.springframework.stereotype.Component;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.mitocode.exceptions.ArchivoException;
import com.mitocode.model.Cliente;
import com.mitocode.service.IClienteService;
import com.mitocode.validators.RequestValidator;

import reactor.core.publisher.Mono;

@Component
public class ClienteHandler {
	
	@Autowired
	private IClienteService service;
	
	@Autowired
	private Validator validador;	
	
	@Autowired
	private RequestValidator validadorGeneral;
		
	public Mono<ServerResponse> listar(ServerRequest req){
		return ServerResponse
				.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(service.listar(), Cliente.class);
	}
	
	public Mono<ServerResponse> listarPorId(ServerRequest req){
		String id = req.pathVariable("id");
		
		return service.listarPorId(id)
				.flatMap(p -> ServerResponse
						.ok()
						.contentType(MediaType.APPLICATION_JSON)
						.body(fromValue(p))
				)
				.switchIfEmpty(ServerResponse.notFound().build());
	}
	
	/*public Mono<ServerResponse> registrar(ServerRequest req) {
		Mono<Cliente> monoPlato = req.bodyToMono(Cliente.class);
		
		return monoPlato
				.flatMap(service::registrar)//p -> service.registrar(p)
				.flatMap(p -> ServerResponse.created(URI.create(req.uri().toString().concat(p.getId())))
						.contentType(MediaType.APPLICATION_JSON)
						.body(fromValue(p))
				);
	}*/
	
	public Mono<ServerResponse> registrar(ServerRequest req) {
		Mono<Cliente> monoPlato = req.bodyToMono(Cliente.class);
		
		/*return monoPlato.flatMap(p -> {
			Errors errores = new BeanPropertyBindingResult(p, Cliente.class.getName());
			validador.validate(p, errores);
			
			if(errores.hasErrors()) {
				return Flux.fromIterable(errores.getFieldErrors())
						.map(error -> new ValidacionDTO(error.getField(), error.getDefaultMessage()))						
						.collectList() //Mono
						.flatMap(listaErrores -> {							
							return ServerResponse.badRequest()
									.contentType(MediaType.APPLICATION_JSON)
									.body(fromValue(listaErrores));	
									}
								); 
			}else {
				return service.registrar(p)
						.flatMap(pdb -> ServerResponse
						.created(URI.create(req.uri().toString().concat(p.getId())))
						.contentType(MediaType.APPLICATION_JSON)
						.body(fromValue(pdb))
						);
			}
		});*/
		
		return monoPlato
				//validacion
				.flatMap(validadorGeneral::validate)
				.flatMap(service::registrar)
				.flatMap(p -> ServerResponse.created(URI.create(req.uri().toString().concat(p.getId())))
						.contentType(MediaType.APPLICATION_JSON)
						.body(fromValue(p))
				);
	}
	
	public Mono<ServerResponse> modificar(ServerRequest req) {
		
		Mono<Cliente> monoPlato = req.bodyToMono(Cliente.class);		
		Mono<Cliente> monoBD = service.listarPorId(req.pathVariable("id"));
		
		return monoBD
				.zipWith(monoPlato, (bd, p) -> {				
					bd.setId(req.pathVariable("id"));
					bd.setNombres(p.getNombres());
					bd.setApellidos(p.getApellidos());
					bd.setFechaNac(p.getFechaNac());
					bd.setUrlFoto(p.getUrlFoto());
					return bd;
				})							
				.flatMap(validadorGeneral::validate)
				.flatMap(service::modificar)
				.flatMap(p -> ServerResponse.ok()
						.contentType(MediaType.APPLICATION_JSON)
						.body(fromValue(p))
				)
				.switchIfEmpty(ServerResponse.notFound().build());
	}
	
	public Mono<ServerResponse> eliminar(ServerRequest req){
		String id = req.pathVariable("id");
		
		return service.listarPorId(id)
				.flatMap(p -> service.eliminar(p.getId())
						.then(ServerResponse.noContent().build())
				)
				.switchIfEmpty(ServerResponse.notFound().build());
	}
	
	public Mono<ServerResponse> subirImagen(ServerRequest req) {
		/*Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
				"cloud_name", "xxxx",
				"api_key", "xxx",
				"api_secret", "xxxx"));*/
		
		//String id = req.pathVariable("id");		
		
		req.formData()
			.map(value -> {
				System.out.println(value);
				return Mono.empty();
			});
		return Mono.empty();
		
		/*File f = Files.createTempFile("temp", file.filename()).toFile();
		
		service.listarPorId(id)
				.flatMap(c -> {
					try {
						File f = Files.createTempFile("temp", file.filename()).toFile();
						file.transferTo(f).block(); //para tener la transferencia lista
						
						Map response= cloudinary.uploader().upload(f, ObjectUtils.asMap("resource_type", "auto"));
						JSONObject json = new JSONObject(response);
						String url = json.getString("url");
						
						c.setUrlFoto(url);
						return service.modificar(c).thenReturn(ResponseEntity.ok().body(c));
					}catch(Exception e) {
						throw new ArchivoException("error al subir el archivo");  
					}	
					//return Mono.just(ResponseEntity.ok().body(c));
				})
				.defaultIfEmpty(ResponseEntity.notFound().build());	*/
	}
}
