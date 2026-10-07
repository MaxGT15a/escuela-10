package com.max.escuela.utils;

import com.max.escuela.exceptions.NoSuchResourceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;

@Slf4j
public class ServiceUtils {
    public static <E, ID> E obtenerEntidadOException(
            JpaRepository<E,ID> repository,
            ID id,
            Class<E> eClass
    ){
        String nombreEntidad = eClass.getSimpleName();

        log.info("Buscando {} con id: {}", nombreEntidad, id);

        return repository.findById(id)
                .orElseThrow(() ->
                        new NoSuchResourceException(
                                nombreEntidad +
                                " no encontrado con id: " +
                                id
                        )
                );
    }
}
