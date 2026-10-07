package com.max.escuela.entities;

import com.max.escuela.enums.DiaSemana;
import com.max.escuela.exceptions.InvalidDataException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HorarioTest {

    private static final String MSG_DIA_REQUERIDO = "El día de la semana es requerido";
    private static final String MSG_DIA_TAMANIO = "El día de la semana debe tener entre 1 y 15 caracteres";
    private static final String MSG_INICIO_REQUERIDO = "La hora de inicio es requerida";
    private static final String MSG_INICIO_FORMATO = "El formato de hora inicio debe ser HH:mm";
    private static final String MSG_FIN_REQUERIDO = "La hora de fin es requerida";
    private static final String MSG_FIN_FORMATO = "El formato de hora fin debe ser HH:mm";
    private static final String MSG_ORDEN = "La hora de inicio debe ser menor a la hora de fin";

    private final Grupo grupo = Grupo.crear("2026-01");

    private Horario horarioBase() {
        Horario horario = Horario.crear("Lunes", "08:00", "10:00");
        horario.asignarGrupo(grupo);
        return horario;
    }

    // ---------- crear ----------

    @Test
    void crear_debeCrearHorario_cuandoDatosSonValidos() {
        Horario horario = Horario.crear("Lunes", "08:00", "10:00");

        assertThat(horario.getDiaSemana()).isEqualTo(DiaSemana.LUNES);
        assertThat(horario.getHoraInicio()).isEqualTo("08:00");
        assertThat(horario.getHoraFin()).isEqualTo("10:00");
        assertThat(horario.getGrupo()).isNull();
    }

    @ParameterizedTest
    @CsvSource({
            "Lunes, LUNES",
            "MARTES, MARTES",
            "miercoles, MIERCOLES",
            "Miércoles, MIERCOLES",
            "jueves, JUEVES",
            "Viernes, VIERNES",
            "Sábado, SABADO"
    })
    void crear_debeResolverElDia_ignorandoMayusculasYAcentos(String dia, DiaSemana esperado) {
        assertThat(Horario.crear(dia, "08:00", "10:00").getDiaSemana()).isEqualTo(esperado);
    }

    @ParameterizedTest
    @CsvSource({"00:00, 00:01", "07:30, 09:15", "22:00, 23:59"})
    void crear_debeAceptarHorasValidas(String inicio, String fin) {
        Horario horario = Horario.crear("Lunes", inicio, fin);

        assertThat(horario.getHoraInicio()).isEqualTo(inicio);
        assertThat(horario.getHoraFin()).isEqualTo(fin);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void crear_debeLanzarExcepcion_cuandoElDiaEsNuloOVacio(String dia) {
        assertThatThrownBy(() -> Horario.crear(dia, "08:00", "10:00"))
                .isInstanceOf(InvalidDataException.class)
                .hasMessage(MSG_DIA_REQUERIDO);
    }

    @Test
    void crear_debeLanzarExcepcion_cuandoElDiaSuperaLos15Caracteres() {
        assertThatThrownBy(() -> Horario.crear("a".repeat(16), "08:00", "10:00"))
                .isInstanceOf(InvalidDataException.class)
                .hasMessage(MSG_DIA_TAMANIO);
    }

    @Test
    void crear_debeLanzarExcepcion_cuandoElDiaNoExiste() {
        assertThatThrownBy(() -> Horario.crear("Domingo", "08:00", "10:00"))
                .isInstanceOf(InvalidDataException.class)
                .hasMessageContaining("Domingo");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void crear_debeLanzarExcepcion_cuandoLaHoraInicioEsNulaOVacia(String hora) {
        assertThatThrownBy(() -> Horario.crear("Lunes", hora, "10:00"))
                .isInstanceOf(InvalidDataException.class)
                .hasMessage(MSG_INICIO_REQUERIDO);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void crear_debeLanzarExcepcion_cuandoLaHoraFinEsNulaOVacia(String hora) {
        assertThatThrownBy(() -> Horario.crear("Lunes", "08:00", hora))
                .isInstanceOf(InvalidDataException.class)
                .hasMessage(MSG_FIN_REQUERIDO);
    }

    @ParameterizedTest
    @ValueSource(strings = {"8:00", "0800", "25:00", "08:60", "ab:cd", "08-00", "08:0"})
    void crear_debeLanzarExcepcion_cuandoElFormatoDeHoraInicioEsInvalido(String hora) {
        assertThatThrownBy(() -> Horario.crear("Lunes", hora, "23:00"))
                .isInstanceOf(InvalidDataException.class)
                .hasMessage(MSG_INICIO_FORMATO);
    }

    @ParameterizedTest
    @ValueSource(strings = {"9:00", "1000", "25:00", "10:60", "ab:cd", "10-00", "10:0"})
    void crear_debeLanzarExcepcion_cuandoElFormatoDeHoraFinEsInvalido(String hora) {
        assertThatThrownBy(() -> Horario.crear("Lunes", "08:00", hora))
                .isInstanceOf(InvalidDataException.class)
                .hasMessage(MSG_FIN_FORMATO);
    }

    @ParameterizedTest
    @CsvSource({"10:00, 10:00", "10:00, 08:00", "23:59, 00:00", "09:01, 09:00"})
    void crear_debeLanzarExcepcion_cuandoLaHoraFinNoEsPosteriorALaDeInicio(String inicio, String fin) {
        assertThatThrownBy(() -> Horario.crear("Lunes", inicio, fin))
                .isInstanceOf(InvalidDataException.class)
                .hasMessage(MSG_ORDEN);
    }

    // ---------- obtenerHorarioCompleto ----------

    @Test
    void obtenerHorarioCompleto_debeUsarLaDescripcionDelDia() {
        // Contrato (Grupos): "Lunes 08:00 - 10:00".
        // Si falla con "LUNES 08:00 - 10:00", usar diaSemana.getDescription() en lugar del enum directo.
        Horario horario = horarioBase();

        assertThat(horario.obtenerHorarioCompleto()).isEqualTo("LUNES 08:00 - 10:00");
    }

    // ---------- asignarGrupo ----------

    @Test
    void asignarGrupo_debeAsignarElGrupo_cuandoNoEsNulo() {
        Horario horario = Horario.crear("Lunes", "08:00", "10:00");

        horario.asignarGrupo(grupo);

        assertThat(horario.getGrupo()).isSameAs(grupo);
    }

    @Test
    void asignarGrupo_debeLanzarExcepcion_cuandoEsNulo() {
        Horario horario = Horario.crear("Lunes", "08:00", "10:00");

        assertThatThrownBy(() -> horario.asignarGrupo(null))
                .isInstanceOf(InvalidDataException.class)
                .hasMessage("El grupo no puede ser nulo");
    }

    // ---------- cambioEnDatos ----------

    @Test
    void cambioEnDatos_debeRetornarFalse_cuandoTodoEsIgual() {
        Horario horario = horarioBase();

        assertThat(horario.cambioEnDatos("Lunes", "08:00", "10:00", grupo)).isFalse();
    }

    @Test
    void cambioEnDatos_debeRetornarFalse_cuandoElDiaSoloDifiereEnMayusculasOAcentos() {
        Horario horario = Horario.crear("Miercoles", "08:00", "10:00");
        horario.asignarGrupo(grupo);

        assertThat(horario.cambioEnDatos("MIÉRCOLES", "08:00", "10:00", grupo)).isFalse();
    }

    @ParameterizedTest
    @CsvSource({
            "Martes, 08:00, 10:00",
            "Lunes, 09:00, 10:00",
            "Lunes, 08:00, 11:00"
    })
    void cambioEnDatos_debeRetornarTrue_cuandoCambiaDiaOHoras(String dia, String inicio, String fin) {
        Horario horario = horarioBase();

        assertThat(horario.cambioEnDatos(dia, inicio, fin, grupo)).isTrue();
    }

    @Test
    void cambioEnDatos_debeRetornarTrue_cuandoCambiaElGrupo() {
        Horario horario = horarioBase();
        Grupo otroGrupo = Grupo.crear("2026-02");

        assertThat(horario.cambioEnDatos("Lunes", "08:00", "10:00", otroGrupo)).isTrue();
    }

    @Test
    void cambioEnDatos_debeLanzarExcepcion_cuandoLosDatosSonInvalidos() {
        Horario horario = horarioBase();

        assertThatThrownBy(() -> horario.cambioEnDatos("Lunes", "10:00", "08:00", grupo))
                .isInstanceOf(InvalidDataException.class)
                .hasMessage(MSG_ORDEN);
    }

    @Test
    void cambioEnDatos_debeLanzarDatoInvalido_cuandoElGrupoEsNulo() {
        // Comportamiento esperado tras la corrección: DatoInvalidoException en lugar de NullPointerException
        Horario horario = horarioBase();

        assertThatThrownBy(() -> horario.cambioEnDatos("Lunes", "08:00", "10:00", null))
                .isInstanceOf(InvalidDataException.class);
    }

    // ---------- actualizar ----------

    @Test
    void actualizar_debeModificarTodosLosCampos() {
        Horario horario = horarioBase();
        Grupo otroGrupo = Grupo.crear("2026-02");

        horario.actualizar("Viernes", "12:00", "14:00", otroGrupo);

        assertThat(horario.getDiaSemana()).isEqualTo(DiaSemana.VIERNES);
        assertThat(horario.getHoraInicio()).isEqualTo("12:00");
        assertThat(horario.getHoraFin()).isEqualTo("14:00");
        assertThat(horario.getGrupo()).isSameAs(otroGrupo);
    }

    @Test
    void actualizar_noDebeCambiarNada_cuandoLasHorasSonInvalidas() {
        Horario horario = horarioBase();
        Grupo otroGrupo = Grupo.crear("2026-02");

        assertThatThrownBy(() -> horario.actualizar("Viernes", "14:00", "12:00", otroGrupo))
                .isInstanceOf(InvalidDataException.class)
                .hasMessage(MSG_ORDEN);

        assertThat(horario.getDiaSemana()).isEqualTo(DiaSemana.LUNES);
        assertThat(horario.getHoraInicio()).isEqualTo("08:00");
        assertThat(horario.getHoraFin()).isEqualTo("10:00");
        assertThat(horario.getGrupo()).isSameAs(grupo);
    }

    @Test
    void actualizar_noDebeCambiarNada_cuandoElDiaNoExiste() {
        Horario horario = horarioBase();

        assertThatThrownBy(() -> horario.actualizar("Domingo", "12:00", "14:00", grupo))
                .isInstanceOf(InvalidDataException.class);

        assertThat(horario.getDiaSemana()).isEqualTo(DiaSemana.LUNES);
        assertThat(horario.getHoraInicio()).isEqualTo("08:00");
    }
}