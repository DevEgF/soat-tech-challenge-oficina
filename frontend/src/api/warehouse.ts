import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { api } from './client'
import type { PartReservationResponse, LowStockAlertResponse } from '@/lib/types'

export function usePendingReservations(workOrderId: string) {
  return useQuery({
    queryKey: ['pendingReservations', workOrderId],
    queryFn: async () => {
      const { data } = await api.get<PartReservationResponse[]>(
        `/api/warehouse/ordens-servico/${workOrderId}/reservas-pendentes`
      )
      return data
    },
    enabled: !!workOrderId,
  })
}

export function useAllPendingReservations() {
  return useQuery({
    queryKey: ['pendingReservationsAll'],
    queryFn: async () => {
      const { data } = await api.get<PartReservationResponse[]>('/api/warehouse/reservas-pendentes')
      return data
    },
  })
}

export function useConfirmStockExit() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async (workOrderId: string) => {
      await api.post(`/api/warehouse/ordens-servico/${workOrderId}/confirmar-saida`)
    },
    onSuccess: (_, id) => {
      qc.invalidateQueries({ queryKey: ['pendingReservations', id] })
      qc.invalidateQueries({ queryKey: ['pendingReservationsAll'] })
      qc.invalidateQueries({ queryKey: ['lowStockAlerts'] })
    },
  })
}

export function useLowStockAlerts() {
  return useQuery({
    queryKey: ['lowStockAlerts'],
    queryFn: async () => {
      const { data } = await api.get<LowStockAlertResponse[]>('/api/warehouse/alertas-estoque-baixo')
      return data
    },
  })
}
