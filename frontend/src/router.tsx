import { createBrowserRouter, Navigate } from 'react-router-dom'
import { lazy, Suspense } from 'react'
import { AppLayout } from '@/components/AppLayout'
import { ProtectedRoute } from '@/auth/ProtectedRoute'

const Login = lazy(() => import('@/pages/internal/LoginPage'))
const TrackOrder = lazy(() => import('@/pages/public/TrackOrderPage'))
const QuoteResult = lazy(() => import('@/pages/public/QuoteResultPage'))
const WorkOrders = lazy(() => import('@/pages/internal/WorkOrdersPage'))
const WorkOrderDetail = lazy(() => import('@/pages/internal/WorkOrderDetailPage'))
const Customers = lazy(() => import('@/pages/internal/CustomersPage'))
const Vehicles = lazy(() => import('@/pages/internal/VehiclesPage'))
const Parts = lazy(() => import('@/pages/internal/PartsPage'))
const CatalogServices = lazy(() => import('@/pages/internal/CatalogServicesPage'))
const Warehouse = lazy(() => import('@/pages/internal/WarehousePage'))
const Metrics = lazy(() => import('@/pages/internal/MetricsPage'))

const S = ({ children }: { children: React.ReactNode }) => (
  <Suspense fallback={<div className="p-6 text-stone-400 text-sm">Carregando...</div>}>{children}</Suspense>
)

export const router = createBrowserRouter([
  { path: '/', element: <Navigate to="/public/acompanhar" replace /> },
  { path: '/login', element: <S><Login /></S> },
  { path: '/public/acompanhar', element: <S><TrackOrder /></S> },
  { path: '/public/resultado', element: <S><QuoteResult /></S> },
  {
    path: '/internal',
    element: <ProtectedRoute><AppLayout /></ProtectedRoute>,
    children: [
      { index: true, element: <Navigate to="/internal/ordens-servico" replace /> },
      { path: 'ordens-servico', element: <S><WorkOrders /></S> },
      { path: 'ordens-servico/:id', element: <S><WorkOrderDetail /></S> },
      { path: 'clientes', element: <S><Customers /></S> },
      { path: 'veiculos', element: <S><Vehicles /></S> },
      { path: 'pecas', element: <S><Parts /></S> },
      { path: 'servicos-catalogo', element: <S><CatalogServices /></S> },
      { path: 'almoxarifado', element: <S><Warehouse /></S> },
      { path: 'metricas', element: <S><Metrics /></S> },
    ],
  },
])
