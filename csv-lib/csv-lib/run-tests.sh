#!/bin/bash
# Ejecuta las pruebas con aserciones manuales (sin JUnit).
set -e

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
OUT="$ROOT/out"

if [ ! -d "$OUT" ]; then
  echo "El directorio $OUT no existe. Ejecuta primero ./compile.sh"
  exit 1
fi

java -cp "$OUT" csvlib.Pruebas
