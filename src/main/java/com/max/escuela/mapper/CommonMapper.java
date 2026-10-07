package com.max.escuela.mapper;

public interface CommonMapper<RQ, RS, E> {
    E requestAEntidad(RQ request);
    RS entidadAResponse(E entidad);
}
