package com.max.escuela.controller;

import com.max.escuela.dto.horario.HorarioRequestDTO;
import com.max.escuela.dto.horario.HorarioResponseDTO;
import com.max.escuela.services.horario.HorarioService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/horarios")
@Tag(name = "Horarios", description = "Gestion de horarios de la escuela")
public class HorarioController extends CrudController<HorarioRequestDTO, HorarioResponseDTO, HorarioService>{
    public HorarioController(HorarioService service) {
        super(service);
    }
}
