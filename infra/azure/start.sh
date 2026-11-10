#!/usr/bin/env bash
set -euo pipefail

resource_group="${AZURE_RESOURCE_GROUP:-rg-beforeyouvisit}"
cluster_name="${AKS_CLUSTER_NAME:-aks-beforeyouvisit}"

az aks start --resource-group "$resource_group" --name "$cluster_name"
echo "AKS start requested. Wait for the cluster to be running before deploying or port-forwarding."
