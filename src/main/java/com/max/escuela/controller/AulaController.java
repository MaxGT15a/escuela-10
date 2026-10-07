package com.max.escuela.controller;

import com.max.escuela.dto.aula.AulaRequestDTO;
import com.max.escuela.dto.aula.AulaResponseDTO;
import com.max.escuela.services.aula.AulaService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/aulas")
@Tag(name = "Aulas", description = "Gestion de aulas de la escuela")
public class AulaController extends CrudController<AulaRequestDTO, AulaResponseDTO, AulaService>{
    public AulaController(AulaService service) {
        super(service);
    }
}
