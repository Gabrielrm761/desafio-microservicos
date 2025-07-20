#!/bin/bash

echo "Iniciando todos os serviços do sistema de microserviços..."

# Função para verificar se uma porta está sendo usada
check_port() {
    netstat -an | grep ":$1 " > /dev/null 2>&1
}

# Função para aguardar um serviço ficar disponível
wait_for_service() {
    local port=$1
    local service_name=$2
    local max_attempts=30
    local attempt=0
    
    echo "Aguardando $service_name (porta $port) ficar disponível..."
    while [ $attempt -lt $max_attempts ]; do
        if check_port $port; then
            echo "$service_name está rodando!"
            return 0
        fi
        sleep 2
        attempt=$((attempt + 1))
    done
    
    echo "ERRO: $service_name não ficou disponível após $((max_attempts * 2)) segundos"
    return 1
}

# 1. Iniciar Service Discovery
echo "1. Iniciando Service Discovery (Eureka Server)..."
cd service-discovery
mvn spring-boot:run > ../logs/service-discovery.log 2>&1 &
EUREKA_PID=$!
cd ..

# Aguardar Eureka ficar disponível
wait_for_service 8761 "Service Discovery"
if [ $? -ne 0 ]; then
    echo "Parando execução devido a falha no Service Discovery"
    exit 1
fi

sleep 5

# 2. Iniciar User Service
echo "2. Iniciando User Service..."
cd user-service
mvn spring-boot:run > ../logs/user-service.log 2>&1 &
USER_PID=$!
cd ..

# Aguardar User Service ficar disponível
wait_for_service 8300 "User Service"

sleep 5

# 3. Iniciar Product Service
echo "3. Iniciando Product Service..."
cd product-service
mvn spring-boot:run > ../logs/product-service.log 2>&1 &
PRODUCT_PID=$!
cd ..

# Aguardar Product Service ficar disponível
wait_for_service 8100 "Product Service"

sleep 5

# 4. Iniciar Order Simulator Service
echo "4. Iniciando Order Simulator Service..."
cd order-simulator-service
mvn spring-boot:run > ../logs/order-simulator-service.log 2>&1 &
ORDER_PID=$!
cd ..

# Aguardar Order Simulator Service ficar disponível
wait_for_service 8200 "Order Simulator Service"

sleep 5

# 5. Iniciar API Gateway
echo "5. Iniciando API Gateway..."
cd api-gateway
mvn spring-boot:run > ../logs/api-gateway.log 2>&1 &
GATEWAY_PID=$!
cd ..

# Aguardar API Gateway ficar disponível
wait_for_service 8700 "API Gateway"

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