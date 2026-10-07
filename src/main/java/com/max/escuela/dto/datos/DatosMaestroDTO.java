package com.max.escuela.dto.datos;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Datos de un maestro")
public record DatosMaestroDTO(

        @Schema(description = "Nombre del maestro", example = "Laura Martínez Martínez")
        String nombre,

        @Schema(description = "Email del maestro", example = "laura.martinez@escuela.com")
        String email,

        @Schema(description = "Numero telefonico del maestro", example = "5551010789")
        String telefono
) { }