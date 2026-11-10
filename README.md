# BeforeYouVisit

BeforeYouVisit helps visitors prepare for administrative-office visits with office information, document checklists, visit steps, and an issue-report form.

## Local application startup

The application uses Java 25 and Maven. Start it directly with:

```bash
APP_VERSION=local mvn spring-boot:run
```

Open `http://localhost:8080`. Health is available at `http://localhost:8080/actuator/health`.

## Maven

```bash
mvn clean verify
mvn clean package
java -jar target/before-you-visit-1.0.0.jar
```

## Docker

Build and run the Java 25 image:

```bash
docker build -t beforeyouvisit:1.0.0 .
docker run --rm --name beforeyouvisit --memory=512m -p 8080:8080 -e APP_VERSION=1.0.0 beforeyouvisit:1.0.0
```

Verify it:

```bash
curl http://localhost:8080/actuator/health
```

Stop a detached run with `docker rm -f beforeyouvisit`.

## Minikube

Build the image inside Minikube, deploy the local-image overlay, then access it through port forwarding:

```bash
minikube start
minikube image build -t beforeyouvisit:1.0.0 .
kubectl apply -k k8s/overlays/minikube
kubectl rollout status deployment/beforeyouvisit
kubectl port-forward service/beforeyouvisit 8080:8080
```

Remove it with:

```bash
kubectl delete -k k8s/overlays/minikube
```

The optional Ansible equivalent uses the active `kubectl` context:

```bash
ansible-playbook ansible/deploy.yml -e deployment_target=minikube
```

## GitHub Actions CI/CD

Every push runs `.github/workflows/ci-cd.yml`. It checks out the source, uses Java 25, executes `mvn clean verify`, and builds and publishes a Docker image to GHCR with the full commit SHA as its tag:

```
ghcr.io/pj2005/beforeyouvisit:<commit-sha>
```

Only a push to `main`, after the build-and-push job succeeds, runs the AKS deployment job. That job authenticates to Azure using OIDC, retrieves AKS credentials, updates the `beforeyouvisit` Deployment image, waits for the rollout, and verifies `/actuator/health` through a temporary port-forward.

## Required Azure configuration

No Azure resources are created by this repository. Before enabling deployment:

1. Create or select an AKS cluster and grant the deployment identity permission to retrieve cluster credentials and update workloads in the target namespace.
2. Create a Microsoft Entra application/service principal with a federated identity credential for this GitHub repository's `main` branch. OIDC avoids storing an Azure client secret.
3. Configure AKS to pull the GHCR image. The simplest assignment setup is a public GHCR package; for a private package, create an image-pull secret in the target namespace and reference it from the Deployment.
4. Create GitHub Actions repository variables:
   - `AKS_RESOURCE_GROUP`
   - `AKS_CLUSTER_NAME`

Create these GitHub Actions repository secrets for Azure OIDC authentication:

- `AZURE_CLIENT_ID`
- `AZURE_TENANT_ID`
- `AZURE_SUBSCRIPTION_ID`

`GITHUB_TOKEN` is supplied automatically by GitHub Actions and is used for GHCR publishing; the workflow grants it `packages: write` permission.

## Azure deployment commands

For a manual AKS deploy, authenticate to Azure, get the cluster context, and update the image built by GitHub Actions:

```bash
az login
az aks get-credentials --resource-group <resource-group> --name <aks-cluster-name>
kubectl set image deployment/beforeyouvisit beforeyouvisit=ghcr.io/pj2005/beforeyouvisit:<commit-sha>
kubectl rollout status deployment/beforeyouvisit --timeout=180s
kubectl port-forward service/beforeyouvisit 8080:8080
```

The AKS Service must already be deployed from `k8s/overlays/aks`:

```bash
kubectl apply -k k8s/overlays/aks
```

See [DEPLOYMENT.md](DEPLOYMENT.md) for additional Docker, Minikube, and Azure deployment commands.

## Azure AKS cost gate

BeforeYouVisit is deployed to the quota-compatible Central India AKS configuration: two `Standard_D2as_v4` nodes, one application replica, public LoadBalancer service, and a public GHCR image. The application is available at `http://40.81.254.115` and the health endpoint is `http://40.81.254.115/actuator/health`.

The known 72-hour subtotal is approximately $9.32 before variable Load Balancer/data-transfer usage. Azure for Students does not support the attempted Cost Management budget, so monitor the subscription manually and run `./infra/azure/destroy.sh` after the three-day demonstration. See [the Azure deployment guide](docs/azure-deployment.md) for the architecture, CI/CD OIDC configuration, verification, and safe start/stop/destroy scripts.
