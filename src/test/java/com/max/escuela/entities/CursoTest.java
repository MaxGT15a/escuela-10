package com.max.escuela.entities;

import com.max.escuela.exceptions.InvalidDataException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CursoTest {

    private static final String MSG_NOMBRE = "El nombre es requerido y debe tener entre 5 y 100 caracteres";
    private static final String MSG_CREDITOS = "El credito es requerido y debe ser positivo";

    // ---------- crear ----------

    @Test
    void crear_debeCrearCurso_cuandoDatosSonValidos() {
        Curso curso = Curso.crear("  Matemáticas I  ", "  Fundamentos  ", 6);

        assertThat(curso.getNombre()).isEqualTo("Matemáticas I");
        assertThat(curso.getDescripcion()).isEqualTo("Fundamentos");
        assertThat(curso.getCreditos()).isEqualTo(6);
    }

    @Test
    void crear_debePermitirDescripcionNula() {
        Curso curso = Curso.crear("Matemáticas I", null, 6);

        assertThat(curso.getDescripcion()).isNull();
    }

    @Test
    void crear_debeAceptarLimitesDeNombre_cuandoTiene5o100Caracteres() {
        assertThat(Curso.crear("a".repeat(5), null, 1).getNombre()).hasSize(5);
        assertThat(Curso.crear("a".repeat(100), null, 1).getNombre()).hasSize(100);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "Mate", "Abc"})
    void crear_debeLanzarExcepcion_cuandoElNombreEsInvalido(String nombre) {
        assertThatThrownBy(() -> Curso.crear(nombre, "desc", 6))
                .isInstanceOf(InvalidDataException.class)
                .hasMessage(MSG_NOMBRE);
    }

    @Test
    void crear_debeLanzarExcepcion_cuandoElNombreEsMuyLargo() {
        assertThatThrownBy(() -> Curso.crear("a".repeat(101), "desc", 6))
                .isInstanceOf(InvalidDataException.class)
                .hasMessage(MSG_NOMBRE);
    }

    @Test
    void crear_debeLanzarExcepcion_cuandoLosCreditosSonNulos() {
        assertThatThrownBy(() -> Curso.crear("Matemáticas I", "desc", null))
                .isInstanceOf(InvalidDataException.class)
                .hasMessage(MSG_CREDITOS);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -100})
    void crear_debeLanzarExcepcion_cuandoLosCreditosNoSonPositivos(int creditos) {
        assertThatThrownBy(() -> Curso.crear("Matemáticas I", "desc", creditos))
                .isInstanceOf(InvalidDataException.class)
                .hasMessage(MSG_CREDITOS);
    }

    // ---------- actualizar ----------

    @Test
    void actualizar_debeModificarDatos_cuandoTodoEsValido() {
        Curso curso = Curso.crear("Matemáticas I", "Vieja", 6);

        curso.actualizar("  Matemáticas II  ", "  Nueva  ", 8);

        assertThat(curso.getNombre()).isEqualTo("Matemáticas II");
        assertThat(curso.getDescripcion()).isEqualTo("Nueva");
        assertThat(curso.getCreditos()).isEqualTo(8);
    }

    @Test
    void actualizar_debePermitirDescripcionNula() {
        Curso curso = Curso.crear("Matemáticas I", "Vieja", 6);

        curso.actualizar("Matemáticas I", null, 6);

        assertThat(curso.getDescripcion()).isNull();
    }

    @Test
    void actualizar_debeLanzarExcepcion_cuandoElNombreEsInvalido() {
        Curso curso = Curso.crear("Matemáticas I", "desc", 6);

        assertThatThrownBy(() -> curso.actualizar("Ab", "desc", 6))
                .isInstanceOf(InvalidDataException.class)
                .hasMessage(MSG_NOMBRE);
    }

    @Test
    void actualizar_noDebeCambiarNada_cuandoLosCreditosSonInvalidos() {
        Curso curso = Curso.crear("Matemáticas I", "desc", 6);

        assertThatThrownBy(() -> curso.actualizar("Otro curso", "otra", 0))
                .isInstanceOf(InvalidDataException.class)
                .hasMessage(MSG_CREDITOS);

        assertThat(curso.getNombre()).isEqualTo("Matemáticas I");
        assertThat(curso.getDescripcion()).isEqualTo("desc");
        assertThat(curso.getCreditos()).isEqualTo(6);
    }
}