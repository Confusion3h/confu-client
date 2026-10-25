#!/usr/bin/env bash
# Optional Authenticode signature for a Confu Client Windows installer.
# Exits 0 without changing the file when no certificate is configured.
#
# PKCS#12 (osslsigncode, or signtool if that is the one on PATH):
#   CONFU_SIGN_PFX          path to the .pfx / .p12
#   CONFU_SIGN_PASSWORD     password for that file
#   CONFU_SIGN_TIMESTAMP    RFC 3161 URL (default http://timestamp.digicert.com)
#
# Azure Trusted Signing (signtool only; the dlib reads the Azure env vars):
#   CONFU_AZURE_DLIB        path to the Trusted Signing dlib
#   CONFU_AZURE_METADATA    path to the .json metadata file
#   AZURE_TENANT_ID AZURE_CLIENT_ID AZURE_CLIENT_SECRET
#   CONFU_SIGN_TIMESTAMP    default http://timestamp.acs.microsoft.com
#
# If a certificate variable is set but the tool is missing, this script exits 1
# so a release build does not ship an installer it was asked to sign.
set -euo pipefail

EXE=${1:?usage: scripts/sign-windows.sh <installer.exe>}

if [[ -z "${CONFU_SIGN_PFX:-}" && -z "${CONFU_AZURE_DLIB:-}" ]]; then
  echo "sign-windows: no certificate configured, leaving unsigned: $EXE"
  exit 0
fi

if [[ ! -f "$EXE" ]]; then
  echo "sign-windows: missing $EXE" >&2
  exit 1
fi

if [[ -n "${CONFU_AZURE_DLIB:-}" ]]; then
  if [[ -z "${CONFU_AZURE_METADATA:-}" ]]; then
    echo "sign-windows: CONFU_AZURE_DLIB is set but CONFU_AZURE_METADATA is empty" >&2
    exit 1
  fi
  if ! command -v signtool >/dev/null 2>&1; then
    echo "sign-windows: signtool not found; Azure Trusted Signing needs it" >&2
    exit 1
  fi
  signtool sign /v /fd SHA256 \
    /tr "${CONFU_SIGN_TIMESTAMP:-http://timestamp.acs.microsoft.com}" /td SHA256 \
    /d "Confu Client" \
    /dlib "$CONFU_AZURE_DLIB" /dmdf "$CONFU_AZURE_METADATA" \
    "$EXE"
  exit 0
fi

if [[ -z "${CONFU_SIGN_PASSWORD:-}" ]]; then
  echo "sign-windows: CONFU_SIGN_PFX is set but CONFU_SIGN_PASSWORD is empty" >&2
  exit 1
fi

TS=${CONFU_SIGN_TIMESTAMP:-http://timestamp.digicert.com}

if command -v osslsigncode >/dev/null 2>&1; then
  tmp=$(mktemp)
  # Password stays in the environment. osslsigncode reads it from stdin via -readpass.
  printf '%s\n' "$CONFU_SIGN_PASSWORD" | osslsigncode sign \
    -pkcs12 "$CONFU_SIGN_PFX" -readpass /dev/stdin \
    -n "Confu Client" -t "$TS" -h sha256 \
    -in "$EXE" -out "$tmp"
  mv "$tmp" "$EXE"
  exit 0
fi

if command -v signtool >/dev/null 2>&1; then
  signtool sign /v /fd SHA256 \
    /f "$CONFU_SIGN_PFX" /p "$CONFU_SIGN_PASSWORD" \
    /tr "$TS" /td SHA256 /d "Confu Client" \
    "$EXE"
  exit 0
fi

echo "sign-windows: a certificate is set but neither osslsigncode nor signtool is installed" >&2
exit 1
