#!/usr/bin/env bash
set -euo pipefail

SRC="$(cd "$(dirname "$0")" && pwd)"
DST="/Users/iplahte/GitHub/onrelay/sipxecs/build/sipXdao"

mkdir -p "$DST"
rsync -a --delete \
  --exclude "node_modules" \
  --exclude "dist" \
  --exclude "build" \
  --exclude ".git" \
  "$SRC"/ "$DST"/

echo "Synced sipXdao source to $DST"
