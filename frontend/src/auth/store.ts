import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import { jwtDecode } from 'jwt-decode'
import type { Scope } from '@/lib/constants'

interface JwtPayload { sub: string; scope: string[]; exp: number }

interface AuthState {
  token: string | null
  username: string | null
  scope: Scope | null
  setAuth: (token: string) => void
  logout: () => void
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      token: null,
      username: null,
      scope: null,
      setAuth: (token) => {
        try {
          const decoded = jwtDecode<JwtPayload>(token)
          const scopes = decoded.scope ?? []
          const scope = (scopes.includes('MASTER')
            ? 'MASTER'
            : scopes[0]) as Scope | undefined
          set({ token, username: decoded.sub, scope })
        } catch {
          set({ token, username: null, scope: null })
        }
      },
      logout: () => set({ token: null, username: null, scope: null }),
    }),
    { name: 'oficina-auth' }
  )
)
