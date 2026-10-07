package com.max.escuela.controller;

import com.max.escuela.dto.alumno.AlumnoRequestDTO;
import com.max.escuela.dto.alumno.AlumnoResponseDTO;
import com.max.escuela.services.alumno.AlumnoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/alumnos")
@Tag(name = "Alumnos", description = "Gestion de alumnos de la escuela")
public class AlumnoController extends CrudController<AlumnoRequestDTO, AlumnoResponseDTO, AlumnoService>{
    public AlumnoController(AlumnoService service) {
        super(service);
    }
}
