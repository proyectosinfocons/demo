package com.upc.demo.model;


import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table(name="Producto")
public class Producto{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int codigo;

    private String nombre;

    private String volumen;


    private  int factor;

    private  String riesgo;

}
