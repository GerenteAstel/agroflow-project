package src.main.java.com.agroflow.repository;

import com.agroflow.model.Cosecha;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface CosechaRepository extends JpaRepository<Cosecha, UUID> {
}
