package com.max.escuela.controller;

import com.max.escuela.dto.calificacion.CalificacionRequestDTO;
import com.max.escuela.dto.calificacion.CalificacionResponseDTO;
import com.max.escuela.services.calificacion.CalificacionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/calificaciones")
@Tag(name = "Calificaciones", description = "Gestion de calificaciones de la escuela")
public class CalificacionController extends CrudController<CalificacionRequestDTO, CalificacionResponseDTO, CalificacionService>{
    public CalificacionController(CalificacionService service) {
        super(service);
    }
}
