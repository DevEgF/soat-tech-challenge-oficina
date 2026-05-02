import { api } from './client'
import type { LoginRequest, LoginResponse } from '@/lib/types'

export async function login(req: LoginRequest): Promise<LoginResponse> {
  const { data } = await api.post<LoginResponse>('/api/public/auth/login', req)
  return data
}
