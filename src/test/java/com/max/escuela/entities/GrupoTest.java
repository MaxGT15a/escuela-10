package com.max.escuela.entities;

import com.max.escuela.exceptions.InvalidDataException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GrupoTest {

    private static final String MSG_FORMATO_PERIODO = "El periodo debe tener el formato YYYY-MM";

    private final Curso curso = Curso.crear("Matemáticas 6/7", "Fundamentos", 6);
    private final Maestro maestro = Maestro.crear("Laura", "Martínez", "Martínez",
            "laura.martinez@escuela.com", "5551010789");
    private final Aula aula = Aula.crear("Aula 101", 30);

    private Grupo grupoCompleto() {
        return Grupo.builder()
                .curso(curso)
                .maestro(maestro)
                .aula(aula)
                .periodo("2026-01")
                .build();
    }

    // ---------- crear ----------

    @Test
    void crear_debeCrearGrupo_cuandoElPeriodoEsValido() {
        Grupo grupo = Grupo.crear("2026-01");

        assertThat(grupo.getPeriodo()).isEqualTo("2026-01");
        assertThat(grupo.getCurso()).isNull();
        assertThat(grupo.getMaestro()).isNull();
        assertThat(grupo.getAula()).isNull();
        assertThat(grupo.getInscripciones()).isEmpty();
        assertThat(grupo.getHorarios()).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"2025-01", "2026-12", "1999-06"})
    void crear_debeAceptarPeriodosConFormatoYYYYMM(String periodo) {
        assertThat(Grupo.crear(periodo).getPeriodo()).isEqualTo(periodo);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "2026", "xxxxxxxxxxxxxxxxxxxxx"}) // 21 caracteres
    void crear_debeLanzarExcepcion_cuandoElPeriodoEsNuloVacioOFueraDeTamanio(String periodo) {
        assertThatThrownBy(() -> Grupo.crear(periodo))
                .isInstanceOf(InvalidDataException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"2025-1", "2026-13", "2026-00", "2026/01", "01-2026", "abcdefg"})
    void crear_debeLanzarExcepcion_cuandoElFormatoDelPeriodoEsInvalido(String periodo) {
        assertThatThrownBy(() -> Grupo.crear(periodo))
                .isInstanceOf(InvalidDataException.class)
                .hasMessage(MSG_FORMATO_PERIODO);
    }

    // ---------- asignar ----------

    @Test
    void asignarCurso_debeAsignarlo_cuandoNoEsNulo() {
        Grupo grupo = Grupo.crear("2026-01");

        grupo.asignarCurso(curso);

        assertThat(grupo.getCurso()).isSameAs(curso);
    }

    @Test
    void asignarCurso_debeLanzarExcepcion_cuandoEsNulo() {
        Grupo grupo = Grupo.crear("2026-01");

        assertThatThrownBy(() -> grupo.asignarCurso(null))
                .isInstanceOf(InvalidDataException.class)
                .hasMessage("El curso es requerido");
    }

    @Test
    void asignarMaestro_debeAsignarlo_cuandoNoEsNulo() {
        Grupo grupo = Grupo.crear("2026-01");

        grupo.asignarMaestro(maestro);

        assertThat(grupo.getMaestro()).isSameAs(maestro);
    }

    @Test
    void asignarMaestro_debeLanzarExcepcion_cuandoEsNulo() {
        Grupo grupo = Grupo.crear("2026-01");

        assertThatThrownBy(() -> grupo.asignarMaestro(null))
                .isInstanceOf(InvalidDataException.class)
                .hasMessage("El maestro es requerido");
    }

    @Test
    void asignarAula_debeAsignarla_cuandoNoEsNula() {
        Grupo grupo = Grupo.crear("2026-01");

        grupo.asignarAula(aula);

        assertThat(grupo.getAula()).isSameAs(aula);
    }

    @Test
    void asignarAula_debeLanzarExcepcion_cuandoEsNula() {
        Grupo grupo = Grupo.crear("2026-01");

        assertThatThrownBy(() -> grupo.asignarAula(null))
                .isInstanceOf(InvalidDataException.class)
                .hasMessage("El aula es requerida");
    }

    // ---------- inscripciones ----------

    @Test
    void agregarInscripcion_debeAgregarlaALaLista() {
        Grupo grupo = Grupo.crear("2026-01");
        Inscripcion inscripcion = Inscripcion.crear();

        grupo.agregarInscripcion(inscripcion);

        assertThat(grupo.getInscripciones()).containsExactly(inscripcion);
    }

    @Test
    void agregarInscripcion_debeLanzarExcepcion_cuandoEsNula() {
        Grupo grupo = Grupo.crear("2026-01");

        assertThatThrownBy(() -> grupo.agregarInscripcion(null))
                .isInstanceOf(InvalidDataException.class)
                .hasMessage("La inscripcion es requerida");
    }

    @Test
    void quitarInscripcion_debeQuitarlaDeLaLista() {
        Grupo grupo = Grupo.crear("2026-01");
        Inscripcion inscripcion = Inscripcion.crear();
        grupo.agregarInscripcion(inscripcion);

        grupo.quitarInscripcion(inscripcion);

        assertThat(grupo.getInscripciones()).isEmpty();
    }

    @Test
    void quitarInscripcion_debeLanzarExcepcion_cuandoEsNula() {
        Grupo grupo = Grupo.crear("2026-01");

        assertThatThrownBy(() -> grupo.quitarInscripcion(null))
                .isInstanceOf(InvalidDataException.class)
                .hasMessage("La inscripcion es requerida");
    }

    // ---------- cambioEnDatos ----------
    // Nota: las entidades no sobrescriben equals/hashCode, así que se compara por identidad.

    @Test
    void cambioEnDatos_debeRetornarFalse_cuandoSonLasMismasReferenciasYPeriodo() {
        Grupo grupo = grupoCompleto();

        assertThat(grupo.cambioEnDatos(curso, maestro, aula, "2026-01")).isFalse();
    }

    @Test
    void cambioEnDatos_debeRetornarTrue_cuandoCambiaElPeriodo() {
        Grupo grupo = grupoCompleto();

        assertThat(grupo.cambioEnDatos(curso, maestro, aula, "2026-02")).isTrue();
    }

    @Test
    void cambioEnDatos_debeRetornarTrue_cuandoCambiaElCurso() {
        Grupo grupo = grupoCompleto();
        Curso otro = Curso.crear("Programación Java", "Intro", 8);

        assertThat(grupo.cambioEnDatos(otro, maestro, aula, "2026-01")).isTrue();
    }

    @Test
    void cambioEnDatos_debeRetornarTrue_cuandoCambiaElMaestro() {
        Grupo grupo = grupoCompleto();
        Maestro otro = Maestro.crear("Carlos", "Hernández", "Hernández",
                "carlos.hernandez@escuela.com", "5552020359");

        assertThat(grupo.cambioEnDatos(curso, otro, aula, "2026-01")).isTrue();
    }

    @Test
    void cambioEnDatos_debeRetornarTrue_cuandoCambiaElAula() {
        Grupo grupo = grupoCompleto();
        Aula otra = Aula.crear("Laboratorio A", 28);

        assertThat(grupo.cambioEnDatos(curso, maestro, otra, "2026-01")).isTrue();
    }

    @Test
    void cambioEnDatos_debeLanzarExcepcion_cuandoElPeriodoEsInvalido() {
        Grupo grupo = grupoCompleto();

        assertThatThrownBy(() -> grupo.cambioEnDatos(curso, maestro, aula, "2026-1"))
                .isInstanceOf(InvalidDataException.class)
                .hasMessage(MSG_FORMATO_PERIODO);
    }

    // Comportamiento esperado tras la corrección: DatoInvalidoException en lugar de NullPointerException

    @Test
    void cambioEnDatos_debeLanzarDatoInvalido_cuandoElCursoEsNulo() {
        Grupo grupo = grupoCompleto();

        assertThatThrownBy(() -> grupo.cambioEnDatos(null, maestro, aula, "2026-01"))
                .isInstanceOf(InvalidDataException.class);
    }

    @Test
    void cambioEnDatos_debeLanzarDatoInvalido_cuandoElMaestroEsNulo() {
        Grupo grupo = grupoCompleto();

        assertThatThrownBy(() -> grupo.cambioEnDatos(curso, null, aula, "2026-01"))
                .isInstanceOf(InvalidDataException.class);
    }

    @Test
    void cambioEnDatos_debeLanzarDatoInvalido_cuandoElAulaEsNula() {
        Grupo grupo = grupoCompleto();

        assertThatThrownBy(() -> grupo.cambioEnDatos(curso, maestro, null, "2026-01"))
                .isInstanceOf(InvalidDataException.class);
    }

    // ---------- actualizarDatos ----------

    @Test
    void actualizarDatos_debeModificarTodosLosCampos() {
        Grupo grupo = grupoCompleto();
        Curso otroCurso = Curso.crear("Programación Java", "Intro", 8);
        Maestro otroMaestro = Maestro.crear("Carlos", "Hernández", "Hernández",
                "carlos.hernandez@escuela.com", "5552020359");
        Aula otraAula = Aula.crear("Laboratorio A", 28);

        grupo.actualizarDatos(otroCurso, otroMaestro, otraAula, "2026-06");

        assertThat(grupo.getCurso()).isSameAs(otroCurso);
        assertThat(grupo.getMaestro()).isSameAs(otroMaestro);
        assertThat(grupo.getAula()).isSameAs(otraAula);
        assertThat(grupo.getPeriodo()).isEqualTo("2026-06");
    }

    @Test
    void actualizarDatos_noDebeCambiarNada_cuandoElPeriodoEsInvalido() {
        Grupo grupo = grupoCompleto();
        Curso otroCurso = Curso.crear("Programación Java", "Intro", 8);

        assertThatThrownBy(() -> grupo.actualizarDatos(otroCurso, maestro, aula, "2026-13"))
                .isInstanceOf(InvalidDataException.class)
                .hasMessage(MSG_FORMATO_PERIODO);

        assertThat(grupo.getCurso()).isSameAs(curso);
        assertThat(grupo.getPeriodo()).isEqualTo("2026-01");
    }

    @Test
    void actualizarDatos_debeLanzarExcepcion_cuandoAlgunaRelacionEsNula() {
        Grupo grupo = grupoCompleto();

        assertThatThrownBy(() -> grupo.actualizarDatos(null, maestro, aula, "2026-01"))
                .isInstanceOf(InvalidDataException.class);
        assertThatThrownBy(() -> grupo.actualizarDatos(curso, null, aula, "2026-01"))
                .isInstanceOf(InvalidDataException.class);
        assertThatThrownBy(() -> grupo.actualizarDatos(curso, maestro, null, "2026-01"))
                .isInstanceOf(InvalidDataException.class);
    }
}