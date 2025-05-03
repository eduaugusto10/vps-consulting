# Microserviço de Gestão de Pedidos B2B

Microserviço responsável por receber, processar e gerenciar pedidos de parceiros comerciais em um sistema B2B. Desenvolvido com Spring Boot e compatível com alta concorrência.

## Recursos Principais

- API REST para gerenciamento completo de pedidos
- Sistema de crédito para parceiros comerciais
- Validação de regras de negócio
- Notificações de mudanças de status (simulação de sistema de mensageria)
- Documentação da API com Swagger/OpenAPI
- Testes automatizados
- Containerização com Docker

## Requisitos Técnicos

- Java 17+
- Maven 3.6+
- Docker e Docker Compose

## Estrutura do Projeto

```
order-management-service/
├── src/main/java/com/vps/ordermanagement/
│   ├── controller/       # Controladores REST
│   ├── dto/              # Data Transfer Objects
│   ├── exception/        # Exceções personalizadas
│   ├── model/            # Entidades JPA
│   ├── repository/       # Repositórios Spring Data
│   ├── service/          # Interfaces de serviços
│   │   └── impl/         # Implementações dos serviços
├── src/test/             # Testes automatizados
├── Dockerfile            # Configuração do Docker
├── docker-compose.yml    # Configuração para execução com Docker Compose
├── pom.xml               # Dependências Maven
```

## Endpoints da API

### Parceiros

- `GET /api/partners` - Listar todos os parceiros
- `GET /api/partners/{id}` - Buscar parceiro por ID
- `POST /api/partners` - Criar novo parceiro
- `PUT /api/partners/{id}` - Atualizar parceiro existente
- `DELETE /api/partners/{id}` - Excluir parceiro

### Pedidos

- `GET /api/orders` - Listar todos os pedidos
- `GET /api/orders/{id}` - Buscar pedido por ID
- `GET /api/orders/partner/{partnerId}` - Buscar pedidos por ID do parceiro
- `GET /api/orders/status/{status}` - Buscar pedidos por status
- `GET /api/orders/date-range` - Buscar pedidos por intervalo de data
- `POST /api/orders` - Criar novo pedido
- `PATCH /api/orders/{id}/status/{status}` - Atualizar status do pedido
- `DELETE /api/orders/{id}` - Cancelar pedido

## Instruções de Execução

### Usando Docker Compose (Recomendado)

1. Certifique-se de ter o Docker e o Docker Compose instalados em sua máquina.
2. Navegue até o diretório raiz do projeto onde está o arquivo `docker-compose.yml`.
3. Execute o comando:

```bash
docker-compose up
```

A aplicação estará disponível em `http://localhost:8080`.

### Execução Local (Desenvolvimento)

1. Certifique-se de ter o Java 17+ e o Maven instalados.
2. Configure um banco de dados PostgreSQL local.
3. Atualize as configurações de banco de dados em `src/main/resources/application.yml`.
4. Execute os comandos:

```bash
mvn clean install
mvn spring-boot:run
```

A aplicação estará disponível em `http://localhost:8080`.

## Documentação da API

Após iniciar a aplicação, a documentação completa da API estará disponível em:

- **Swagger UI**: `http://localhost:8080/swagger-ui.html`

  - Interface interativa para testar os endpoints
  - Exemplos de requisições e respostas
  - Descrições detalhadas de todos os parâmetros
  - Códigos de erro e suas explicações

- **API Docs JSON**: `http://localhost:8080/api-docs`
  - Especificação OpenAPI em formato JSON
  - Pode ser importada em ferramentas como Postman

A documentação inclui:

- Descrições de todos os endpoints com seus parâmetros
- Exemplos de valores para facilitar o teste
- Códigos de resposta HTTP esperados
- Modelos de dados (schemas) completos
- Informações sobre validações

## Testes

Para executar os testes automatizados:

```bash
mvn test
```

## Fluxo de Pedidos

1. **Criação de Pedido**: Verifica se o parceiro possui crédito suficiente (sem reservar)
2. **Aprovação de Pedido**: Reserva o crédito do parceiro
3. **Cancelamento de Pedido**: Estorna o crédito se o pedido estava aprovado
4. **Entrega de Pedido**: Finaliza o ciclo do pedido

## Considerações de Produção

- A aplicação está configurada para alta concorrência
- Todas as operações críticas são transacionais
- O banco de dados está configurado para persistência de dados
- As notificações simulam um sistema de mensageria real que poderia ser implementado com Kafka ou RabbitMQ
