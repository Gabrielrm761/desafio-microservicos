#!/bin/bash

echo "Parando todos os serviços do sistema de microserviços..."

# Função para parar um processo Java por porta
stop_java_process() {
    local port=$1
    local service_name=$2
    
    echo "Parando $service_name (porta $port)..."
    
    # Encontrar o PID do processo que está usando a porta
    local pid=$(netstat -tlnp 2>/dev/null | grep ":$port " | awk '{print $7}' | cut -d'/' -f1)
    
    if [ ! -z "$pid" ] && [ "$pid" != "-" ]; then
        kill $pid 2>/dev/null
        echo "$service_name (PID: $pid) foi parado."
    else
        echo "$service_name não estava rodando na porta $port."
    fi
}

# Parar todos os serviços
stop_java_process 8700 "API Gateway"
stop_java_process 8200 "Order Simulator Service"
stop_java_process 8100 "Product Service"
stop_java_process 8300 "User Service"
stop_java_process 8761 "Service Discovery"

# Alternativamente, parar todos os processos Java Maven
echo ""
echo "Parando todos os processos Maven..."
pkill -f "spring-boot:run" 2>/dev/null

sleep 2

echo ""
echo "Todos os serviços foram parados!"
echo "Logs mantidos em: logs/"