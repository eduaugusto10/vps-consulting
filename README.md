# Microserviço de Gestão de Pedidos B2B

Microserviço responsável por receber, processar e gerenciar pedidos de parceiros comerciais em um sistema B2B. Desenvolvido com Spring Boot e compatível com alta concorrência.

## Recursos Principais

- API REST para gerenciamento completo de pedidos
- Sistema de crédito para parceiros comerciais
- Validação de regras de negócio
- Notificações de mudanças de status
- Documentação da API com Swagger/OpenAPI
- Testes automatizados
- Containerização com Docker

## Requisitos Técnicos

- Java 17+
- Maven 3.6+
- Docker e Docker Compose

## Instruções de Execução

### Usando Docker Compose (Recomendado)

1. Execute o comando:

```bash
docker-compose up
```

A aplicação estará disponível em `http://localhost:8080`.

## Documentação da API

A documentação completa da API estará disponível em:

- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
