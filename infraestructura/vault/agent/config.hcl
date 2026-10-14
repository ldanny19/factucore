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
  destination = "/vault/secrets/postgres_admin_password"
  contents = <<EOT
{{- with secret "secret/factucore/basedatos/admin" -}}
{{ .Data.data.password }}
{{- end }}
EOT
  error_on_missing_key = true
}

template {
  destination = "/vault/secrets/postgres_facturador_password"
  contents = <<EOT
{{- with secret "secret/factucore/basedatos/facturador" -}}
{{ .Data.data.password }}
{{- end }}
EOT
  error_on_missing_key = true
}

template {
  destination = "/vault/secrets/postgres_auth_password"
  contents = <<EOT
{{- with secret "secret/factucore/basedatos/auth" -}}
{{ .Data.data.password }}
{{- end }}
EOT
  error_on_missing_key = true
}

template {
  destination = "/vault/secrets/postgres_notificaciones_password"
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
  destination = "/vault/secrets/spring.datasource.password"
  contents = <<EOT
{{- with secret "secret/factucore/basedatos/facturador" -}}
{{ .Data.data.password }}
{{- end }}
EOT
  error_on_missing_key = true
}

template {
  destination = "/vault/secrets/spring.mail.password"
  contents = <<EOT
{{- with secret "secret/factucore/mail" -}}
{{ .Data.data.password }}
{{- end }}
EOT
  error_on_missing_key = true
}

template {
  destination = "/vault/secrets/FACTUCORE_FIRMA_PASSWORD"
  contents = <<EOT
{{- with secret "secret/factucore/firma" -}}
{{ .Data.data.password }}
{{- end }}
EOT
  error_on_missing_key = true
}
