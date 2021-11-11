#!/usr/bin/env bash
set -euo pipefail

PLATFORM=$(uname -s | tr '[:upper:]' '[:lower:]')
ARCH=$([[ $(uname -m) = "x86_64" ]] && echo 'amd64' || echo 'arm64')
ROOT_PROJECT=$(git rev-parse --show-toplevel)
BUILD_DIR="${ROOT_PROJECT}/build"
TMP_DIR="${BUILD_DIR}/tmp"
TOOLS_DIR="${BUILD_DIR}/tools"
mkdir -p "${TOOLS_DIR}"
export PATH="${TOOLS_DIR}:${PATH}"

HELM_VERSION_TAG=$(curl -sSL https://github.com/helm/helm/releases/latest | sed -n '/<title>/,$p' | grep -oE '[0-9]+\.[0-9]+\.[0-9]+' | head -1)
HELM_VERSION=v${HELM_VERSION_TAG}

## Install kubeval
mkdir -p "${TMP_DIR}/kubeval"
curl -sSL https://github.com/instrumenta/kubeval/releases/latest/download/kubeval-${PLATFORM}-${ARCH}.tar.gz | tar xz -C "${TMP_DIR}/kubeval"
mv "${TMP_DIR}/kubeval/kubeval" "${TOOLS_DIR}/kubeval"

## Install helm v3
mkdir -p "${TMP_DIR}/helmv3"
curl -sSL https://get.helm.sh/helm-${HELM_VERSION}-${PLATFORM}-${ARCH}.tar.gz | tar xz -C "${TMP_DIR}/helmv3"
mv "${TMP_DIR}/helmv3/${PLATFORM}-${ARCH}/helm" "${TOOLS_DIR}/helmv3"
rm -rf "${PLATFORM}-${ARCH}"

## Remove TMP directory
rm -rf ${TMP_DIR}

# Source Code from: https://github.com/aws/eks-charts
