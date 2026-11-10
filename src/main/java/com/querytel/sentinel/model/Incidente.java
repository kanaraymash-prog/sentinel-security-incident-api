package com.querytel.sentinel.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "incidente")
public class Incidente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String titulo;

    @Column(nullable = false, length = 1000)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private Categoria categoria;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Severidade severidade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusIncidente status;

    private String origem;

    @Column(columnDefinition = "TEXT")
    private String detalhesResolucao;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dataRegistro;

    @Column(nullable = false)
    private LocalDateTime dataAtualizacao;

    protected Incidente() {
    }

    public Incidente(
            String titulo,
            String descricao,
            Categoria categoria,
            Severidade severidade,
            StatusIncidente status,
            String origem,
            String detalhesResolucao
    ) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.categoria = categoria;
        this.severidade = severidade;
        this.status = status;
        this.origem = origem;
        this.detalhesResolucao = detalhesResolucao;
    }

    @PrePersist
    protected void aoCriar() {
        LocalDateTime agora = agora();
        dataRegistro = agora;
        dataAtualizacao = agora;
    }

    @PreUpdate
    protected void aoAtualizar() {
        dataAtualizacao = agora();
    }

    public void atualizar(
            String titulo,
            String descricao,
            Categoria categoria,
            Severidade severidade,
            StatusIncidente status,
            String origem,
            String detalhesResolucao
    ) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.categoria = categoria;
        this.severidade = severidade;
        this.status = status;
        this.origem = origem;
        this.detalhesResolucao = detalhesResolucao;
        this.dataAtualizacao = agora();
    }

    private LocalDateTime agora() {
        return LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);
    }

    public Long getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public Severidade getSeveridade() {
        return severidade;
    }

    public StatusIncidente getStatus() {
        return status;
    }

    public String getOrigem() {
        return origem;
    }

    public String getDetalhesResolucao() {
        return detalhesResolucao;
    }

    public LocalDateTime getDataRegistro() {
        return dataRegistro;
    }

    public LocalDateTime getDataAtualizacao() {
        return dataAtualizacao;
    }
}
