package com.max.escuela.controller;

import com.max.escuela.dto.inscripcion.InscripcionRequestDTO;
import com.max.escuela.dto.inscripcion.InscripcionResponseDTO;
import com.max.escuela.dto.datos.DatosAlumnoDTO;
import com.max.escuela.dto.datos.DatosGrupoDTO;
import com.max.escuela.exceptions.NoSuchResourceException;
import com.max.escuela.services.inscripcion.InscripcionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InscripcionController.class)
class InscripcionControllerTest {

    private static final String URL = "/api/inscripciones";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private InscripcionService inscripcionService;

    private InscripcionRequestDTO requestValido() {
        return new InscripcionRequestDTO(1L, 1L);
    }

    private InscripcionResponseDTO responseValida() {
        return new InscripcionResponseDTO(
                1L,
                new DatosAlumnoDTO("Carlos González Ramírez", "A2026001", "carlos.gonzalez@alumnos.com", "10/01/2026"),
                new DatosGrupoDTO("Matemáticas I", "Laura Martínez Martínez", "Aula 101", "2026-01"),
                new BigDecimal("8.5"),
                "15/01/2026"
        );
    }

    // ---------- GET /api/inscripciones ----------

    @Test
    void listar_debeRetornar200ConLaLista() throws Exception {
        when(inscripcionService.listar()).thenReturn(List.of(responseValida()));

        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].alumno.nombre").value("Carlos González Ramírez"))
                .andExpect(jsonPath("$[0].alumno.matricula").value("A2026001"))
                .andExpect(jsonPath("$[0].grupo.curso").value("Matemáticas I"))
                .andExpect(jsonPath("$[0].grupo.periodo").value("2026-01"))
                .andExpect(jsonPath("$[0].calificacion").value(8.5))
                .andExpect(jsonPath("$[0].fechaInscripcion").value("15/01/2026"));
    }

    @Test
    void listar_debeRetornar200ConListaVacia_cuandoNoHayInscripciones() throws Exception {
        when(inscripcionService.listar()).thenReturn(List.of());

        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ---------- GET /api/inscripciones/{id} ----------

    @Test
    void obtenerPorId_debeRetornar200_cuandoLaInscripcionExiste() throws Exception {
        when(inscripcionService.obtenerPorId(1L)).thenReturn(responseValida());

        mockMvc.perform(get(URL + "/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.alumno.nombre").value("Carlos González Ramírez"))
                .andExpect(jsonPath("$.alumno.matricula").value("A2026001"))
                .andExpect(jsonPath("$.grupo.curso").value("Matemáticas I"))
                .andExpect(jsonPath("$.grupo.periodo").value("2026-01"))
                .andExpect(jsonPath("$.calificacion").value(8.5))
                .andExpect(jsonPath("$.fechaInscripcion").value("15/01/2026"));
    }

    @Test
    void obtenerPorId_debeRetornar404_cuandoLaInscripcionNoExiste() throws Exception {
        when(inscripcionService.obtenerPorId(99L))
                .thenThrow(new NoSuchResourceException("Inscripcion no encontrada con id: 99"));

        mockMvc.perform(get(URL + "/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Inscripcion no encontrada con id: 99"));
    }

    @Test
    void obtenerPorId_debeRetornar400_cuandoElIdEsNegativo() throws Exception {
        mockMvc.perform(get(URL + "/{id}", -1L))
                .andExpect(status().isBadRequest());

        verify(inscripcionService, never()).obtenerPorId(any());
    }

    @Test
    void obtenerPorId_debeRetornar400_cuandoElIdEsCero() throws Exception {
        mockMvc.perform(get(URL + "/{id}", 0L))
                .andExpect(status().isBadRequest());

        verify(inscripcionService, never()).obtenerPorId(any());
    }

    // ---------- POST /api/inscripciones ----------

    @Test
    void registrar_debeRetornar201_cuandoDatosSonValidos() throws Exception {
        when(inscripcionService.registrar(any(InscripcionRequestDTO.class))).thenReturn(responseValida());

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.alumno.nombre").value("Carlos González Ramírez"))
                .andExpect(jsonPath("$.alumno.matricula").value("A2026001"))
                .andExpect(jsonPath("$.grupo.curso").value("Matemáticas I"))
                .andExpect(jsonPath("$.grupo.periodo").value("2026-01"))
                .andExpect(jsonPath("$.calificacion").value(8.5))
                .andExpect(jsonPath("$.fechaInscripcion").value("15/01/2026"));

        verify(inscripcionService).registrar(any(InscripcionRequestDTO.class));
    }

    @Test
    void registrar_debeRetornar404_cuandoElAlumnoOElGrupoNoExiste() throws Exception {
        when(inscripcionService.registrar(any(InscripcionRequestDTO.class)))
                .thenThrow(new NoSuchResourceException("Alumno no encontrado con id: 99"));

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Alumno no encontrado con id: 99"));
    }

    @Test
    void registrar_debeRetornar400_cuandoElIdDeAlumnoEsNulo() throws Exception {
        InscripcionRequestDTO request = new InscripcionRequestDTO(null, 1L);

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(inscripcionService, never()).registrar(any());
    }

    @Test
    void registrar_debeRetornar400_cuandoElIdDeAlumnoEsCero() throws Exception {
        InscripcionRequestDTO request = new InscripcionRequestDTO(0L, 1L);

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(inscripcionService, never()).registrar(any());
    }

    @Test
    void registrar_debeRetornar400_cuandoElIdDeAlumnoEsNegativo() throws Exception {
        InscripcionRequestDTO request = new InscripcionRequestDTO(-1L, 1L);

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(inscripcionService, never()).registrar(any());
    }

    @Test
    void registrar_debeRetornar400_cuandoElIdDeGrupoEsNulo() throws Exception {
        InscripcionRequestDTO request = new InscripcionRequestDTO(1L, null);

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(inscripcionService, never()).registrar(any());
    }

    @Test
    void registrar_debeRetornar400_cuandoElIdDeGrupoEsCero() throws Exception {
        InscripcionRequestDTO request = new InscripcionRequestDTO(1L, 0L);

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(inscripcionService, never()).registrar(any());
    }

    @Test
    void registrar_debeRetornar400_cuandoElIdDeGrupoEsNegativo() throws Exception {
        InscripcionRequestDTO request = new InscripcionRequestDTO(1L, -1L);

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(inscripcionService, never()).registrar(any());
    }

    @Test
    void registrar_debeRetornar400_cuandoNoHayBody() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(inscripcionService, never()).registrar(any());
    }

    // ---------- PUT /api/inscripciones/{id} ----------

    @Test
    void actualizar_debeRetornar200_cuandoDatosSonValidos() throws Exception {
        when(inscripcionService.actualizar(any(InscripcionRequestDTO.class), eq(1L))).thenReturn(responseValida());

        mockMvc.perform(put(URL + "/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.alumno.nombre").value("Carlos González Ramírez"))
                .andExpect(jsonPath("$.alumno.matricula").value("A2026001"))
                .andExpect(jsonPath("$.grupo.curso").value("Matemáticas I"))
                .andExpect(jsonPath("$.grupo.periodo").value("2026-01"))
                .andExpect(jsonPath("$.calificacion").value(8.5))
                .andExpect(jsonPath("$.fechaInscripcion").value("15/01/2026"));
    }

    @Test
    void actualizar_debeRetornar404_cuandoLaInscripcionNoExiste() throws Exception {
        when(inscripcionService.actualizar(any(InscripcionRequestDTO.class), eq(99L)))
                .thenThrow(new NoSuchResourceException("Inscripcion no encontrada con id: 99"));

        mockMvc.perform(put(URL + "/{id}", 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Inscripcion no encontrada con id: 99"));
    }

    @Test
    void actualizar_debeRetornar400_cuandoElIdEsNegativo() throws Exception {
        mockMvc.perform(put(URL + "/{id}", -1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido())))
                .andExpect(status().isBadRequest());

        verify(inscripcionService, never()).actualizar(any(), any());
    }

    @Test
    void actualizar_debeRetornar400_cuandoElBodyEsInvalido() throws Exception {
        InscripcionRequestDTO request = new InscripcionRequestDTO(null, 1L);

        mockMvc.perform(put(URL + "/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(inscripcionService, never()).actualizar(any(), any());
    }

    // ---------- DELETE /api/inscripciones/{id} ----------

    @Test
    void eliminar_debeRetornar204_cuandoLaInscripcionExiste() throws Exception {
        mockMvc.perform(delete(URL + "/{id}", 1L))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(inscripcionService).eliminar(1L);
    }

    @Test
    void eliminar_debeRetornar404_cuandoLaInscripcionNoExiste() throws Exception {
        doThrow(new NoSuchResourceException("Inscripcion no encontrada con id: 99"))
                .when(inscripcionService).eliminar(99L);

        mockMvc.perform(delete(URL + "/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Inscripcion no encontrada con id: 99"));
    }

    @Test
    void eliminar_debeRetornar400_cuandoElIdEsNegativo() throws Exception {
        mockMvc.perform(delete(URL + "/{id}", -1L))
                .andExpect(status().isBadRequest());

        verify(inscripcionService, never()).eliminar(any());
    }
}