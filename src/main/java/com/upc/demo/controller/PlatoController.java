package com.mitocode.controller;

import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.linkTo;
import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.methodOn;
import static reactor.function.TupleUtils.function;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
//import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mitocode.dto.RestResponse;
import com.mitocode.model.Plato;
import com.mitocode.pagination.PageSupport;
import com.mitocode.service.IPlatoService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;


@RestController
@RequestMapping("/platos")
public class PlatoController {
	
	private static final Logger log = LoggerFactory.getLogger(PlatoController.class);
	
	@Autowired
	private IPlatoService service;
	
	//@Async Future ListenableFuture, etc
	//@PreAuthorize("hasAuthority('ADMIN')")
	//@PreAuthorize("@authExample.miMetodo()")
	@GetMapping
	public Mono<ResponseEntity<Flux<Plato>>> listar() {
		Flux<Plato> fxPlatos = service.listar();
		
		service.listar().repeat(3).publishOn(Schedulers.parallel()).subscribe(i -> log.info(i.toString()));
		service.listar().repeat(3).parallel().runOn(Schedulers.parallel()).subscribe(i -> log.info(i.toString()));
		
		return Mono.just(ResponseEntity
				.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(fxPlatos)
				);
	}
	
	@GetMapping("/RR")
	public Mono<ResponseEntity<RestResponse>> listarRR() {
		Flux<Plato> fxPlatos = service.listar();
		
		return fxPlatos
			.collectList()
			.map(lista -> {
				RestResponse rr = new RestResponse();
				rr.setContent(lista);
				rr.setErrors(new ArrayList<>());	
				return rr;
			})
			.map(rr -> ResponseEntity
					.ok()
					.contentType(MediaType.APPLICATION_JSON)
					.body(rr));			
	}
	
	@GetMapping("/{id}")
	public Mono<ResponseEntity<Plato>> listarPorId(@PathVariable("id") String id){
		return service.listarPorId(id) //Mono<Plato>
				.map(p -> ResponseEntity.ok()
						.contentType(MediaType.APPLICATION_JSON)
						.body(p)
						) //Mono<ResponseEntity<Plato>>
				.defaultIfEmpty(ResponseEntity.notFound().build());
				
	}
	
	@GetMapping("/RR/{id}")
	public Mono<ResponseEntity<RestResponse>> listarPorIdRR(@PathVariable("id") String id){
		return service.listarPorId(id)
				.map(p -> {
					RestResponse rr = new RestResponse();
					rr.setContent(Arrays.asList(p));
					rr.setErrors(new ArrayList<>());	
					return rr;
				})
				.map(p -> ResponseEntity.ok()
						.contentType(MediaType.APPLICATION_JSON)
						.body(p)
						)
				.defaultIfEmpty(ResponseEntity.notFound().build());
				
	}
	
	@PostMapping
	public Mono<ResponseEntity<Plato>> registrar(@Valid @RequestBody Plato p, final ServerHttpRequest req){
		return service.registrar(p)
				.map(pl -> ResponseEntity.created(URI.create(req.getURI().toString().concat("/").concat(pl.getId())))
						.contentType(MediaType.APPLICATION_JSON)
						.body(pl)
				);
	}
	
	@PutMapping("/{id}")
	public Mono<ResponseEntity<Plato>> modificar(@Valid @RequestBody Plato p, @PathVariable("id") String id){
		
		Mono<Plato> monoPlato = Mono.just(p);
		Mono<Plato> monoBD = service.listarPorId(id);
		
		return monoBD
				.zipWith(monoPlato, (bd, pl) -> {
					bd.setId(id);
					bd.setNombre(pl.getNombre());
					bd.setPrecio(pl.getPrecio());
					bd.setEstado(pl.getEstado());
					return bd;
				})
				.flatMap(service::modificar) //bd -> service.modificar(bd)
				.map(pl -> ResponseEntity.ok()
						.contentType(MediaType.APPLICATION_JSON)
						.body(pl))
				.defaultIfEmpty(new ResponseEntity<Plato>(HttpStatus.NOT_FOUND));
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
	
	private Plato platoHateoas;
	
	@GetMapping("/hateoas/{id}")
	public Mono<EntityModel<Plato>> listarHateoasPorId(@PathVariable("id") String id){
		//localhost:8080/platos/60779cc08e37a27164468033	
		Mono<Link> link1 =linkTo(methodOn(PlatoController.class).listarPorId(id)).withSelfRel().toMono();
		Mono<Link> link2 =linkTo(methodOn(PlatoController.class).listarPorId(id)).withSelfRel().toMono();
		
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
	public Mono<ResponseEntity<PageSupport<Plato>>> listarPagebale(
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

}
