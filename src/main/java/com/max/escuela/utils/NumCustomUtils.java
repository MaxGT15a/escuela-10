package com.max.escuela.utils;

import com.max.escuela.exceptions.InvalidDataException;

import java.math.BigDecimal;

public class NumCustomUtils {
    private static final String NUMEROS = "0123456789";
    public static <N extends Number> void validarNumeroRequerido(N numero, String mensaje){
        if (numero == null)
            throw new InvalidDataException(mensaje);
    }

    public static void validarEnteroPositvo(Integer numero, String mensaje){
        validarNumeroRequerido(numero, mensaje);
        if (numero <= 0)
            throw new InvalidDataException(mensaje);
    }

    public static void validarBigDecimalPositvo(BigDecimal numero, String mensaje){
        validarNumeroRequerido(numero, mensaje);
        if (numero.compareTo(BigDecimal.ZERO) <= 0)
            throw new InvalidDataException(mensaje);
    }

    public static void validarFloatPositvo(Float numero, String mensaje){
        validarNumeroRequerido(numero, mensaje);
        if (numero <= 0)
            throw new InvalidDataException(mensaje);
    }

    public static void validarCharSeaNumero(char c, String mensaje) {
        if(!NUMEROS.contains(String.valueOf(c)))
            throw new InvalidDataException(mensaje);
    }

    public static void validarStringSoloNumeros(String texto, String mensaje){
        String trimTexto = texto.trim();
        for(int i = 0; i < trimTexto.length(); i++){
            validarCharSeaNumero(trimTexto.charAt(i), mensaje);
        }
    }
}
