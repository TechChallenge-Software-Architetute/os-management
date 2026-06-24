resource "kubernetes_namespace" "app" {
  metadata {
    name = var.namespace
  }

  depends_on = [kubernetes_namespace.app]
}

# ConfigMap for application configuration
resource "kubernetes_config_map" "app_config" {
  metadata {
    name      = "${var.app_name}-config"
    namespace = var.namespace
  }

  data = {
    DB_HOST = var.db_host
    DB_PORT = var.db_port
    DB_NAME = var.db_name
  }

  depends_on = [kubernetes_namespace.app]
}

# Secret for database credentials
resource "kubernetes_secret" "db_credentials" {
  metadata {
    name      = "${var.app_name}-db-secret"
    namespace = var.namespace
  }

  type = "Opaque"

  data = {
    DB_USER     = base64encode(var.db_user)
    DB_PASSWORD = base64encode(var.db_password)
  }

  depends_on = [kubernetes_namespace.app]
}

# Deployment
resource "kubernetes_deployment" "app" {
  metadata {
    name      = var.app_name
    namespace = var.namespace
    labels = {
      app = var.app_name
    }
  }

  spec {
    replicas = var.replicas

    selector {
      match_labels = {
        app = var.app_name
      }
    }

    template {
      metadata {
        labels = {
          app = var.app_name
        }
      }

      spec {
        container {
          image = "${var.docker_image}:${var.docker_image_tag}"
          name  = var.app_name
          port {
            container_port = var.container_port
          }

          env_from {
            config_map_ref {
              name = kubernetes_config_map.app_config.metadata[0].name
            }
          }

          env {
            name = "DB_USER"
            value_from {
              secret_key_ref {
                name = kubernetes_secret.db_credentials.metadata[0].name
                key  = "DB_USER"
              }
            }
          }

          env {
            name = "DB_PASSWORD"
            value_from {
              secret_key_ref {
                name = kubernetes_secret.db_credentials.metadata[0].name
                key  = "DB_PASSWORD"
              }
            }
          }

          resources {
            requests = {
              cpu    = "100m"
              memory = "256Mi"
            }
            limits = {
              cpu    = "500m"
              memory = "512Mi"
            }
          }

          liveness_probe {
            http_get {
              path = "/actuator/health"
              port = var.container_port
            }
            initial_delay_seconds = 30
            period_seconds        = 10
          }

          readiness_probe {
            http_get {
              path = "/actuator/health"
              port = var.container_port
            }
            initial_delay_seconds = 5
            period_seconds        = 5
          }
        }
      }
    }
  }

  depends_on = [
    kubernetes_config_map.app_config,
    kubernetes_secret.db_credentials
  ]
}

# Service
resource "kubernetes_service" "app" {
  metadata {
    name      = "${var.app_name}-service"
    namespace = var.namespace
    labels = {
      app = var.app_name
    }
  }

  spec {
    selector = {
      app = var.app_name
    }

    port {
      port        = 80
      target_port = var.container_port
      protocol    = "TCP"
    }

    type = var.service_type
  }

  depends_on = [kubernetes_deployment.app]
}

# Horizontal Pod Autoscaler
resource "kubernetes_horizontal_pod_autoscaler" "app" {
  metadata {
    name      = "${var.app_name}-hpa"
    namespace = var.namespace
  }

  spec {
    scale_target_ref {
      api_version = "apps/v1"
      kind        = "Deployment"
      name        = kubernetes_deployment.app.metadata[0].name
    }

    min_replicas = var.replicas
    max_replicas = var.replicas * 3

    target_cpu_utilization_percentage = 70
  }

  depends_on = [kubernetes_deployment.app]
}
