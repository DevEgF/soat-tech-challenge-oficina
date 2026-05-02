import type { WorkOrderStatus } from './types'

export type Scope = 'MASTER' | 'ADMIN' | 'ATTENDANT' | 'TECHNICIAN' | 'WAREHOUSE'

export const STATUS_LABELS: Record<WorkOrderStatus, string> = {
  RECEIVED: 'Recebida',
  IN_DIAGNOSIS: 'Em Diagnóstico',
  PENDING_INTERNAL_APPROVAL: 'Aguardando Aprovação Interna',
  PENDING_APPROVAL: 'Aguardando Aprovação do Cliente',
  AWAITING_PARTS_RELEASE: 'Aprovado',
  IN_EXECUTION: 'Em Execução',
  FINALIZED: 'Finalizada',
  DELIVERED: 'Entregue',
  CANCELLED: 'Cancelada',
}

export const STATUS_COLORS: Record<WorkOrderStatus, string> = {
  RECEIVED: 'bg-stone-100 text-stone-700',
  IN_DIAGNOSIS: 'bg-blue-100 text-blue-700',
  PENDING_INTERNAL_APPROVAL: 'bg-yellow-100 text-yellow-800',
  PENDING_APPROVAL: 'bg-orange-100 text-orange-800',
  AWAITING_PARTS_RELEASE: 'bg-purple-100 text-purple-700',
  IN_EXECUTION: 'bg-orange-500 text-white',
  FINALIZED: 'bg-green-100 text-green-700',
  DELIVERED: 'bg-green-500 text-white',
  CANCELLED: 'bg-red-100 text-red-700',
}

export const STATUS_ORDER: WorkOrderStatus[] = [
  'RECEIVED', 'IN_DIAGNOSIS', 'PENDING_INTERNAL_APPROVAL', 'PENDING_APPROVAL',
  'AWAITING_PARTS_RELEASE', 'IN_EXECUTION', 'FINALIZED', 'DELIVERED',
]

export const SCOPE_LABELS: Record<Scope, string> = {
  MASTER: 'Master',
  ADMIN: 'Administrador',
  ATTENDANT: 'Atendente',
  TECHNICIAN: 'Técnico',
  WAREHOUSE: 'Almoxarife',
}
