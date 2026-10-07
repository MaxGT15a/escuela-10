package com.max.escuela.controller;

import com.max.escuela.dto.datos.DatosCursoDTO;
import com.max.escuela.dto.maestro.MaestroRequestDTO;
import com.max.escuela.dto.maestro.MaestroResponseDTO;
import com.max.escuela.exceptions.NoSuchResourceException;
import com.max.escuela.services.maestro.MaestroService;
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

@WebMvcTest(MaestroController.class)
class MaestroControllerTest {

    private static final String URL = "/api/maestros";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MaestroService maestroService;

    private MaestroRequestDTO requestValido() {
        return new MaestroRequestDTO("Laura", "Hernández", "Sánchez", "ana.her@correo.com", "5512345678");
    }

    private MaestroResponseDTO responseValida() {
        return new MaestroResponseDTO(
                1L,
                "Laura Martínez Martínez",
                "laura.martinez@escuela.com",
                "5551010789",
                List.of(new DatosCursoDTO("Matemáticas 6/7", "Fundamentos matemáticos para nivel básico", 6))
        );
    }

    // ---------- GET /api/maestros ----------

    @Test
    void listar_debeRetornar200ConLaLista() throws Exception {
        when(maestroService.listar()).thenReturn(List.of(responseValida()));

        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Laura Martínez Martínez"))
                .andExpect(jsonPath("$[0].email").value("laura.martinez@escuela.com"))
                .andExpect(jsonPath("$[0].telefono").value("5551010789"))
                .andExpect(jsonPath("$[0].cursos[0].nombre").value("Matemáticas 6/7"));
    }

    @Test
    void listar_debeRetornar200ConListaVacia_cuandoNoHayMaestros() throws Exception {
        when(maestroService.listar()).thenReturn(List.of());

        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ---------- GET /api/maestros/{id} ----------

    @Test
    void obtenerPorId_debeRetornar200_cuandoElMaestroExiste() throws Exception {
        when(maestroService.obtenerPorId(1L)).thenReturn(responseValida());

        mockMvc.perform(get(URL + "/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Laura Martínez Martínez"))
                .andExpect(jsonPath("$.cursos.length()").value(1));
    }

    @Test
    void obtenerPorId_debeRetornar404_cuandoElMaestroNoExiste() throws Exception {
        when(maestroService.obtenerPorId(99L))
                .thenThrow(new NoSuchResourceException("Maestro no encontrado con id: 99"));

        mockMvc.perform(get(URL + "/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Maestro no encontrado con id: 99"));
    }

    @Test
    void obtenerPorId_debeRetornar400_cuandoElIdEsNegativo() throws Exception {
        mockMvc.perform(get(URL + "/{id}", -1L))
                .andExpect(status().isBadRequest());

        verify(maestroService, never()).obtenerPorId(any());
    }

    // ---------- POST /api/maestros ----------

    @Test
    void registrar_debeRetornar201_cuandoDatosSonValidos() throws Exception {
        when(maestroService.registrar(any(MaestroRequestDTO.class))).thenReturn(responseValida());

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("laura.martinez@escuela.com"))
                .andExpect(jsonPath("$.telefono").value("5551010789"));
    }

    @Test
    void registrar_debeRetornar400_cuandoElNombreEstaVacio() throws Exception {
        MaestroRequestDTO request = new MaestroRequestDTO("", "Hernández", "Sánchez", "ana.her@correo.com", "5512345678");

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(maestroService, never()).registrar(any());
    }

    @Test
    void registrar_debeRetornar400_cuandoElNombreEsMuyCorto() throws Exception {
        // "Ana" viola @Size(min = 5) del MaestroRequest
        MaestroRequestDTO request = new MaestroRequestDTO("Ana", "Hernández", "Sánchez", "ana.her@correo.com", "5512345678");

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(maestroService, never()).registrar(any());
    }

    @Test
    void registrar_debeRetornar400_cuandoElEmailEsInvalido() throws Exception {
        MaestroRequestDTO request = new MaestroRequestDTO("Laura", "Hernández", "Sánchez", "esto-no-es-un-email", "5512345678");

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(maestroService, never()).registrar(any());
    }

    @Test
    void registrar_debeRetornar400_cuandoElTelefonoEsInvalido() throws Exception {
        MaestroRequestDTO request = new MaestroRequestDTO("Laura", "Hernández", "Sánchez", "ana.her@correo.com", "123");

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(maestroService, never()).registrar(any());
    }

    @Test
    void registrar_debeRetornar400_cuandoNoHayBody() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(maestroService, never()).registrar(any());
    }

    // ---------- PUT /api/maestros/{id} ----------

    @Test
    void actualizar_debeRetornar200_cuandoDatosSonValidos() throws Exception {
        when(maestroService.actualizar(any(MaestroRequestDTO.class), eq(1L))).thenReturn(responseValida());

        mockMvc.perform(put(URL + "/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Laura Martínez Martínez"));
    }

    @Test
    void actualizar_debeRetornar404_cuandoElMaestroNoExiste() throws Exception {
        when(maestroService.actualizar(any(MaestroRequestDTO.class), eq(99L)))
                .thenThrow(new NoSuchResourceException("Maestro no encontrado con id: 99"));

        mockMvc.perform(put(URL + "/{id}", 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Maestro no encontrado con id: 99"));
    }

    @Test
    void actualizar_debeRetornar400_cuandoElIdEsNegativo() throws Exception {
        mockMvc.perform(put(URL + "/{id}", -1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido())))
                .andExpect(status().isBadRequest());

        verify(maestroService, never()).actualizar(any(), any());
    }

    @Test
    void actualizar_debeRetornar400_cuandoElBodyEsInvalido() throws Exception {
        MaestroRequestDTO request = new MaestroRequestDTO("Laura", "Hernández", "Sánchez", "no-valido", "5512345678");

        mockMvc.perform(put(URL + "/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(maestroService, never()).actualizar(any(), any());
    }

    // ---------- DELETE /api/maestros/{id} ----------

    @Test
    void eliminar_debeRetornar204_cuandoElMaestroExiste() throws Exception {
        mockMvc.perform(delete(URL + "/{id}", 1L))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(maestroService).eliminar(1L);
    }

    @Test
    void eliminar_debeRetornar404_cuandoElMaestroNoExiste() throws Exception {
        doThrow(new NoSuchResourceException("Maestro no encontrado con id: 99"))
                .when(maestroService).eliminar(99L);

        mockMvc.perform(delete(URL + "/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Maestro no encontrado con id: 99"));
    }

    @Test
    void eliminar_debeRetornar400_cuandoElIdEsNegativo() throws Exception {
        mockMvc.perform(delete(URL + "/{id}", -1L))
                .andExpect(status().isBadRequest());

        verify(maestroService, never()).eliminar(any());
    }

    // ---------- GET /api/maestros/cursos/{id} ----------

    @Test
    void obtenerCursosDeUnMaestroConId_debeRetornar200ConLosCursos() throws Exception {
        when(maestroService.obtenerCursosDeUnMaestroConId(1L)).thenReturn(List.of(
                new DatosCursoDTO("Matemáticas I", "Fundamentos matemáticos para nivel básico", 6),
                new DatosCursoDTO("Bases de Datos", "Introducción a SQL", 5)
        ));

        mockMvc.perform(get(URL + "/cursos/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nombre").value("Matemáticas I"))
                .andExpect(jsonPath("$[0].creditos").value(6))
                .andExpect(jsonPath("$[1].nombre").value("Bases de Datos"));
    }

    @Test
    void obtenerCursosDeUnMaestroConId_debeRetornar200ConListaVacia_cuandoNoImparteCursos() throws Exception {
        when(maestroService.obtenerCursosDeUnMaestroConId(1L)).thenReturn(List.of());

        mockMvc.perform(get(URL + "/cursos/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void obtenerCursosDeUnMaestroConId_debeRetornar404_cuandoElMaestroNoExiste() throws Exception {
        when(maestroService.obtenerCursosDeUnMaestroConId(99L))
                .thenThrow(new NoSuchResourceException("Maestro no encontrado con id: 99"));

        mockMvc.perform(get(URL + "/cursos/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Maestro no encontrado con id: 99"));
    }

    @Test
    void obtenerCursosDeUnMaestroConId_debeRetornar400_cuandoElIdEsNegativo() throws Exception {
        mockMvc.perform(get(URL + "/cursos/{id}", -1L))
                .andExpect(status().isBadRequest());

        verify(maestroService, never()).obtenerCursosDeUnMaestroConId(any());
    }
}