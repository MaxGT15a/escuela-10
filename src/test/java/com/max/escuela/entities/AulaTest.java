package com.max.escuela.entities;

import com.max.escuela.exceptions.InvalidDataException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AulaTest {

    private static final String MSG_NOMBRE = "El nombre es requerido y debe tener entre 5 y 30 caracteres";
    private static final String MSG_CAPACIDAD = "La capacidad es requerida y debe ser positiva";

    // ---------- crear ----------

    @Test
    void crear_debeCrearAula_cuandoDatosSonValidos() {
        // Act
        Aula aula = Aula.crear("  Aula 101  ", 30);

        // Assert
        assertThat(aula.getNombre()).isEqualTo("Aula 101");
        assertThat(aula.getCapacidad()).isEqualTo(30);
        assertThat(aula.getGrupos()).isEmpty();
    }

    @Test
    void crear_debeAceptarLimitesDeNombre_cuandoTiene5o30Caracteres() {
        assertThat(Aula.crear("a".repeat(5), 1).getNombre()).hasSize(5);
        assertThat(Aula.crear("a".repeat(30), 1).getNombre()).hasSize(30);
    }

    @Test
    void crear_debeLanzarExcepcion_cuandoElNombreEsMuyCorto() {
        // "Ab" tiene menos de 5 caracteres
        assertThatThrownBy(() -> Aula.crear("Ab", 30))
                .isInstanceOf(InvalidDataException.class)
                .hasMessageContaining("entre 5 y 30 caracteres");
    }

    @Test
    void crear_debeLanzarExcepcion_cuandoElNombreEsMuyLargo() {
        assertThatThrownBy(() -> Aula.crear("a".repeat(31), 30))
                .isInstanceOf(InvalidDataException.class)
                .hasMessage(MSG_NOMBRE);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void crear_debeLanzarExcepcion_cuandoElNombreEsNuloOVacio(String nombre) {
        assertThatThrownBy(() -> Aula.crear(nombre, 30))
                .isInstanceOf(InvalidDataException.class)
                .hasMessage(MSG_NOMBRE);
    }

    @Test
    void crear_debeLanzarExcepcion_cuandoLaCapacidadEsNegativa() {
        assertThatThrownBy(() -> Aula.crear("Aula 101", -5))
                .isInstanceOf(InvalidDataException.class)
                .hasMessage(MSG_CAPACIDAD);
    }

    @Test
    void crear_debeLanzarExcepcion_cuandoLaCapacidadEsCero() {
        assertThatThrownBy(() -> Aula.crear("Aula 101", 0))
                .isInstanceOf(InvalidDataException.class)
                .hasMessage(MSG_CAPACIDAD);
    }

    @Test
    void crear_debeLanzarExcepcion_cuandoLaCapacidadEsNula() {
        assertThatThrownBy(() -> Aula.crear("Aula 101", null))
                .isInstanceOf(InvalidDataException.class)
                .hasMessage(MSG_CAPACIDAD);
    }

    // ---------- actualizar ----------

    @Test
    void actualizar_debeModificarDatos_cuandoTodoEsValido() {
        // Arrange
        Aula aula = Aula.builder()
                .nombre("Aula vieja")
                .capacidad(10)
                .build();

        // Act
        aula.actualizar("Aula Nueva", 45);

        // Assert
        assertThat(aula.getNombre()).isEqualTo("Aula Nueva");
        assertThat(aula.getCapacidad()).isEqualTo(45);
    }

    @Test
    void actualizar_debeRecortarEspacios_enElNombre() {
        Aula aula = Aula.crear("Aula 101", 30);

        aula.actualizar("   Aula 202   ", 30);

        assertThat(aula.getNombre()).isEqualTo("Aula 202");
    }

    @Test
    void actualizar_debeLanzarExcepcion_cuandoElNombreEsMuyCorto() {
        // Arrange
        Aula aula = Aula.builder()
                .nombre("Aula válida")
                .capacidad(10)
                .build();

        // Act + Assert
        assertThatThrownBy(() -> aula.actualizar("Ab", 20))
                .isInstanceOf(InvalidDataException.class)
                .hasMessageContaining("entre 5 y 30 caracteres");
    }

    @Test
    void actualizar_noDebeCambiarNada_cuandoLaCapacidadEsInvalida() {
        Aula aula = Aula.crear("Aula 101", 30);

        assertThatThrownBy(() -> aula.actualizar("Aula 202", 0))
                .isInstanceOf(InvalidDataException.class);

        assertThat(aula.getNombre()).isEqualTo("Aula 101");
        assertThat(aula.getCapacidad()).isEqualTo(30);
    }

    // ---------- asignarGrupo / desasignarGrupo ----------

    @Test
    void asignarGrupo_debeLanzarExcepcion_cuandoElGrupoEsNulo() {
        // Arrange
        Aula aula = Aula.crear("Aula 101", 30);

        // Act + Assert
        assertThatThrownBy(() -> aula.asignarGrupo(null))
                .isInstanceOf(InvalidDataException.class)
                .hasMessage("El grupo es requerido");
    }

    @Test
    void asignarGrupo_debeAsociarAmbosLados_cuandoElGrupoEsValido() {
        Aula aula = Aula.crear("Aula 101", 30);
        Grupo grupo = Grupo.crear("2026-01");

        aula.asignarGrupo(grupo);

        assertThat(aula.getGrupos()).containsExactly(grupo);
        assertThat(grupo.getAula()).isSameAs(aula);
    }

    @Test
    void desasignarGrupo_debeQuitarElGrupoDeLaLista() {
        Aula aula = Aula.crear("Aula 101", 30);
        Grupo grupo = Grupo.crear("2026-01");
        aula.asignarGrupo(grupo);

        aula.desasignarGrupo(grupo);

        assertThat(aula.getGrupos()).isEmpty();
    }
}