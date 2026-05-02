import { useLocation, useNavigate } from 'react-router-dom'
import { StatusBadge } from '@/components/StatusBadge'
import { formatCurrency } from '@/lib/utils'
import type { WorkOrderTrackingResponse } from '@/lib/types'

export default function QuoteResultPage() {
  const location = useLocation()
  const navigate = useNavigate()
  const state = location.state as { result: WorkOrderTrackingResponse; action: string } | null

  if (!state) {
    navigate('/public/acompanhar', { replace: true })
    return null
  }

  const { result, action } = state
  const isApproved = action === 'aprovado'

  return (
    <div className="min-h-screen bg-orange-50 flex flex-col items-center justify-center px-4">
      <div className="w-full max-w-sm bg-white rounded-2xl shadow border border-orange-200 p-8 text-center">
        <div className="text-5xl mb-4">{isApproved ? '✅' : '❌'}</div>
        <h2 className="text-xl font-bold text-stone-900 mb-1">
          Orçamento {isApproved ? 'Aprovado' : 'Reprovado'}
        </h2>
        <p className="text-stone-500 text-sm mb-5">
          {isApproved ? 'Os serviços serão iniciados em breve.' : 'O serviço foi cancelado conforme solicitado.'}
        </p>

        <div className="bg-orange-50 rounded-xl p-4 mb-5 text-left">
          <div className="flex justify-between items-center mb-2">
            <span className="text-xs text-stone-500">Placa</span>
            <span className="font-medium text-stone-800 text-sm">{result.vehiclePlate}</span>
          </div>
          <div className="flex justify-between items-center mb-2">
            <span className="text-xs text-stone-500">Status</span>
            <StatusBadge status={result.status} />
          </div>
          <div className="flex justify-between items-center">
            <span className="text-xs text-stone-500">Total</span>
            <span className="font-bold text-orange-600">{formatCurrency(result.totalCents)}</span>
          </div>
        </div>

        <button
          onClick={() => navigate('/public/acompanhar')}
          className="w-full py-2.5 bg-orange-600 hover:bg-orange-700 text-white text-sm font-semibold rounded-lg"
        >
          Nova Consulta
        </button>
      </div>
    </div>
  )
}
