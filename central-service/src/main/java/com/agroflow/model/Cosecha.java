package src.main.java.com.agroflow.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;

@Data
@Entity
@Table(name = "cosechas")
public class Cosecha {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @Column(nullable = false)
    private UUID agricultorId;
    
    @Column(nullable = false)
    private String producto;
    
    @Column(nullable = false)
    private double toneladas;
    
    private String ubicacion;
    
    @Column(nullable = false)
    private String estado = "REGISTRADA";
    
    private UUID facturaId;
}
