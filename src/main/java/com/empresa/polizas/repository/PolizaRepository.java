package com.empresa.polizas.repository;

import com.empresa.polizas.entity.Poliza;
import com.empresa.polizas.enums.EstadoPoliza;
import com.empresa.polizas.enums.TipoPoliza;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PolizaRepository extends JpaRepository<Poliza, Long> {

    @Query("""
            select p from Poliza p
            where (:tipo is null or p.tipo = :tipo)
              and (:estado is null or p.estado = :estado)
            order by p.id
            """)
    List<Poliza> buscarPorFiltros(@Param("tipo") TipoPoliza tipo,
                                  @Param("estado") EstadoPoliza estado);
}