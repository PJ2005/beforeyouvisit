#!/usr/bin/env bash
set -euo pipefail

resource_group="${AZURE_RESOURCE_GROUP:-rg-beforeyouvisit}"

read -r -p "This permanently deletes resource group '${resource_group}' and all project resources. Type the resource group name to continue: " confirmation
if [[ "$confirmation" != "$resource_group" ]]; then
  echo "Deletion cancelled."
  exit 1
fi

az group delete --name "$resource_group" --yes --no-wait
echo "Deletion requested for ${resource_group}. Verify completion with: az group exists --name ${resource_group}"
