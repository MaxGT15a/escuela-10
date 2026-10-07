package com.max.escuela.entities;

import com.max.escuela.exceptions.InvalidDataException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InscripcionTest {

    private final Alumno alumno = Alumno.crear("Mauricio", "García", "Ramírez");
    private final Grupo grupo = Grupo.crear("2026-01");

    /** Inscripción ya vinculada a alumno y grupo (ambos lados de la relación). */
    private Inscripcion inscripcionVinculada() {
        Inscripcion inscripcion = Inscripcion.crear();
        inscripcion.asignarAlumno(alumno);
        inscripcion.asignarGrupo(grupo);
        return inscripcion;
    }

    // ---------- crear ----------

    @Test
    void crear_debeAsignarFechaActual_yNoTenerRelaciones() {
        LocalDate antes = LocalDate.now();

        Inscripcion inscripcion = Inscripcion.crear();

        assertThat(inscripcion.getFechaInscripcion()).isBetween(antes, LocalDate.now());
        assertThat(inscripcion.getAlumno()).isNull();
        assertThat(inscripcion.getGrupo()).isNull();
        assertThat(inscripcion.getCalificacion()).isNull();
    }

    // ---------- asignarAlumno ----------

    @Test
    void asignarAlumno_debeAsociarAmbosLados_cuandoElAlumnoEsValido() {
        Inscripcion inscripcion = Inscripcion.crear();

        inscripcion.asignarAlumno(alumno);

        assertThat(inscripcion.getAlumno()).isSameAs(alumno);
        assertThat(alumno.getInscripciones()).containsExactly(inscripcion);
    }

    @Test
    void asignarAlumno_debeLanzarExcepcion_cuandoEsNulo() {
        Inscripcion inscripcion = Inscripcion.crear();

        assertThatThrownBy(() -> inscripcion.asignarAlumno(null))
                .isInstanceOf(InvalidDataException.class)
                .hasMessage("El alumno es requerido");
    }

    // ---------- asignarGrupo ----------

    @Test
    void asignarGrupo_debeAsociarAmbosLados_cuandoElGrupoEsValido() {
        Inscripcion inscripcion = Inscripcion.crear();

        inscripcion.asignarGrupo(grupo);

        assertThat(inscripcion.getGrupo()).isSameAs(grupo);
        assertThat(grupo.getInscripciones()).containsExactly(inscripcion);
    }

    @Test
    void asignarGrupo_debeLanzarExcepcion_cuandoEsNulo() {
        Inscripcion inscripcion = Inscripcion.crear();

        assertThatThrownBy(() -> inscripcion.asignarGrupo(null))
                .isInstanceOf(InvalidDataException.class)
                .hasMessage("El grupo es requerido");
    }

    // ---------- validarDatos ----------

    @Test
    void validarDatos_noDebeLanzarExcepcion_cuandoAmbosSonValidos() {
        Inscripcion inscripcion = Inscripcion.crear();

        inscripcion.validarDatos(alumno, grupo); // no debe lanzar

        assertThat(inscripcion.getAlumno()).isNull(); // no modifica estado
    }

    @Test
    void validarDatos_debeLanzarExcepcion_cuandoElAlumnoEsNulo() {
        Inscripcion inscripcion = Inscripcion.crear();

        assertThatThrownBy(() -> inscripcion.validarDatos(null, grupo))
                .isInstanceOf(InvalidDataException.class)
                .hasMessage("El alumno es requerido");
    }

    @Test
    void validarDatos_debeLanzarExcepcion_cuandoElGrupoEsNulo() {
        Inscripcion inscripcion = Inscripcion.crear();

        assertThatThrownBy(() -> inscripcion.validarDatos(alumno, null))
                .isInstanceOf(InvalidDataException.class)
                .hasMessage("El grupo es requerido");
    }

    // ---------- cambioEnDatos ----------
    // Nota: las entidades no sobrescriben equals/hashCode, así que se compara por identidad.

    @Test
    void cambioEnDatos_debeRetornarFalse_cuandoAlumnoYGrupoSonLosMismos() {
        Inscripcion inscripcion = inscripcionVinculada();

        assertThat(inscripcion.cambioEnDatos(alumno, grupo)).isFalse();
    }

    @Test
    void cambioEnDatos_debeRetornarTrue_cuandoCambiaElAlumno() {
        Inscripcion inscripcion = inscripcionVinculada();
        Alumno otro = Alumno.crear("Marcos", "Pérez", "Salgado");

        assertThat(inscripcion.cambioEnDatos(otro, grupo)).isTrue();
    }

    @Test
    void cambioEnDatos_debeRetornarTrue_cuandoCambiaElGrupo() {
        Inscripcion inscripcion = inscripcionVinculada();
        Grupo otro = Grupo.crear("2026-02");

        assertThat(inscripcion.cambioEnDatos(alumno, otro)).isTrue();
    }

    @Test
    void cambioEnDatos_debeLanzarExcepcion_cuandoAlumnoOGrupoSonNulos() {
        Inscripcion inscripcion = inscripcionVinculada();

        assertThatThrownBy(() -> inscripcion.cambioEnDatos(null, grupo))
                .isInstanceOf(InvalidDataException.class)
                .hasMessage("El alumno es requerido");
        assertThatThrownBy(() -> inscripcion.cambioEnDatos(alumno, null))
                .isInstanceOf(InvalidDataException.class)
                .hasMessage("El grupo es requerido");
    }

    // ---------- actualizar ----------

    @Test
    void actualizar_debeMoverLaInscripcionAlNuevoAlumnoYGrupo() {
        Inscripcion inscripcion = inscripcionVinculada();
        Alumno nuevoAlumno = Alumno.crear("Marcos", "Pérez", "Salgado");
        Grupo nuevoGrupo = Grupo.crear("2026-02");

        inscripcion.actualizar(nuevoAlumno, nuevoGrupo);

        assertThat(inscripcion.getAlumno()).isSameAs(nuevoAlumno);
        assertThat(inscripcion.getGrupo()).isSameAs(nuevoGrupo);

        // Se quita de los anteriores
        assertThat(alumno.getInscripciones()).isEmpty();
        assertThat(grupo.getInscripciones()).isEmpty();

        // Se agrega a los nuevos
        assertThat(nuevoAlumno.getInscripciones()).containsExactly(inscripcion);
        assertThat(nuevoGrupo.getInscripciones()).containsExactly(inscripcion);
    }

    @Test
    void actualizar_noDebeDuplicarLaInscripcion_cuandoSeUsanLosMismosAlumnoYGrupo() {
        Inscripcion inscripcion = inscripcionVinculada();

        inscripcion.actualizar(alumno, grupo);

        assertThat(alumno.getInscripciones()).containsExactly(inscripcion);
        assertThat(grupo.getInscripciones()).containsExactly(inscripcion);
    }

    @Test
    void actualizar_noDebeCambiarNada_cuandoElAlumnoEsNulo() {
        Inscripcion inscripcion = inscripcionVinculada();

        assertThatThrownBy(() -> inscripcion.actualizar(null, Grupo.crear("2026-02")))
                .isInstanceOf(InvalidDataException.class)
                .hasMessage("El alumno es requerido");

        assertThat(inscripcion.getAlumno()).isSameAs(alumno);
        assertThat(inscripcion.getGrupo()).isSameAs(grupo);
        assertThat(alumno.getInscripciones()).containsExactly(inscripcion);
        assertThat(grupo.getInscripciones()).containsExactly(inscripcion);
    }

    @Test
    void actualizar_noDebeCambiarNada_cuandoElGrupoEsNulo() {
        Inscripcion inscripcion = inscripcionVinculada();

        assertThatThrownBy(() -> inscripcion.actualizar(Alumno.crear("Marcos", "Pérez", "Salgado"), null))
                .isInstanceOf(InvalidDataException.class)
                .hasMessage("El grupo es requerido");

        assertThat(inscripcion.getAlumno()).isSameAs(alumno);
        assertThat(alumno.getInscripciones()).containsExactly(inscripcion);
    }
}