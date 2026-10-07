package com.max.escuela.controller;

import com.max.escuela.dto.calificacion.CalificacionRequestDTO;
import com.max.escuela.dto.calificacion.CalificacionResponseDTO;
import com.max.escuela.dto.datos.DatosAlumnoDTO;
import com.max.escuela.dto.datos.DatosGrupoDTO;
import com.max.escuela.dto.datos.DatosInscripcionDTO;
import com.max.escuela.exceptions.NoSuchResourceException;
import com.max.escuela.services.calificacion.CalificacionService;
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

@WebMvcTest(CalificacionController.class)
class CalificacionControllerTest {

    private static final String URL = "/api/calificaciones";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CalificacionService calificacionService;

    private CalificacionRequestDTO requestValido() {
        return new CalificacionRequestDTO(1L, new BigDecimal("9.0"));
    }

    private CalificacionResponseDTO responseValida() {
        return new CalificacionResponseDTO(
                1L,
                new DatosInscripcionDTO(
                        new DatosAlumnoDTO("Carlos González Ramírez", "A2026001", "carlos.gonzalez@alumnos.com", "10/01/2026"),
                        new DatosGrupoDTO("Matemáticas I", "Laura Martínez Martínez", "Aula 101", "2026-01"),
                        "15/01/2026"
                ),
                new BigDecimal("9.0"),
                "11/02/2026"
        );
    }

    // ---------- GET /api/calificaciones ----------

    @Test
    void listar_debeRetornar200ConLaLista() throws Exception {
        when(calificacionService.listar()).thenReturn(List.of(responseValida()));

        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].inscripcion.alumno.nombre").value("Carlos González Ramírez"))
                .andExpect(jsonPath("$[0].inscripcion.alumno.matricula").value("A2026001"))
                .andExpect(jsonPath("$[0].inscripcion.grupo.curso").value("Matemáticas I"))
                .andExpect(jsonPath("$[0].inscripcion.grupo.periodo").value("2026-01"))
                .andExpect(jsonPath("$[0].inscripcion.fechaInscripcion").value("15/01/2026"))
                .andExpect(jsonPath("$[0].calificacion").value(9.0))
                .andExpect(jsonPath("$[0].fechaRegistro").value("11/02/2026"));
    }

    @Test
    void listar_debeRetornar200ConListaVacia_cuandoNoHayCalificaciones() throws Exception {
        when(calificacionService.listar()).thenReturn(List.of());

        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ---------- GET /api/calificaciones/{id} ----------

    @Test
    void obtenerPorId_debeRetornar200_cuandoLaCalificacionExiste() throws Exception {
        when(calificacionService.obtenerPorId(1L)).thenReturn(responseValida());

        mockMvc.perform(get(URL + "/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.inscripcion.alumno.nombre").value("Carlos González Ramírez"))
                .andExpect(jsonPath("$.inscripcion.alumno.matricula").value("A2026001"))
                .andExpect(jsonPath("$.inscripcion.grupo.curso").value("Matemáticas I"))
                .andExpect(jsonPath("$.inscripcion.grupo.periodo").value("2026-01"))
                .andExpect(jsonPath("$.inscripcion.fechaInscripcion").value("15/01/2026"))
                .andExpect(jsonPath("$.calificacion").value(9.0))
                .andExpect(jsonPath("$.fechaRegistro").value("11/02/2026"));
    }

    @Test
    void obtenerPorId_debeRetornar404_cuandoLaCalificacionNoExiste() throws Exception {
        when(calificacionService.obtenerPorId(99L))
                .thenThrow(new NoSuchResourceException("Calificacion no encontrada con id: 99"));

        mockMvc.perform(get(URL + "/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Calificacion no encontrada con id: 99"));
    }

    @Test
    void obtenerPorId_debeRetornar400_cuandoElIdEsNegativo() throws Exception {
        mockMvc.perform(get(URL + "/{id}", -1L))
                .andExpect(status().isBadRequest());

        verify(calificacionService, never()).obtenerPorId(any());
    }

    @Test
    void obtenerPorId_debeRetornar400_cuandoElIdEsCero() throws Exception {
        mockMvc.perform(get(URL + "/{id}", 0L))
                .andExpect(status().isBadRequest());

        verify(calificacionService, never()).obtenerPorId(any());
    }

    // ---------- POST /api/calificaciones ----------

    @Test
    void registrar_debeRetornar201_cuandoDatosSonValidos() throws Exception {
        when(calificacionService.registrar(any(CalificacionRequestDTO.class))).thenReturn(responseValida());

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.inscripcion.alumno.nombre").value("Carlos González Ramírez"))
                .andExpect(jsonPath("$.inscripcion.alumno.matricula").value("A2026001"))
                .andExpect(jsonPath("$.inscripcion.grupo.curso").value("Matemáticas I"))
                .andExpect(jsonPath("$.inscripcion.grupo.periodo").value("2026-01"))
                .andExpect(jsonPath("$.inscripcion.fechaInscripcion").value("15/01/2026"))
                .andExpect(jsonPath("$.calificacion").value(9.0))
                .andExpect(jsonPath("$.fechaRegistro").value("11/02/2026"));

        verify(calificacionService).registrar(any(CalificacionRequestDTO.class));
    }

    @Test
    void registrar_debeRetornar404_cuandoLaInscripcionNoExiste() throws Exception {
        when(calificacionService.registrar(any(CalificacionRequestDTO.class)))
                .thenThrow(new NoSuchResourceException("Inscripcion no encontrada con id: 99"));

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Inscripcion no encontrada con id: 99"));
    }

    @Test
    void registrar_debeRetornar400_cuandoElIdDeInscripcionEsNulo() throws Exception {
        CalificacionRequestDTO request = new CalificacionRequestDTO(null, new BigDecimal("9.0"));

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(calificacionService, never()).registrar(any());
    }

    @Test
    void registrar_debeRetornar400_cuandoElIdDeInscripcionEsCero() throws Exception {
        CalificacionRequestDTO request = new CalificacionRequestDTO(0L, new BigDecimal("9.0"));

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(calificacionService, never()).registrar(any());
    }

    @Test
    void registrar_debeRetornar400_cuandoElIdDeInscripcionEsNegativo() throws Exception {
        CalificacionRequestDTO request = new CalificacionRequestDTO(-1L, new BigDecimal("9.0"));

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(calificacionService, never()).registrar(any());
    }

    @Test
    void registrar_debeRetornar400_cuandoLaCalificacionEsNula() throws Exception {
        CalificacionRequestDTO request = new CalificacionRequestDTO(1L, null);

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(calificacionService, never()).registrar(any());
    }

    @Test
    void registrar_debeRetornar400_cuandoLaCalificacionEsCero() throws Exception {
        CalificacionRequestDTO request = new CalificacionRequestDTO(1L, BigDecimal.ZERO);

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(calificacionService, never()).registrar(any());
    }

    @Test
    void registrar_debeRetornar400_cuandoLaCalificacionEsNegativa() throws Exception {
        CalificacionRequestDTO request = new CalificacionRequestDTO(1L, new BigDecimal("-5"));

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(calificacionService, never()).registrar(any());
    }

    @Test
    void registrar_debeRetornar400_cuandoNoHayBody() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(calificacionService, never()).registrar(any());
    }

    // ---------- PUT /api/calificaciones/{id} ----------

    @Test
    void actualizar_debeRetornar200_cuandoDatosSonValidos() throws Exception {
        when(calificacionService.actualizar(any(CalificacionRequestDTO.class), eq(1L))).thenReturn(responseValida());

        mockMvc.perform(put(URL + "/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.inscripcion.alumno.nombre").value("Carlos González Ramírez"))
                .andExpect(jsonPath("$.inscripcion.alumno.matricula").value("A2026001"))
                .andExpect(jsonPath("$.inscripcion.grupo.curso").value("Matemáticas I"))
                .andExpect(jsonPath("$.inscripcion.grupo.periodo").value("2026-01"))
                .andExpect(jsonPath("$.inscripcion.fechaInscripcion").value("15/01/2026"))
                .andExpect(jsonPath("$.calificacion").value(9.0))
                .andExpect(jsonPath("$.fechaRegistro").value("11/02/2026"));
    }

    @Test
    void actualizar_debeRetornar404_cuandoLaCalificacionNoExiste() throws Exception {
        when(calificacionService.actualizar(any(CalificacionRequestDTO.class), eq(99L)))
                .thenThrow(new NoSuchResourceException("Calificacion no encontrada con id: 99"));

        mockMvc.perform(put(URL + "/{id}", 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Calificacion no encontrada con id: 99"));
    }

    @Test
    void actualizar_debeRetornar400_cuandoElIdEsNegativo() throws Exception {
        mockMvc.perform(put(URL + "/{id}", -1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido())))
                .andExpect(status().isBadRequest());

        verify(calificacionService, never()).actualizar(any(), any());
    }

    @Test
    void actualizar_debeRetornar400_cuandoElBodyEsInvalido() throws Exception {
        CalificacionRequestDTO request = new CalificacionRequestDTO(1L, null);

        mockMvc.perform(put(URL + "/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(calificacionService, never()).actualizar(any(), any());
    }

    // ---------- DELETE /api/calificaciones/{id} ----------

    @Test
    void eliminar_debeRetornar204_cuandoLaCalificacionExiste() throws Exception {
        mockMvc.perform(delete(URL + "/{id}", 1L))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(calificacionService).eliminar(1L);
    }

    @Test
    void eliminar_debeRetornar404_cuandoLaCalificacionNoExiste() throws Exception {
        doThrow(new NoSuchResourceException("Calificacion no encontrada con id: 99"))
                .when(calificacionService).eliminar(99L);

        mockMvc.perform(delete(URL + "/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Calificacion no encontrada con id: 99"));
    }

    @Test
    void eliminar_debeRetornar400_cuandoElIdEsNegativo() throws Exception {
        mockMvc.perform(delete(URL + "/{id}", -1L))
                .andExpect(status().isBadRequest());

        verify(calificacionService, never()).eliminar(any());
    }
}