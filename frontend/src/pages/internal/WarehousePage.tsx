import { useState } from 'react'
import { toast } from 'sonner'
import { Search, AlertTriangle, CheckCircle } from 'lucide-react'
import { usePendingReservations, useConfirmStockExit, useLowStockAlerts, useAllPendingReservations } from '@/api/warehouse'

export default function WarehousePage() {
  const [searchId, setSearchId] = useState('')
  const [workOrderId, setWorkOrderId] = useState('')

  const { data: reservations = [], isLoading: loadingRes } = usePendingReservations(workOrderId)
  const { data: allReservations = [] } = useAllPendingReservations()
  const confirmExit = useConfirmStockExit()
  const { data: alerts = [] } = useLowStockAlerts()

  function handleSearch(e: React.FormEvent) {
    e.preventDefault()
    setWorkOrderId(searchId.trim())
  }

  async function handleConfirm() {
    try {
      await confirmExit.mutateAsync(workOrderId)
      toast.success('Saída de peças confirmada! OS avançou para Em Execução.')
    } catch { /* interceptor */ }
  }

  return (
    <div className="p-6 max-w-2xl">
      <h1 className="text-xl font-bold text-stone-900 mb-6">Almoxarifado</h1>

      {/* Search by OS ID */}
      <div className="bg-white rounded-xl border border-orange-200 p-5 mb-5">
        <h2 className="text-sm font-semibold text-stone-700 mb-3">Consultar Reservas por OS</h2>
        <form onSubmit={handleSearch} className="flex gap-2">
          <input
            value={searchId}
            onChange={e => setSearchId(e.target.value)}
            placeholder="ID da Ordem de Serviço (UUID)"
            className="flex-1 px-3 py-2 border border-orange-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-orange-400"
          />
          <button type="submit" className="flex items-center gap-2 px-4 py-2 bg-orange-600 text-white text-sm rounded-lg hover:bg-orange-700">
            <Search size={14} /> Buscar
          </button>
        </form>
      </div>

      {workOrderId && (
        <div className="bg-white rounded-xl border border-orange-200 p-5 mb-5">
          <h2 className="text-sm font-semibold text-stone-700 mb-3">Reservas Pendentes</h2>
          {loadingRes ? (
            <p className="text-stone-500 text-sm">Carregando...</p>
          ) : reservations.length === 0 ? (
            <p className="text-stone-400 text-sm">Nenhuma reserva pendente para esta OS.</p>
          ) : (
            <>
              <table className="w-full text-sm mb-4">
                <thead>
                  <tr className="border-b border-orange-100">
                    <th className="text-left pb-2 text-stone-500 font-medium">Peça</th>
                    <th className="text-left pb-2 text-stone-500 font-medium">Qtd</th>
                    <th className="text-left pb-2 text-stone-500 font-medium">Status</th>
                  </tr>
                </thead>
                <tbody>
                  {reservations.map((r, i) => (
                    <tr key={r.id} className={i % 2 === 0 ? '' : 'bg-orange-50/30'}>
                      <td className="py-2 text-stone-800 font-medium">{r.partName}</td>
                      <td className="py-2 text-stone-600">{r.quantity}</td>
                      <td className="py-2">
                        <span className="bg-yellow-100 text-yellow-800 text-xs px-2 py-0.5 rounded-full">{r.status}</span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
              <button
                onClick={handleConfirm}
                disabled={confirmExit.isPending}
                className="flex items-center gap-2 px-4 py-2.5 bg-green-600 text-white text-sm font-semibold rounded-lg hover:bg-green-700 disabled:opacity-60"
              >
                <CheckCircle size={15} />
                {confirmExit.isPending ? 'Confirmando...' : 'Confirmar Saída de Peças'}
              </button>
            </>
          )}
        </div>
      )}

      {allReservations.length > 0 && (
        <div className="bg-white rounded-xl border border-orange-200 p-5 mb-5">
          <h2 className="text-sm font-semibold text-stone-700 mb-3">Reservas Pendentes (todas as OS)</h2>
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-orange-100">
                <th className="text-left pb-2 text-stone-500 font-medium">OS</th>
                <th className="text-left pb-2 text-stone-500 font-medium">Peça</th>
                <th className="text-left pb-2 text-stone-500 font-medium">Qtd</th>
                <th className="text-left pb-2 text-stone-500 font-medium">Status</th>
              </tr>
            </thead>
            <tbody>
              {allReservations.map((r, i) => (
                <tr key={r.id} className={i % 2 === 0 ? '' : 'bg-orange-50/30'}>
                  <td className="py-2">
                    <button
                      onClick={() => {
                        setSearchId(r.workOrderId)
                        setWorkOrderId(r.workOrderId)
                      }}
                      className="text-orange-700 hover:text-orange-900 font-mono text-xs"
                    >
                      {r.workOrderId.slice(0, 8)}...
                    </button>
                  </td>
                  <td className="py-2 text-stone-800 font-medium">{r.partName}</td>
                  <td className="py-2 text-stone-600">{r.quantity}</td>
                  <td className="py-2">
                    <span className="bg-yellow-100 text-yellow-800 text-xs px-2 py-0.5 rounded-full">{r.status}</span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Low Stock Alerts */}
      {alerts.length > 0 && (
        <div className="bg-yellow-50 border border-yellow-200 rounded-xl p-5">
          <div className="flex items-center gap-2 mb-3 text-yellow-700 font-semibold text-sm">
            <AlertTriangle size={15} /> Alertas de Estoque Baixo
          </div>
          <div className="flex flex-col gap-2">
            {alerts.map(a => (
              <div key={a.partId} className="flex justify-between items-center bg-white rounded-lg px-3 py-2 border border-yellow-100">
                <div>
                  <p className="text-sm font-medium text-stone-800">{a.name}</p>
                  <p className="text-xs text-stone-400">Código: {a.code}</p>
                </div>
                <div className="text-right">
                  <p className="text-sm font-semibold text-yellow-700">{a.stockQuantity} em estoque</p>
                  {a.replenishmentPoint && <p className="text-xs text-stone-400">Mín: {a.replenishmentPoint}</p>}
                  {a.pendingReservedQuantity > 0 && <p className="text-xs text-orange-500">{a.pendingReservedQuantity} reservados</p>}
                </div>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  )
}
