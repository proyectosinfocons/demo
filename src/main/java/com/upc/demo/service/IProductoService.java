package com.upc.demo.service;

import com.upc.demo.model.Producto;

import java.util.List;

public interface IProductoService{


     Producto registrar(Producto e);

    List<Producto> listado();

    List<Producto>findProductosByRiesgo(String risk);
}
