package com.example.VerduleriaBot.model;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Producto {
    private String nombre;
    private Double precio;
    private Integer stock;
}

