package com.querytel.sentinel.exception;

public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(Long id) {
        super("Incidente com ID " + id + " não foi encontrado.");
    }
}
