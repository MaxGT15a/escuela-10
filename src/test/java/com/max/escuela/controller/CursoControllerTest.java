package com.max.escuela.controller;

import com.max.escuela.dto.curso.CursoRequestDTO;
import com.max.escuela.dto.curso.CursoResponseDTO;
import com.max.escuela.exceptions.NoSuchResourceException;
import com.max.escuela.services.curso.CursoService;
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

@WebMvcTest(CursoController.class)
class CursoControllerTest {

    private static final String URL = "/api/cursos";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CursoService cursoService;

    private CursoRequestDTO requestValido() {
        return new CursoRequestDTO("Matemáticas I", "Fundamentos matemáticos para nivel básico", 6);
    }

    private CursoResponseDTO responseValida() {
        return new CursoResponseDTO(1L, "Matemáticas I", "Fundamentos matemáticos para nivel básico", 6);
    }

    // ---------- GET /api/cursos ----------

    @Test
    void listar_debeRetornar200ConLaLista() throws Exception {
        when(cursoService.listar()).thenReturn(List.of(responseValida()));

        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Matemáticas I"))
                .andExpect(jsonPath("$[0].creditos").value(6));
    }

    @Test
    void listar_debeRetornar200ConListaVacia_cuandoNoHayCursos() throws Exception {
        when(cursoService.listar()).thenReturn(List.of());

        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ---------- GET /api/cursos/{id} ----------

    @Test
    void obtenerPorId_debeRetornar200_cuandoElCursoExiste() throws Exception {
        when(cursoService.obtenerPorId(1L)).thenReturn(responseValida());

        mockMvc.perform(get(URL + "/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Matemáticas I"))
                .andExpect(jsonPath("$.descripcion").value("Fundamentos matemáticos para nivel básico"))
                .andExpect(jsonPath("$.creditos").value(6));
    }

    @Test
    void obtenerPorId_debeRetornar404_cuandoElCursoNoExiste() throws Exception {
        when(cursoService.obtenerPorId(99L))
                .thenThrow(new NoSuchResourceException("Curso no encontrado con id: 99"));

        mockMvc.perform(get(URL + "/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Curso no encontrado con id: 99"));
    }

    @Test
    void obtenerPorId_debeRetornar400_cuandoElIdEsNegativo() throws Exception {
        mockMvc.perform(get(URL + "/{id}", -1L))
                .andExpect(status().isBadRequest());

        verify(cursoService, never()).obtenerPorId(any());
    }

    // ---------- POST /api/cursos ----------

    @Test
    void registrar_debeRetornar201_cuandoDatosSonValidos() throws Exception {
        when(cursoService.registrar(any(CursoRequestDTO.class))).thenReturn(responseValida());

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Matemáticas I"))
                .andExpect(jsonPath("$.creditos").value(6));
    }

    @Test
    void registrar_debeRetornar400_cuandoElNombreEstaVacio() throws Exception {
        CursoRequestDTO request = new CursoRequestDTO("", "Descripción válida", 6);

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(cursoService, never()).registrar(any());
    }

    @Test
    void registrar_debeRetornar400_cuandoLosCreditosSonNegativos() throws Exception {
        CursoRequestDTO request = new CursoRequestDTO("Matemáticas I", "Descripción válida", -1);

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(cursoService, never()).registrar(any());
    }

    @Test
    void registrar_debeRetornar400_cuandoNoHayBody() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(cursoService, never()).registrar(any());
    }

    // ---------- PUT /api/cursos/{id} ----------

    @Test
    void actualizar_debeRetornar200_cuandoDatosSonValidos() throws Exception {
        CursoResponseDTO actualizado = new CursoResponseDTO(1L, "Matemáticas I", "Fundamentos matemáticos para nivel básico", 6);
        when(cursoService.actualizar(any(CursoRequestDTO.class), eq(1L))).thenReturn(actualizado);

        mockMvc.perform(put(URL + "/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Matemáticas I"));
    }

    @Test
    void actualizar_debeRetornar404_cuandoElCursoNoExiste() throws Exception {
        when(cursoService.actualizar(any(CursoRequestDTO.class), eq(99L)))
                .thenThrow(new NoSuchResourceException("Curso no encontrado con id: 99"));

        mockMvc.perform(put(URL + "/{id}", 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Curso no encontrado con id: 99"));
    }

    @Test
    void actualizar_debeRetornar400_cuandoElIdEsNegativo() throws Exception {
        mockMvc.perform(put(URL + "/{id}", -1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido())))
                .andExpect(status().isBadRequest());

        verify(cursoService, never()).actualizar(any(), any());
    }

    @Test
    void actualizar_debeRetornar400_cuandoElBodyEsInvalido() throws Exception {
        CursoRequestDTO request = new CursoRequestDTO("", "Descripción válida", 6);

        mockMvc.perform(put(URL + "/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(cursoService, never()).actualizar(any(), any());
    }

    // ---------- DELETE /api/cursos/{id} ----------

    @Test
    void eliminar_debeRetornar204_cuandoElCursoExiste() throws Exception {
        mockMvc.perform(delete(URL + "/{id}", 1L))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(cursoService).eliminar(1L);
    }

    @Test
    void eliminar_debeRetornar404_cuandoElCursoNoExiste() throws Exception {
        doThrow(new NoSuchResourceException("Curso no encontrado con id: 99"))
                .when(cursoService).eliminar(99L);

        mockMvc.perform(delete(URL + "/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Curso no encontrado con id: 99"));
    }

    @Test
    void eliminar_debeRetornar400_cuandoElIdEsNegativo() throws Exception {
        mockMvc.perform(delete(URL + "/{id}", -1L))
                .andExpect(status().isBadRequest());

        verify(cursoService, never()).eliminar(any());
    }
}