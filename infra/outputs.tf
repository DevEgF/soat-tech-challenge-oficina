output "cluster_name" {
  description = "Nome do cluster kind provisionado."
  value       = kind_cluster.oficina.name
}

output "kubeconfig_path" {
  description = "Caminho do kubeconfig gerado. Use com: kubectl --kubeconfig <path> ..."
  value       = kind_cluster.oficina.kubeconfig_path
}

output "endpoint" {
  description = "Endpoint da API do cluster."
  value       = kind_cluster.oficina.endpoint
}

output "app_url_hint" {
  description = "Após aplicar os workloads, faça port-forward ou use o NodePort mapeado."
  value       = "kubectl --kubeconfig ${kind_cluster.oficina.kubeconfig_path} -n oficina port-forward svc/oficina-app 8080:8080"
}
