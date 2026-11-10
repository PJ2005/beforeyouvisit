# Assignment evidence checklist

Capture screenshots that show the browser address bar, terminal command, or GitHub/Azure page context needed to identify the result. Do not include credentials, tokens, subscription IDs, or other secrets.

## Application and local build

- `mvn clean verify` finishing with `BUILD SUCCESS`.
- BeforeYouVisit homepage showing Registrar, Accounts, and Library.
- An office-detail page showing the required-document checklist and progress.
- `http://localhost:8080/actuator/health` returning `{"status":"UP"}`.

## Docker

- `docker build -t beforeyouvisit:1.0.0 .` completing successfully.
- `docker ps` showing the `beforeyouvisit` container and port `8080` mapping.
- The Docker-served homepage or health endpoint.

## Kubernetes / Minikube

- `minikube image build` completing successfully.
- `kubectl get deployment,pods,service` showing one ready BeforeYouVisit pod and service.
- `kubectl port-forward service/beforeyouvisit 8080:8080` running.
- The port-forwarded health endpoint returning `UP`.

## GitHub Actions and GHCR

- A successful CI/CD workflow run showing build, test, and GHCR publish steps.
- The GHCR package/image details page showing the commit-SHA tag.
- On `main`, the successful AKS deployment job and rollout output.

## AKS

- `kubectl get deployment,pods,service` against AKS after rollout.
- The AKS Service showing an external address when using the LoadBalancer overlay.
- The deployed application's health endpoint response.
