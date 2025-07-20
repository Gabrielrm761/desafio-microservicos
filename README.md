# Microserviços - Sistema de Pedidos

Este projeto é composto por cinco módulos Spring Boot que implementam um sistema completo de microserviços para gerenciamento de pedidos com autenticação JWT.

## Arquitetura

- **service-discovery**: Eureka Server para registro e descoberta de serviços (porta 8761)
- **api-gateway**: Spring Cloud Gateway com autenticação JWT (porta 8700)
- **user-service**: Serviço de autenticação e gerenciamento de usuários (porta 8300)
- **product-service**: Catálogo de Produtos com CRUD e banco H2 (porta 8100)
- **order-simulator-service**: Simulador de Pedidos que consome o catálogo (porta 8200)

## Como executar

### Pré-requisitos
- Java 17+
- Maven 3.6+

### Execução rápida
```bash
cd microservicos-pedidos
./start-all.sh
```

### Ordem de inicialização manual
1. **Service Discovery** (obrigatório primeiro):
   ```bash
   cd service-discovery
   mvn spring-boot:run
   ```

2. **User Service**:
   ```bash
   cd user-service
   mvn spring-boot:run
   ```

3. **Product Service**:
   ```bash
   cd product-service
   mvn spring-boot:run
   ```

4. **Order Simulator Service**:
   ```bash
   cd order-simulator-service
   mvn spring-boot:run
   ```

5. **API Gateway** (por último):
   ```bash
   cd api-gateway
   mvn spring-boot:run
   ```

## Autenticação

O sistema suporta dois tipos de autenticação:

### 1. Autenticação JWT (Recomendado)
Primeiro, faça login para obter um token JWT:
```bash
curl -X POST \
     -H "Content-Type: application/json" \
     -d '{
       "username": "admin",
       "password": "123456"
     }' \
     http://localhost:8700/api/auth/login
```

Use o token retornado nos próximos requests:
```
Authorization: Bearer <seu-jwt-token>
```

### 2. Token fixo (Fallback)
Para compatibilidade, ainda é possível usar o token fixo:
```
Authorization: Bearer microservices-token-2024
```

### Usuários pré-cadastrados
- **admin** / 123456 (roles: ADMIN, USER)
- **user1** / 123456 (role: USER)  
- **user2** / 123456 (role: USER)

## Endpoints disponíveis

### Autenticação e Usuários (via Gateway: http://localhost:8700)
- `POST /api/auth/register` - Registrar novo usuário
- `POST /api/auth/login` - Fazer login e obter token JWT
- `POST /api/auth/validate` - Validar token JWT
- `GET /api/auth/me` - Obter dados do usuário autenticado
- `GET /api/users` - Listar todos os usuários (requer autenticação)
- `GET /api/users/{id}` - Buscar usuário por ID
- `PUT /api/users/{id}` - Atualizar usuário
- `DELETE /api/users/{id}` - Deletar usuário

### Produtos (via Gateway: http://localhost:8700)
- `GET /api/products` - Listar todos os produtos
- `GET /api/products/{id}` - Buscar produto por ID
- `GET /api/products/category/{category}` - Buscar por categoria
- `GET /api/products/available` - Produtos disponíveis em estoque
- `POST /api/products` - Criar novo produto
- `PUT /api/products/{id}` - Atualizar produto
- `DELETE /api/products/{id}` - Deletar produto

### Pedidos (via Gateway: http://localhost:8700)
- `GET /api/orders` - Listar todos os pedidos
- `GET /api/orders/{orderId}` - Buscar pedido por ID
- `POST /api/orders` - Criar novo pedido
- `POST /api/orders/simulate` - Simular pedido aleatório
- `GET /api/orders/products/available` - Produtos disponíveis
- `GET /api/orders/products/category/{category}` - Produtos por categoria

## Exemplos de uso

### Registrar novo usuário:
```bash
curl -X POST \
     -H "Content-Type: application/json" \
     -d '{
       "username": "novouser",
       "email": "novouser@example.com",
       "password": "123456",
       "firstName": "Novo",
       "lastName": "Usuário"
     }' \
     http://localhost:8700/api/auth/register
```

### Fazer login e obter token:
```bash
curl -X POST \
     -H "Content-Type: application/json" \
     -d '{
       "username": "admin",
       "password": "123456"
     }' \
     http://localhost:8700/api/auth/login
```

### Listar produtos (com JWT):
```bash
curl -H "Authorization: Bearer <seu-jwt-token>" \
     http://localhost:8700/api/products
```

### Criar pedido:
```bash
curl -X POST \
     -H "Authorization: Bearer <seu-jwt-token>" \
     -H "Content-Type: application/json" \
     -d '{
       "customerName": "João Silva",
       "items": [
         {
           "productId": 1,
           "quantity": 2
         }
       ]
     }' \
     http://localhost:8700/api/orders
```

### Simular pedido aleatório:
```bash
curl -X POST \
     -H "Authorization: Bearer <seu-jwt-token>" \
     http://localhost:8700/api/orders/simulate
```

## Monitoramento

- **Eureka Dashboard**: http://localhost:8761
- **H2 Console** (Product Service): http://localhost:8100/h2-console
  - JDBC URL: `jdbc:h2:mem:productdb`
  - Username: `sa`
  - Password: (vazio)
- **H2 Console** (User Service): http://localhost:8300/h2-console
  - JDBC URL: `jdbc:h2:mem:userdb`
  - Username: `sa`
  - Password: (vazio)

## Tecnologias utilizadas

- Java 17
- Spring Boot 3.2.0
- Spring Cloud 2023.0.0
- Spring Cloud Gateway
- Netflix Eureka
- Spring Security
- JWT (JSON Web Tokens)
- BCrypt (Hash de senhas)
- H2 Database
- OpenFeign
- Spring Data JPA

## Estrutura do projeto

```
microservicos-pedidos/
├── service-discovery/          # Eureka Server
├── api-gateway/               # Spring Cloud Gateway com JWT
├── user-service/              # Serviço de Usuários e Autenticação
├── product-service/           # Serviço de Produtos
├── order-simulator-service/   # Simulador de Pedidos
├── start-all.sh              # Script para iniciar todos os serviços
├── stop-all.sh               # Script para parar todos os serviços
└── README.md                 # Este arquivo
```

## Segurança

### Autenticação JWT
- Tokens JWT com expiração de 24 horas
- Chave secreta configurável via propriedades
- Validação tanto local (no gateway) quanto remota (via user-service)
- Suporte a múltiplas roles (USER, ADMIN, MODERATOR)

### Hash de senhas
- Senhas são criptografadas com BCrypt
- Salt automático para cada senha
- Nunca retorna senhas nas respostas da API

### Fallback de compatibilidade
- Token fixo ainda aceito para compatibilidade retroativa
- Logs detalhados para debugging de autenticação