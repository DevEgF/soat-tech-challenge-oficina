import { useState } from 'react'
import { toast } from 'sonner'
import { Plus, Pencil, Trash2, PackagePlus, AlertTriangle } from 'lucide-react'
import { useParts, useCreatePart, useUpdatePart, useDeletePart, useGoodsReceipt } from '@/api/parts'
import { ConfirmDialog } from '@/components/ConfirmDialog'
import { formatCurrency } from '@/lib/utils'
import type { PartResponse, PartRequest } from '@/lib/types'

const EMPTY: PartRequest = { code: '', name: '', priceCents: 0, stockQuantity: 0 }

export default function PartsPage() {
  const { data: parts = [], isLoading } = useParts()
  const create = useCreatePart()
  const update = useUpdatePart()
  const del = useDeletePart()
  const receipt = useGoodsReceipt()

  const [modal, setModal] = useState<'create' | 'edit' | 'receipt' | null>(null)
  const [selected, setSelected] = useState<PartResponse | null>(null)
  const [form, setForm] = useState<PartRequest>(EMPTY)
  const [receiptQty, setReceiptQty] = useState(1)
  const [receiptRef, setReceiptRef] = useState('')
  const [deleteTarget, setDeleteTarget] = useState<PartResponse | null>(null)

  const lowStock = parts.filter(p => p.replenishmentPoint != null && p.stockQuantity <= p.replenishmentPoint)

  function openCreate() { setForm(EMPTY); setSelected(null); setModal('create') }
  function openEdit(p: PartResponse) {
    setForm({ code: p.code, name: p.name, priceCents: p.priceCents, stockQuantity: p.stockQuantity, replenishmentPoint: p.replenishmentPoint ?? undefined })
    setSelected(p); setModal('edit')
  }
  function openReceipt(p: PartResponse) { setSelected(p); setReceiptQty(1); setReceiptRef(''); setModal('receipt') }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    try {
      if (modal === 'create') { await create.mutateAsync(form); toast.success('Peça criada!') }
      else { await update.mutateAsync({ id: selected!.id, req: form }); toast.success('Peça atualizada!') }
      setModal(null)
    } catch { /* interceptor */ }
  }

  async function handleReceipt(e: React.FormEvent) {
    e.preventDefault()
    try {
      await receipt.mutateAsync({ id: selected!.id, req: { quantity: receiptQty, reference: receiptRef || undefined } })
      toast.success(`Entrada de ${receiptQty} unidade(s) registrada!`)
      setModal(null)
    } catch { /* interceptor */ }
  }

  async function handleDelete() {
    try { await del.mutateAsync(deleteTarget!.id); toast.success('Peça excluída!') }
    catch { /* interceptor */ }
    setDeleteTarget(null)
  }

  return (
    <div className="p-6">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-xl font-bold text-stone-900">Peças e Insumos</h1>
        <button onClick={openCreate} className="flex items-center gap-2 px-4 py-2 bg-orange-600 text-white text-sm rounded-lg hover:bg-orange-700">
          <Plus size={15} /> Nova Peça
        </button>
      </div>

      {lowStock.length > 0 && (
        <div className="mb-4 bg-yellow-50 border border-yellow-200 rounded-xl p-4">
          <div className="flex items-center gap-2 mb-2 text-yellow-700 font-semibold text-sm">
            <AlertTriangle size={15} /> Alertas de Estoque Baixo
          </div>
          <div className="flex flex-wrap gap-2">
            {lowStock.map(p => (
              <span key={p.id} className="bg-yellow-100 text-yellow-800 text-xs px-2 py-1 rounded-full">
                {p.name} — {p.stockQuantity} restantes
              </span>
            ))}
          </div>
        </div>
      )}

      {isLoading ? <p className="text-stone-500 text-sm">Carregando...</p> : (
        <div className="bg-white rounded-xl border border-orange-200 overflow-hidden">
          <table className="w-full text-sm">
            <thead className="bg-orange-50 border-b border-orange-200">
              <tr>
                <th className="text-left px-4 py-3 text-stone-600 font-medium">Código</th>
                <th className="text-left px-4 py-3 text-stone-600 font-medium">Nome</th>
                <th className="text-left px-4 py-3 text-stone-600 font-medium">Preço</th>
                <th className="text-left px-4 py-3 text-stone-600 font-medium">Estoque</th>
                <th className="text-left px-4 py-3 text-stone-600 font-medium">P. Reposição</th>
                <th className="px-4 py-3"></th>
              </tr>
            </thead>
            <tbody>
              {parts.length === 0 ? (
                <tr><td colSpan={6} className="px-4 py-8 text-center text-stone-400">Nenhuma peça cadastrada.</td></tr>
              ) : parts.map((p, i) => {
                const isLow = p.replenishmentPoint != null && p.stockQuantity <= p.replenishmentPoint
                return (
                  <tr key={p.id} className={i % 2 === 0 ? 'bg-white' : 'bg-orange-50/40'}>
                    <td className="px-4 py-3 font-mono text-xs text-stone-600">{p.code}</td>
                    <td className="px-4 py-3 font-medium text-stone-800">{p.name}</td>
                    <td className="px-4 py-3 text-orange-600 font-medium">{formatCurrency(p.priceCents)}</td>
                    <td className="px-4 py-3">
                      <span className={isLow ? 'text-yellow-700 font-semibold' : 'text-stone-600'}>{p.stockQuantity}</span>
                    </td>
                    <td className="px-4 py-3 text-stone-500">{p.replenishmentPoint ?? '—'}</td>
                    <td className="px-4 py-3">
                      <div className="flex gap-2 justify-end">
                        <button onClick={() => openReceipt(p)} title="Registrar Entrada" className="text-stone-400 hover:text-green-600"><PackagePlus size={14} /></button>
                        <button onClick={() => openEdit(p)} className="text-stone-400 hover:text-orange-600"><Pencil size={14} /></button>
                        <button onClick={() => setDeleteTarget(p)} className="text-stone-400 hover:text-red-600"><Trash2 size={14} /></button>
                      </div>
                    </td>
                  </tr>
                )
              })}
            </tbody>
          </table>
        </div>
      )}

      {/* Create/Edit Modal */}
      {(modal === 'create' || modal === 'edit') && (
        <div className="fixed inset-0 z-50 flex items-center justify-center">
          <div className="absolute inset-0 bg-black/40" onClick={() => setModal(null)} />
          <div className="relative bg-white rounded-xl shadow-xl p-6 w-full max-w-md mx-4">
            <h2 className="text-base font-bold text-stone-900 mb-4">{modal === 'create' ? 'Nova Peça' : 'Editar Peça'}</h2>
            <form onSubmit={handleSubmit} className="flex flex-col gap-3">
              <div className="grid grid-cols-2 gap-3">
                <FI label="Código *" value={form.code} onChange={e => setForm(f => ({ ...f, code: e.target.value }))} required />
                <FI label="Nome *" value={form.name} onChange={e => setForm(f => ({ ...f, name: e.target.value }))} required />
                <FI label="Preço (centavos) *" type="number" min="0" value={form.priceCents} onChange={e => setForm(f => ({ ...f, priceCents: Number(e.target.value) }))} required />
                <FI label="Estoque Inicial *" type="number" min="0" value={form.stockQuantity} onChange={e => setForm(f => ({ ...f, stockQuantity: Number(e.target.value) }))} required />
                <FI label="P. Reposição" type="number" min="0" value={form.replenishmentPoint ?? ''} onChange={e => setForm(f => ({ ...f, replenishmentPoint: e.target.value ? Number(e.target.value) : undefined }))} />
              </div>
              <div className="flex gap-3 pt-2">
                <button type="button" onClick={() => setModal(null)} className="flex-1 py-2 border border-stone-200 rounded-lg text-sm text-stone-700">Cancelar</button>
                <button type="submit" disabled={create.isPending || update.isPending} className="flex-1 py-2 bg-orange-600 text-white text-sm font-semibold rounded-lg hover:bg-orange-700 disabled:opacity-60">
                  {create.isPending || update.isPending ? 'Salvando...' : 'Salvar'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Goods Receipt Modal */}
      {modal === 'receipt' && (
        <div className="fixed inset-0 z-50 flex items-center justify-center">
          <div className="absolute inset-0 bg-black/40" onClick={() => setModal(null)} />
          <div className="relative bg-white rounded-xl shadow-xl p-6 w-full max-w-sm mx-4">
            <h2 className="text-base font-bold text-stone-900 mb-1">Registrar Entrada</h2>
            <p className="text-sm text-stone-500 mb-4">{selected?.name}</p>
            <form onSubmit={handleReceipt} className="flex flex-col gap-3">
              <FI label="Quantidade *" type="number" min="1" value={receiptQty} onChange={e => setReceiptQty(Number(e.target.value))} required />
              <FI label="Referência / NF" value={receiptRef} onChange={e => setReceiptRef(e.target.value)} />
              <div className="flex gap-3 pt-2">
                <button type="button" onClick={() => setModal(null)} className="flex-1 py-2 border border-stone-200 rounded-lg text-sm text-stone-700">Cancelar</button>
                <button type="submit" disabled={receipt.isPending} className="flex-1 py-2 bg-green-600 text-white text-sm font-semibold rounded-lg hover:bg-green-700 disabled:opacity-60">
                  {receipt.isPending ? 'Registrando...' : 'Registrar'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      <ConfirmDialog
        open={!!deleteTarget}
        title="Excluir peça"
        description={`Deseja excluir "${deleteTarget?.name}"?`}
        confirmLabel="Excluir"
        onConfirm={handleDelete}
        onCancel={() => setDeleteTarget(null)}
        loading={del.isPending}
      />
    </div>
  )
}

function FI({ label, ...props }: { label: string } & React.InputHTMLAttributes<HTMLInputElement>) {
  return (
    <div>
      <label className="block text-xs font-medium text-stone-600 mb-1">{label}</label>
      <input {...props} className="w-full px-3 py-2 border border-orange-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-orange-400" />
    </div>
  )
}
