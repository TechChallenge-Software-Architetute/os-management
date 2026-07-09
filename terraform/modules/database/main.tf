# =============================================================================
# Módulo: database
# Provisiona um PostgreSQL 16 dentro do cluster Kubernetes via StatefulSet.
# Adequado para ambiente local (Kind, Minikube, Docker Desktop) e dev/staging.
# Para produção, substitua por um serviço gerenciado (RDS, Cloud SQL, etc.).
# =============================================================================

# Secret com credenciais do banco
resource "kubernetes_secret" "postgres_credentials" {
  metadata {
    name      = "postgres-secret"
    namespace = var.namespace
  }

  type = "Opaque"

  string_data = {
    POSTGRES_DB       = var.db_name
    POSTGRES_USER     = var.db_user
    POSTGRES_PASSWORD = var.db_password
  }
}

# PersistentVolumeClaim para dados do PostgreSQL
resource "kubernetes_persistent_volume_claim" "postgres_pvc" {
  metadata {
    name      = "postgres-pvc"
    namespace = var.namespace
  }

  spec {
    access_modes       = ["ReadWriteOnce"]
    storage_class_name = var.storage_class

    resources {
      requests = {
        storage = var.storage_size
      }
    }
  }
}

# StatefulSet do PostgreSQL
resource "kubernetes_stateful_set" "postgres" {
  metadata {
    name      = "postgres"
    namespace = var.namespace
    labels = {
      app       = "postgres"
      component = "database"
    }
  }

  spec {
    service_name = "postgres"
    replicas     = 1

    selector {
      match_labels = {
        app = "postgres"
      }
    }

    template {
      metadata {
        labels = {
          app       = "postgres"
          component = "database"
        }
      }

      spec {
        container {
          name  = "postgres"
          image = var.postgres_image

          port {
            name           = "postgres"
            container_port = 5432
            protocol       = "TCP"
          }

          # Credenciais via Secret
          env_from {
            secret_ref {
              name = kubernetes_secret.postgres_credentials.metadata[0].name
            }
          }

          resources {
            requests = {
              cpu    = var.cpu_request
              memory = var.memory_request
            }
            limits = {
              cpu    = var.cpu_limit
              memory = var.memory_limit
            }
          }

          volume_mount {
            name       = "postgres-data"
            mount_path = "/var/lib/postgresql/data"
            sub_path   = "pgdata"
          }

          # Liveness: verifica se o PostgreSQL está aceitando conexões
          liveness_probe {
            exec {
              command = ["pg_isready", "-U", var.db_user, "-d", var.db_name]
            }
            initial_delay_seconds = 30
            period_seconds        = 10
            timeout_seconds       = 5
            failure_threshold     = 3
          }

          # Readiness: verifica se o banco está pronto para receber queries
          readiness_probe {
            exec {
              command = ["pg_isready", "-U", var.db_user, "-d", var.db_name]
            }
            initial_delay_seconds = 5
            period_seconds        = 5
            timeout_seconds       = 3
            failure_threshold     = 3
          }
        }

        volume {
          name = "postgres-data"
          persistent_volume_claim {
            claim_name = kubernetes_persistent_volume_claim.postgres_pvc.metadata[0].name
          }
        }
      }
    }
  }

  depends_on = [
    kubernetes_secret.postgres_credentials,
    kubernetes_persistent_volume_claim.postgres_pvc
  ]
}

# Service ClusterIP — expõe o PostgreSQL internamente no cluster
resource "kubernetes_service" "postgres" {
  metadata {
    name      = "postgres"
    namespace = var.namespace
    labels = {
      app       = "postgres"
      component = "database"
    }
  }

  spec {
    selector = {
      app = "postgres"
    }

    port {
      name        = "postgres"
      port        = 5432
      target_port = 5432
      protocol    = "TCP"
    }

    # ClusterIP: acessível somente dentro do cluster (correto para bancos de dados)
    type = "ClusterIP"
  }

  depends_on = [kubernetes_stateful_set.postgres]
}
