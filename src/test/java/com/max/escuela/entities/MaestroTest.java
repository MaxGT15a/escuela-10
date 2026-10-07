package com.max.escuela.entities;

import com.max.escuela.exceptions.InvalidDataException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MaestroTest {

    private static final String MSG_NOMBRE = "El nombre es requerido y debe tener entre 5 y 50 caracteres";
    private static final String MSG_PATERNO = "El apellido paterno es requerido y debe tener entre 5 y 50 caracteres";
    private static final String MSG_MATERNO = "El apellido materno es requerido y debe tener entre 5 y 50 caracteres";
    private static final String MSG_EMAIL = "El email es requerido y debe tener entre 5 y 100 caracteres";
    private static final String MSG_TEL_TAMANIO = "El teléfono es requerido y debe tener exactamente 10 caracteres";
    private static final String MSG_TEL_INVALIDO = "El telefono es invalido";

    private static Maestro maestroBase() {
        return Maestro.crear("Laura", "Martínez", "Martínez",
                "laura.martinez@escuela.com", "5551010789");
    }

    // ---------- crear ----------
    @Test
    void crear_debeCrearMaestro_cuandoDatosSonValidos() {
        Maestro maestro = Maestro.crear(
                "  Laura  ", "  Martínez ", " Sánchez ",
                "  Laura.Martinez@Escuela.COM  ", "  5551010789  ");

        assertThat(maestro.getNombre()).isEqualTo("Laura");
        assertThat(maestro.getApellidoPaterno()).isEqualTo("Martínez");
        assertThat(maestro.getApellidoMaterno()).isEqualTo("Sánchez");
        assertThat(maestro.getEmail()).isEqualTo("laura.martinez@escuela.com");
        assertThat(maestro.getTelefono()).isEqualTo("5551010789");
        assertThat(maestro.getGrupos()).isEmpty();
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "Ana|Martínez|Martínez|a@escuela.com|5551010789|" + MSG_NOMBRE,
            "Laura|Mart|Martínez|a@escuela.com|5551010789|" + MSG_PATERNO,
            "Laura|Martínez|Mart|a@escuela.com|5551010789|" + MSG_MATERNO,
            "Laura|Martínez|Martínez|a@b|5551010789|" + MSG_EMAIL,
            "|Martínez|Martínez|a@escuela.com|5551010789|" + MSG_NOMBRE,
            "Laura||Martínez|a@escuela.com|5551010789|" + MSG_PATERNO,
            "Laura|Martínez||a@escuela.com|5551010789|" + MSG_MATERNO,
            "Laura|Martínez|Martínez||5551010789|" + MSG_EMAIL,
            "Laura|Martínez|Martínez|a@escuela.com||" + MSG_TEL_TAMANIO
    })
    void crear_debeLanzarExcepcion_cuandoUnCampoEsInvalido(
            String nombre, String paterno, String materno,
            String email, String telefono, String mensaje) {

        assertThatThrownBy(() -> Maestro.crear(nombre, paterno, materno, email, telefono))
                .isInstanceOf(InvalidDataException.class)
                .hasMessage(mensaje);
    }

    @Test
    void crear_debeLanzarExcepcion_cuandoElNombreEsMuyLargo() {
        assertThatThrownBy(() -> Maestro.crear("a".repeat(51), "Martínez", "Martínez",
                "a@escuela.com", "5551010789"))
                .isInstanceOf(InvalidDataException.class)
                .hasMessage(MSG_NOMBRE);
    }

    @Test
    void crear_debeLanzarExcepcion_cuandoElEmailEsMuyLargo() {
        String email = "a".repeat(101);

        assertThatThrownBy(() -> Maestro.crear("Laura", "Martínez", "Martínez", email, "5551010789"))
                .isInstanceOf(InvalidDataException.class)
                .hasMessage(MSG_EMAIL);
    }

    @ParameterizedTest
    @ValueSource(strings = {"555101078", "55510107890", "123"})
    void crear_debeLanzarExcepcion_cuandoElTelefonoNoTiene10Caracteres(String telefono) {
        assertThatThrownBy(() -> Maestro.crear("Laura", "Martínez", "Martínez",
                "a@escuela.com", telefono))
                .isInstanceOf(InvalidDataException.class)
                .hasMessage(MSG_TEL_TAMANIO);
    }

    @ParameterizedTest
    @ValueSource(strings = {"55510107a9", "5551010 78", "555-101-07", "+521234567"})
    void crear_debeLanzarExcepcion_cuandoElTelefonoTieneCaracteresNoNumericos(String telefono) {
        assertThatThrownBy(() -> Maestro.crear("Laura", "Martínez", "Martínez",
                "a@escuela.com", telefono))
                .isInstanceOf(InvalidDataException.class)
                .hasMessage(MSG_TEL_INVALIDO);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"     "})
    void crear_debeLanzarExcepcion_cuandoElEmailEsNuloOVacio(String email) {
        assertThatThrownBy(() -> Maestro.crear("Laura", "Martínez", "Martínez", email, "5551010789"))
                .isInstanceOf(InvalidDataException.class)
                .hasMessage(MSG_EMAIL);
    }

    // ---------- actualizar ----------

    @Test
    void actualizar_debeModificarDatos_cuandoTodoEsValido() {
        Maestro maestro = maestroBase();

        maestro.actualizar("  Carlos  ", " Hernández ", " Ramírez ",
                "  CARLOS@Escuela.com ", " 5552020359 ");

        assertThat(maestro.getNombre()).isEqualTo("Carlos");
        assertThat(maestro.getApellidoPaterno()).isEqualTo("Hernández");
        assertThat(maestro.getApellidoMaterno()).isEqualTo("Ramírez");
        assertThat(maestro.getEmail()).isEqualTo("carlos@escuela.com");
        assertThat(maestro.getTelefono()).isEqualTo("5552020359");
    }

    @Test
    void actualizar_noDebeCambiarNada_cuandoElTelefonoEsInvalido() {
        Maestro maestro = maestroBase();

        assertThatThrownBy(() -> maestro.actualizar("Carlos", "Hernández", "Ramírez",
                "carlos@escuela.com", "12345"))
                .isInstanceOf(InvalidDataException.class);

        assertThat(maestro.getNombre()).isEqualTo("Laura");
        assertThat(maestro.getEmail()).isEqualTo("laura.martinez@escuela.com");
        assertThat(maestro.getTelefono()).isEqualTo("5551010789");
    }

    // ---------- cambioEnDatos ----------

    @Test
    void cambioEnDatos_debeRetornarFalse_cuandoLosDatosSonIguales() {
        Maestro maestro = maestroBase();

        boolean cambio = maestro.cambioEnDatos("Laura", "Martínez", "Martínez",
                "laura.martinez@escuela.com", "5551010789");

        assertThat(cambio).isFalse();
    }

    @Test
    void cambioEnDatos_debeIgnorarEspaciosYMayusculasDelEmail() {
        Maestro maestro = maestroBase();

        boolean cambio = maestro.cambioEnDatos("  Laura ", " Martínez", "Martínez ",
                "  LAURA.Martinez@Escuela.com ", " 5551010789 ");

        assertThat(cambio).isFalse();
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "Laurita|Martínez|Martínez|laura.martinez@escuela.com|5551010789",
            "Laura|Martínes|Martínez|laura.martinez@escuela.com|5551010789",
            "Laura|Martínez|Martínes|laura.martinez@escuela.com|5551010789",
            "Laura|Martínez|Martínez|otro.correo@escuela.com|5551010789",
            "Laura|Martínez|Martínez|laura.martinez@escuela.com|5559999999"
    })
    void cambioEnDatos_debeRetornarTrue_cuandoCambiaAlgunCampo(
            String nombre, String paterno, String materno, String email, String telefono) {

        assertThat(maestroBase().cambioEnDatos(nombre, paterno, materno, email, telefono)).isTrue();
    }

    @Test
    void cambioEnDatos_debeLanzarExcepcion_cuandoLosDatosSonInvalidos() {
        Maestro maestro = maestroBase();

        assertThatThrownBy(() -> maestro.cambioEnDatos("Ab", "Martínez", "Martínez",
                "a@escuela.com", "5551010789"))
                .isInstanceOf(InvalidDataException.class)
                .hasMessage(MSG_NOMBRE);
    }

    // ---------- obtenerNombreCompleto ----------

    @Test
    void obtenerNombreCompleto_debeConcatenarNombreYApellidos() {
        assertThat(maestroBase().obtenerNombreCompleto()).isEqualTo("Laura Martínez Martínez");
    }

    // ---------- asignarGrupo ----------

    @Test
    void asignarGrupo_debeLanzarExcepcion_cuandoElGrupoEsNulo() {
        Maestro maestro = maestroBase();

        assertThatThrownBy(() -> maestro.asignarGrupo(null))
                .isInstanceOf(InvalidDataException.class)
                .hasMessage("El grupo es requerido");
    }

    @Test
    void asignarGrupo_debeAsociarAmbosLados_cuandoElGrupoEsValido() {
        Maestro maestro = maestroBase();
        Grupo grupo = Grupo.crear("2026-01");

        maestro.asignarGrupo(grupo);

        assertThat(maestro.getGrupos()).containsExactly(grupo);
        assertThat(grupo.getMaestro()).isSameAs(maestro);
    }
}