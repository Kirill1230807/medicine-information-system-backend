{{- define "user-service.fullname" -}}
{{- .Release.Name -}}
{{- end }}

{{- define "user-service.labels" -}}
app: {{ .Release.Name }}
app.kubernetes.io/name: {{ .Release.Name }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{- define "user-service.selectorLabels" -}}
app: {{ .Release.Name }}
{{- end }}