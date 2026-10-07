package com.max.escuela.enums;

import com.max.escuela.exceptions.InvalidDataException;
import com.max.escuela.utils.StringCustomUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum DiaSemana {
    LUNES("Lunes"),
    MARTES("Martes"),
    MIERCOLES("Miercoles"),
    JUEVES("Jueves"),
    VIERNES("Viernes"),
    SABADO("Sabado");
    private final String description;
    
    public static DiaSemana obtenerDiaPorDescripcion(String description){
        StringCustomUtils.validarNoVacioNoNull(description, "El día de la semana es requerido");
        String descripcionNormalizada = StringCustomUtils.normalizarTexto(description);

        for (DiaSemana diaSemana : values()){
            if(StringCustomUtils.normalizarTexto(diaSemana.getDescription()).equals(descripcionNormalizada))
                return diaSemana;
        }

        throw new InvalidDataException("No existe un dia con descripcion: " + description);
    }
}
