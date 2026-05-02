import axios from 'axios'
import { toast } from 'sonner'

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? '',
})

api.interceptors.request.use((config) => {
  const raw = localStorage.getItem('oficina-auth')
  if (raw) {
    try {
      const { state } = JSON.parse(raw) as { state: { token: string | null } }
      if (state?.token) {
        config.headers.Authorization = `Bearer ${state.token}`
      }
    } catch {
      // ignore
    }
  }
  return config
})

api.interceptors.response.use(
  (res) => res,
  (error) => {
    const status = error?.response?.status
    if (status === 401) {
      localStorage.removeItem('oficina-auth')
      window.location.href = '/login'
    } else if (status === 403) {
      toast.error('Você não tem permissão para essa ação.')
    } else if (status >= 500 || !status) {
      toast.error('Erro ao processar. Tente novamente.')
    }
    return Promise.reject(error)
  }
)
