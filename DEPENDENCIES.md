# OS Management — Deployment Dependency Analysis

> Cross-repository analysis of what must change to deploy the platform (API Gateway,
> Lambda, Database, and Datadog monitoring) to AWS and have it working end to end.
>
> Scope: the 5 public repos in the `TechChallenge-Software-Architetute` org.
> Status legend: ✅ ready · 🟡 partial / needs work · ❌ broken or missing.

---

## 1. Executive summary

The project is **half-migrated** from a monolith (the `os-management` app holding all
Terraform) into per-component repos. The migration is incomplete in ways that block a
clean deploy. Two foundational problems cascade into everything else:

1. **`os-management-k8s-terraform` is empty** (only a README on every branch). The
   VPC + EKS + HPA Terraform still lives *inside* the `os-management` app repo, so there is
   no standalone network/cluster state — yet the lambda and database both assume one exists.
2. **The app Terraform does not validate on `main`.** `main.tf` and `outputs.tf` reference
   `module.rds[0]`, but there is **no `module "rds"` block and no `terraform/modules/rds/`**
   on `main`. That module only exists on the unmerged `feature/datadog` and
   `feature/terraform` branches. `terraform plan` on `main` fails with an undeclared-module error.

Everything else (auth core, gateway, database, Datadog) is individually in good shape but
**wired together inconsistently** (state-bucket names, state keys, environment names, and
which repo owns the network/DB).

---

## 2. Component-by-component findings

### 2.1 Database — `os-management-database` 🟡
Standalone RDS PostgreSQL (`db.t3.micro`, free-tier), security group, subnet group; runs
`ddl.sql`/`dml.sql` via `local-exec psql`. Deploys on push to `main`.

- **State-bucket mismatch:** pipeline **hardcodes** `TF_STATE_BUCKET: tfstate-backend-fiap-soat-giovanni`,
  while lambda/gateway use the repo variable `vars.TF_STATE_BUCKET`. Cross-repo
  `terraform_remote_state` reads only work if **all repos share one bucket**.
- **VPC not sourced from managed state:** needs `vpc_id` + `subnet_ids` via GitHub secrets.
  Nothing creates that VPC in a dedicated repo (see k8s-terraform gap).
- **Output name mismatch:** exposes `aurora_jdbc_url`, but the lambda reads `rds_jdbc_url`
  from the **app** state, not from this repo. Nothing consumes `aurora_jdbc_url`.
- **Security (flag):** `publicly_accessible = true` and SG ingress `0.0.0.0/0:5432`.
  Acceptable only for a throwaway demo; tighten for anything real.

### 2.2 Lambda — `os-management-lambda` 🟡
Auth issuer + JWT authorizer (Java 21), IAM, Secrets Manager, VPC config. Real code +
Terraform only exist on **`feature/cpf-auth-lambda`** (correctly has no API Gateway).
`feature/auth-lambda` still carries a stale `apigw.tf` — do **not** merge that one.
`main`/`develop` are README-only.

- **Nothing is merged**, so the CD (triggers on develop/main) has nothing to deploy yet.
- **DB/network source is broken:** reads the app state for `rds_jdbc_url`,
  `private_subnet_ids`, `node_security_group_id`. The app RDS module is broken on `main`
  and the DB moved to its own repo, so those reads won't resolve.
- Environment naming has been normalized to the branch name (`develop`/`main`) — see §4.

### 2.3 API Gateway — `os-management-gateway` 🟡 (Terraform ✅, CD ❌)
On `develop`: a clean API Gateway — public `POST /auth` → issuer, `TOKEN` authorizer,
`ANY /{proxy+}` → backend, JSON access logs, X-Ray, throttling. Best-built piece.

- **State-key mismatch (hard blocker):** CD sets `TF_VAR_environment: shared` and its
  precheck **hard-fails (`exit 1`) unless `lambda/shared/terraform.tfstate` exists**
  (`data.tf` reads `lambda/${environment}/…`). But the lambda CD writes
  `lambda/develop/…` or `lambda/main/…` — never `shared`. The gateway deploy always fails.
- **`ORIGIN_URL`** must be the app's externally reachable URL on EKS (ingress/LB hostname),
  which only exists once the app is deployed. Gateway is therefore last in the order.

### 2.4 Application — `os-management` 🟡
Spring Boot app + `k8s/` manifests (incl. HPA min 1 / max 6) + Dockerfile. Still owns the
VPC/EKS Terraform.

- `terraform plan` broken on `main` (dangling `module.rds` — see §1).
- Deploys a fixed Docker Hub image; supports both Kind (local) and EKS (`USE_AWS=true`).

### 2.5 Monitoring / Datadog ❌ (implemented, not deployable as-is)
Everything is on the unmerged **`feature/datadog`** branch of the app repo: Micrometer
Datadog registry, JSON structured logs + correlation filter, custom business metrics
(`ServiceOrderMetrics`), kustomize overlays injecting `DD_ENV`/`DD_API_KEY`, plus a
dashboard and 4 monitor JSON files.

- **Merge `feature/datadog`** (note it also restores `module "rds"`, which happens to fix
  the broken app Terraform — reinforcing the need for a clear split decision, §3).
- **No Datadog Agent in the cluster.** Manifests only set `DD_*` env vars and push app
  metrics via Micrometer HTTP (agentless). The dashboard queries `trace.servlet.request`
  (APM traces) and `kubernetes.cpu/memory.*`, which require the **Datadog Agent DaemonSet +
  Cluster Agent** (and `dd-java-agent` for traces). These are absent → half the dashboard has
  no data. **This is the single biggest monitoring gap.**
- **Dashboards/monitors are just JSON files** — no pipeline applies them. Add a step using
  the Datadog Terraform provider or the Datadog API.
- **Provide a real `DD_API_KEY`.**
- **Verify metric prefix:** dashboards query `workshop.*`; confirm the Micrometer export
  prefix matches what `ServiceOrderMetrics` emits, or panels stay empty.

---

## 3. Cross-cutting decisions (make these first — they unblock everything)

1. **One state bucket** for all repos (fix the DB hardcode so it uses `vars.TF_STATE_BUCKET`).
2. **Finish or revert the split.** Pick one:
   - **Path A — fastest to a working demo:** keep `os-management` as the source of
     VPC + EKS + RDS. Merge `feature/datadog` (restores the RDS module), keep the lambda
     pointing at the app state, leave `os-management-database`/`k8s-terraform` as
     documentation. Fewest moving parts.
   - **Path B — matches the rubric (6 repos):** move VPC/EKS/HPA into `k8s-terraform`, keep
     RDS in `os-management-database`, strip Terraform from the app, and repoint the lambda's
     remote state to those two states (rename `aurora_jdbc_url` to what the lambda expects).
     More work but satisfies "separate repositories."
3. **Consistent environment naming** across lambda ↔ gateway (both `develop`/`main`).

---

## 4. Step-by-step changes (recommended order)

The order matters because of `terraform_remote_state` dependencies:
`network/EKS → database → lambda → app (get URL) → gateway → Datadog`.

### Step 0 — Foundations (do once)
- [ ] Create a single S3 bucket for Terraform state (versioning on) and use its name as
      `TF_STATE_BUCKET` in **every** repo. Example:
      `aws s3 mb s3://os-management-tfstate && aws s3api put-bucket-versioning --bucket os-management-tfstate --versioning-configuration Status=Enabled`
- [ ] Decide **Path A or Path B** (§3). The steps below assume **Path A** unless noted.
- [ ] Set the shared AWS credentials / region and per-repo secrets from §5.

### Step 1 — Fix the app Terraform (network + RDS source of truth)
- [ ] Merge `feature/datadog` **or** `feature/terraform` into `develop`/`main` so `module "rds"`
      and `terraform/modules/rds/` exist again → `terraform plan` on `main` passes.
- [ ] Confirm the app pipeline (EKS job) writes state under the **branch-name key**:
      `develop/terraform.tfstate` and `main/terraform.tfstate` (this is what the lambda now reads).
- [ ] Ensure the app exposes the outputs the lambda consumes: `private_subnet_ids`,
      `node_security_group_id`, `rds_jdbc_url`.
- [ ] Deploy with `USE_AWS=true` so EKS + RDS are created and the state is written.

### Step 2 — Database (only if Path B)
- [ ] Convert the hardcoded `TF_STATE_BUCKET` in the pipeline to `vars.TF_STATE_BUCKET`.
- [ ] Feed `VPC_ID` / `SUBNET_IDS` from the network state (or the app's `vpc_id` /
      `private_subnet_ids` outputs).
- [ ] Rename/duplicate the output so the lambda's expected key (`rds_jdbc_url`) is available,
      and repoint the lambda's `terraform_remote_state` at the database state.

### Step 3 — Lambda
- [ ] Merge **`feature/cpf-auth-lambda`** → `develop` → `main` (NOT `feature/auth-lambda`).
- [ ] Verify the CD state key is `lambda/develop/terraform.tfstate` /
      `lambda/main/terraform.tfstate` (already branch-based after the recent change).
- [ ] Push to `develop` → confirm both functions deploy and the state object appears in S3.

### Step 4 — Align the Gateway CD with the lambda (fix the hard blocker)
- [ ] Change the gateway CD so `TF_VAR_environment` follows the branch name (`develop`/`main`)
      instead of the literal `shared`, and update the "validate lambda state" precheck to look
      for `lambda/${branch}/terraform.tfstate`. This makes `data.tf`'s
      `lambda/${var.environment}/terraform.tfstate` line up with what the lambda writes.

### Step 5 — App public URL → Gateway
- [ ] Deploy the app to EKS; get the ingress/LoadBalancer hostname
      (`kubectl get ingress -n os-management` or `kubectl get svc -n os-management`).
- [ ] Set the gateway's `ORIGIN_URL` secret to that URL.
- [ ] Push the gateway on `develop`/`main` → API Gateway deploys; capture `auth_endpoint`.

### Step 6 — Datadog monitoring
- [ ] Merge `feature/datadog`.
- [ ] Add the **Datadog Agent** to the cluster (Helm `datadog/datadog` chart as a DaemonSet +
      Cluster Agent) so `kubernetes.*` metrics and APM traces (`trace.servlet.request`) flow.
      Attach `dd-java-agent` to the app container (JVM `-javaagent`) for traces.
- [ ] Provide `DD_API_KEY` (secret) and set `DD_METRICS_ENABLED=true` / `DD_ENV` per environment.
- [ ] Apply the dashboards/monitors JSON via the Datadog Terraform provider (or a CI step that
      POSTs them to the Datadog API).
- [ ] Verify metric names/prefix (`workshop.*`) match what the app emits.

### Step 7 — Validate end to end
- [ ] `POST {auth_endpoint}` with a valid CPF → receive a JWT.
- [ ] Call a protected route through the gateway with the JWT → reaches the app on EKS.
- [ ] Confirm the Datadog dashboard shows order volume, average time per status, and errors.

---

## 5. Environment variables & secrets by repo (and how to get them)

> GitHub → repo → **Settings → Secrets and variables → Actions**. "Secret" = encrypted;
> "Variable" = plain (non-sensitive). `TF_STATE_BUCKET` and `AWS_REGION` are **variables**;
> everything credential-like is a **secret**. Keep `JWT_SECRET` **identical** in the app and
> the lambda.

### 5.0 Configuration matrix

Two deploy targets, keyed by branch: `develop` (dev account) and `main` (prod account).
Deploy jobs declare `environment: ${{ github.ref_name }}`.

**Org-level — set once for the whole org, not per repo.** These four live as **organization
secrets** with `_MAIN` and `_DEVELOP` suffixes; workflows pick the right one by branch
(`main` → `_MAIN`, `develop` → `_DEVELOP`) via an inline expression, e.g.
`${{ github.ref_name == 'main' && secrets.TF_STATE_BUCKET_MAIN || secrets.TF_STATE_BUCKET_DEVELOP }}`.
Grant the org secret visibility to all five repos (Org → Settings → Secrets and
variables → Actions → *value* → Repository access).

| Name (org, both suffixes) | Type | Purpose |
|---|---|---|
| `AWS_ACCESS_KEY_ID_MAIN` / `_DEVELOP` | secret | AWS auth per account |
| `AWS_SECRET_ACCESS_KEY_MAIN` / `_DEVELOP` | secret | AWS auth per account |
| `AWS_ACCOUNT_ID_MAIN` / `_DEVELOP` | secret | feeds `allowed_account_ids` guard |
| `TF_STATE_BUCKET_MAIN` / `_DEVELOP` | secret | per-account S3 state bucket |

**Per-repo, per-environment — GitHub Environments `develop` / `main`.** These differ per
account but are repo-specific, so set them in each repo's `develop` and `main` environments
under the same name. `AWS_REGION` is a normal repo/environment variable (not suffixed).
✓ = configure it in that repo (in **both** environments).

| Name | Type | k8s-terraform | database | lambda | gateway | app |
|---|---|:--:|:--:|:--:|:--:|:--:|
| `AWS_REGION` | variable | ✓ | ✓ | ✓ | ✓ | ✓ |
| `DB_PASSWORD` | secret | | ✓ | ✓ | | ✓ |
| `DB_USERNAME` / `DB_USER`\* | secret | | ✓ | ✓ | | ✓ |
| `JWT_SECRET` | secret | | | ✓ | | ✓ |
| `ORIGIN_URL` | secret | | | | ✓ | |
| `DOCKER_USERNAME`, `DOCKER_HUB_TOKEN` | secret | | | | | ✓ |
| `DB_URL` | secret | | | | | ✓ |
| `AWS_SNS_TOPIC_ARN` | secret | | | | | ✓ |

- The `AWS_ACCOUNT_ID_*` value feeds the provider guard `allowed_account_ids = [var.aws_account_id]`,
  which fails a deploy fast if the active credentials point at the wrong account.
- Each account gets **its own** S3 state bucket (versioning on); no cross-account bucket policy
  is needed because every `terraform_remote_state` read stays within one environment/account,
  and state keys are branch-scoped (`k8s/develop`, `database/develop`, `lambda/develop`,
  `gateway/develop`, and the `main` equivalents).
- Recommended: add **required reviewers** on the `main` environment for a prod approval gate.
- \* **Name mismatch to fix:** the database repo currently calls it `DB_USERNAME` while the
  lambda/app use `DB_USER`. They must hold the **same value**; the names intentionally stay
  different (`DB_USERNAME` in the database, `DB_USER` in the lambda/app).
- `k8s-terraform` only needs `AWS_REGION` here (plus the org-level four) — no DB/JWT/Docker values.

### 5.0.1 Manually-synced Terraform outputs (temporary)

> **Note (Path B, current state):** we decided **not** to wire the downstream repos to
> `k8s-terraform` / `database` via `terraform_remote_state`. Instead, every repo takes its
> infrastructure inputs as **explicit secrets** ("deployment-independent" — no cross-repo state
> reads, no deploy-order coupling). The trade-off is that a few Terraform **outputs must be
> copied by hand into secrets** after the upstream stack is applied. Until we (optionally)
> switch to remote state, keep these in sync **manually** whenever the network or database is
> recreated:

| Source Terraform output | Copy into (secret, per env `_MAIN`/`_DEVELOP`) | Consumed by |
|---|---|---|
| `k8s-terraform` → `vpc_id` | `VPC_ID_MAIN` / `VPC_ID_DEVELOP` | database |
| `k8s-terraform` → `private_subnet_ids` | `SUBNET_IDS_MAIN` / `SUBNET_IDS_DEVELOP` | database + lambda |
| `k8s-terraform` → `node_security_group_id` | `VPC_SECURITY_GROUP_IDS_MAIN` / `_DEVELOP` | lambda |
| `database` → `aurora_jdbc_url` | `DB_URL_MAIN` / `DB_URL_DEVELOP` | lambda (and app) |
| `k8s-terraform` → `cluster_name` (or derive `os-management-<env>`) | app deploy `aws eks update-kubeconfig` (Part 3, TBD) | app |

- **Why manual:** the database and lambda were built deployment-independent by the team, so they
  read these values from secrets rather than `terraform_remote_state`. `k8s-terraform` still
  *produces* the outputs; they're just not auto-consumed.
- **Risk:** if `k8s-terraform` recreates the VPC/subnets (new IDs) and the secrets aren't
  refreshed, RDS and the Lambda land in a stale/mismatched VPC and connectivity breaks. Treat
  "re-apply k8s-terraform" and "refresh these secrets" as a single operation.
- **Order:** apply `k8s-terraform` → copy its outputs into the secrets above → apply `database`
  → copy `aurora_jdbc_url` into `DB_URL_*` → apply `lambda`.

### 5.1 Shared across all IaC repos
| Name | Type | Purpose | How to get it |
|---|---|---|---|
| `AWS_ACCESS_KEY_ID` | secret | AWS auth for Terraform/deploy | IAM → Users → *user* → Security credentials → **Create access key** (prefer an OIDC role for real use) |
| `AWS_SECRET_ACCESS_KEY` | secret | pairs with the key above | shown once at access-key creation |
| `TF_STATE_BUCKET` | variable | S3 bucket holding all TF state | `aws s3 mb s3://<name>` — **same value in every repo** |
| `AWS_REGION` | variable | region (e.g. `us-east-1`) | your chosen region |

### 5.2 `os-management-lambda`
| Name | Type | Purpose | How to get it |
|---|---|---|---|
| `DB_USER` | secret | RDS username the issuer uses | same value used by the database repo (`dbadmin`, not a reserved name) |
| `DB_PASSWORD` | secret | RDS password | your chosen strong password |
| `JWT_SECRET` | secret | HS256 sign/verify (shared) | `openssl rand -base64 32` — **must match the app's value** |
| `AWS_ACCESS_KEY_ID` / `AWS_SECRET_ACCESS_KEY` | secret | AWS auth | §5.1 |
| `TF_STATE_BUCKET` / `AWS_REGION` | variable | §5.1 | §5.1 |

### 5.3 `os-management-gateway`
| Name | Type | Purpose | How to get it |
|---|---|---|---|
| `ORIGIN_URL` | secret | backend app URL on EKS | `kubectl get ingress -n os-management` (or `get svc`) after the app is deployed; e.g. `https://<lb-hostname>` |
| `AWS_ACCESS_KEY_ID` / `AWS_SECRET_ACCESS_KEY` | secret | AWS auth | §5.1 |
| `TF_STATE_BUCKET` / `AWS_REGION` | variable | §5.1 | §5.1 |

### 5.4 `os-management-database`
| Name | Type | Purpose | How to get it |
|---|---|---|---|
| `VPC_ID` | secret | VPC for the RDS SG/subnet group | network/EKS output `vpc_id`, or AWS Console → VPC |
| `SUBNET_IDS` | secret | private subnets (JSON array string) | e.g. `["subnet-aaa","subnet-bbb"]` from `private_subnet_ids` output |
| `DB_USERNAME` | secret | RDS master user | choose a non-reserved name (`dbadmin`); must equal the lambda/app `DB_USER` |
| `DB_PASSWORD` | secret | RDS master password | your chosen strong password |
| `AWS_ACCESS_KEY_ID` / `AWS_SECRET_ACCESS_KEY` | secret | AWS auth | §5.1 |
| `TF_STATE_BUCKET` / `AWS_REGION` | variable | §5.1 (convert the hardcoded bucket to this) | §5.1 |

### 5.5 `os-management` (app)
| Name | Type | Purpose | How to get it |
|---|---|---|---|
| `DB_URL` | secret | JDBC URL (EKS mode) | database output `aurora_jdbc_url` after RDS exists, e.g. `jdbc:postgresql://<host>:5432/workshop` |
| `DB_USER` / `DB_PASSWORD` | secret | DB credentials | same as the database repo |
| `JWT_SECRET` | secret | HS256 (shared) | same value as the lambda |
| `DOCKER_USERNAME` | secret | Docker Hub push | your Docker Hub username |
| `DOCKER_HUB_TOKEN` | secret | Docker Hub push token | Docker Hub → Account Settings → Security → **New Access Token** |
| `AWS_ACCESS_KEY_ID` / `AWS_SECRET_ACCESS_KEY` | secret | AWS auth (EKS mode) | §5.1 |
| `AWS_SNS_TOPIC_ARN` | secret | order notifications (SNS/SES) | `aws sns create-topic --name os-management` → copy the `TopicArn` |
| `AWS_VPC_ID` | secret | existing-resource import in the EKS job | same VPC id as the database repo |
| `USE_AWS` | variable | `true` = EKS + RDS, `false` = Kind (local) | set `true` for AWS deploys |
| `TF_STATE_BUCKET` / `AWS_REGION` | variable | §5.1 | §5.1 |
| `KUBE_CONFIG` | secret* | base64 kubeconfig (only if TF manages GH secrets / kubectl) | `aws eks update-kubeconfig --name os-management && base64 -i ~/.kube/config` |
| `github_token` (TF var) | secret* | lets the app TF create the repo's GH secrets | GitHub → Settings → Developer settings → **Personal access token** (scopes: `repo`, `secrets`) |

\* Optional — only needed if you use the app's `modules/github` to auto-populate GitHub
secrets, or the Terraform-driven kubectl apply.

### 5.6 Datadog (app runtime, on `feature/datadog`)
| Name | Type | Purpose | How to get it |
|---|---|---|---|
| `DD_API_KEY` | secret | authenticate metrics/agent to Datadog | Datadog → **Organization Settings → API Keys → New Key** |
| `DD_METRICS_ENABLED` | var/env | toggle Micrometer export | `true` to enable |
| `DD_ENV` | var/env | environment tag (`develop`/`main`) | set per environment/overlay |
| `DD_METRICS_URI` | var/env | Datadog intake URL | default `https://api.datadoghq.com` (use the EU URL if your org is EU) |
| `datadog_api_key` / `datadog_metrics_enabled` / `datadog_environment` (TF vars) | — | wire the above through Terraform | mirror the values above |

---

## 6. Repository quick-reference

| Repo | Owns | Deploy trigger | State key (recommended) |
|---|---|---|---|
| `os-management` (app) | Spring app, k8s manifests, (currently) VPC/EKS/RDS TF | push `develop`/`main` | `develop/…`, `main/…` |
| `os-management-lambda` | auth issuer + JWT authorizer, IAM, Secrets | push `develop`/`main` | `lambda/develop/…`, `lambda/main/…` |
| `os-management-gateway` | API Gateway (auth + proxy) | push `develop`/`main` | `gateway/develop/…`, `gateway/main/…` |
| `os-management-database` | RDS PostgreSQL + DDL/DML | push `main` | `os-management-database/…` |
| `os-management-k8s-terraform` | (intended) VPC + EKS + HPA — **currently empty** | — | `k8s/…` |

---

*Generated from a read-only review of the repositories. No infrastructure was modified.*
