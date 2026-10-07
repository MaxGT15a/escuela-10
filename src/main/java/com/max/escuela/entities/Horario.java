package com.max.escuela.entities;

import com.max.escuela.enums.DiaSemana;
import com.max.escuela.exceptions.InvalidDataException;
import com.max.escuela.utils.TimeCustomUtils;
import com.max.escuela.utils.StringCustomUtils;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "HORARIOS")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class Horario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_HORARIO")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_GRUPO", nullable = false)
    private Grupo grupo;

    @Enumerated(EnumType.STRING)
    @Column(name = "DIA", length = 15, nullable = false)
    private DiaSemana diaSemana;

    @Column(name = "HORA_INICIO", length = 5, nullable = false)
    private String horaInicio;

    @Column(name = "HORA_FIN", length = 5, nullable = false)
    private String horaFin;

    private static void validarDatos(
        String diaSemana,
        String horaInicio,
        String horaFin
    ){
        StringCustomUtils.validarNoVacioNoNull(diaSemana, "El día de la semana es requerido");
        StringCustomUtils.validarTamanio(diaSemana, 1, 15, "El día de la semana debe tener entre 1 y 15 caracteres");

        StringCustomUtils.validarNoVacioNoNull(horaInicio, "La hora de inicio es requerida");
        TimeCustomUtils.validarFormatoHora(horaInicio, "El formato de hora inicio debe ser HH:mm");

        StringCustomUtils.validarNoVacioNoNull(horaFin, "La hora de fin es requerida");
        TimeCustomUtils.validarFormatoHora(horaFin, "El formato de hora fin debe ser HH:mm");

        TimeCustomUtils.validarHoraInicioFin(horaInicio, horaFin, "La hora de inicio debe ser menor a la hora de fin");
    }

    public String obtenerHorarioCompleto() {
        return String.format("%s %s - %s", diaSemana, horaInicio, horaFin);
    }

    public void asignarGrupo(Grupo grupo) {
        if (grupo == null) {
            throw new InvalidDataException("El grupo no puede ser nulo");
        }
        this.grupo = grupo;
    }

    public boolean cambioEnDatos(
            String diaSemana,
            String horaInicio,
            String horaFin,
            Grupo grupo
    ){
        if(grupo == null)
            throw new InvalidDataException("El grupo es requerido");
        validarDatos(diaSemana, horaInicio, horaFin);
        return !this.diaSemana.equals(DiaSemana.obtenerDiaPorDescriptcion(diaSemana)) ||
                !this.horaInicio.equals(horaInicio) ||
                !this.horaFin.equals(horaFin) ||
                !this.grupo.equals(grupo);
    }

    public void actualizar(
            String diaSemana,
            String horaInicio,
            String horaFin,
            Grupo grupo
    ){
        validarDatos(diaSemana, horaInicio, horaFin);
        this.diaSemana = DiaSemana.obtenerDiaPorDescriptcion(diaSemana);
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.grupo = grupo;
    }

    public static Horario crear(
            String diaSemana,
            String horaInicio,
            String horaFin
    ){
        validarDatos(diaSemana, horaInicio, horaFin);

      return Horario.builder()
              .diaSemana(DiaSemana.obtenerDiaPorDescriptcion(diaSemana))
              .horaInicio(horaInicio)
              .horaFin(horaFin)
              .build();
    }
}
