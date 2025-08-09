package src.main.java.com.agroflow.controller;

import src.main.java.com.agroflow.dto.CosechaDTO;
import com.agroflow.model.Cosecha;
import com.agroflow.repository.CosechaRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/cosechas")
public class CosechaController {

    private final CosechaRepository cosechaRepository;
    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public CosechaController(CosechaRepository cosechaRepository, RabbitTemplate rabbitTemplate) {
        this.cosechaRepository = cosechaRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @PostMapping
    public ResponseEntity<Cosecha> registrarCosecha(@RequestBody CosechaDTO cosechaDTO) {
        // Validación de datos
        if (cosechaDTO.getAgricultorId() == null || cosechaDTO.getAgricultorId().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El ID del agricultor es requerido");
        }

        if (cosechaDTO.getProducto() == null || cosechaDTO.getProducto().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El producto es requerido");
        }

        if (cosechaDTO.getToneladas() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Las toneladas deben ser mayores a 0");
        }

        try {
            // Crear y guardar la entidad
            Cosecha cosecha = new Cosecha();
            cosecha.setAgricultorId(UUID.fromString(cosechaDTO.getAgricultorId()));
            cosecha.setProducto(cosechaDTO.getProducto());
            cosecha.setToneladas(cosechaDTO.getToneladas());
            cosecha.setUbicacion(cosechaDTO.getUbicacion());
            cosecha.setEstado("REGISTRADA");

            Cosecha savedCosecha = cosechaRepository.save(cosecha);

            // Publicar evento en RabbitMQ
            publicarEventoCosecha(savedCosecha);

            return ResponseEntity.status(HttpStatus.CREATED).body(savedCosecha);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "UUID del agricultor no válido");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al registrar la cosecha");
        }
    }

    private void publicarEventoCosecha(Cosecha cosecha) {
        Map<String, Object> evento = new HashMap<>();
        evento.put("event_id", UUID.randomUUID().toString());
        evento.put("event_type", "nueva_cosecha");
        evento.put("timestamp", Instant.now().toString());
        
        Map<String, Object> payload = new HashMap<>();
        payload.put("cosecha_id", cosecha.getId().toString());
        payload.put("producto", cosecha.getProducto());
        payload.put("toneladas", cosecha.getToneladas());
        payload.put("ubicacion", cosecha.getUbicacion());
        payload.put("requiere_insumos", Arrays.asList(
            "Semilla " + cosecha.getProducto(),
            "Fertilizante N-PK"
        ));
        
        evento.put("payload", payload);
        
        rabbitTemplate.convertAndSend(
            "cosechas.exchange", // Nombre del exchange
            "nueva.cosecha",    // Routing key
            evento              // Cuerpo del mensaje
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cosecha> obtenerCosecha(@PathVariable UUID id) {
        return cosechaRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Cosecha no encontrada"));
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<Cosecha> actualizarEstado(
            @PathVariable UUID id,
            @RequestParam String estado,
            @RequestParam(required = false) UUID facturaId) {
        
        return cosechaRepository.findById(id)
                .map(cosecha -> {
                    cosecha.setEstado(estado);
                    if (facturaId != null) {
                        cosecha.setFacturaId(facturaId);
                    }
                    return ResponseEntity.ok(cosechaRepository.save(cosecha));
                })
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Cosecha no encontrada"));
    }
}