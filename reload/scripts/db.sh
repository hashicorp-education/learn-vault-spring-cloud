#!/bin/sh

PGPASSWORD=${POSTGRES_PASSWORD} psql -h postgres -U ${POSTGRES_USER} -f /scripts/schema.sql ${POSTGRES_DB}