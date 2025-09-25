#!/bin/bash
# Usage: ./find_servlet_classes.sh input.jar output.jar

INPUT="$1"
OUTPUT="$2"

# Resolve script directory (so it works no matter where you call it from)
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
LIB_DIR="$SCRIPT_DIR/../lib"

java -cp "$LIB_DIR/*" org.eclipse.transformer.cli.JakartaTransformerCLI "$INPUT" "$OUTPUT"