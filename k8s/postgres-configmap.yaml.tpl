apiVersion: v1
kind: ConfigMap
metadata:
  name: postgres-config
  namespace: os-management
data:
  POSTGRES_DB: "${db_name}"
  POSTGRES_USER: "${db_user}"
  PGDATA: /var/lib/postgresql/data/pgdata
