# =============================================================================
# Módulo: eks
# Provisiona VPC + cluster EKS + managed node group na AWS.
# Usa os community modules da HashiCorp para VPC e EKS — padrão de mercado.
# =============================================================================

# -----------------------------------------------------------------------------
# VPC — rede isolada para o cluster e banco de dados
# Subnets privadas: nodes EKS e RDS (sem exposição direta à internet)
# Subnets públicas: Load Balancers gerados pelos Services do tipo LoadBalancer
# -----------------------------------------------------------------------------
module "vpc" {
  source  = "terraform-aws-modules/vpc/aws"
  version = "~> 5.0"

  name = "${var.cluster_name}-vpc"
  cidr = var.vpc_cidr

  azs             = var.availability_zones
  private_subnets = var.private_subnets
  public_subnets  = var.public_subnets

  # NAT Gateway: permite que nodes em subnets privadas acessem a internet
  # (para pull de imagens Docker, atualizações, etc.)
  enable_nat_gateway   = true
  single_nat_gateway   = true   # Um NAT para dev/staging; use false (um por AZ) em prod
  enable_dns_hostnames = true
  enable_dns_support   = true

  # Tags obrigatórias para que o EKS descubra as subnets automaticamente
  public_subnet_tags = {
    "kubernetes.io/role/elb"                        = "1"
    "kubernetes.io/cluster/${var.cluster_name}"     = "owned"
  }

  private_subnet_tags = {
    "kubernetes.io/role/internal-elb"               = "1"
    "kubernetes.io/cluster/${var.cluster_name}"     = "owned"
  }

  tags = var.tags
}

# -----------------------------------------------------------------------------
# EKS Cluster
# Managed node group: EC2 gerenciados pela AWS (patching automático, etc.)
# -----------------------------------------------------------------------------
module "eks" {
  source  = "terraform-aws-modules/eks/aws"
  version = "~> 20.0"

  cluster_name    = var.cluster_name
  cluster_version = var.cluster_version

  vpc_id                         = module.vpc.vpc_id
  subnet_ids                     = module.vpc.private_subnet_ids
  cluster_endpoint_public_access = true   # API Server acessível pela internet (necessário para CI/CD)

  # Add-ons gerenciados pela AWS
  cluster_addons = {
    coredns    = { most_recent = true }
    kube-proxy = { most_recent = true }
    vpc-cni    = { most_recent = true }
  }

  # Managed Node Group — instâncias EC2 onde os pods rodam
  eks_managed_node_groups = {
    default = {
      instance_types = [var.node_instance_type]
      min_size       = var.node_min_size
      max_size       = var.node_max_size
      desired_size   = var.node_desired_size

      # Nodes nas subnets privadas — não expostos diretamente
      subnet_ids = module.vpc.private_subnet_ids
    }
  }

  tags = var.tags
}
