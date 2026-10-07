package com.max.escuela.controller;

import com.max.escuela.dto.grupo.GrupoRequestDTO;
import com.max.escuela.dto.grupo.GrupoResponseDTO;
import com.max.escuela.services.grupo.GrupoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/grupos")
@Tag(name = "Grupos", description = "Gestion de grupos de la escuela")
public class GrupoController extends CrudController<GrupoRequestDTO, GrupoResponseDTO, GrupoService>{
    public GrupoController(GrupoService service) {
        super(service);
    }
}
