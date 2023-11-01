package com.upc.demo.repository;

import com.upc.demo.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface IProductosRepository extends JpaRepository<Producto,Integer> {
        public List<Producto>findProductosByRiesgo(String risk);
}
