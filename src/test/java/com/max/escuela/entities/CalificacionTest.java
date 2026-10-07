package com.max.escuela.entities;

import com.max.escuela.exceptions.InvalidDataException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CalificacionTest {

    private static final String MSG_RANGO = "La calificacion debe ser positiva y estar entre 0 y 10";

    // ---------- crear ----------

    @Test
    void crear_debeCrearCalificacion_cuandoElValorEsValido() {
        LocalDate antes = LocalDate.now();

        Calificacion calificacion = Calificacion.crear(new BigDecimal("8.5"));

        assertThat(calificacion.getCalificacion()).isEqualByComparingTo("8.5");
        assertThat(calificacion.getFechaRegistro()).isBetween(antes, LocalDate.now());
        assertThat(calificacion.getInscripcion()).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "0.0", "0.1", "5", "9.9", "10", "10.0"})
    void crear_debeAceptarValoresEnElRangoDe0a10(String valor) {
        Calificacion calificacion = Calificacion.crear(new BigDecimal(valor));

        assertThat(calificacion.getCalificacion()).isEqualByComparingTo(valor);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-0.1", "-1", "10.1", "11", "100"})
    void crear_debeLanzarExcepcion_cuandoElValorEstaFueraDeRango(String valor) {
        assertThatThrownBy(() -> Calificacion.crear(new BigDecimal(valor)))
                .isInstanceOf(InvalidDataException.class)
                .hasMessage(MSG_RANGO);
    }

    @Test
    void crear_debeLanzarDatoInvalido_cuandoElValorEsNulo() {
        // Comportamiento esperado: DatoInvalidoException (no NullPointerException)
        assertThatThrownBy(() -> Calificacion.crear(null))
                .isInstanceOf(InvalidDataException.class);
    }

    // ---------- actualizar ----------

    @Test
    void actualizar_debeModificarElValor_cuandoEsValido() {
        Calificacion calificacion = Calificacion.crear(new BigDecimal("6.0"));

        calificacion.actualizar(new BigDecimal("9.5"));

        assertThat(calificacion.getCalificacion()).isEqualByComparingTo("9.5");
    }

    @ParameterizedTest
    @ValueSource(strings = {"-0.1", "10.1"})
    void actualizar_noDebeCambiarElValor_cuandoEstaFueraDeRango(String valor) {
        Calificacion calificacion = Calificacion.crear(new BigDecimal("6.0"));

        assertThatThrownBy(() -> calificacion.actualizar(new BigDecimal(valor)))
                .isInstanceOf(InvalidDataException.class)
                .hasMessage(MSG_RANGO);

        assertThat(calificacion.getCalificacion()).isEqualByComparingTo("6.0");
    }

    @Test
    void actualizar_debeLanzarDatoInvalido_cuandoElValorEsNulo() {
        Calificacion calificacion = Calificacion.crear(new BigDecimal("6.0"));

        assertThatThrownBy(() -> calificacion.actualizar(null))
                .isInstanceOf(InvalidDataException.class);

        assertThat(calificacion.getCalificacion()).isEqualByComparingTo("6.0");
    }

    // ---------- asignarInscripcion ----------

    @Test
    void asignarInscripcion_debeAsignarla_cuandoNoEsNula() {
        Calificacion calificacion = Calificacion.crear(new BigDecimal("8.0"));
        Inscripcion inscripcion = Inscripcion.crear();

        calificacion.asignarInscripcion(inscripcion);

        assertThat(calificacion.getInscripcion()).isSameAs(inscripcion);
    }

    @Test
    void asignarInscripcion_debeLanzarExcepcion_cuandoEsNula() {
        Calificacion calificacion = Calificacion.crear(new BigDecimal("8.0"));

        assertThatThrownBy(() -> calificacion.asignarInscripcion(null))
                .isInstanceOf(InvalidDataException.class)
                .hasMessage("La inscripcion es requerida");
    }
}