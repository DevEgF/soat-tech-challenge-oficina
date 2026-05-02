import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuthStore } from '@/auth/store'
import { SCOPE_LABELS } from '@/lib/constants'
import { cn } from '@/lib/utils'
import {
  ClipboardList, Users, Car, Package, Wrench, Warehouse, BarChart2, LogOut,
} from 'lucide-react'

const NAV = [
  { to: '/internal/ordens-servico', label: 'Ordens de Serviço', icon: ClipboardList },
  { to: '/internal/clientes', label: 'Clientes', icon: Users },
  { to: '/internal/veiculos', label: 'Veículos', icon: Car },
  { to: '/internal/pecas', label: 'Peças', icon: Package },
  { to: '/internal/servicos-catalogo', label: 'Serviços Catálogo', icon: Wrench },
  { to: '/internal/almoxarifado', label: 'Almoxarifado', icon: Warehouse },
  { to: '/internal/metricas', label: 'Métricas', icon: BarChart2 },
]

export function AppLayout() {
  const { username, scope, logout } = useAuthStore()
  const navigate = useNavigate()

  function handleLogout() {
    logout()
    navigate('/login')
  }

  return (
    <div className="flex min-h-screen w-full">
      {/* Sidebar */}
      <aside className="w-52 shrink-0 bg-stone-900 flex flex-col">
        <div className="px-4 py-5">
          <span className="text-orange-500 font-bold text-base flex items-center gap-2">
            🔧 OficinaSys
          </span>
        </div>

        <nav className="flex-1 flex flex-col gap-0.5 px-2">
          {NAV.map(({ to, label, icon: Icon }) => (
            <NavLink
              key={to}
              to={to}
              className={({ isActive }) =>
                cn(
                  'flex items-center gap-2.5 px-3 py-2 rounded-md text-sm transition-colors',
                  isActive
                    ? 'bg-white/10 text-orange-400 border-l-2 border-orange-500 font-medium'
                    : 'text-stone-400 hover:text-stone-200 hover:bg-white/5'
                )
              }
            >
              <Icon size={15} />
              {label}
            </NavLink>
          ))}
        </nav>

        <div className="px-4 py-4 border-t border-stone-800">
          <p className="text-orange-400 text-sm font-medium truncate">{username}</p>
          <p className="text-stone-500 text-xs mb-3">{scope ? SCOPE_LABELS[scope] : ''}</p>
          <button
            onClick={handleLogout}
            className="flex items-center gap-2 text-stone-400 hover:text-stone-200 text-xs transition-colors"
          >
            <LogOut size={13} /> Sair
          </button>
        </div>
      </aside>

      {/* Main */}
      <main className="flex-1 min-h-screen bg-orange-50 overflow-auto">
        <Outlet />
      </main>
    </div>
  )
}
