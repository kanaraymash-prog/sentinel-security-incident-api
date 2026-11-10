package com.querytel.sentinel;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.querytel.sentinel.model.Incidente;
import com.querytel.sentinel.repository.IncidenteRepository;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@SpringBootTest
@AutoConfigureMockMvc
class IncidenteApiIntegrationTest {

    private static final String BASE_URL = "/api/incidentes";
    private static final String INCIDENTE_VALIDO = """
            {
              "titulo": "Acesso suspeito",
              "descricao": "Tentativas de acesso fora do horário comercial.",
              "categoria": "ACESSO_NAO_AUTORIZADO",
              "severidade": "ALTA",
              "status": "ABERTO",
              "origem": "Monitoramento"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private IncidenteRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @BeforeEach
    void limparBanco() {
        repository.deleteAll();
    }

    @Test
    void deveExecutarCicloCrudCompleto() throws Exception {
        String respostaCriacao = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(INCIDENTE_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.dataRegistro", notNullValue()))
                .andExpect(jsonPath("$.dataAtualizacao", notNullValue()))
                .andReturn()
                .getResponse()
                .getContentAsString();
        long id = objectMapper.readTree(respostaCriacao).get("id").asLong();
        String dataRegistroOriginal = objectMapper.readTree(respostaCriacao).get("dataRegistro").asText();
        String dataAtualizacaoOriginal = objectMapper.readTree(respostaCriacao).get("dataAtualizacao").asText();

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].categoria").value("ACESSO_NAO_AUTORIZADO"));

        mockMvc.perform(get(BASE_URL + "/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Acesso suspeito"));

        String atualizacao = """
                {
                  "titulo": "Acesso não autorizado",
                  "descricao": "Credenciais acessadas sem permissão.",
                  "categoria": "ATIVIDADE_SUSPEITA",
                  "severidade": "CRITICA",
                  "status": "EM_ANALISE",
                  "origem": "EDR",
                  "detalhesResolucao": null
                }
                """;
        String respostaAtualizacao = mockMvc.perform(put(BASE_URL + "/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(atualizacao))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Acesso não autorizado"))
                .andExpect(jsonPath("$.severidade").value("CRITICA"))
                .andExpect(jsonPath("$.status").value("EM_ANALISE"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        var incidenteAtualizado = objectMapper.readTree(respostaAtualizacao);
        assertEquals(dataRegistroOriginal, incidenteAtualizado.get("dataRegistro").asText());
        assertNotEquals(dataAtualizacaoOriginal, incidenteAtualizado.get("dataAtualizacao").asText());

        mockMvc.perform(patch(BASE_URL + "/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"RESOLVIDO\",\"detalhesResolucao\":\"Senha redefinida e sessão encerrada.\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESOLVIDO"))
                .andExpect(jsonPath("$.detalhesResolucao").value("Senha redefinida e sessão encerrada."));

        mockMvc.perform(delete(BASE_URL + "/" + id))
                .andExpect(status().isNoContent());
        mockMvc.perform(get(BASE_URL + "/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar400QuandoCamposObrigatoriosForemInvalidos() throws Exception {
        String corpoInvalido = """
                {
                  "titulo": "abc",
                  "descricao": "",
                  "categoria": null,
                  "severidade": "ALTA",
                  "status": "ABERTO"
                }
                """;

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoInvalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.campos.titulo").exists())
                .andExpect(jsonPath("$.campos.descricao").exists());
    }

    @Test
    void deveRejeitarValorDeEnumDesconhecido() throws Exception {
        String corpo = INCIDENTE_VALIDO.replace("\"ALTA\"", "\"URGENTE\"");

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value(
                        "O corpo da requisição está malformado ou contém um valor inválido."
                ));
    }

    @Test
    void deveExigirDetalhesAoResolverIncidente() throws Exception {
        String resolvidoSemDetalhes = """
                {
                  "titulo": "Malware identificado",
                  "descricao": "Arquivo malicioso detectado na estação.",
                  "categoria": "MALWARE",
                  "severidade": "ALTA",
                  "status": "RESOLVIDO"
                }
                """;

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(resolvidoSemDetalhes))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.mensagem").value(
                        "Incidentes com status RESOLVIDO devem possuir detalhes da resolução."
                ));
    }

    @Test
    void deveRetornar404ParaIdentificadorInexistenteEmOperacoesDeEscrita() throws Exception {
        mockMvc.perform(put(BASE_URL + "/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(INCIDENTE_VALIDO))
                .andExpect(status().isNotFound());
        mockMvc.perform(patch(BASE_URL + "/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"Título atualizado\"}"))
                .andExpect(status().isNotFound());
        mockMvc.perform(patch(BASE_URL + "/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete(BASE_URL + "/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveIgnorarIdEDatasEnviadosPeloCliente() throws Exception {
        String corpoComCamposDeControle = """
                {
                  "id": 789,
                  "titulo": "Incidente informado",
                  "descricao": "Registro válido para testar campos controlados pelo servidor.",
                  "categoria": "OUTRO",
                  "severidade": "BAIXA",
                  "status": "ABERTO",
                  "dataRegistro": "2000-01-01T00:00:00",
                  "dataAtualizacao": "2000-01-01T00:00:00"
                }
                """;

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoComCamposDeControle))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(org.hamcrest.Matchers.not(789)))
                .andExpect(jsonPath("$.dataRegistro").value(org.hamcrest.Matchers.not("2000-01-01T00:00:00")));
    }

    @Test
    void deveRejeitarPatchVazioECamposInvalidos() throws Exception {
        Incidente incidente = criarIncidentePersistido();
        mockMvc.perform(patch(BASE_URL + "/" + incidente.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnprocessableEntity());
        mockMvc.perform(patch(BASE_URL + "/" + incidente.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"x\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.titulo").exists());
    }

    @Test
    void deveAplicarLimitesDeTituloEDescricao() throws Exception {
        String tituloNoLimite = "T".repeat(120);
        String descricaoNoLimite = "D".repeat(1000);
        String corpoNoLimite = objectMapper.writeValueAsString(java.util.Map.of(
                "titulo", tituloNoLimite,
                "descricao", descricaoNoLimite,
                "categoria", "OUTRO",
                "severidade", "BAIXA",
                "status", "ABERTO"
        ));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoNoLimite))
                .andExpect(status().isCreated());

        String tituloLongo = "T".repeat(121);
        String corpoTituloLongo = corpoNoLimite.replace(tituloNoLimite, tituloLongo);
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoTituloLongo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.titulo").exists());

        String descricaoLonga = "D".repeat(1001);
        String corpoDescricaoLonga = corpoNoLimite.replace(descricaoNoLimite, descricaoLonga);
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoDescricaoLonga))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.descricao").exists());
    }

    @Test
    void deveCriarExatamenteUmaEntidadeJPA() {
        org.junit.jupiter.api.Assertions.assertEquals(
                1,
                entityManagerFactory.getMetamodel().getEntities().size()
        );
    }

    private Incidente criarIncidentePersistido() {
        Incidente incidente = new Incidente(
                "Acesso suspeito",
                "Tentativas de acesso fora do horário comercial.",
                com.querytel.sentinel.model.Categoria.ACESSO_NAO_AUTORIZADO,
                com.querytel.sentinel.model.Severidade.ALTA,
                com.querytel.sentinel.model.StatusIncidente.ABERTO,
                null,
                null
        );
        return repository.save(incidente);
    }
}
