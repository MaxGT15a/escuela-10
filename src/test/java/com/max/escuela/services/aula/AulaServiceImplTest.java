package com.max.escuela.services.aula;

import com.max.escuela.dto.aula.AulaRequestDTO;
import com.max.escuela.dto.aula.AulaResponseDTO;
import com.max.escuela.entities.Aula;
import com.max.escuela.exceptions.ConflictException;
import com.max.escuela.exceptions.InvalidDataException;
import com.max.escuela.exceptions.RelatedEntityException;
import com.max.escuela.exceptions.NoSuchResourceException;
import com.max.escuela.mapper.AulaMapper;
import com.max.escuela.repositories.AulaRepository;
import com.max.escuela.repositories.GrupoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AulaServiceImplTest {

    @Mock
    private AulaRepository aulaRepository;

    @Mock
    private AulaMapper aulaMapper;

    // Sin este mock, @InjectMocks inyecta null y eliminar() lanza NullPointerException
    @Mock
    private GrupoRepository grupoRepository;

    @InjectMocks
    private AulaServiceImpl aulaService;

    private Aula aula;
    private AulaResponseDTO aulaResponse;

    @BeforeEach
    void setUp() {
        aula = Aula.builder()
                .id(1L)
                .nombre("Aula 101")
                .capacidad(30)
                .build();

        aulaResponse = new AulaResponseDTO(1L, "Aula 101", 30);
    }

    // ---------- listar() ----------

    @Test
    void listar_debeRetornarListaDeAulas_cuandoExistenRegistros() {
        when(aulaRepository.findAll()).thenReturn(List.of(aula));
        when(aulaMapper.entidadAResponse(aula)).thenReturn(aulaResponse);

        List<AulaResponseDTO> resultado = aulaService.listar();

        assertThat(resultado).containsExactly(aulaResponse);
        verify(aulaRepository).findAll();
    }

    @Test
    void listar_debeRetornarListaVacia_cuandoNoHayAulasRegistradas() {
        when(aulaRepository.findAll()).thenReturn(List.of());

        List<AulaResponseDTO> resultado = aulaService.listar();

        assertThat(resultado).isEmpty();
    }

    // ---------- obtenerPorId() ----------

    @Test
    void obtenerPorId_debeRetornarAula_cuandoExiste() {
        when(aulaRepository.findById(1L)).thenReturn(Optional.of(aula));
        when(aulaMapper.entidadAResponse(aula)).thenReturn(aulaResponse);

        AulaResponseDTO resultado = aulaService.obtenerPorId(1L);

        assertThat(resultado.id()).isEqualTo(1L);
        assertThat(resultado.capacidad()).isEqualTo(30);
    }

    @Test
    void obtenerPorId_debeLanzarExcepcion_cuandoNoExiste() {
        when(aulaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> aulaService.obtenerPorId(99L))
                .isInstanceOf(NoSuchResourceException.class)
                .hasMessageContaining("Aula no encontrado con id: 99");
    }

    // ---------- registrar() ----------

    @Test
    void registrar_debeGuardarYRetornarAula_cuandoElNombreNoExistePreviamente() {
        // El service pregunta por el nombre ya limpio (trimmed)
        AulaRequestDTO request = new AulaRequestDTO("  Aula 101  ", 30);
        when(aulaRepository.existsByNombre("Aula 101")).thenReturn(false);
        when(aulaMapper.entidadAResponse(any(Aula.class))).thenReturn(aulaResponse);

        AulaResponseDTO resultado = aulaService.registrar(request);

        assertThat(resultado).isEqualTo(aulaResponse);
        ArgumentCaptor<Aula> captor = ArgumentCaptor.forClass(Aula.class);
        verify(aulaRepository).saveAndFlush(captor.capture());
        assertThat(captor.getValue().getNombre()).isEqualTo("Aula 101");
        assertThat(captor.getValue().getCapacidad()).isEqualTo(30);
    }

    @Test
    void registrar_debeLanzarExcepcion_cuandoYaExisteUnaAulaConEseNombre() {
        AulaRequestDTO request = new AulaRequestDTO("Aula 101", 30);
        when(aulaRepository.existsByNombre("Aula 101")).thenReturn(true);

        assertThatThrownBy(() -> aulaService.registrar(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("El nombre del aula ya existe");

        verify(aulaRepository, never()).saveAndFlush(any());
    }

    @Test
    void registrar_debeLanzarExcepcion_cuandoLosDatosSonInvalidos() {
        AulaRequestDTO request = new AulaRequestDTO("Ab", 30);

        assertThatThrownBy(() -> aulaService.registrar(request))
                .isInstanceOf(InvalidDataException.class);

        verifyNoInteractions(aulaRepository, aulaMapper);
    }

    // ---------- actualizar() ----------

    @Test
    void actualizar_debeActualizarDatos_cuandoAulaExisteYNombreEstaLibre() {
        when(aulaRepository.findById(1L)).thenReturn(Optional.of(aula));
        when(aulaRepository.existsByNombreAndIdNot("Aula 202", 1L)).thenReturn(false);

        AulaRequestDTO request = new AulaRequestDTO("Aula 202", 40);
        AulaResponseDTO respuestaEsperada = new AulaResponseDTO(1L, "Aula 202", 40);
        when(aulaMapper.entidadAResponse(aula)).thenReturn(respuestaEsperada);

        AulaResponseDTO resultado = aulaService.actualizar(request, 1L);

        assertThat(resultado.nombre()).isEqualTo("Aula 202");
        assertThat(aula.getNombre()).isEqualTo("Aula 202");
        assertThat(aula.getCapacidad()).isEqualTo(40);
        verify(aulaRepository).saveAndFlush(aula);
    }

    @Test
    void actualizar_debeLanzarExcepcion_cuandoElAulaNoExiste() {
        when(aulaRepository.findById(99L)).thenReturn(Optional.empty());

        AulaRequestDTO request = new AulaRequestDTO("Aula 202", 40);

        assertThatThrownBy(() -> aulaService.actualizar(request, 99L))
                .isInstanceOf(NoSuchResourceException.class)
                .hasMessageContaining("Aula no encontrado con id: 99");

        verify(aulaRepository, never()).saveAndFlush(any());
    }

    @Test
    void actualizar_debeLanzarExcepcion_cuandoElNuevoNombreYaLoUsaOtraAula() {
        when(aulaRepository.findById(1L)).thenReturn(Optional.of(aula));
        when(aulaRepository.existsByNombreAndIdNot("Aula 202", 1L)).thenReturn(true);

        AulaRequestDTO request = new AulaRequestDTO("Aula 202", 40);

        assertThatThrownBy(() -> aulaService.actualizar(request, 1L))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Una aula con el mismo nombre ya existe");

        assertThat(aula.getNombre()).isEqualTo("Aula 101"); // sin cambios
        verify(aulaRepository, never()).saveAndFlush(any());
    }

    @Test
    void actualizar_debeLanzarExcepcion_cuandoLosDatosSonInvalidos() {
        when(aulaRepository.findById(1L)).thenReturn(Optional.of(aula));

        AulaRequestDTO request = new AulaRequestDTO("Aula 202", 0);

        assertThatThrownBy(() -> aulaService.actualizar(request, 1L))
                .isInstanceOf(InvalidDataException.class);

        verify(aulaRepository, never()).existsByNombreAndIdNot(anyString(), anyLong());
        verify(aulaRepository, never()).saveAndFlush(any());
    }

    // ---------- eliminar() ----------

    @Test
    void eliminar_debeEliminarAula_cuandoExisteYNoTieneGrupos() {
        when(aulaRepository.findById(1L)).thenReturn(Optional.of(aula));
        when(grupoRepository.existsByAulaId(1L)).thenReturn(false);

        aulaService.eliminar(1L);

        verify(aulaRepository).delete(aula);
        verify(aulaRepository).flush();
    }

    @Test
    void eliminar_debeLanzarExcepcion_cuandoElAulaTieneGruposAsignados() {
        when(aulaRepository.findById(1L)).thenReturn(Optional.of(aula));
        when(grupoRepository.existsByAulaId(1L)).thenReturn(true);

        assertThatThrownBy(() -> aulaService.eliminar(1L))
                .isInstanceOf(RelatedEntityException.class)
                .hasMessageContaining("No se puede eliminar si tiene grupos asignados");

        verify(aulaRepository, never()).delete(any());
    }

    @Test
    void eliminar_debeLanzarExcepcion_cuandoNoExiste() {
        when(aulaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> aulaService.eliminar(99L))
                .isInstanceOf(NoSuchResourceException.class);

        verify(aulaRepository, never()).delete(any());
        verifyNoInteractions(grupoRepository);
    }
}