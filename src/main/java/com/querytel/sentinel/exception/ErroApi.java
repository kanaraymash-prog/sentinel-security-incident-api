package com.querytel.sentinel.exception;

import java.time.LocalDateTime;
import java.util.Map;

public record ErroApi(
        LocalDateTime timestamp,
        int status,
        String erro,
        String mensagem,
        String caminho,
        Map<String, String> campos
) {
}
