package com.max.escuela.controller;

import com.max.escuela.dto.datos.DatosCursoDTO;
import com.max.escuela.dto.maestro.MaestroRequestDTO;
import com.max.escuela.dto.maestro.MaestroResponseDTO;
import com.max.escuela.services.maestro.MaestroService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/maestros")
@Tag(name = "Maestros", description = "Gestion de maestros de la escuela")
public class MaestroController extends CrudController<MaestroRequestDTO, MaestroResponseDTO, MaestroService> {

    public MaestroController(MaestroService service) {
        super(service);
    }

    @GetMapping("/cursos/{id}")
    public ResponseEntity<List<DatosCursoDTO>> obtenerCursosDeUnMaestroConId(
            @Parameter(description = "Identificador del maestro", example = "1")
            @PathVariable @Positive(message = "El identificador debe ser positivo") Long id
    ){
        return ResponseEntity.ok(service.obtenerCursosDeUnMaestroConId(id));
    }
}
