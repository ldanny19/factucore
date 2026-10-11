#!/usr/bin/env bash
set -Eeuo pipefail

readonly BOOTSTRAP_SERVER="kafka:9092"
readonly TOPICS=("auditoria" "notificacion")
readonly MAX_INTENTOS=30
readonly ESPERA_SEGUNDOS=2

echo "Iniciando inicialización de topics de FactuCore"

for ((intento = 1; intento <= MAX_INTENTOS; intento++)); do
    if /opt/kafka/bin/kafka-broker-api-versions.sh --bootstrap-server "$BOOTSTRAP_SERVER" >/dev/null 2>&1; then
        break
    fi

    if ((intento == MAX_INTENTOS)); then
        echo "ERROR: Kafka no estuvo disponible después de $((MAX_INTENTOS * ESPERA_SEGUNDOS)) segundos." >&2
        exit 1
    fi

    sleep "$ESPERA_SEGUNDOS"
done

for topico in "${TOPICS[@]}"; do
    echo "Verificando topic: $topico"
    /opt/kafka/bin/kafka-topics.sh \
        --bootstrap-server "$BOOTSTRAP_SERVER" \
        --create \
        --if-not-exists \
        --topic "$topico" \
        --partitions 1 \
        --replication-factor 1
done

echo "Inicialización de topics completada correctamente"
