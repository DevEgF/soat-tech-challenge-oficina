import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { api } from './client'
import type { CatalogServiceResponse, CatalogServiceRequest } from '@/lib/types'

const BASE = '/api/admin/servicos-catalogo'

export function useCatalogServices() {
  return useQuery({
    queryKey: ['catalogServices'],
    queryFn: async () => {
      const { data } = await api.get<CatalogServiceResponse[]>(BASE)
      return data
    },
  })
}

export function useCreateCatalogService() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async (req: CatalogServiceRequest) => {
      const { data } = await api.post<CatalogServiceResponse>(BASE, req)
      return data
    },
    onSuccess: () => qc.invalidateQueries({ queryKey: ['catalogServices'] }),
  })
}

export function useUpdateCatalogService() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async ({ id, req }: { id: string; req: CatalogServiceRequest }) => {
      const { data } = await api.put<CatalogServiceResponse>(`${BASE}/${id}`, req)
      return data
    },
    onSuccess: () => qc.invalidateQueries({ queryKey: ['catalogServices'] }),
  })
}

export function useDeleteCatalogService() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async (id: string) => api.delete(`${BASE}/${id}`),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['catalogServices'] }),
  })
}
