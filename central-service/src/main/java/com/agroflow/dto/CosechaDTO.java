package src.main.java.com.agroflow.dto;

import lombok.Data;
import javax.validation.constraints.*;

@Data
public class CosechaDTO {
    @NotBlank(message = "El ID del agricultor es obligatorio")
    private String agricultorId;
    
    @NotBlank(message = "El producto es obligatorio")
    private String producto;
    
    @Positive(message = "Las toneladas deben ser mayores a 0")
    private double toneladas;
    
    private String ubicacion;
}