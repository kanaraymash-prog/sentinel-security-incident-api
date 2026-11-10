# Sentinel — Security Incident Management API

API REST para registrar, classificar e acompanhar incidentes de segurança da informação. O projeto usa Java 17+, Spring Boot, Spring Data JPA, Hibernate, H2 e Maven, conforme o escopo do Trabalho 1.

## Requisitos

- Java 17 ou superior
- Maven Wrapper incluído no projeto (ou Maven instalado)

## Compilar, testar e executar

Na raiz do projeto:

```bash
./mvnw clean verify
./mvnw spring-boot:run
```

Alternativamente, com Maven instalado:

```bash
mvn clean verify
mvn spring-boot:run
```

A API fica disponível em `http://localhost:8080`. O H2 em memória cria a tabela automaticamente ao iniciar; os dados duram enquanto a aplicação estiver em execução. Console H2: `http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:mem:sentineldb`, usuário `sa`, senha vazia).

## Entidade

Existe uma única entidade JPA, `Incidente`, persistida na tabela `incidente`.

| Campo | Tipo | Regra |
|---|---|---|
| `id` | Long | Gerado automaticamente |
| `titulo` | String | Obrigatório; de 5 a 120 caracteres |
| `descricao` | String | Obrigatória; até 1.000 caracteres |
| `categoria` | Enum | Obrigatória |
| `severidade` | Enum | Obrigatória |
| `status` | Enum | Obrigatório |
| `origem` | String | Opcional |
| `detalhesResolucao` | String | Obrigatório quando status for `RESOLVIDO` |
| `dataRegistro` | LocalDateTime | Gerada pelo servidor |
| `dataAtualizacao` | LocalDateTime | Gerada/atualizada pelo servidor |

Valores aceitos:

- Categoria: `ACESSO_NAO_AUTORIZADO`, `VAZAMENTO_DE_DADOS`, `MALWARE`, `INDISPONIBILIDADE`, `ATIVIDADE_SUSPEITA`, `OUTRO`
- Severidade: `BAIXA`, `MEDIA`, `ALTA`, `CRITICA`
- Status: `ABERTO`, `EM_ANALISE`, `RESOLVIDO`

Os enums são persistidos como texto. O cliente não pode definir ID ou datas automáticas.

## Endpoints

| Método | Rota | Sucesso |
|---|---|---|
| `GET` | `/api/incidentes` | `200 OK`, lista (ordem crescente de ID) |
| `GET` | `/api/incidentes/{id}` | `200 OK` |
| `POST` | `/api/incidentes` | `201 Created`, corpo e cabeçalho `Location` |
| `PUT` | `/api/incidentes/{id}` | `200 OK`, substituição completa |
| `PATCH` | `/api/incidentes/{id}` | `200 OK`, atualização dos campos enviados |
| `DELETE` | `/api/incidentes/{id}` | `204 No Content` |

IDs inexistentes retornam `404`. Dados inválidos, enum desconhecido e corpo JSON malformado retornam `400`. Regras de negócio não atendidas (como resolver sem informar detalhes) e PATCH vazio retornam `422`. Violações de integridade do banco retornam `409`. Erros usam JSON padronizado com `timestamp`, `status`, `erro`, `mensagem`, `caminho` e `campos`.

No PATCH, campos omitidos ou `null` permanecem inalterados. Envie pelo menos um campo não nulo.

## Exemplos HTTP

### Criar

```bash
curl -i -X POST http://localhost:8080/api/incidentes \
  -H 'Content-Type: application/json' \
  -d '{
    "titulo": "Acesso suspeito",
    "descricao": "Tentativas de acesso fora do horário comercial.",
    "categoria": "ACESSO_NAO_AUTORIZADO",
    "severidade": "ALTA",
    "status": "ABERTO",
    "origem": "Monitoramento"
  }'
```

Resposta `201 Created` (IDs e datas são gerados):

```json
{
  "id": 1,
  "titulo": "Acesso suspeito",
  "descricao": "Tentativas de acesso fora do horário comercial.",
  "categoria": "ACESSO_NAO_AUTORIZADO",
  "severidade": "ALTA",
  "status": "ABERTO",
  "origem": "Monitoramento",
  "detalhesResolucao": null,
  "dataRegistro": "2026-11-10T10:00:00",
  "dataAtualizacao": "2026-11-10T10:00:00"
}
```

### Listar e consultar

```bash
curl http://localhost:8080/api/incidentes
curl http://localhost:8080/api/incidentes/1
```

### Atualizar por completo

`PUT` exige todos os campos obrigatórios:

```bash
curl -i -X PUT http://localhost:8080/api/incidentes/1 \
  -H 'Content-Type: application/json' \
  -d '{
    "titulo": "Acesso não autorizado",
    "descricao": "Credenciais acessadas sem permissão.",
    "categoria": "ATIVIDADE_SUSPEITA",
    "severidade": "CRITICA",
    "status": "EM_ANALISE",
    "origem": "EDR",
    "detalhesResolucao": null
  }'
```

### Atualizar parcialmente e resolver

```bash
curl -i -X PATCH http://localhost:8080/api/incidentes/1 \
  -H 'Content-Type: application/json' \
  -d '{
    "status": "RESOLVIDO",
    "detalhesResolucao": "Senha redefinida e sessão encerrada."
  }'
```

### Excluir

```bash
curl -i -X DELETE http://localhost:8080/api/incidentes/1
```

O arquivo [`requests.http`](./requests.http) contém as mesmas operações para a extensão REST Client do VS Code.

## Organização

```text
src/main/java/com/querytel/sentinel/
├── controller/   # Endpoints REST
├── dto/          # Contratos de entrada e saída
├── exception/    # Erros padronizados e tratamento global
├── model/        # Entidade e enums
├── repository/   # Persistência Spring Data JPA
└── service/      # Regras de negócio
```

## Testes

Os testes de integração usam MockMvc e H2 e cobrem o CRUD, PATCH, validações, regra de resolução, erros 404 e proteção dos campos gerados pelo servidor:

```bash
./mvnw test
```
