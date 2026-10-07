package com.max.escuela.controller;

import com.max.escuela.dto.horario.HorarioRequestDTO;
import com.max.escuela.dto.horario.HorarioResponseDTO;
import com.max.escuela.dto.datos.DatosGrupoDTO;
import com.max.escuela.exceptions.NoSuchResourceException;
import com.max.escuela.services.horario.HorarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HorarioController.class)
class HorarioControllerTest {

    private static final String URL = "/api/horarios";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private HorarioService horarioService;

    private HorarioRequestDTO requestValido() {
        return new HorarioRequestDTO(1L, "Lunes", "08:00", "10:00");
    }

    private HorarioResponseDTO responseValida() {
        return new HorarioResponseDTO(
                1L,
                new DatosGrupoDTO("Matemáticas I", "Laura Martínez Martínez", "Aula 101", "2026-01"),
                "Lunes 08:00 - 10:00"
        );
    }

    // ---------- GET /api/horarios ----------

    @Test
    void listar_debeRetornar200ConLaLista() throws Exception {
        when(horarioService.listar()).thenReturn(List.of(responseValida()));

        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].grupo.curso").value("Matemáticas I"))
                .andExpect(jsonPath("$[0].grupo.maestro").value("Laura Martínez Martínez"))
                .andExpect(jsonPath("$[0].grupo.aula").value("Aula 101"))
                .andExpect(jsonPath("$[0].grupo.periodo").value("2026-01"))
                .andExpect(jsonPath("$[0].horario").value("Lunes 08:00 - 10:00"));
    }

    @Test
    void listar_debeRetornar200ConListaVacia_cuandoNoHayHorarios() throws Exception {
        when(horarioService.listar()).thenReturn(List.of());

        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ---------- GET /api/horarios/{id} ----------

    @Test
    void obtenerPorId_debeRetornar200_cuandoElHorarioExiste() throws Exception {
        when(horarioService.obtenerPorId(1L)).thenReturn(responseValida());

        mockMvc.perform(get(URL + "/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.grupo.curso").value("Matemáticas I"))
                .andExpect(jsonPath("$.grupo.maestro").value("Laura Martínez Martínez"))
                .andExpect(jsonPath("$.grupo.aula").value("Aula 101"))
                .andExpect(jsonPath("$.grupo.periodo").value("2026-01"))
                .andExpect(jsonPath("$.horario").value("Lunes 08:00 - 10:00"));
    }

    @Test
    void obtenerPorId_debeRetornar404_cuandoElHorarioNoExiste() throws Exception {
        when(horarioService.obtenerPorId(99L))
                .thenThrow(new NoSuchResourceException("Horario no encontrado con id: 99"));

        mockMvc.perform(get(URL + "/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Horario no encontrado con id: 99"));
    }

    @Test
    void obtenerPorId_debeRetornar400_cuandoElIdEsNegativo() throws Exception {
        mockMvc.perform(get(URL + "/{id}", -1L))
                .andExpect(status().isBadRequest());

        verify(horarioService, never()).obtenerPorId(any());
    }

    @Test
    void obtenerPorId_debeRetornar400_cuandoElIdEsCero() throws Exception {
        mockMvc.perform(get(URL + "/{id}", 0L))
                .andExpect(status().isBadRequest());

        verify(horarioService, never()).obtenerPorId(any());
    }

    // ---------- POST /api/horarios ----------

    @Test
    void registrar_debeRetornar201_cuandoDatosSonValidos() throws Exception {
        when(horarioService.registrar(any(HorarioRequestDTO.class))).thenReturn(responseValida());

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.grupo.curso").value("Matemáticas I"))
                .andExpect(jsonPath("$.grupo.maestro").value("Laura Martínez Martínez"))
                .andExpect(jsonPath("$.grupo.aula").value("Aula 101"))
                .andExpect(jsonPath("$.grupo.periodo").value("2026-01"))
                .andExpect(jsonPath("$.horario").value("Lunes 08:00 - 10:00"));

        verify(horarioService).registrar(any(HorarioRequestDTO.class));
    }

    @Test
    void registrar_debeRetornar404_cuandoElGrupoNoExiste() throws Exception {
        when(horarioService.registrar(any(HorarioRequestDTO.class)))
                .thenThrow(new NoSuchResourceException("Grupo no encontrado con id: 99"));

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Grupo no encontrado con id: 99"));
    }

    @Test
    void registrar_debeRetornar400_cuandoElIdDeGrupoEsNulo() throws Exception {
        HorarioRequestDTO request = new HorarioRequestDTO(null, "Lunes", "08:00", "10:00");

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(horarioService, never()).registrar(any());
    }

    @Test
    void registrar_debeRetornar400_cuandoElIdDeGrupoEsCero() throws Exception {
        HorarioRequestDTO request = new HorarioRequestDTO(0L, "Lunes", "08:00", "10:00");

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(horarioService, never()).registrar(any());
    }

    @Test
    void registrar_debeRetornar400_cuandoElIdDeGrupoEsNegativo() throws Exception {
        HorarioRequestDTO request = new HorarioRequestDTO(-1L, "Lunes", "08:00", "10:00");

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(horarioService, never()).registrar(any());
    }

    @Test
    void registrar_debeRetornar400_cuandoElDiaEstaVacio() throws Exception {
        HorarioRequestDTO request = new HorarioRequestDTO(1L, "", "08:00", "10:00");

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(horarioService, never()).registrar(any());
    }

    @Test
    void registrar_debeRetornar400_cuandoElDiaSoloTieneEspacios() throws Exception {
        HorarioRequestDTO request = new HorarioRequestDTO(1L, "   ", "08:00", "10:00");

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(horarioService, never()).registrar(any());
    }

    @Test
    void registrar_debeRetornar400_cuandoElDiaEsNulo() throws Exception {
        HorarioRequestDTO request = new HorarioRequestDTO(1L, null, "08:00", "10:00");

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(horarioService, never()).registrar(any());
    }

    @Test
    void registrar_debeRetornar400_cuandoElDiaEsMuyLargo() throws Exception {
        HorarioRequestDTO request = new HorarioRequestDTO(1L, "A".repeat(16), "08:00", "10:00");

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(horarioService, never()).registrar(any());
    }

    @Test
    void registrar_debeRetornar400_cuandoLaHoraDeInicioEstaVacia() throws Exception {
        HorarioRequestDTO request = new HorarioRequestDTO(1L, "Lunes", "", "10:00");

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(horarioService, never()).registrar(any());
    }

    @Test
    void registrar_debeRetornar400_cuandoLaHoraDeInicioEsNula() throws Exception {
        HorarioRequestDTO request = new HorarioRequestDTO(1L, "Lunes", null, "10:00");

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(horarioService, never()).registrar(any());
    }

    @Test
    void registrar_debeRetornar400_cuandoLaHoraDeInicioNoTieneCincoCaracteres() throws Exception {
        HorarioRequestDTO request = new HorarioRequestDTO(1L, "Lunes", "8:00", "10:00");

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(horarioService, never()).registrar(any());
    }

    @Test
    void registrar_debeRetornar400_cuandoLaHoraDeFinEstaVacia() throws Exception {
        HorarioRequestDTO request = new HorarioRequestDTO(1L, "Lunes", "08:00", "");

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(horarioService, never()).registrar(any());
    }

    @Test
    void registrar_debeRetornar400_cuandoLaHoraDeFinEsNula() throws Exception {
        HorarioRequestDTO request = new HorarioRequestDTO(1L, "Lunes", "08:00", null);

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(horarioService, never()).registrar(any());
    }

    @Test
    void registrar_debeRetornar400_cuandoLaHoraDeFinNoTieneCincoCaracteres() throws Exception {
        HorarioRequestDTO request = new HorarioRequestDTO(1L, "Lunes", "08:00", "10:0");

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(horarioService, never()).registrar(any());
    }

    @Test
    void registrar_debeRetornar400_cuandoNoHayBody() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(horarioService, never()).registrar(any());
    }

    // ---------- PUT /api/horarios/{id} ----------

    @Test
    void actualizar_debeRetornar200_cuandoDatosSonValidos() throws Exception {
        when(horarioService.actualizar(any(HorarioRequestDTO.class), eq(1L))).thenReturn(responseValida());

        mockMvc.perform(put(URL + "/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.grupo.curso").value("Matemáticas I"))
                .andExpect(jsonPath("$.grupo.maestro").value("Laura Martínez Martínez"))
                .andExpect(jsonPath("$.grupo.aula").value("Aula 101"))
                .andExpect(jsonPath("$.grupo.periodo").value("2026-01"))
                .andExpect(jsonPath("$.horario").value("Lunes 08:00 - 10:00"));
    }

    @Test
    void actualizar_debeRetornar404_cuandoElHorarioNoExiste() throws Exception {
        when(horarioService.actualizar(any(HorarioRequestDTO.class), eq(99L)))
                .thenThrow(new NoSuchResourceException("Horario no encontrado con id: 99"));

        mockMvc.perform(put(URL + "/{id}", 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Horario no encontrado con id: 99"));
    }

    @Test
    void actualizar_debeRetornar400_cuandoElIdEsNegativo() throws Exception {
        mockMvc.perform(put(URL + "/{id}", -1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido())))
                .andExpect(status().isBadRequest());

        verify(horarioService, never()).actualizar(any(), any());
    }

    @Test
    void actualizar_debeRetornar400_cuandoElBodyEsInvalido() throws Exception {
        HorarioRequestDTO request = new HorarioRequestDTO(1L, "", "08:00", "10:00");

        mockMvc.perform(put(URL + "/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(horarioService, never()).actualizar(any(), any());
    }

    // ---------- DELETE /api/horarios/{id} ----------

    @Test
    void eliminar_debeRetornar204_cuandoElHorarioExiste() throws Exception {
        mockMvc.perform(delete(URL + "/{id}", 1L))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(horarioService).eliminar(1L);
    }

    @Test
    void eliminar_debeRetornar404_cuandoElHorarioNoExiste() throws Exception {
        doThrow(new NoSuchResourceException("Horario no encontrado con id: 99"))
                .when(horarioService).eliminar(99L);

        mockMvc.perform(delete(URL + "/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Horario no encontrado con id: 99"));
    }

    @Test
    void eliminar_debeRetornar400_cuandoElIdEsNegativo() throws Exception {
        mockMvc.perform(delete(URL + "/{id}", -1L))
                .andExpect(status().isBadRequest());

        verify(horarioService, never()).eliminar(any());
    }
}