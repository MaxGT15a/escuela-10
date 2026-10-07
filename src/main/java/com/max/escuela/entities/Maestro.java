package com.max.escuela.entities;

import com.max.escuela.exceptions.InvalidDataException;
import com.max.escuela.utils.StringCustomUtils;
import com.max.escuela.utils.NumCustomUtils;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "MAESTROS",
        uniqueConstraints = {
                @UniqueConstraint(name = "MAESTRO_EMAIL_UK", columnNames = "EMAIL"),
                @UniqueConstraint(name = "MAESTRO_TELEFONO_UK", columnNames = "TELEFONO")
        }
)
@AllArgsConstructor
@NoArgsConstructor
@Builder @Getter
public class Maestro {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_MAESTRO")
    private Long id;

    @Column(name = "NOMBRE", length = 50, nullable = false)
    private String nombre;

    @Column(name = "APELLIDO_PATERNO", length = 50, nullable = false)
    private String apellidoPaterno;

    @Column(name = "APELLIDO_MATERNO", length = 50, nullable = false)
    private String apellidoMaterno;

    @Column(name = "EMAIL", length = 100, nullable = false)
    private String email;

    @Column(name = "TELEFONO", length = 10, nullable = false)
    private String telefono;

    @Builder.Default
    @OneToMany(mappedBy = "maestro", fetch = FetchType.LAZY)
    private List<Grupo> grupos = new ArrayList<>();

    private static void validarDatos(
            String nombre,
            String apellidoPaterno,
            String apellidoMaterno,
            String email,
            String telefono
    ){
        StringCustomUtils.validarTamanio(
                nombre, 5, 50,
                "El nombre es requerido y debe tener entre 5 y 50 caracteres"
        );

        StringCustomUtils.validarTamanio(
                apellidoPaterno, 5, 50,
                "El apellido paterno es requerido y debe tener entre 5 y 50 caracteres"
        );

        StringCustomUtils.validarTamanio(
                apellidoMaterno, 5, 50,
                "El apellido materno es requerido y debe tener entre 5 y 50 caracteres"
        );

        StringCustomUtils.validarTamanio(
                email, 5, 100,
                "El email es requerido y debe tener entre 5 y 100 caracteres"
        );

        StringCustomUtils.validarTamanio(
                telefono, 10, 10,
                "El teléfono es requerido y debe tener exactamente 10 caracteres"
        );

        NumCustomUtils.validarStringSoloNumeros(telefono, "El telefono es invalido");
    }

    public void actualizar(
            String nombre,
            String apellidoPaterno,
            String apellidoMaterno,
            String email,
            String telefono
    ){
        validarDatos(nombre,apellidoPaterno,apellidoMaterno,email,telefono);

        this.nombre = nombre.trim();
        this.apellidoPaterno = apellidoPaterno.trim();
        this.apellidoMaterno = apellidoMaterno.trim();
        this.email = email.trim().toLowerCase();
        this.telefono = telefono.trim();
    }

    public boolean cambioEnDatos(
            String nombre,
            String apellidoPaterno,
            String apellidoMaterno,
            String email,
            String telefono
    ){
        validarDatos(nombre, apellidoPaterno, apellidoMaterno, email, telefono);
        return !this.nombre.equals(nombre.trim()) ||
                !this.apellidoPaterno.equals(apellidoPaterno.trim()) ||
                !this.apellidoMaterno.equals(apellidoMaterno.trim()) ||
                !this.email.equals(email.trim().toLowerCase()) ||
                !this.telefono.equals(telefono.trim());
    }

    public String obtenerNombreCompleto() {
        return String.format("%s %s %s", nombre, apellidoPaterno, apellidoMaterno);
    }

    public void asignarGrupo(Grupo grupo){
        if (grupo == null)
            throw new InvalidDataException("El grupo es requerido");

        grupo.asignarMaestro(this);

        this.grupos.add(grupo);
    }

    public static Maestro crear(
            String nombre,
            String apellidoPaterno,
            String apellidoMaterno,
            String email,
            String telefono
    ){
        validarDatos(nombre,apellidoPaterno,apellidoMaterno,email,telefono);

        return Maestro.builder()
                .nombre(nombre.trim())
                .apellidoPaterno(apellidoPaterno.trim())
                .apellidoMaterno(apellidoMaterno.trim())
                .email(email.trim().toLowerCase())
                .telefono(telefono.trim())
                .build();
    }
}
