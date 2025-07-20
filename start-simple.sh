#!/bin/bash

echo "Iniciando todos os serviços do sistema de microserviços..."

# 1. Iniciar Service Discovery
echo "1. Iniciando Service Discovery (Eureka Server)..."
cd service-discovery
mvn spring-boot:run > ../logs/service-discovery.log 2>&1 &
EUREKA_PID=$!
cd ..
echo "Service Discovery iniciado (PID: $EUREKA_PID)"

sleep 10

# 2. Iniciar User Service
echo "2. Iniciando User Service..."
cd user-service
mvn spring-boot:run > ../logs/user-service.log 2>&1 &
USER_PID=$!
cd ..
echo "User Service iniciado (PID: $USER_PID)"

sleep 10

# 3. Iniciar Product Service
echo "3. Iniciando Product Service..."
cd product-service
mvn spring-boot:run > ../logs/product-service.log 2>&1 &
PRODUCT_PID=$!
cd ..
echo "Product Service iniciado (PID: $PRODUCT_PID)"

sleep 10

# 4. Iniciar Order Simulator Service
echo "4. Iniciando Order Simulator Service..."
cd order-simulator-service
mvn spring-boot:run > ../logs/order-simulator-service.log 2>&1 &
ORDER_PID=$!
cd ..
echo "Order Simulator Service iniciado (PID: $ORDER_PID)"

sleep 10

# 5. Iniciar API Gateway
echo "5. Iniciando API Gateway..."
cd api-gateway
mvn spring-boot:run > ../logs/api-gateway.log 2>&1 &
GATEWAY_PID=$!
cd ..
echo "API Gateway iniciado (PID: $GATEWAY_PID)"

sleep 15

echo ""
echo "=========================================="
echo "Todos os serviços foram iniciados!"
echo "=========================================="
echo "Service Discovery: http://localhost:8761"
echo "User Service: http://localhost:8300"
echo "Product Service: http://localhost:8100"
echo "Order Simulator: http://localhost:8200"
echo "API Gateway: http://localhost:8700"
echo ""
echo "=== TESTANDO CONECTIVIDADE ==="
echo "Aguarde alguns segundos para os serviços iniciarem completamente..."
echo ""

# Aguardar um pouco mais
sleep 30

echo "Testando se os serviços estão respondendo:"
echo ""

# Testar cada serviço
echo "✓ Testando Service Discovery..."
curl -s http://localhost:8761/actuator/health || echo "❌ Service Discovery não está respondendo"

echo "✓ Testando User Service..."
curl -s http://localhost:8300/actuator/health || echo "❌ User Service não está respondendo"

echo "✓ Testando Product Service..."
curl -s http://localhost:8100/actuator/health || echo "❌ Product Service não está respondendo"

echo "✓ Testando Order Simulator..."
curl -s http://localhost:8200/actuator/health || echo "❌ Order Simulator não está respondendo"

echo "✓ Testando API Gateway..."
curl -s http://localhost:8700/actuator/health || echo "❌ API Gateway não está respondendo"

echo ""
echo "=== AUTENTICAÇÃO ==="
echo "Faça login para obter um JWT token:"
echo "curl -X POST -H 'Content-Type: application/json' \\"
echo "     -d '{\"username\":\"admin\",\"password\":\"123456\"}' \\"
echo "     http://localhost:8700/api/auth/login"
echo ""
echo "Usuários disponíveis:"
echo "- admin/123456 (ADMIN, USER)"
echo "- user1/123456 (USER)"
echo "- user2/123456 (USER)"
echo ""
echo "Token fixo (fallback): microservices-token-2024"
echo ""
echo "PIDs dos processos:"
echo "Service Discovery: $EUREKA_PID"
echo "User Service: $USER_PID"
echo "Product Service: $PRODUCT_PID"
echo "Order Simulator: $ORDER_PID"
echo "API Gateway: $GATEWAY_PID"
echo ""
echo "Para parar todos os serviços, execute: ./stop-all.sh"
echo "Logs disponíveis em: logs/"