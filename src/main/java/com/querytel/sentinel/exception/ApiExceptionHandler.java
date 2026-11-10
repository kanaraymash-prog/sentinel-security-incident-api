package com.querytel.sentinel.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public org.springframework.http.ResponseEntity<ErroApi> tratarNaoEncontrado(
            RecursoNaoEncontradoException exception,
            HttpServletRequest request
    ) {
        return criarResposta(HttpStatus.NOT_FOUND, exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(RegraNegocioException.class)
    public org.springframework.http.ResponseEntity<ErroApi> tratarRegraNegocio(
            RegraNegocioException exception,
            HttpServletRequest request
    ) {
        return criarResposta(HttpStatus.UNPROCESSABLE_ENTITY, exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public org.springframework.http.ResponseEntity<ErroApi> tratarValidacao(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        Map<String, String> campos = new LinkedHashMap<>();
        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            campos.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return criarResposta(
                HttpStatus.BAD_REQUEST,
                "Um ou mais campos são inválidos.",
                request,
                campos
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public org.springframework.http.ResponseEntity<ErroApi> tratarCorpoInvalido(
            HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {
        return criarResposta(
                HttpStatus.BAD_REQUEST,
                "O corpo da requisição está malformado ou contém um valor inválido.",
                request,
                Map.of()
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public org.springframework.http.ResponseEntity<ErroApi> tratarConflito(
            DataIntegrityViolationException exception,
            HttpServletRequest request
    ) {
        return criarResposta(
                HttpStatus.CONFLICT,
                "A operação viola uma restrição de integridade dos dados.",
                request,
                Map.of()
        );
    }

    private org.springframework.http.ResponseEntity<ErroApi> criarResposta(
            HttpStatus status,
            String mensagem,
            HttpServletRequest request,
            Map<String, String> campos
    ) {
        ErroApi erro = new ErroApi(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                mensagem,
                request.getRequestURI(),
                campos
        );
        return org.springframework.http.ResponseEntity.status(status).body(erro);
    }
}
