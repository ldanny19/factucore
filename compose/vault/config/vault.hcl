ui = true

api_addr = "http://vault:8200"

listener "tcp" {
  address         = "0.0.0.0:8200"
  cluster_address = "0.0.0.0:8201"
  tls_disable     = 1
}

storage "file" {
  path = "/vault/data"
}
