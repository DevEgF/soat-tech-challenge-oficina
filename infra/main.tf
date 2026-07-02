# Provisiona um cluster Kubernetes LOCAL via kind e, por padrão, sobe os workloads.
# O banco de dados (PostgreSQL) é provisionado como workload dentro do cluster,
# pelos manifestos em ../k8s (ver var.bootstrap_workloads, default = true).

resource "kind_cluster" "oficina" {
  name            = var.cluster_name
  node_image      = var.node_image
  kubeconfig_path = pathexpand(var.kubeconfig_path)
  wait_for_ready  = true

  kind_config {
    kind        = "Cluster"
    api_version = "kind.x-k8s.io/v1alpha4"

    node {
      role = "control-plane"

      # Expõe o NodePort 30080 (Service da app) na porta do host.
      extra_port_mappings {
        container_port = 30080
        host_port      = var.host_http_port
        protocol       = "TCP"
      }
    }
  }
}

# metrics-server: pré-requisito do HorizontalPodAutoscaler.
# Em kind os kubelets usam certificado self-signed, daí o --kubelet-insecure-tls
# aplicado via patch (infra/metrics-server-kubelet-insecure.yaml).
resource "null_resource" "metrics_server" {
  count = var.install_metrics_server ? 1 : 0

  depends_on = [kind_cluster.oficina]

  triggers = {
    cluster = kind_cluster.oficina.id
  }

  provisioner "local-exec" {
    command = "kubectl --kubeconfig \"${kind_cluster.oficina.kubeconfig_path}\" apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml"
  }

  provisioner "local-exec" {
    command = "kubectl --kubeconfig \"${kind_cluster.oficina.kubeconfig_path}\" -n kube-system patch deployment metrics-server --patch-file \"${path.module}/metrics-server-kubelet-insecure.yaml\""
  }
}

# Aplicação dos manifestos da aplicação (namespace, config, secret, db, app, hpa).
# Ligado por padrão: o `kubectl apply -f ../k8s` aplica o 11-secret.example.yaml
# (placeholder) para dev local; se ../k8s/11-secret.yaml existir, ele sobrescreve.
# O banco (20-db.yaml) sobe aqui — é o recurso "Banco de Dados" exigido pela IaC.
resource "null_resource" "workloads" {
  count = var.bootstrap_workloads ? 1 : 0

  depends_on = [
    kind_cluster.oficina,
    null_resource.metrics_server,
  ]

  triggers = {
    cluster = kind_cluster.oficina.id
  }

  provisioner "local-exec" {
    command = "kubectl --kubeconfig \"${kind_cluster.oficina.kubeconfig_path}\" apply -f \"${path.module}/${var.manifests_path}\""
  }
}
