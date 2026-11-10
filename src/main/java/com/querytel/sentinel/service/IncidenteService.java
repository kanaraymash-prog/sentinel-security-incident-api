package com.querytel.sentinel.service;

import com.querytel.sentinel.dto.IncidentePatchRequest;
import com.querytel.sentinel.dto.IncidenteRequest;
import com.querytel.sentinel.exception.RegraNegocioException;
import com.querytel.sentinel.exception.RecursoNaoEncontradoException;
import com.querytel.sentinel.model.Incidente;
import com.querytel.sentinel.model.StatusIncidente;
import com.querytel.sentinel.repository.IncidenteRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class IncidenteService {

    private final IncidenteRepository repository;

    public IncidenteService(IncidenteRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Incidente> listar() {
        return repository.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }

    @Transactional(readOnly = true)
    public Incidente buscarPorId(Long id) {
        return repository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException(id));
    }

    @Transactional
    public Incidente criar(IncidenteRequest request) {
        validarResolucao(request.status(), request.detalhesResolucao());
        Incidente incidente = new Incidente(
                request.titulo(),
                request.descricao(),
                request.categoria(),
                request.severidade(),
                request.status(),
                request.origem(),
                request.detalhesResolucao()
        );
        return repository.save(incidente);
    }

    @Transactional
    public Incidente atualizar(Long id, IncidenteRequest request) {
        Incidente incidente = buscarPorId(id);
        validarResolucao(request.status(), request.detalhesResolucao());
        incidente.atualizar(
                request.titulo(),
                request.descricao(),
                request.categoria(),
                request.severidade(),
                request.status(),
                request.origem(),
                request.detalhesResolucao()
        );
        return repository.save(incidente);
    }

    @Transactional
    public Incidente atualizarParcialmente(Long id, IncidentePatchRequest request) {
        Incidente incidente = buscarPorId(id);
        if (request.estaVazio()) {
            throw new RegraNegocioException("Informe ao menos um campo para atualizar.");
        }

        String titulo = request.titulo() != null ? request.titulo() : incidente.getTitulo();
        String descricao = request.descricao() != null ? request.descricao() : incidente.getDescricao();
        var categoria = request.categoria() != null ? request.categoria() : incidente.getCategoria();
        var severidade = request.severidade() != null ? request.severidade() : incidente.getSeveridade();
        StatusIncidente status = request.status() != null ? request.status() : incidente.getStatus();
        String origem = request.origem() != null ? request.origem() : incidente.getOrigem();
        String detalhesResolucao = request.detalhesResolucao() != null
                ? request.detalhesResolucao()
                : incidente.getDetalhesResolucao();

        validarTitulo(titulo);
        validarDescricao(descricao);
        validarResolucao(status, detalhesResolucao);
        incidente.atualizar(
                titulo,
                descricao,
                categoria,
                severidade,
                status,
                origem,
                detalhesResolucao
        );
        return repository.save(incidente);
    }

    @Transactional
    public void excluir(Long id) {
        Incidente incidente = buscarPorId(id);
        repository.delete(incidente);
    }

    private void validarResolucao(StatusIncidente status, String detalhesResolucao) {
        if (status == StatusIncidente.RESOLVIDO
                && (detalhesResolucao == null || detalhesResolucao.isBlank())) {
            throw new RegraNegocioException(
                    "Incidentes com status RESOLVIDO devem possuir detalhes da resolução."
            );
        }
    }

    private void validarTitulo(String titulo) {
        if (titulo == null || titulo.isBlank() || titulo.length() < 5 || titulo.length() > 120) {
            throw new RegraNegocioException("O título deve ter entre 5 e 120 caracteres e não pode ser vazio.");
        }
    }

    private void validarDescricao(String descricao) {
        if (descricao == null || descricao.isBlank() || descricao.length() > 1000) {
            throw new RegraNegocioException("A descrição é obrigatória e deve ter no máximo 1.000 caracteres.");
        }
    }
}
