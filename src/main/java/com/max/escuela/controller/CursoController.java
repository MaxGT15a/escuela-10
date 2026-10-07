package com.max.escuela.controller;


import com.max.escuela.dto.curso.CursoRequestDTO;
import com.max.escuela.dto.curso.CursoResponseDTO;
import com.max.escuela.services.curso.CursoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cursos")
@Tag(name = "Cursos", description = "Gestion de cursos de la escuela")
public class CursoController extends CrudController<CursoRequestDTO, CursoResponseDTO, CursoService>{

    public CursoController(CursoService service) {
        super(service);
    }
}
