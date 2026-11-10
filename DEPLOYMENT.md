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
curl http://<external-ip>:8080/actuator/health
```

Remove the AKS deployment:

```bash
kubectl delete -k k8s/overlays/aks
```
