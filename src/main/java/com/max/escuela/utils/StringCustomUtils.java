package com.max.escuela.utils;

import com.max.escuela.exceptions.InvalidDataException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


public class StringCustomUtils {
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static void validarTamanio(String texto, Integer min, Integer max, String mensaje){
        validarNoVacioNoNull(texto, mensaje);

        if ( texto.trim().length() < min || texto.trim().length() > max)
            throw new InvalidDataException(mensaje);
    }
    public static void validarNoVacioNoNull(String texto, String mensaje){
        if (texto == null || texto.isBlank())
            throw new InvalidDataException(mensaje);
    }
    public static String normalizarTexto(String texto){
        return texto.toLowerCase()

                .replace("á", "a")
                .replace("à", "a")
                .replace("ä", "a")
                .replace("â", "a")
                .replace("ã", "a")
                .replace("å", "a")

                .replace("é", "e")
                .replace("è", "e")
                .replace("ë", "e")
                .replace("ê", "e")

                .replace("í", "i")
                .replace("ì", "i")
                .replace("ï", "i")
                .replace("î", "i")

                .replace("ó", "o")
                .replace("ò", "o")
                .replace("ö", "o")
                .replace("ô", "o")
                .replace("õ", "o")
                .replace("ø", "o")

                .replace("ú", "u")
                .replace("ù", "u")
                .replace("ü", "u")
                .replace("û", "u")

                .replace("ñ", "n")
                .replace("ç", "c")
                .replace("ý", "y")
                .replace("ÿ", "y")

                .trim();
    }

    public static String localDateAString(LocalDate fecha) {
        return fecha == null ? null : fecha.format(FORMATO_FECHA);
    }
}
