#!/bin/bash
# Ejecuta la demostración Principal sobre el CSV de ejemplo.
set -e

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
OUT="$ROOT/out"

if [ ! -d "$OUT" ]; then
  echo "El directorio $OUT no existe. Ejecuta primero ./compile.sh"
  exit 1
fi

CSV="${1:-$ROOT/data/personas.csv}"
echo "Ejecutando demo con CSV: $CSV"
echo "------------------------------------------------------------"
java -cp "$OUT" csvlib.demo.Principal "$CSV"
