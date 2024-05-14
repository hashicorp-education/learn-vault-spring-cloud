#!/bin/bash

PGUSER=postgres
PGPASSWORD=postgres-admin-password

## Store database admin password in Vault KV

vault secrets enable -version=2 kv
vault kv put kv/application
vault kv put kv/vault-static-secrets spring.datasource.username=${PGUSER} spring.datasource.password=${PGPASSWORD}

## Enable database secrets engine
vault secrets enable -path='database' database

vault write database/config/payments \
	plugin_name=postgresql-database-plugin \
	connection_url='postgresql://{{username}}:{{password}}@postgres:5432/payments' \
	allowed_roles="payments-app" \
	username="${PGUSER}" \
	password="${PGPASSWORD}"

vault write database/roles/payments-app \
	db_name=payments \
	creation_statements="CREATE ROLE \"{{name}}\" WITH LOGIN PASSWORD '{{password}}' VALID UNTIL '{{expiration}}'; \
		GRANT ALL PRIVILEGES ON payments TO \"{{name}}\";" \
	default_ttl="1m" \
	max_ttl="2m"