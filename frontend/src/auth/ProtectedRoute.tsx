import { Navigate, useLocation } from 'react-router-dom'
import { useAuthStore } from './store'

export function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const { token } = useAuthStore()
  const location = useLocation()
  if (!token) return <Navigate to="/login" state={{ from: location.pathname }} replace />
  return <>{children}</>
}
