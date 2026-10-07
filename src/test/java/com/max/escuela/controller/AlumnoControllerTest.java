package com.max.escuela.controller;

import com.max.escuela.dto.alumno.AlumnoRequestDTO;
import com.max.escuela.dto.alumno.AlumnoResponseDTO;
import com.max.escuela.dto.datos.DatosCalificacionDTO;
import com.max.escuela.exceptions.NoSuchResourceException;
import com.max.escuela.services.alumno.AlumnoService;
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

@WebMvcTest(AlumnoController.class)
class AlumnoControllerTest {

    private static final String URL = "/api/alumnos";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AlumnoService alumnoService;

    private AlumnoRequestDTO requestValido() {
        return new AlumnoRequestDTO("Carlos", "González", "Ramírez");
    }

    private AlumnoResponseDTO responseValida() {
        return new AlumnoResponseDTO(
                1L,
                "Mauricio García Ramírez",
                "maugario.ramirez@alumnos.com",
                "A2026001",
                "10/01/2026",
                List.of(new DatosCalificacionDTO("Matemáticas 6/7", "2026-1", new BigDecimal("8.5"))),
                new BigDecimal("8.5")
        );
    }

    // ---------- GET /api/alumnos ----------

    @Test
    void listar_debeRetornar200ConLaLista() throws Exception {
        when(alumnoService.listar()).thenReturn(List.of(responseValida()));

        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Mauricio García Ramírez"))
                .andExpect(jsonPath("$[0].email").value("maugario.ramirez@alumnos.com"))
                .andExpect(jsonPath("$[0].matricula").value("A2026001"))
                .andExpect(jsonPath("$[0].fechaIngreso").value("10/01/2026"))
                .andExpect(jsonPath("$[0].calificaciones.length()").value(1))
                .andExpect(jsonPath("$[0].calificaciones[0].curso").value("Matemáticas 6/7"))
                .andExpect(jsonPath("$[0].calificaciones[0].calificacion").value(8.5))
                .andExpect(jsonPath("$[0].promedio").value(8.5));
    }

    @Test
    void listar_debeRetornar200ConListaVacia_cuandoNoHayAlumnos() throws Exception {
        when(alumnoService.listar()).thenReturn(List.of());

        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ---------- GET /api/alumnos/{id} ----------

    @Test
    void obtenerPorId_debeRetornar200_cuandoElAlumnoExiste() throws Exception {
        when(alumnoService.obtenerPorId(1L)).thenReturn(responseValida());

        mockMvc.perform(get(URL + "/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Mauricio García Ramírez"))
                .andExpect(jsonPath("$.email").value("maugario.ramirez@alumnos.com"))
                .andExpect(jsonPath("$.matricula").value("A2026001"))
                .andExpect(jsonPath("$.fechaIngreso").value("10/01/2026"))
                .andExpect(jsonPath("$.calificaciones.length()").value(1))
                .andExpect(jsonPath("$.calificaciones[0].curso").value("Matemáticas 6/7"))
                .andExpect(jsonPath("$.calificaciones[0].calificacion").value(8.5))
                .andExpect(jsonPath("$.promedio").value(8.5));
    }

    @Test
    void obtenerPorId_debeRetornar404_cuandoElAlumnoNoExiste() throws Exception {
        when(alumnoService.obtenerPorId(99L))
                .thenThrow(new NoSuchResourceException("Alumno no encontrado con id: 99"));

        mockMvc.perform(get(URL + "/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Alumno no encontrado con id: 99"));
    }

    @Test
    void obtenerPorId_debeRetornar400_cuandoElIdEsNegativo() throws Exception {
        mockMvc.perform(get(URL + "/{id}", -1L))
                .andExpect(status().isBadRequest());

        verify(alumnoService, never()).obtenerPorId(any());
    }

    @Test
    void obtenerPorId_debeRetornar400_cuandoElIdEsCero() throws Exception {
        mockMvc.perform(get(URL + "/{id}", 0L))
                .andExpect(status().isBadRequest());

        verify(alumnoService, never()).obtenerPorId(any());
    }

    // ---------- POST /api/alumnos ----------

    @Test
    void registrar_debeRetornar201_cuandoDatosSonValidos() throws Exception {
        when(alumnoService.registrar(any(AlumnoRequestDTO.class))).thenReturn(responseValida());

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Mauricio García Ramírez"))
                .andExpect(jsonPath("$.email").value("maugario.ramirez@alumnos.com"))
                .andExpect(jsonPath("$.matricula").value("A2026001"))
                .andExpect(jsonPath("$.fechaIngreso").value("10/01/2026"))
                .andExpect(jsonPath("$.calificaciones.length()").value(1))
                .andExpect(jsonPath("$.calificaciones[0].curso").value("Matemáticas 6/7"))
                .andExpect(jsonPath("$.calificaciones[0].calificacion").value(8.5))
                .andExpect(jsonPath("$.promedio").value(8.5));

        verify(alumnoService).registrar(any(AlumnoRequestDTO.class));
    }

    @Test
    void registrar_debeRetornar400_cuandoElNombreEstaVacio() throws Exception {
        AlumnoRequestDTO request = new AlumnoRequestDTO("", "González", "Ramírez");

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(alumnoService, never()).registrar(any());
    }

    @Test
    void registrar_debeRetornar400_cuandoElNombreEsMuyCorto() throws Exception {
        AlumnoRequestDTO request = new AlumnoRequestDTO("Ana", "González", "Ramírez");

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(alumnoService, never()).registrar(any());
    }

    @Test
    void registrar_debeRetornar400_cuandoElNombreEsMuyLargo() throws Exception {
        AlumnoRequestDTO request = new AlumnoRequestDTO("A".repeat(51), "González", "Ramírez");

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(alumnoService, never()).registrar(any());
    }

    @Test
    void registrar_debeRetornar400_cuandoElApellidoPaternoEstaVacio() throws Exception {
        AlumnoRequestDTO request = new AlumnoRequestDTO("Mauricio", "", "Ramírez");

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(alumnoService, never()).registrar(any());
    }

    @Test
    void registrar_debeRetornar400_cuandoElApellidoPaternoEsMuyCorto() throws Exception {
        AlumnoRequestDTO request = new AlumnoRequestDTO("Mauricio", "Ga", "Ramírez");

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(alumnoService, never()).registrar(any());
    }

    @Test
    void registrar_debeRetornar400_cuandoElApellidoMaternoEstaVacio() throws Exception {
        AlumnoRequestDTO request = new AlumnoRequestDTO("Mauricio", "González", "");

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(alumnoService, never()).registrar(any());
    }

    @Test
    void registrar_debeRetornar400_cuandoElApellidoMaternoEsMuyCorto() throws Exception {
        AlumnoRequestDTO request = new AlumnoRequestDTO("Mauricio", "García", "Gil");

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(alumnoService, never()).registrar(any());
    }

    @Test
    void registrar_debeRetornar400_cuandoElNombreEsNulo() throws Exception {
        AlumnoRequestDTO request = new AlumnoRequestDTO(null, "García", "Ramírez");

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(alumnoService, never()).registrar(any());
    }

    @Test
    void registrar_debeRetornar400_cuandoNoHayBody() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(alumnoService, never()).registrar(any());
    }

    // ---------- PUT /api/alumnos/{id} ----------

    @Test
    void actualizar_debeRetornar200_cuandoDatosSonValidos() throws Exception {
        when(alumnoService.actualizar(any(AlumnoRequestDTO.class), eq(1L))).thenReturn(responseValida());

        mockMvc.perform(put(URL + "/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Mauricio García Ramírez"))
                .andExpect(jsonPath("$.email").value("maugario.ramirez@alumnos.com"))
                .andExpect(jsonPath("$.matricula").value("A2026001"))
                .andExpect(jsonPath("$.fechaIngreso").value("10/01/2026"))
                .andExpect(jsonPath("$.calificaciones.length()").value(1))
                .andExpect(jsonPath("$.calificaciones[0].curso").value("Matemáticas 6/7"))
                .andExpect(jsonPath("$.calificaciones[0].calificacion").value(8.5))
                .andExpect(jsonPath("$.promedio").value(8.5));
    }

    @Test
    void actualizar_debeRetornar404_cuandoElAlumnoNoExiste() throws Exception {
        when(alumnoService.actualizar(any(AlumnoRequestDTO.class), eq(99L)))
                .thenThrow(new NoSuchResourceException("Alumno no encontrado con id: 99"));

        mockMvc.perform(put(URL + "/{id}", 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Alumno no encontrado con id: 99"));
    }

    @Test
    void actualizar_debeRetornar400_cuandoElIdEsNegativo() throws Exception {
        mockMvc.perform(put(URL + "/{id}", -1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido())))
                .andExpect(status().isBadRequest());

        verify(alumnoService, never()).actualizar(any(), any());
    }

    @Test
    void actualizar_debeRetornar400_cuandoElBodyEsInvalido() throws Exception {
        AlumnoRequestDTO request = new AlumnoRequestDTO("", "García", "Ramírez");

        mockMvc.perform(put(URL + "/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(alumnoService, never()).actualizar(any(), any());
    }

    // ---------- DELETE /api/alumnos/{id} ----------

    @Test
    void eliminar_debeRetornar204_cuandoElAlumnoExiste() throws Exception {
        mockMvc.perform(delete(URL + "/{id}", 1L))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(alumnoService).eliminar(1L);
    }

    @Test
    void eliminar_debeRetornar404_cuandoElAlumnoNoExiste() throws Exception {
        doThrow(new NoSuchResourceException("Alumno no encontrado con id: 99"))
                .when(alumnoService).eliminar(99L);

        mockMvc.perform(delete(URL + "/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Alumno no encontrado con id: 99"));
    }

    @Test
    void eliminar_debeRetornar400_cuandoElIdEsNegativo() throws Exception {
        mockMvc.perform(delete(URL + "/{id}", -1L))
                .andExpect(status().isBadRequest());

        verify(alumnoService, never()).eliminar(any());
    }
}