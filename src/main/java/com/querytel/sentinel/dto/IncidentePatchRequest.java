package com.querytel.sentinel.dto;

import com.querytel.sentinel.model.Categoria;
import com.querytel.sentinel.model.Severidade;
import com.querytel.sentinel.model.StatusIncidente;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record IncidentePatchRequest(
        @Pattern(regexp = "(?s).*\\S.*", message = "O título não pode conter apenas espaços.")
        @Size(min = 5, max = 120, message = "O título deve ter entre 5 e 120 caracteres.")
        String titulo,

        @Pattern(regexp = "(?s).*\\S.*", message = "A descrição não pode conter apenas espaços.")
        @Size(max = 1000, message = "A descrição deve ter no máximo 1.000 caracteres.")
        String descricao,

        Categoria categoria,
        Severidade severidade,
        StatusIncidente status,
        String origem,
        String detalhesResolucao
) {

    public boolean estaVazio() {
        return titulo == null
                && descricao == null
                && categoria == null
                && severidade == null
                && status == null
                && origem == null
                && detalhesResolucao == null;
    }
}
