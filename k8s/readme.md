# Kubernetes Manifests for OS Management

This directory contains all Kubernetes YAML manifests for deploying the OS Management application.

## Files Overview

| File | Purpose |
|------|---------|
| `namespace.yaml` | Creates the `os-management` namespace |
| `configmap.yaml` | Application configuration (non-sensitive) |
| `secret.yaml` | Database credentials (sensitive data) |
| `service-account.yaml` | ServiceAccount for RBAC |
| `deployment.yaml` | Main application deployment |
| `service.yaml` | Kubernetes Service (LoadBalancer) |
| `hpa.yaml` | Horizontal Pod Autoscaler |
| `pdb.yaml` | Pod Disruption Budget for high availability |
| `network-policy.yaml` | Network policies for security |

## Deployment Order

Deploy the manifests in this order:

```bash
# 1. Create namespace
kubectl apply -f namespace.yaml

# 2. Create configuration and secrets
kubectl apply -f configmap.yaml
kubectl apply -f secret.yaml

# 3. Create service account
kubectl apply -f service-account.yaml

# 4. Create the application
kubectl apply -f deployment.yaml
kubectl apply -f service.yaml

# 5. Set up autoscaling and policies
kubectl apply -f hpa.yaml
kubectl apply -f pdb.yaml
kubectl apply -f network-policy.yaml