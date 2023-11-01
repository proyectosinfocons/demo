package com.upc.demo;

import com.upc.demo.model.Producto;
import com.upc.demo.service.ProductoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class DemoApplication implements CommandLineRunner {


    private static Logger log = LoggerFactory.getLogger(DemoApplication.class);


    @Autowired
    private ProductoService service;


    public static void main(String[] args) {

        SpringApplication.run(DemoApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        Producto po1 = new Producto();
        po1.setCodigo(1);
        po1.setNombre("P1");
        po1.setVolumen("2.3");
        po1.setFactor(2);
        if (po1.getFactor() < 3) {
            po1.setRiesgo("Bajo");
        }

        service.registrar(po1);


        Producto po2 = new Producto();
        po2.setCodigo(2);
        po2.setNombre("P2");
        po2.setVolumen("2.222");
        po2.setFactor(4);
        if (po2.getFactor() >= 3) {
            po2.setRiesgo("Alto");
        }

        service.registrar(po2);


        Producto po3 = new Producto();
        po3.setCodigo(3);
        po3.setNombre("P3");
        po3.setVolumen("3.333");
        po3.setFactor(8);
        if (po3.getFactor() >= 3) {
            po3.setRiesgo("Alto");
        }

        service.registrar(po3);


        Producto po4 = new Producto();
        po4.setCodigo(4);
        po4.setNombre("P4");
        po4.setVolumen("4.444");
        po4.setFactor(9);
        if (po4.getFactor() >= 3) {
            po4.setRiesgo("Alto");
        }

        service.registrar(po4);


        Producto po5 = new Producto();
        po5.setCodigo(4);
        po5.setNombre("P5");
        po5.setVolumen("5.555");
        po5.setFactor(3);
        if (po5.getFactor() >= 3) {
            po5.setRiesgo("Alto");
        }


        service.registrar(po5);
        System.out.print("===========================================================================");
        System.out.print("Listado total : " + service.listado());
        log.info("Listado total : " + service.listado());
        System.out.print("=============================================================================");


        System.out.print("============================================================================");
        System.out.print("Listado total de prodcutos con un riesgo alto : " + service.findProductosByRiesgo("Alto"));
        log.info("Listado total de prodcutos con un riesgo alto : " + service.findProductosByRiesgo("Alto"));
        System.out.print("=============================================================================");

    }


}
