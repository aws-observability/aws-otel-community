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

HELMV3_VERSION="v3.7.1"
BATS_VERSION=1.4.1

## Install kubeval
mkdir -p "${TMP_DIR}/kubeval"
curl -sSL https://github.com/instrumenta/kubeval/releases/latest/download/kubeval-${PLATFORM}-${ARCH}.tar.gz | tar xz -C "${TMP_DIR}/kubeval"
mv "${TMP_DIR}/kubeval/kubeval" "${TOOLS_DIR}/kubeval"

## Install helm v3
mkdir -p "${TMP_DIR}/helmv3"
curl -sSL https://get.helm.sh/helm-${HELMV3_VERSION}-${PLATFORM}-${ARCH}.tar.gz | tar xz -C "${TMP_DIR}/helmv3"
mv "${TMP_DIR}/helmv3/${PLATFORM}-${ARCH}/helm" "${TOOLS_DIR}/helmv3"
rm -rf "${PLATFORM}-${ARCH}"

## Install Bats
curl -sSL https://github.com/bats-core/bats-core/archive/v${BATS_VERSION}.tar.gz | tar xz -C "${TOOLS_DIR}"
ln -s ${TOOLS_DIR}/bats-core-${BATS_VERSION}/bin/bats ${TOOLS_DIR}/bats

## Remove TMP directory
rm -rf ${TMP_DIR}

# Source Code from: https://github.com/aws/eks-charts
