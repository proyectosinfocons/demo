package com.mitocode.model;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Document(collection = "platos")
public class Plato {

	@Id
	private String id;

	@NotEmpty
	@Size(min = 3, message = "nombre minimo 3")
	@Field(name = "nombre")
	private String nombre;

	private Double precio;

	@NotNull
	// @Pattern(regexp = "")
	@Field(name = "estado")
	private Boolean estado;
	
	public Plato() {}

	public Plato(String id, @NotEmpty @Size(min = 3, message = "nombre minimo 3") String nombre, Double precio,
			@NotNull Boolean estado) {
		super();
		this.id = id;
		this.nombre = nombre;
		this.precio = precio;
		this.estado = estado;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public Double getPrecio() {
		return precio;
	}

	public void setPrecio(Double precio) {
		this.precio = precio;
	}

	public Boolean getEstado() {
		return estado;
	}

	public void setEstado(Boolean estado) {
		this.estado = estado;
	}

	@Override
	public String toString() {
		return "Plato [id=" + id + "]";
	}

}
