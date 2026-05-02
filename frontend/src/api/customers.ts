import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { api } from './client'
import { useAuthStore } from '@/auth/store'
import type { CustomerResponse, CustomerRequest } from '@/lib/types'

const ADMIN_BASE = '/api/admin/clientes'
const INTERNAL_BASE = '/api/internal/clientes'

export function useCustomers() {
  const { scope } = useAuthStore()
  const endpoint = scope === 'ADMIN' || scope === 'MASTER' ? ADMIN_BASE : INTERNAL_BASE
  return useQuery({
    queryKey: ['customers', scope],
    queryFn: async () => {
      const { data } = await api.get<CustomerResponse[]>(endpoint)
      return data
    },
    enabled: !!scope,
  })
}

export function useCreateCustomer() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async (req: CustomerRequest) => {
      const { data } = await api.post<CustomerResponse>(ADMIN_BASE, req)
      return data
    },
    onSuccess: () => qc.invalidateQueries({ queryKey: ['customers'] }),
  })
}

export function useUpdateCustomer() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async ({ id, req }: { id: string; req: CustomerRequest }) => {
      const { data } = await api.put<CustomerResponse>(`${ADMIN_BASE}/${id}`, req)
      return data
    },
    onSuccess: () => qc.invalidateQueries({ queryKey: ['customers'] }),
  })
}

export function useDeleteCustomer() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async (id: string) => api.delete(`${ADMIN_BASE}/${id}`),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['customers'] }),
  })
}
