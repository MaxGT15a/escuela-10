package com.max.escuela.mapper;

import com.max.escuela.dto.aula.AulaRequestDTO;
import com.max.escuela.dto.aula.AulaResponseDTO;
import com.max.escuela.dto.datos.DatosAulaDTO;
import com.max.escuela.entities.Aula;
import org.springframework.stereotype.Component;

@Component
public class AulaMapper implements CommonMapper<AulaRequestDTO, AulaResponseDTO, Aula>{
    @Override
    public Aula requestAEntidad(AulaRequestDTO request) {
        return request == null ? null
            : Aula.crear(
                request.nombre().trim(),
                request.capacidad()
        );
    }

    @Override
    public AulaResponseDTO entidadAResponse(Aula entidad) {
        return entidad == null ? null
                : new AulaResponseDTO(
                        entidad.getId(),
                        entidad.getNombre(),
                        entidad.getCapacidad()
        );
    }

    public DatosAulaDTO entidadADatosAula(Aula aula){
        return aula == null ? null
                : new DatosAulaDTO(
                aula.getNombre(),
                aula.getCapacidad()
        );
    }
}
