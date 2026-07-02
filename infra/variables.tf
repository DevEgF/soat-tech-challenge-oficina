variable "cluster_name" {
  description = "Nome do cluster kind."
  type        = string
  default     = "oficina"
}

variable "node_image" {
  description = "Imagem do nó kind (fixe a versão do Kubernetes desejada)."
  type        = string
  default     = "kindest/node:v1.31.0"
}

variable "kubeconfig_path" {
  description = "Caminho onde o kubeconfig do cluster será gravado."
  type        = string
  default     = "./oficina.kubeconfig"
}

variable "host_http_port" {
  description = "Porta no host mapeada para o NodePort do Ingress/app (extraPortMapping)."
  type        = number
  default     = 8080
}

variable "install_metrics_server" {
  description = "Instala o metrics-server (necessário para o HorizontalPodAutoscaler)."
  type        = bool
  default     = true
}

variable "bootstrap_workloads" {
  description = <<-EOT
    Se true (padrão), aplica os manifestos de ../k8s após criar o cluster,
    subindo o banco de dados (PostgreSQL), a aplicação e o HPA — atendendo ao
    requisito de IaC da Fase 2 (Terraform provisiona o banco).
    Para dev local não é necessário k8s/11-secret.yaml: o 11-secret.example.yaml
    (senhas placeholder autoconsistentes) é aplicado. Para uso real, crie
    k8s/11-secret.yaml a partir do .example — ele sobrescreve o template.
  EOT
  type        = bool
  default     = true
}

variable "manifests_path" {
  description = "Caminho relativo dos manifestos Kubernetes da aplicação."
  type        = string
  default     = "../k8s"
}
