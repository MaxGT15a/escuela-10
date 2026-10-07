package com.max.escuela.utils;

import com.max.escuela.exceptions.InvalidDataException;

import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class TimeCustomUtils {

    public static void validarFormatoHora(String hora, String mensaje) {
        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("HH:mm");

        try {
            LocalTime.parse(hora, formatter);
        } catch (DateTimeParseException e) {
            throw new InvalidDataException(mensaje);
        }
    }

    public static void validarFormatoPeriodo(String periodo, String mensaje) {
        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("uuuu-MM");

        try {
            YearMonth.parse(periodo, formatter);
        } catch (DateTimeParseException e) {
            throw new InvalidDataException(mensaje);
        }
    }

    public static void validarHoraInicioFin(String horaInicio, String horaFin, String mensaje) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        LocalTime inicio = LocalTime.parse(horaInicio, formatter);
        LocalTime fin = LocalTime.parse(horaFin, formatter);

        if (!inicio.isBefore(fin)) {
            throw new InvalidDataException(mensaje);
        }
    }
}
