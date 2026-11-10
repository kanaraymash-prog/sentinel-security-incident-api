package com.querytel.sentinel.controller;

import com.querytel.sentinel.dto.IncidentePatchRequest;
import com.querytel.sentinel.dto.IncidenteRequest;
import com.querytel.sentinel.dto.IncidenteResponse;
import com.querytel.sentinel.service.IncidenteService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/incidentes")
public class IncidenteController {

    private final IncidenteService service;

    public IncidenteController(IncidenteService service) {
        this.service = service;
    }

    @GetMapping
    public List<IncidenteResponse> listar() {
        return service.listar().stream().map(IncidenteResponse::de).toList();
    }

    @GetMapping("/{id}")
    public IncidenteResponse buscarPorId(@PathVariable Long id) {
        return IncidenteResponse.de(service.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<IncidenteResponse> criar(@Valid @RequestBody IncidenteRequest request) {
        IncidenteResponse resposta = IncidenteResponse.de(service.criar(request));
        var local = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(resposta.id())
                .toUri();
        return ResponseEntity.created(local).body(resposta);
    }

    @PutMapping("/{id}")
    public IncidenteResponse atualizar(
            @PathVariable Long id,
            @Valid @RequestBody IncidenteRequest request
    ) {
        return IncidenteResponse.de(service.atualizar(id, request));
    }

    @PatchMapping("/{id}")
    public IncidenteResponse atualizarParcialmente(
            @PathVariable Long id,
            @Valid @RequestBody IncidentePatchRequest request
    ) {
        return IncidenteResponse.de(service.atualizarParcialmente(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
