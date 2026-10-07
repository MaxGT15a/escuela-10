package com.max.escuela.services.horario;

import com.max.escuela.dto.datos.DatosGrupoDTO;
import com.max.escuela.dto.horario.HorarioRequestDTO;
import com.max.escuela.dto.horario.HorarioResponseDTO;
import com.max.escuela.entities.Aula;
import com.max.escuela.entities.Grupo;
import com.max.escuela.entities.Horario;
import com.max.escuela.enums.DiaSemana;
import com.max.escuela.exceptions.InvalidDataException;
import com.max.escuela.exceptions.RelatedEntityException;
import com.max.escuela.exceptions.NoSuchResourceException;
import com.max.escuela.mapper.HorarioMapper;
import com.max.escuela.repositories.GrupoRepository;
import com.max.escuela.repositories.HorarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HorarioServiceImplTest {

    private static final long SIN_EXCLUIR = -1L; // id que usa registrar() para no excluir ningún horario

    @Mock
    private HorarioRepository horarioRepository;

    @Mock
    private HorarioMapper horarioMapper;

    @Mock
    private GrupoRepository grupoRepository;

    @InjectMocks
    private HorarioServiceImpl horarioService;

    private Aula aula;
    private Grupo grupo;
    private Horario horario;
    private HorarioResponseDTO horarioResponse;

    @BeforeEach
    void setUp() {
        aula = Aula.builder().id(3L).nombre("Aula 101").capacidad(30).build();
        grupo = Grupo.builder().id(5L).aula(aula).periodo("2026-01").build();

        horario = Horario.builder()
                .id(1L)
                .grupo(grupo)
                .diaSemana(DiaSemana.LUNES)
                .horaInicio("08:00")
                .horaFin("10:00")
                .build();

        horarioResponse = new HorarioResponseDTO(
                1L,
                new DatosGrupoDTO("Matemáticas I", "Laura Martínez Martínez", "Aula 101", "2026-01"),
                "Lunes 08:00 - 10:00");
    }

    // ---------- listar() / obtenerPorId() ----------

    @Test
    void listar_debeRetornarListaDeHorarios_cuandoExistenRegistros() {
        when(horarioRepository.findAll()).thenReturn(List.of(horario));
        when(horarioMapper.entidadAResponse(horario)).thenReturn(horarioResponse);

        assertThat(horarioService.listar()).containsExactly(horarioResponse);
    }

    @Test
    void listar_debeRetornarListaVacia_cuandoNoHayHorarios() {
        when(horarioRepository.findAll()).thenReturn(List.of());

        assertThat(horarioService.listar()).isEmpty();
    }

    @Test
    void obtenerPorId_debeRetornarHorario_cuandoExiste() {
        when(horarioRepository.findById(1L)).thenReturn(Optional.of(horario));
        when(horarioMapper.entidadAResponse(horario)).thenReturn(horarioResponse);

        assertThat(horarioService.obtenerPorId(1L).id()).isEqualTo(1L);
    }

    @Test
    void obtenerPorId_debeLanzarExcepcion_cuandoNoExiste() {
        when(horarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> horarioService.obtenerPorId(99L))
                .isInstanceOf(NoSuchResourceException.class)
                .hasMessageContaining("Horario no encontrado con id: 99");
    }

    // ---------- registrar() ----------

    @Test
    void registrar_debeGuardarYRetornarHorario_cuandoNoHayTraslape() {
        HorarioRequestDTO request = new HorarioRequestDTO(5L, "Lunes", "08:00", "10:00");
        when(grupoRepository.findById(5L)).thenReturn(Optional.of(grupo));
        when(horarioRepository.existeTraslape(
                DiaSemana.LUNES, "08:00", "10:00", "2026-01", 5L, 3L, SIN_EXCLUIR)).thenReturn(false);
        when(horarioMapper.requestAEntidad(request, grupo)).thenReturn(horario);
        when(horarioMapper.entidadAResponse(horario)).thenReturn(horarioResponse);

        HorarioResponseDTO resultado = horarioService.registrar(request);

        assertThat(resultado).isEqualTo(horarioResponse);
        verify(horarioRepository).saveAndFlush(horario);
    }

    @Test
    void registrar_debeResolverElDiaIgnorandoAcentosYMayusculas() {
        HorarioRequestDTO request = new HorarioRequestDTO(5L, "MIÉRCOLES", "08:00", "10:00");
        when(grupoRepository.findById(5L)).thenReturn(Optional.of(grupo));
        when(horarioRepository.existeTraslape(
                DiaSemana.MIERCOLES, "08:00", "10:00", "2026-01", 5L, 3L, SIN_EXCLUIR)).thenReturn(false);
        when(horarioMapper.requestAEntidad(request, grupo)).thenReturn(horario);
        when(horarioMapper.entidadAResponse(horario)).thenReturn(horarioResponse);

        horarioService.registrar(request);

        verify(horarioRepository).saveAndFlush(horario);
    }

    @Test
    void registrar_debeLanzarExcepcion_cuandoElGrupoNoExiste() {
        HorarioRequestDTO request = new HorarioRequestDTO(99L, "Lunes", "08:00", "10:00");
        when(grupoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> horarioService.registrar(request))
                .isInstanceOf(NoSuchResourceException.class)
                .hasMessageContaining("Grupo no encontrado con id: 99");

        verifyNoInteractions(horarioMapper);
        verify(horarioRepository, never()).saveAndFlush(any());
    }

    @Test
    void registrar_debeLanzarExcepcion_cuandoElDiaNoExiste() {
        HorarioRequestDTO request = new HorarioRequestDTO(5L, "Domingo", "08:00", "10:00");
        when(grupoRepository.findById(5L)).thenReturn(Optional.of(grupo));

        assertThatThrownBy(() -> horarioService.registrar(request))
                .isInstanceOf(InvalidDataException.class)
                .hasMessageContaining("Domingo");

        verify(horarioRepository, never()).existeTraslape(any(), anyString(), anyString(), anyString(),
                anyLong(), anyLong(), anyLong());
        verifyNoInteractions(horarioMapper);
    }

    @Test
    void registrar_debeLanzarExcepcion_cuandoHayTraslapeConOtroHorarioDelGrupoOAula() {
        // Comportamiento ACTUAL: EntidadRelacionadaException. El contrato pide 400, ver test deshabilitado.
        HorarioRequestDTO request = new HorarioRequestDTO(5L, "Lunes", "08:00", "10:00");
        when(grupoRepository.findById(5L)).thenReturn(Optional.of(grupo));
        when(horarioRepository.existeTraslape(
                DiaSemana.LUNES, "08:00", "10:00", "2026-01", 5L, 3L, SIN_EXCLUIR)).thenReturn(true);

        assertThatThrownBy(() -> horarioService.registrar(request))
                .isInstanceOf(RelatedEntityException.class)
                .hasMessageContaining("El horario se traslapa con otro del mismo grupo o aula");

        verifyNoInteractions(horarioMapper);
        verify(horarioRepository, never()).saveAndFlush(any());
    }

    @Disabled("Contrato: traslape = regla de negocio => 400 (DatoInvalidoException / IllegalArgumentException). " +
            "Hoy se lanza EntidadRelacionadaException (409). Activa al corregirlo y elimina el test anterior.")
    @Test
    void registrar_debeLanzarDatoInvalido_cuandoHayTraslape() {
        HorarioRequestDTO request = new HorarioRequestDTO(5L, "Lunes", "08:00", "10:00");
        when(grupoRepository.findById(5L)).thenReturn(Optional.of(grupo));
        when(horarioRepository.existeTraslape(
                DiaSemana.LUNES, "08:00", "10:00", "2026-01", 5L, 3L, SIN_EXCLUIR)).thenReturn(true);

        assertThatThrownBy(() -> horarioService.registrar(request))
                .isInstanceOf(InvalidDataException.class);
    }

    @Disabled("Mejora pendiente: registrar() consulta el traslape ANTES de validar formato y orden de horas " +
            "(esa validación ocurre en Horario.crear, dentro del mapper). Con horaFin <= horaInicio no " +
            "debería llegar a consultar el repositorio. Activa al mover la validación antes de la consulta.")
    @Test
    void registrar_noDebeConsultarTraslape_cuandoLaHoraFinNoEsPosteriorALaDeInicio() {
        HorarioRequestDTO request = new HorarioRequestDTO(5L, "Lunes", "10:00", "08:00");
        when(grupoRepository.findById(5L)).thenReturn(Optional.of(grupo));

        assertThatThrownBy(() -> horarioService.registrar(request))
                .isInstanceOf(InvalidDataException.class);

        verify(horarioRepository, never()).existeTraslape(any(), anyString(), anyString(), anyString(),
                anyLong(), anyLong(), anyLong());
    }

    // ---------- actualizar() ----------

    @Test
    void actualizar_noDebeValidarNiGuardar_cuandoNoHayCambios() {
        HorarioRequestDTO request = new HorarioRequestDTO(5L, "Lunes", "08:00", "10:00");
        when(horarioRepository.findById(1L)).thenReturn(Optional.of(horario));
        when(grupoRepository.findById(5L)).thenReturn(Optional.of(grupo));
        when(horarioMapper.entidadAResponse(horario)).thenReturn(horarioResponse);

        HorarioResponseDTO resultado = horarioService.actualizar(request, 1L);

        assertThat(resultado).isEqualTo(horarioResponse);
        verify(horarioRepository, never()).existeTraslape(any(), anyString(), anyString(), anyString(),
                anyLong(), anyLong(), anyLong());
        verify(horarioRepository, never()).saveAndFlush(any());
    }

    @Test
    void actualizar_debeActualizarYGuardar_cuandoCambiaDiaYHorasSinTraslape() {
        HorarioRequestDTO request = new HorarioRequestDTO(5L, "Viernes", "12:00", "14:00");
        when(horarioRepository.findById(1L)).thenReturn(Optional.of(horario));
        when(grupoRepository.findById(5L)).thenReturn(Optional.of(grupo));
        // Al actualizar se excluye a sí mismo (id = 1)
        when(horarioRepository.existeTraslape(
                DiaSemana.VIERNES, "12:00", "14:00", "2026-01", 5L, 3L, 1L)).thenReturn(false);
        when(horarioMapper.entidadAResponse(horario)).thenReturn(horarioResponse);

        horarioService.actualizar(request, 1L);

        assertThat(horario.getDiaSemana()).isEqualTo(DiaSemana.VIERNES);
        assertThat(horario.getHoraInicio()).isEqualTo("12:00");
        assertThat(horario.getHoraFin()).isEqualTo("14:00");
        verify(horarioRepository).saveAndFlush(horario);
    }

    @Test
    void actualizar_debeUsarElAulaYElPeriodoDelNuevoGrupo_cuandoCambiaElGrupo() {
        Aula otraAula = Aula.builder().id(4L).nombre("Laboratorio A").capacidad(28).build();
        Grupo otroGrupo = Grupo.builder().id(6L).aula(otraAula).periodo("2026-02").build();
        HorarioRequestDTO request = new HorarioRequestDTO(6L, "Lunes", "08:00", "10:00");
        when(horarioRepository.findById(1L)).thenReturn(Optional.of(horario));
        when(grupoRepository.findById(6L)).thenReturn(Optional.of(otroGrupo));
        when(horarioRepository.existeTraslape(
                DiaSemana.LUNES, "08:00", "10:00", "2026-02", 6L, 4L, 1L)).thenReturn(false);
        when(horarioMapper.entidadAResponse(horario)).thenReturn(horarioResponse);

        horarioService.actualizar(request, 1L);

        assertThat(horario.getGrupo()).isSameAs(otroGrupo);
        verify(horarioRepository).saveAndFlush(horario);
    }

    @Test
    void actualizar_debeLanzarExcepcion_cuandoHayTraslape() {
        HorarioRequestDTO request = new HorarioRequestDTO(5L, "Viernes", "12:00", "14:00");
        when(horarioRepository.findById(1L)).thenReturn(Optional.of(horario));
        when(grupoRepository.findById(5L)).thenReturn(Optional.of(grupo));
        when(horarioRepository.existeTraslape(
                DiaSemana.VIERNES, "12:00", "14:00", "2026-01", 5L, 3L, 1L)).thenReturn(true);

        assertThatThrownBy(() -> horarioService.actualizar(request, 1L))
                .isInstanceOf(RelatedEntityException.class)
                .hasMessageContaining("El horario se traslapa");

        assertThat(horario.getDiaSemana()).isEqualTo(DiaSemana.LUNES); // sin cambios
        verify(horarioRepository, never()).saveAndFlush(any());
    }

    @Test
    void actualizar_noDebeConsultarTraslape_cuandoLaHoraFinNoEsPosteriorALaDeInicio() {
        // Horario.cambioEnDatos valida las horas antes de que el service consulte el repositorio
        HorarioRequestDTO request = new HorarioRequestDTO(5L, "Lunes", "10:00", "08:00");
        when(horarioRepository.findById(1L)).thenReturn(Optional.of(horario));
        when(grupoRepository.findById(5L)).thenReturn(Optional.of(grupo));

        assertThatThrownBy(() -> horarioService.actualizar(request, 1L))
                .isInstanceOf(InvalidDataException.class)
                .hasMessage("La hora de inicio debe ser menor a la hora de fin");

        verify(horarioRepository, never()).existeTraslape(any(), anyString(), anyString(), anyString(),
                anyLong(), anyLong(), anyLong());
        verify(horarioRepository, never()).saveAndFlush(any());
    }

    @Test
    void actualizar_debeLanzarExcepcion_cuandoElFormatoDeHoraEsInvalido() {
        HorarioRequestDTO request = new HorarioRequestDTO(5L, "Lunes", "8:00 ", "10:00");
        when(horarioRepository.findById(1L)).thenReturn(Optional.of(horario));
        when(grupoRepository.findById(5L)).thenReturn(Optional.of(grupo));

        assertThatThrownBy(() -> horarioService.actualizar(request, 1L))
                .isInstanceOf(InvalidDataException.class);

        verify(horarioRepository, never()).saveAndFlush(any());
    }

    @Test
    void actualizar_debeLanzarExcepcion_cuandoElDiaNoExiste() {
        HorarioRequestDTO request = new HorarioRequestDTO(5L, "Domingo", "08:00", "10:00");
        when(horarioRepository.findById(1L)).thenReturn(Optional.of(horario));
        when(grupoRepository.findById(5L)).thenReturn(Optional.of(grupo));

        assertThatThrownBy(() -> horarioService.actualizar(request, 1L))
                .isInstanceOf(InvalidDataException.class)
                .hasMessageContaining("Domingo");

        verify(horarioRepository, never()).saveAndFlush(any());
    }

    @Test
    void actualizar_debeLanzarExcepcion_cuandoElHorarioNoExiste() {
        HorarioRequestDTO request = new HorarioRequestDTO(5L, "Lunes", "08:00", "10:00");
        when(horarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> horarioService.actualizar(request, 99L))
                .isInstanceOf(NoSuchResourceException.class)
                .hasMessageContaining("Horario no encontrado con id: 99");

        verifyNoInteractions(grupoRepository);
    }

    @Test
    void actualizar_debeLanzarExcepcion_cuandoElGrupoNoExiste() {
        HorarioRequestDTO request = new HorarioRequestDTO(99L, "Lunes", "08:00", "10:00");
        when(horarioRepository.findById(1L)).thenReturn(Optional.of(horario));
        when(grupoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> horarioService.actualizar(request, 1L))
                .isInstanceOf(NoSuchResourceException.class)
                .hasMessageContaining("Grupo no encontrado con id: 99");

        verify(horarioRepository, never()).saveAndFlush(any());
    }

    // ---------- eliminar() ----------

    @Test
    void eliminar_debeEliminarHorario_cuandoExiste() {
        when(horarioRepository.findById(1L)).thenReturn(Optional.of(horario));

        horarioService.eliminar(1L);

        verify(horarioRepository).delete(horario);
        verify(horarioRepository).flush();
    }

    @Test
    void eliminar_debeLanzarExcepcion_cuandoNoExiste() {
        when(horarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> horarioService.eliminar(99L))
                .isInstanceOf(NoSuchResourceException.class);

        verify(horarioRepository, never()).delete(any());
    }
}