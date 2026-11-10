#!/usr/bin/env bash
set -euo pipefail

resource_group="${AZURE_RESOURCE_GROUP:-rg-beforeyouvisit}"
cluster_name="${AKS_CLUSTER_NAME:-aks-beforeyouvisit}"

az aks stop --resource-group "$resource_group" --name "$cluster_name"
echo "AKS stop requested. Managed disks, public IPs, and other retained resources can still incur charges."
