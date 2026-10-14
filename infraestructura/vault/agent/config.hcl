pid_file = "/tmp/vault-agent.pid"

vault {
  address = "http://vault:8200"
}

auto_auth {
  method "token_file" {
    config = {
      token_file_path = "/run/secrets/vault_agent_token.txt"
    }
  }
}

template {
  destination = "/vault/secrets/basedatos_admin_password"
  contents = <<EOT
{{- with secret "secret/factucore/basedatos/admin" -}}
{{ .Data.data.password }}
{{- end }}
EOT
  error_on_missing_key = true
}

template {
  destination = "/vault/secrets/basedatos_facturador_password"
  contents = <<EOT
{{- with secret "secret/factucore/basedatos/facturador" -}}
{{ .Data.data.password }}
{{- end }}
EOT
  error_on_missing_key = true
}

template {
  destination = "/vault/secrets/basedatos_auth_password"
  contents = <<EOT
{{- with secret "secret/factucore/basedatos/auth" -}}
{{ .Data.data.password }}
{{- end }}
EOT
  error_on_missing_key = true
}

template {
  destination = "/vault/secrets/basedatos_notificaciones_password"
  contents = <<EOT
{{- with secret "secret/factucore/basedatos/notificaciones" -}}
{{ .Data.data.password }}
{{- end }}
EOT
  error_on_missing_key = true
}

template {
  destination = "/vault/secrets/keycloak_admin_password"
  contents = <<EOT
{{- with secret "secret/factucore/keycloak" -}}
{{ .Data.data.password }}
{{- end }}
EOT
  error_on_missing_key = true
}

template {
  destination = "/vault/secrets/mail_password"
  contents = <<EOT
{{- with secret "secret/factucore/mail" -}}
{{ .Data.data.password }}
{{- end }}
EOT
  error_on_missing_key = true
}

template {
  destination = "/vault/secrets/fima_password"
  contents = <<EOT
{{- with secret "secret/factucore/firma" -}}
{{ .Data.data.password }}
{{- end }}
EOT
  error_on_missing_key = true
}
