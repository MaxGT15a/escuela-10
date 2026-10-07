package com.max.escuela.services.maestro;

import com.max.escuela.dto.datos.DatosCursoDTO;
import com.max.escuela.dto.maestro.MaestroRequestDTO;
import com.max.escuela.dto.maestro.MaestroResponseDTO;
import com.max.escuela.services.CrudService;

import java.util.List;

public interface MaestroService extends CrudService<MaestroRequestDTO, MaestroResponseDTO> {
    List<DatosCursoDTO> obtenerCursosDeUnMaestroConId(Long id);
}
