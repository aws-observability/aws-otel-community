REPO_ROOT ?= $(shell git rev-parse --show-toplevel)

.PHONY:all
all: validate lint

install-tools:
	${REPO_ROOT}/scripts/install-tools.sh

.PHONY: validate
validate:
	${REPO_ROOT}/scripts/validate-charts.sh

.PHONY: lint
lint:
	${REPO_ROOT}/scripts/lint-charts.sh

.PHONY: clean
clean:
	rm -rf ${REPO_ROOT}/build/

# Source Code from: https://github.com/aws/eks-charts
