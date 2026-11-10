# Azure AKS deployment

## Live deployment

BeforeYouVisit is deployed to AKS in Central India.

| Resource | Value |
| --- | --- |
| Resource group | `rg-beforeyouvisit` |
| AKS cluster | `aks-beforeyouvisit` |
| Managed node resource group | `MC_rg-beforeyouvisit_aks-beforeyouvisit_centralindia` |
| AKS management tier | Free |
| Node pool | 2 × `Standard_D2as_v4` Linux nodes |
| Application replicas | 1 |
| Service | Standard LoadBalancer, public port 80 → application port 8080 |
| Application URL | `http://40.81.254.115` |
| Health URL | `http://40.81.254.115/actuator/health` |

The original two-node `Standard_D4as_v5` design required eight vCPUs and could not fit the subscription's six-vCPU regional quota. The deployed non-B-series alternative uses two 2-vCPU D2as v4 nodes, consuming four regional vCPUs and the available four DASv4-family vCPUs. The node pool uses 30 GiB ephemeral OS disks, so no managed OS disks are retained for the node pool.

## Architecture

```text
Browser → AKS LoadBalancer Service → BeforeYouVisit pod (1 replica, port 8080)
                                      └─ H2 in-memory database
GitHub Actions → public GHCR image → AKS Deployment update
```

The Kubernetes Deployment has readiness and liveness probes, CPU/memory requests and limits, `APP_VERSION`, and non-root pod and container security settings. H2 remains in memory and has no persistent disk.

## Cost planning

The current Central India Linux PAYG rate returned by the Azure Retail Prices API for `Standard_D2as_v4` was $0.0622/hour.

| Item | Basis | 72-hour estimate (USD) |
| --- | --- | ---: |
| Two D2as v4 Linux nodes | 2 × $0.0622 × 72 hours | $8.96 |
| One Standard IPv4 static public IP | $0.005 × 72 hours | $0.36 |
| Ephemeral OS disks | node pool configuration | $0.00 |
| AKS Free management tier | control-plane management tier | $0.00 management fee |
| Load Balancer/data transfer | usage-dependent | additional variable charge |

Known fixed-cost subtotal: **$9.32** for 72 hours, plus load-balancer/data-transfer usage. This is below the $45 planning ceiling, but prices and usage can change.

Azure Budget creation was attempted for a $45 resource-group budget with 50%, 75%, and 90% actual-spend alerts. Azure rejected the request because Azure for Students offer `MS-AZR-0170P` does not support Cost Management budgets. No budget or notifications exist. Check Cost Management manually during the demonstration; a budget is monitoring only and never a hard spending cap.

## GitHub Actions and OIDC

The workflow at `.github/workflows/ci-cd.yml` runs on every push to `main`:

1. Uses Java 25 and runs `mvn clean verify`.
2. Builds and publishes `ghcr.io/pj2005/beforeyouvisit:<commit-sha>`.
3. Authenticates to Azure with GitHub OIDC—no client secret is stored.
4. Installs `kubelogin`, connects to the Entra-enabled AKS cluster, applies the AKS overlay, sets the SHA-tagged image, waits for rollout, and verifies health.

The GitHub repository contains `AZURE_CLIENT_ID`, `AZURE_TENANT_ID`, and `AZURE_SUBSCRIPTION_ID` secrets plus `AKS_RESOURCE_GROUP` and `AKS_CLUSTER_NAME` variables. The federated identity is restricted to the `main` branch and has only AKS Cluster User plus AKS RBAC Writer roles at the AKS cluster scope. The public GHCR package avoids an image-pull secret.

## Verify and operate

```bash
curl http://40.81.254.115/actuator/health
./infra/azure/status.sh
./infra/azure/stop.sh
./infra/azure/start.sh
```

Stopping AKS does not necessarily stop charges for retained networking resources or public IPs. Delete the resource group after the three-day demonstration to stop project charges:

```bash
./infra/azure/destroy.sh
```

The destroy script requires typing the resource-group name and is never invoked automatically.
