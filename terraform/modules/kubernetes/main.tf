resource "kubernetes_namespace" "app" {
  metadata {
    name = var.namespace
  }
}

# ConfigMap for application configuration
resource "kubernetes_config_map" "app_config" {
  metadata {
    name      = "${var.app_name}-config"
    namespace = var.namespace
  }

  data = {
    SPRING_DATASOURCE_URL = "jdbc:postgresql://${var.db_host}:${var.db_port}/${var.db_name}"
    LOG_LEVEL             = "INFO"
    JAVA_OPTS             = "-Xms256m -Xmx512m"
  }

  depends_on = [kubernetes_namespace.app]
}

# Secret for database credentials and JWT
resource "kubernetes_secret" "app_secrets" {
  metadata {
    name      = "${var.app_name}-secret"
    namespace = var.namespace
  }

  type = "Opaque"

  # string_data accepts plaintext; the provider handles base64 encoding
  string_data = {
    SPRING_DATASOURCE_USERNAME = var.db_user
    SPRING_DATASOURCE_PASSWORD = var.db_password
    JWT_SECRET                 = var.jwt_secret
    JWT_EXPIRATION             = tostring(var.jwt_expiration)
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

          env_from {
            secret_ref {
              name = kubernetes_secret.app_secrets.metadata[0].name
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
    kubernetes_secret.app_secrets
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

# Pod Disruption Budget — ensures at least 1 pod stays up during maintenance
resource "kubernetes_pod_disruption_budget_v1" "app" {
  metadata {
    name      = "${var.app_name}-pdb"
    namespace = var.namespace
  }

  spec {
    min_available = 1

    selector {
      match_labels = {
        app = var.app_name
      }
    }
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
