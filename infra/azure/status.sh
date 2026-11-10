#!/usr/bin/env bash
set -euo pipefail

resource_group="${AZURE_RESOURCE_GROUP:-rg-beforeyouvisit}"
cluster_name="${AKS_CLUSTER_NAME:-aks-beforeyouvisit}"

az aks show --resource-group "$resource_group" --name "$cluster_name" \
  --query '{name:name,location:location,provisioningState:provisioningState,powerState:powerState.code,nodeResourceGroup:nodeResourceGroup}' \
  --output table

az aks get-credentials --resource-group "$resource_group" --name "$cluster_name" --overwrite-existing
kubectl get nodes
kubectl get pods --all-namespaces
kubectl get services --all-namespaces

external_ip="$(kubectl get service beforeyouvisit -o jsonpath='{.status.loadBalancer.ingress[0].ip}' 2>/dev/null || true)"
if [[ -n "$external_ip" ]]; then
  echo "Application URL: http://${external_ip}:8080"
else
  echo "No external IP is assigned. Use: kubectl port-forward service/beforeyouvisit 8080:8080"
fi
