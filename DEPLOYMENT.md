# BeforeYouVisit deployment

## Docker

Build the image from the repository root:

```bash
docker build -t beforeyouvisit:1.0.0 .
```

Run it locally, injecting the homepage version:

```bash
docker run --rm --name beforeyouvisit -p 8080:8080 -e APP_VERSION=1.0.0 beforeyouvisit:1.0.0
```

Verify it in another terminal:

```bash
curl http://localhost:8080/actuator/health
```

Stop a foreground run with `Ctrl+C`. For a detached container, remove it with:

```bash
docker rm -f beforeyouvisit
```

## Minikube

Start Minikube if needed, then build directly into its image store so no registry is required:

```bash
minikube start
minikube image build -t beforeyouvisit:1.0.0 .
kubectl apply -k k8s/overlays/minikube
kubectl rollout status deployment/beforeyouvisit
```

Access the service through a local port-forward:

```bash
kubectl port-forward service/beforeyouvisit 8080:8080
```

Then open `http://localhost:8080` or run:

```bash
curl http://localhost:8080/actuator/health
```

Remove the Minikube deployment:

```bash
kubectl delete -k k8s/overlays/minikube
```

## Azure AKS

AKS nodes need a registry-accessible image (a local Docker image cannot be pulled by AKS). Set the three shell variables, grant the cluster access to the registry, build in ACR, and deploy the LoadBalancer overlay:

```bash
ACR_NAME=<registry-name>
AKS_RESOURCE_GROUP=<aks-resource-group>
AKS_NAME=<aks-name>
az aks update --resource-group "$AKS_RESOURCE_GROUP" --name "$AKS_NAME" --attach-acr "$ACR_NAME"
az acr build --registry "$ACR_NAME" --image beforeyouvisit:1.0.0 .
ACR_LOGIN_SERVER=$(az acr show --name "$ACR_NAME" --query loginServer --output tsv)
kubectl kustomize k8s/overlays/aks | sed "s|image: beforeyouvisit:1.0.0|image: ${ACR_LOGIN_SERVER}/beforeyouvisit:1.0.0|" | kubectl apply -f -
kubectl rollout status deployment/beforeyouvisit
kubectl get service beforeyouvisit --watch
```

The AKS overlay changes the Service to `LoadBalancer`. When `EXTERNAL-IP` is assigned, access:

```bash
curl http://<external-ip>/actuator/health
```

### Persistent data and crash-recovery demo

The app's local default database is in memory. In AKS, the Deployment overrides it with an H2 file database on a 128 MiB persistent volume claim. Create the claim first:

```bash
kubectl apply -f k8s/base/pvc.yaml
```

The default AKS disk class uses `WaitForFirstConsumer`, so the claim becomes `Bound` only after a pod using it is scheduled. Apply the updated Deployment while keeping the image reference already running in AKS. This avoids replacing it with the local placeholder image in the overlay. Run these PowerShell commands from the repository root:

```powershell
$image = kubectl get deployment beforeyouvisit -o jsonpath="{.spec.template.spec.containers[0].image}"
kubectl kustomize .\k8s\overlays\aks | ForEach-Object { $_ -replace 'image: beforeyouvisit:1.0.0', "image: $image" } | kubectl apply -f -
kubectl rollout status deployment/beforeyouvisit
kubectl wait --for=jsonpath='{.status.phase}'=Bound pvc/beforeyouvisit-data --timeout=5m
kubectl get pvc beforeyouvisit-data
```

No image rebuild is needed for this Kubernetes-only change. The Deployment intentionally stays at one replica: this app uses an embedded H2 database, which must not be concurrently opened by multiple application pods.

To demonstrate persistence:

1. Open the app and visit an office details page.
2. Submit a report with a recognizable marker in the details, for example `PVC-DEMO-2026-11-12`. The report appears under **Submitted reports**.
3. In PowerShell, get the current pod name and delete that pod. The Deployment will create a replacement:

   ```powershell
   $pod = kubectl get pods -l app.kubernetes.io/name=beforeyouvisit -o jsonpath="{.items[0].metadata.name}"
   kubectl delete pod $pod
   kubectl get pods -l app.kubernetes.io/name=beforeyouvisit --watch
   ```

4. Wait for the replacement pod to become `Running` and `Ready`, then refresh the same office page. The report with the marker should still be listed. Confirm the storage claim remains `Bound` with `kubectl get pvc beforeyouvisit-data`.

The PVC requests 128 MiB, which is enough for the demo database. Azure's selected storage class may provision a larger minimum backing disk, so verify the actual Azure disk size and billing if this change is intended to reduce cost. A bound 1 GiB PVC cannot be shrunk in place; if one already exists, do not delete it unless you have backed up any data you need.

The PVC is deliberately kept outside the Kustomize resources, so `kubectl delete -k k8s/overlays/aks` removes the app but not its data. Delete the PVC only when you intend to permanently remove the saved database:

```bash
kubectl delete pvc beforeyouvisit-data
```

The first rollout creates a new database on the PVC; seeded office listings are recreated automatically. Existing records in the old in-memory database (such as submitted reports) cannot be carried over by this configuration change.

Remove the AKS deployment:

```bash
kubectl delete -k k8s/overlays/aks
```
