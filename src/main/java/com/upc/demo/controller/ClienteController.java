package com.mitocode.controller;

import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.linkTo;
import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.methodOn;
import static reactor.function.TupleUtils.function;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.util.Map;

import javax.validation.Valid;

import org.cloudinary.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.mitocode.exceptions.ArchivoException;
import com.mitocode.exceptions.ArchivoException;
import com.mitocode.model.Cliente;
import com.mitocode.pagination.PageSupport;
import com.mitocode.service.IClienteService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;


@RestController
@RequestMapping("/clientes")
public class ClienteController {
	
	@Autowired
	private IClienteService service;
	
	@GetMapping
	public Mono<ResponseEntity<Flux<Cliente>>> listar() {
		Flux<Cliente> fxPlatos = service.listar();
		
		return Mono.just(ResponseEntity
				.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(fxPlatos)
				);
	}
	
	@GetMapping("/{id}")
	public Mono<ResponseEntity<Cliente>> listarPorId(@PathVariable("id") String id){
		return service.listarPorId(id) //Mono<Cliente>
				.map(p -> ResponseEntity.ok()
						.contentType(MediaType.APPLICATION_JSON)
						.body(p)
						) //Mono<ResponseEntity<Cliente>>
				.defaultIfEmpty(ResponseEntity.notFound().build());
				
	}
	
	@PostMapping
	public Mono<ResponseEntity<Cliente>> registrar(@Valid @RequestBody Cliente p, final ServerHttpRequest req){
		return service.registrar(p)
				.map(pl -> ResponseEntity.created(URI.create(req.getURI().toString().concat("/").concat(pl.getId())))
						.contentType(MediaType.APPLICATION_JSON)
						.body(pl)
				);
	}
	
	@PutMapping("/{id}")
	public Mono<ResponseEntity<Cliente>> modificar(@Valid @RequestBody Cliente p, @PathVariable("id") String id){
		
		Mono<Cliente> monoPlato = Mono.just(p);
		Mono<Cliente> monoBD = service.listarPorId(id);
		
		return monoBD
				.zipWith(monoPlato, (bd, pl) -> {
					bd.setId(id);
					bd.setNombres(p.getNombres());
					bd.setApellidos(p.getApellidos());
					bd.setFechaNac(p.getFechaNac());
					bd.setUrlFoto(p.getUrlFoto());
					return bd;
				})
				.flatMap(service::modificar) //bd -> service.modificar(bd)
				.map(pl -> ResponseEntity.ok()
						.contentType(MediaType.APPLICATION_JSON)
						.body(pl))
				.defaultIfEmpty(new ResponseEntity<Cliente>(HttpStatus.NOT_FOUND));
	}
	
	@DeleteMapping("/{id}")
	public Mono<ResponseEntity<Void>> eliminar(@PathVariable("id") String id){
		return service.listarPorId(id)
				.flatMap(p -> {
					return service.eliminar(p.getId()) //Mono<Void>
							.then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)));					
				})
				.defaultIfEmpty(new ResponseEntity<Void>(HttpStatus.NOT_FOUND));
	}
	
	private Cliente platoHateoas;
	
	@GetMapping("/hateoas/{id}")
	public Mono<EntityModel<Cliente>> listarHateoasPorId(@PathVariable("id") String id){
		//localhost:8080/platos/60779cc08e37a27164468033	
		Mono<Link> link1 =linkTo(methodOn(ClienteController.class).listarPorId(id)).withSelfRel().toMono();
		Mono<Link> link2 =linkTo(methodOn(ClienteController.class).listarPorId(id)).withSelfRel().toMono();
		
		//PRACTICA NO RECOMENDADA
		/*return service.listarPorId(id)
				.flatMap(p -> {
					this.platoHateoas = p;
					return link1;
				})
				.map(lk -> {
					return EntityModel.of(this.platoHateoas, lk);
				});*/
		
		//PRACTICA INTERMEDIA
		/*return service.listarPorId(id)
				.flatMap(p -> {
					return link1.map(lk -> EntityModel.of(p, lk));
				});*/
		
		//PRACTICA IDEAL
		/*return service.listarPorId(id)
				.zipWith(link1, (p, lk) -> EntityModel.of(p, lk));*/
		
		//Más de 1 link
		return link1.zipWith(link2)
				.map(function((left, right) -> Links.of(left, right)))				
				.zipWith(service.listarPorId(id), (lk, p) -> EntityModel.of(p, lk));
	}
	
	@GetMapping("/pageable")
	public Mono<ResponseEntity<PageSupport<Cliente>>> listarPagebale(
			@RequestParam(name = "page", defaultValue = "0") int page,
		    @RequestParam(name = "size", defaultValue = "10") int size
			){
		
		Pageable pageRequest = PageRequest.of(page, size);
		
		return service.listarPage(pageRequest)
				.map(p -> ResponseEntity.ok()
						.contentType(MediaType.APPLICATION_JSON)
						.body(p)	
						)
				.defaultIfEmpty(ResponseEntity.noContent().build());
	}

	@PostMapping("/v1/subir/{id}")
	public Mono<ResponseEntity<Cliente>> subir(@PathVariable String id, @RequestPart FilePart file) {
				
		Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
				"cloud_name", "xxxx",
				"api_key", "xxx",
				"api_secret", "xxxx"));  
		
		return service.listarPorId(id)				
				.publishOn(Schedulers.boundedElastic())
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
				.defaultIfEmpty(ResponseEntity.notFound().build());				
	}

	@PostMapping("/v2/subir/{id}")
	public Mono<ResponseEntity<Cliente>> subirV2(@PathVariable String id, @RequestPart FilePart file) throws IOException{
				
		Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
				"cloud_name", "xxxx",
				"api_key", "xxxx",
				"api_secret", "xxxxx"));  
		
		File f = Files.createTempFile("temp", file.filename()).toFile();
		
		return file.transferTo(f)
				.then(service.listarPorId(id)
						.publishOn(Schedulers.boundedElastic())
						.flatMap(c -> {
							Map response;
							try {
								response = cloudinary.uploader().upload(f , ObjectUtils.asMap("resource_type", "auto"));
									        
						        JSONObject json=new JSONObject(response);
					            String url=json.getString("url");			            
					            
						        c.setUrlFoto(url);
						        
							} catch (IOException e) {				
								e.printStackTrace();
							}
							return service.modificar(c).then(Mono.just(ResponseEntity.ok().body(c)));
						})
						.defaultIfEmpty(ResponseEntity.notFound().build())
					);	
	}

}
