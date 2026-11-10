package com.querytel.sentinel.dto;

import com.querytel.sentinel.model.Categoria;
import com.querytel.sentinel.model.Incidente;
import com.querytel.sentinel.model.Severidade;
import com.querytel.sentinel.model.StatusIncidente;

import java.time.LocalDateTime;

public record IncidenteResponse(
        Long id,
        String titulo,
        String descricao,
        Categoria categoria,
        Severidade severidade,
        StatusIncidente status,
        String origem,
        String detalhesResolucao,
        LocalDateTime dataRegistro,
        LocalDateTime dataAtualizacao
) {

    public static IncidenteResponse de(Incidente incidente) {
        return new IncidenteResponse(
                incidente.getId(),
                incidente.getTitulo(),
                incidente.getDescricao(),
                incidente.getCategoria(),
                incidente.getSeveridade(),
                incidente.getStatus(),
                incidente.getOrigem(),
                incidente.getDetalhesResolucao(),
                incidente.getDataRegistro(),
                incidente.getDataAtualizacao()
        );
    }
}
