#!/bin/bash
# Compila la librería + demo + pruebas con javac, sin frameworks.
set -e

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
OUT="$ROOT/out"
SRC="$ROOT/src"
TEST="$ROOT/test"

mkdir -p "$OUT"

echo "Compilando fuentes (src) y pruebas (test) a $OUT ..."
javac -d "$OUT" \
  $(find "$SRC" -name "*.java") \
  $(find "$TEST" -name "*.java")

echo "Compilación exitosa."
