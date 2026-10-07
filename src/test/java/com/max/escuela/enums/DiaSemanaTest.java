package com.max.escuela.enums;

import com.max.escuela.exceptions.InvalidDataException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DiaSemanaTest {

    // Si renombras el método (typo "Descriptcion"), actualiza las llamadas de este archivo.

    @ParameterizedTest
    @CsvSource({
            "Lunes, LUNES",
            "LUNES, LUNES",
            "lunes, LUNES",
            "Martes, MARTES",
            "Miercoles, MIERCOLES",
            "Miércoles, MIERCOLES",
            "MIÉRCOLES, MIERCOLES",
            "Jueves, JUEVES",
            "Viernes, VIERNES",
            "Sabado, SABADO",
            "Sábado, SABADO",
            "'  lunes  ', LUNES"
    })
    void obtenerDiaPorDescriptcion_debeResolverElDia_ignorandoMayusculasAcentosYEspacios(
            String descripcion, DiaSemana esperado) {

        assertThat(DiaSemana.obtenerDiaPorDescriptcion(descripcion)).isEqualTo(esperado);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void obtenerDiaPorDescriptcion_debeLanzarExcepcion_cuandoEsNuloOVacio(String descripcion) {
        assertThatThrownBy(() -> DiaSemana.obtenerDiaPorDescriptcion(descripcion))
                .isInstanceOf(InvalidDataException.class)
                .hasMessage("El horario es requerido");
    }

    @ParameterizedTest
    @ValueSource(strings = {"Domingo", "Lun", "Monday", "Lunes y martes"})
    void obtenerDiaPorDescriptcion_debeLanzarExcepcion_cuandoElDiaNoExiste(String descripcion) {
        assertThatThrownBy(() -> DiaSemana.obtenerDiaPorDescriptcion(descripcion))
                .isInstanceOf(InvalidDataException.class)
                .hasMessage("No existe un dia con descripcion: " + descripcion);
    }

    @Test
    void getDescription_debeRetornarLaDescripcionLegible() {
        assertThat(DiaSemana.LUNES.getDescription()).isEqualTo("Lunes");
        assertThat(DiaSemana.MIERCOLES.getDescription()).isEqualTo("Miercoles");
        assertThat(DiaSemana.SABADO.getDescription()).isEqualTo("Sabado");
    }

    @Test
    void values_debeContenerSoloDeLunesASabado() {
        assertThat(DiaSemana.values()).containsExactly(
                DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES,
                DiaSemana.JUEVES, DiaSemana.VIERNES, DiaSemana.SABADO);
    }
}