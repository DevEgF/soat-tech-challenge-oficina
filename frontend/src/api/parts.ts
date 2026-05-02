import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { api } from './client'
import type { PartResponse, PartRequest, GoodsReceiptRequest } from '@/lib/types'

const BASE = '/api/admin/pecas'

export function useParts() {
  return useQuery({
    queryKey: ['parts'],
    queryFn: async () => {
      const { data } = await api.get<PartResponse[]>(BASE)
      return data
    },
  })
}

export function useCreatePart() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async (req: PartRequest) => {
      const { data } = await api.post<PartResponse>(BASE, req)
      return data
    },
    onSuccess: () => qc.invalidateQueries({ queryKey: ['parts'] }),
  })
}

export function useUpdatePart() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async ({ id, req }: { id: string; req: PartRequest }) => {
      const { data } = await api.put<PartResponse>(`${BASE}/${id}`, req)
      return data
    },
    onSuccess: () => qc.invalidateQueries({ queryKey: ['parts'] }),
  })
}

export function useDeletePart() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async (id: string) => api.delete(`${BASE}/${id}`),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['parts'] }),
  })
}

export function useGoodsReceipt() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async ({ id, req }: { id: string; req: GoodsReceiptRequest }) => {
      const { data } = await api.post<PartResponse>(`${BASE}/${id}/entrada-mercadoria`, req)
      return data
    },
    onSuccess: () => qc.invalidateQueries({ queryKey: ['parts'] }),
  })
}
