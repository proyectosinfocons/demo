package com.upc.demo.service;

import com.upc.demo.model.Producto;
import com.upc.demo.repository.IProductosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductoService implements IProductoService{

    @Autowired
    private IProductosRepository re;

    @Override
    public Producto registrar(Producto e) {
        return re.save(e);
    }

    @Override
    public List<Producto> listado() {
        return re.findAll();
    }

    @Override
    public List<Producto> findProductosByRiesgo(String risk) {

        return re.findProductosByRiesgo(risk);
    }
}
