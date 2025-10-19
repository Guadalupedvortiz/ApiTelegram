package com.example.VerduleriaBot.service;

import com.example.VerduleriaBot.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class TelegramService {

    @Value("${telegram.bot.token}")
    private String telegramToken;

    private final RestTemplate restTemplate;
    private final Map<Long, Carrito> carritos = new ConcurrentHashMap<>();

    // Base de datos simple de productos
    private final Map<String, Producto> productos = Map.of(
            "manzana", new Producto("Manzana", 50.0, 100),
            "naranja", new Producto("Naranja", 30.0, 80),
            "banana", new Producto("Banana", 40.0, 120),
            "zanahoria", new Producto("Zanahoria", 25.0, 60),
            "tomate", new Producto("Tomate", 60.0, 70),
            "lechuga", new Producto("Lechuga", 35.0, 40)
    );

    public void processUpdate(TelegramUpdate update) {
        if (update == null || update.getMessage() == null) return;

        Message message = update.getMessage();
        Long chatId = message.getChat().getId();
        String text = message.getText();

        if (text == null) return;

        try {
            String comando = text.split(" ")[0].toLowerCase();

            switch (comando) {
                case "/start" -> sendWelcomeMessage(chatId);
                case "/productos" -> sendProductList(chatId);
                case "/agregar" -> addToCart(chatId, text);
                case "/carrito" -> showCart(chatId);
                case "/comprar" -> checkout(chatId);
                case "/limpiar" -> clearCart(chatId);
                default -> sendMessage(chatId, "❓ Comando no reconocido. Usa /start para ayuda.");
            }
        } catch (Exception e) {
            sendMessage(chatId, "❌ Error procesando comando.");
        }
    }

    private void sendWelcomeMessage(Long chatId) {
        String message = """
            🍎 *VERDULERÍA BOT* 🥕
            
            *Comandos disponibles:*
            /start - Mensaje de bienvenida
            /productos - Ver productos
            /agregar [producto] [cantidad] - Agregar al carrito
            /carrito - Ver carrito
            /comprar - Finalizar compra
            /limpiar - Vaciar carrito
            
            *Ejemplos:*
            /agregar manzana 2
            /agregar zanahoria 5
            """;
        sendMessage(chatId, message);
    }

    private void sendProductList(Long chatId) {
        StringBuilder sb = new StringBuilder("🏪 *Productos disponibles:*\n\n");
        productos.forEach((key, producto) -> {
            sb.append(String.format("• %s - $%.2f\n", producto.getNombre(), producto.getPrecio()));
        });
        sb.append("\nUsa: /agregar [producto] [cantidad]");
        sendMessage(chatId, sb.toString());
    }

    private void addToCart(Long chatId, String text) {
        String[] parts = text.split(" ");
        if (parts.length != 3) {
            sendMessage(chatId, "📝 Formato: /agregar [producto] [cantidad]");
            return;
        }

        String productName = parts[1].toLowerCase();
        if (!productos.containsKey(productName)) {
            sendMessage(chatId, "❌ Producto no encontrado.");
            return;
        }

        try {
            int quantity = Integer.parseInt(parts[2]);
            if (quantity <= 0) {
                sendMessage(chatId, "❌ Cantidad debe ser mayor a 0.");
                return;
            }

            Producto product = productos.get(productName);
            Carrito cart = carritos.computeIfAbsent(chatId, k -> new Carrito());
            cart.agregarProducto(product.getNombre(), quantity, product.getPrecio());

            sendMessage(chatId, String.format(
                    "✅ Agregado: %d %s - $%.2f\n🛒 Total: $%.2f",
                    quantity, product.getNombre(), quantity * product.getPrecio(), cart.getTotal()));

        } catch (NumberFormatException e) {
            sendMessage(chatId, "❌ Cantidad inválida.");
        }
    }

    private void showCart(Long chatId) {
        Carrito cart = carritos.get(chatId);
        if (cart == null || cart.estaVacio()) {
            sendMessage(chatId, "🛒 Carrito vacío.");
            return;
        }

        StringBuilder sb = new StringBuilder("🛒 *Tu carrito:*\n\n");
        cart.getItems().forEach((product, quantity) -> {
            Producto p = productos.get(product.toLowerCase());
            sb.append(String.format("• %s: %d x $%.2f\n", p.getNombre(), quantity, p.getPrecio()));
        });
        sb.append(String.format("\n💵 *Total: $%.2f*", cart.getTotal()));

        sendMessage(chatId, sb.toString());
    }

    private void checkout(Long chatId) {
        Carrito cart = carritos.get(chatId);
        if (cart == null || cart.estaVacio()) {
            sendMessage(chatId, "🛒 Carrito vacío.");
            return;
        }

        String message = String.format("🎉 *¡Compra realizada!*\n\nTotal: $%.2f\n\n¡Gracias!", cart.getTotal());
        sendMessage(chatId, message);
        carritos.remove(chatId);
    }

    private void clearCart(Long chatId) {
        Carrito cart = carritos.get(chatId);
        if (cart != null) cart.limpiar();
        sendMessage(chatId, "🗑 Carrito limpiado.");
    }

    private void sendMessage(Long chatId, String text) {
        try {
            String url = "https://api.telegram.org/bot" + telegramToken + "/sendMessage";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("chat_id", chatId);
            requestBody.put("text", text);
            requestBody.put("parse_mode", "Markdown");

            restTemplate.postForObject(url, requestBody, String.class);

        } catch (Exception e) {
            System.err.println("Error enviando mensaje: " + e.getMessage());
        }
    }
}