{{/* vim: set filetype=mustache: */}}
{{/*
Expand the name of the chart.
*/}}
{{- define "adotCollector.daemonSet.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Create a default fully qualified app name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
If release name contains chart name it will be used as a full name.
*/}}
{{- define "adotCollector.daemonSet.fullname" -}}
{{- if .Values.fullnameOverride -}}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" -}}
{{- else -}}
{{- $name := default .Chart.Name .Values.nameOverride -}}
{{- if contains $name .Release.Name -}}
{{- .Release.Name | trunc 63 | trimSuffix "-" -}}
{{- else -}}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" -}}
{{- end -}}
{{- end -}}
{{- end -}}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "adotCollector.daemonSet.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Common labels
*/}}
{{- define "adotCollector.daemonSet.labels" -}}
helm.sh/chart: {{ include "adotCollector.daemonSet.chart" . }}
{{ include "adotCollector.daemonSet.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end -}}

{{/*
Selector labels
*/}}
{{- define "adotCollector.daemonSet.selectorLabels" -}}
app.kubernetes.io/name: {{ include "adotCollector.daemonSet.name" . }}
{{- end -}}

{{/*
Create the name of the service account to use
*/}}
{{- define "adotCollector.daemonSet.serviceAccountName" -}}
  {{ default (include "adotCollector.daemonSet.fullname" .) .Values.adotCollector.daemonSet.serviceAccount.name }}
{{- end -}}


{{/*
Allow the release namespace to be overridden for multi-namespace deployments in combined charts.
*/}}
{{- define "adotCollector.daemonSet.namespace" -}}
  {{- if .Values.global -}}
    {{- if .Values.global.namespaceOverride -}}
      {{- .Values.global.namespaceOverride -}}
    {{- else -}}
      {{- .Values.adotCollector.daemonSet.namespace -}}
    {{- end -}}
  {{- else -}}
    {{- .Values.adotCollector.daemonSet.namespace -}}
  {{- end -}}
{{- end -}}


{{/*
Allow the release namespace to be overridden for multi-namespace deployments in combined charts.
*/}}
{{- define "adotCollector.sidecar.namespace" -}}
  {{- if .Values.global -}}
    {{- if .Values.global.namespaceOverride -}}
      {{- .Values.global.namespaceOverride -}}
    {{- else -}}
      {{- .Values.adotCollector.sidecar.namespace -}}
    {{- end -}}
  {{- else -}}
    {{- .Values.adotCollector.sidecar.namespace -}}
  {{- end -}}
{{- end -}}
