# Azure AKS deployment assessment

## Preflight result: blocked, no Azure resources created

Preflight was performed on 2026-11-10 against the active **Azure for Students** subscription `ce7e699c-b870-4538-8a1a-f1eebee2e57e`. The active subscription matched the requested subscription and is enabled. Central India supports AKS managed clusters.

The requested cluster cannot be created under the current quota:

| Requirement | Required | Current Central India quota | Result |
| --- | ---: | ---: | --- |
| Total regional vCPUs | 8 (two 4-vCPU nodes) | 6 | Blocked |
| Standard DASv5 family vCPUs | 8 | 0 | Blocked |
| `Standard_D4as_v5` | 2 nodes | no family quota | Blocked |

No resource group, AKS cluster, public IP, load balancer, budget, service principal, or Azure role assignment was created. The `rg-beforeyouvisit` resource group does not exist.

To proceed with the specified two-node design, request at least **8 total regional vCPUs** and **8 Standard DASv5 family vCPUs** for Central India, then rerun the preflight. SKU capacity is also dynamic and must be checked again immediately before provisioning. Do not use B-series nodes for the AKS system pool.

## 72-hour cost estimate

The Azure Retail Prices API returned a Central India Linux `Standard_D4as_v5` PAYG rate of **$0.111/hour**.

| Item | Basis | 72-hour estimate (USD) |
| --- | --- | ---: |
| Two Linux `Standard_D4as_v5` worker VMs | 2 × $0.111 × 72 hours | $15.98 |
| One Standard IPv4 static public IP | $0.005 × 72 hours | $0.36 |
| OS disks | $0 if ephemeral OS disks are confirmed; otherwise depends on the selected managed-disk SKU | Not verified |
| Standard Load Balancer and outbound/data processing | usage-dependent | Not verified |
| AKS Free management tier | control-plane management tier | $0 management fee |

The partial subtotal is **$16.34**, but it is **not an approval to deploy**: disk support, load-balancer/data charges, and current SKU capacity were not reliably verified, and the required quota is unavailable. This preserves the requested $45 planning ceiling and $30 credit reserve by preventing resource creation.

## Intended architecture after a passing preflight

```text
Browser → AKS LoadBalancer Service → BeforeYouVisit pod (1 replica, port 8080)
                                      └─ H2 in-memory database
GitHub Actions → GHCR image → AKS Deployment update
```

The repository already contains one-replica Deployment, ConfigMap, Service, probes, resource limits, `APP_VERSION`, and non-root security settings. The AKS overlay changes the Service to `LoadBalancer`; Minikube remains `ClusterIP` with port-forwarding.

## Authentication and GitHub Actions

The workflow uses Java **25**, not Java 21, because this project is configured to compile with Java 25. It publishes `ghcr.io/<owner>/<repository>:<commit-sha>` and uses Azure OIDC for `main` deployments.

When Azure preflight passes, configure these GitHub repository secrets:

- `AZURE_CLIENT_ID`
- `AZURE_TENANT_ID`
- `AZURE_SUBSCRIPTION_ID`

Configure these repository variables:

- `AKS_RESOURCE_GROUP=rg-beforeyouvisit`
- `AKS_CLUSTER_NAME=aks-beforeyouvisit`

Create a Microsoft Entra application/service principal and federated credential restricted to `repo:<owner>/<repository>:ref:refs/heads/main`. Grant it the least Azure permissions necessary to obtain AKS user credentials. Kubernetes RBAC must independently allow updating the `beforeyouvisit` Deployment. Do not grant subscription-wide Owner or Contributor. GHCR must be public or AKS must have a namespace image-pull secret; never commit registry credentials.

## Deployment and verification after approval

After quota and cost preflight pass, deploy the AKS overlay and verify the rollout:

```bash
kubectl apply -k k8s/overlays/aks
kubectl rollout status deployment/beforeyouvisit --timeout=180s
kubectl get service beforeyouvisit --watch
```

When the Service receives an external IP:

```bash
curl http://<external-ip>:8080/actuator/health
```

If a public load balancer is rejected by the cost gate, retain `ClusterIP` and use:

```bash
kubectl port-forward service/beforeyouvisit 8080:8080
curl http://localhost:8080/actuator/health
```

## Cost control and cleanup

The cleanup scripts are intentionally not run automatically:

```bash
./infra/azure/status.sh
./infra/azure/stop.sh
./infra/azure/start.sh
./infra/azure/destroy.sh
```

Stopping AKS does not necessarily stop charges for retained networking resources, disks, or public IPs. Delete the entire resource group after the three-day demonstration to stop ongoing project charges. Azure budgets are monitoring alerts, not hard spending caps; a $45 resource-group budget with 50%, 75%, and 90% notifications should be created only after a valid notification recipient can be safely identified.
