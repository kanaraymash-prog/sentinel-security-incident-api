package com.querytel.sentinel.dto;

import com.querytel.sentinel.model.Categoria;
import com.querytel.sentinel.model.Severidade;
import com.querytel.sentinel.model.StatusIncidente;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record IncidenteRequest(
        @NotBlank(message = "O título é obrigatório.")
        @Size(min = 5, max = 120, message = "O título deve ter entre 5 e 120 caracteres.")
        String titulo,

        @NotBlank(message = "A descrição é obrigatória.")
        @Size(max = 1000, message = "A descrição deve ter no máximo 1.000 caracteres.")
        String descricao,

        @NotNull(message = "A categoria é obrigatória.")
        Categoria categoria,

        @NotNull(message = "A severidade é obrigatória.")
        Severidade severidade,

        @NotNull(message = "O status é obrigatório.")
        StatusIncidente status,

        String origem,
        String detalhesResolucao
) {
}
