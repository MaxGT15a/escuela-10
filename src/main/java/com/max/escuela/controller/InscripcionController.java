package com.max.escuela.controller;

import com.max.escuela.dto.inscripcion.InscripcionRequestDTO;
import com.max.escuela.dto.inscripcion.InscripcionResponseDTO;
import com.max.escuela.services.inscripcion.InscripcionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inscripciones")
@Tag(name = "Inscripcion", description = "Gestion de inscripciones de la escuela")
public class InscripcionController extends CrudController<InscripcionRequestDTO, InscripcionResponseDTO, InscripcionService>{
    public InscripcionController(InscripcionService service) {
        super(service);
    }
}
