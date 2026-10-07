package com.max.escuela.controller;

import com.max.escuela.dto.aula.AulaRequestDTO;
import com.max.escuela.dto.aula.AulaResponseDTO;
import com.max.escuela.exceptions.NoSuchResourceException;
import com.max.escuela.services.aula.AulaService;
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

@WebMvcTest(AulaController.class)
class AulaControllerTest {

    private static final String URL = "/api/aulas";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AulaService aulaService;

    private AulaRequestDTO requestValido() {
        return new AulaRequestDTO("Aula 101", 30);
    }

    private AulaResponseDTO responseValida() {
        return new AulaResponseDTO(1L, "Aula 101", 30);
    }

    // ---------- GET /api/aulas ----------

    @Test
    void listar_debeRetornar200ConLaLista() throws Exception {
        when(aulaService.listar()).thenReturn(List.of(responseValida()));

        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Aula 101"))
                .andExpect(jsonPath("$[0].capacidad").value(30));
    }

    @Test
    void listar_debeRetornar200ConListaVacia_cuandoNoHayAulas() throws Exception {
        when(aulaService.listar()).thenReturn(List.of());

        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ---------- GET /api/aulas/{id} ----------

    @Test
    void obtenerPorId_debeRetornar200_cuandoElAulaExiste() throws Exception {
        when(aulaService.obtenerPorId(1L)).thenReturn(responseValida());

        mockMvc.perform(get(URL + "/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Aula 101"))
                .andExpect(jsonPath("$.capacidad").value(30));
    }

    @Test
    void obtenerPorId_debeRetornar404_cuandoElAulaNoExiste() throws Exception {
        when(aulaService.obtenerPorId(99L))
                .thenThrow(new NoSuchResourceException("Aula no encontrado con id: 99"));

        mockMvc.perform(get(URL + "/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Aula no encontrado con id: 99"));
    }

    @Test
    void obtenerPorId_debeRetornar400_cuandoElIdEsNegativo() throws Exception {
        mockMvc.perform(get(URL + "/{id}", -1L))
                .andExpect(status().isBadRequest());

        verify(aulaService, never()).obtenerPorId(any());
    }

    @Test
    void obtenerPorId_debeRetornar400_cuandoElIdEsCero() throws Exception {
        mockMvc.perform(get(URL + "/{id}", 0L))
                .andExpect(status().isBadRequest());

        verify(aulaService, never()).obtenerPorId(any());
    }

    // ---------- POST /api/aulas ----------

    @Test
    void registrar_debeRetornar201_cuandoDatosSonValidos() throws Exception {
        when(aulaService.registrar(any(AulaRequestDTO.class))).thenReturn(responseValida());

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Aula 101"))
                .andExpect(jsonPath("$.capacidad").value(30));

        verify(aulaService).registrar(any(AulaRequestDTO.class));
    }

    @Test
    void registrar_debeRetornar400_cuandoElNombreEstaVacio() throws Exception {
        AulaRequestDTO request = new AulaRequestDTO("", 30);

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(aulaService, never()).registrar(any());
    }

    @Test
    void registrar_debeRetornar400_cuandoElNombreEsMuyCorto() throws Exception {
        AulaRequestDTO request = new AulaRequestDTO("Ab", 30);

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(aulaService, never()).registrar(any());
    }

    @Test
    void registrar_debeRetornar400_cuandoElNombreEsMuyLargo() throws Exception {
        AulaRequestDTO request = new AulaRequestDTO("A".repeat(101), 30);

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(aulaService, never()).registrar(any());
    }

    @Test
    void registrar_debeRetornar400_cuandoLaCapacidadEsCero() throws Exception {
        AulaRequestDTO request = new AulaRequestDTO("Aula 101", 0);

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(aulaService, never()).registrar(any());
    }

    @Test
    void registrar_debeRetornar400_cuandoLaCapacidadEsNegativa() throws Exception {
        AulaRequestDTO request = new AulaRequestDTO("Aula 101", -5);

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(aulaService, never()).registrar(any());
    }

    @Test
    void registrar_debeRetornar400_cuandoNoHayBody() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(aulaService, never()).registrar(any());
    }

    // ---------- PUT /api/aulas/{id} ----------

    @Test
    void actualizar_debeRetornar200_cuandoDatosSonValidos() throws Exception {
        when(aulaService.actualizar(any(AulaRequestDTO.class), eq(1L))).thenReturn(responseValida());

        mockMvc.perform(put(URL + "/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Aula 101"))
                .andExpect(jsonPath("$.capacidad").value(30));
    }

    @Test
    void actualizar_debeRetornar404_cuandoElAulaNoExiste() throws Exception {
        when(aulaService.actualizar(any(AulaRequestDTO.class), eq(99L)))
                .thenThrow(new NoSuchResourceException("Aula no encontrado con id: 99"));

        mockMvc.perform(put(URL + "/{id}", 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Aula no encontrado con id: 99"));
    }

    @Test
    void actualizar_debeRetornar400_cuandoElIdEsNegativo() throws Exception {
        mockMvc.perform(put(URL + "/{id}", -1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido())))
                .andExpect(status().isBadRequest());

        verify(aulaService, never()).actualizar(any(), any());
    }

    @Test
    void actualizar_debeRetornar400_cuandoElBodyEsInvalido() throws Exception {
        AulaRequestDTO request = new AulaRequestDTO("Ab", 30);

        mockMvc.perform(put(URL + "/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(aulaService, never()).actualizar(any(), any());
    }

    // ---------- DELETE /api/aulas/{id} ----------

    @Test
    void eliminar_debeRetornar204_cuandoElAulaExiste() throws Exception {
        mockMvc.perform(delete(URL + "/{id}", 1L))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(aulaService).eliminar(1L);
    }

    @Test
    void eliminar_debeRetornar404_cuandoElAulaNoExiste() throws Exception {
        doThrow(new NoSuchResourceException("Aula no encontrado con id: 99"))
                .when(aulaService).eliminar(99L);

        mockMvc.perform(delete(URL + "/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Aula no encontrado con id: 99"));
    }

    @Test
    void eliminar_debeRetornar400_cuandoElIdEsNegativo() throws Exception {
        mockMvc.perform(delete(URL + "/{id}", -1L))
                .andExpect(status().isBadRequest());

        verify(aulaService, never()).eliminar(any());
    }
}