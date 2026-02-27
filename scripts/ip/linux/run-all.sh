#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "${SCRIPT_DIR}/../../.." && pwd)"
CORE_DIR="${ROOT_DIR}/core"
CP="${CORE_DIR}/target/classes:${CORE_DIR}/target/dependency/*"
COMMON_JAVA_OPTS="-Ddes.transport.profile=ip -Dtransport.mode=IP"

pushd "${ROOT_DIR}" >/dev/null
mvn -pl core -am clean package -DskipTests
mvn -pl core dependency:copy-dependencies -DincludeScope=runtime -DoutputDirectory="${CORE_DIR}/target/dependency"
popd >/dev/null

echo ""
echo "===== Instance Configuration ====="
if [[ -z "${1:-}" ]]; then
  read -rp "Enter Server Instance ID (leave blank for default): " SERVER_INSTANCE
else
  SERVER_INSTANCE="$1"
fi
if [[ -z "${2:-}" ]]; then
  read -rp "Enter Node Instance ID (leave blank for default): " NODE_INSTANCE
else
  NODE_INSTANCE="$2"
fi

echo "Starting EdgeCoordinator [IP profile]"
if [[ -z "${SERVER_INSTANCE}" ]]; then
  echo "Starting EdgeServer (default config)"
else
  echo "Starting EdgeServer (${SERVER_INSTANCE})"
fi
if [[ -z "${NODE_INSTANCE}" ]]; then
  echo "Starting EdgeNode (default config)"
else
  echo "Starting EdgeNode (${NODE_INSTANCE})"
fi

tmux new-session -d -s des "cd '${ROOT_DIR}' && java ${COMMON_JAVA_OPTS} -cp '${CP}' components.coordinator.EdgeCoordinator"
if [[ -z "${SERVER_INSTANCE}" ]]; then
  tmux new-window -t des "cd '${ROOT_DIR}' && java ${COMMON_JAVA_OPTS} -cp '${CP}' components.server.EdgeServer"
else
  tmux new-window -t des "cd '${ROOT_DIR}' && java ${COMMON_JAVA_OPTS} -cp '${CP}' components.server.EdgeServer ${SERVER_INSTANCE}"
fi
if [[ -z "${NODE_INSTANCE}" ]]; then
  tmux new-window -t des "cd '${ROOT_DIR}' && java ${COMMON_JAVA_OPTS} -cp '${CP}' components.node.EdgeNode"
else
  tmux new-window -t des "cd '${ROOT_DIR}' && java ${COMMON_JAVA_OPTS} -cp '${CP}' components.node.EdgeNode ${NODE_INSTANCE}"
fi

echo "Attached to tmux session 'des' (ctrl+b d to detach)"
tmux attach -t des
