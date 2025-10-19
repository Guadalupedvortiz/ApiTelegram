package com.example.VerduleriaBot.model;


import lombok.Data;
import java.util.*;

@Data
public class Carrito {
    private Map<String, Integer> items = new HashMap<>();
    private Double total = 0.0;

    public void agregarProducto(String producto, Integer cantidad, Double precio) {
        items.put(producto, items.getOrDefault(producto, 0) + cantidad);
        total += cantidad * precio;
    }

    public void limpiar() {
        items.clear();
        total = 0.0;
    }

    public boolean estaVacio() {
        return items.isEmpty();
    }
}
