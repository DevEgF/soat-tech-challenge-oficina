import { useState } from 'react'
import { toast } from 'sonner'
import { Plus, Pencil, Trash2 } from 'lucide-react'
import { useCatalogServices, useCreateCatalogService, useUpdateCatalogService, useDeleteCatalogService } from '@/api/catalogServices'
import { ConfirmDialog } from '@/components/ConfirmDialog'
import { formatCurrency } from '@/lib/utils'
import type { CatalogServiceResponse, CatalogServiceRequest } from '@/lib/types'

const EMPTY: CatalogServiceRequest = { name: '', description: '', priceCents: 0, estimatedMinutes: 30 }

export default function CatalogServicesPage() {
  const { data: services = [], isLoading } = useCatalogServices()
  const create = useCreateCatalogService()
  const update = useUpdateCatalogService()
  const del = useDeleteCatalogService()

  const [modal, setModal] = useState<'create' | 'edit' | null>(null)
  const [selected, setSelected] = useState<CatalogServiceResponse | null>(null)
  const [form, setForm] = useState<CatalogServiceRequest>(EMPTY)
  const [deleteTarget, setDeleteTarget] = useState<CatalogServiceResponse | null>(null)

  function openCreate() { setForm(EMPTY); setSelected(null); setModal('create') }
  function openEdit(s: CatalogServiceResponse) {
    setForm({ name: s.name, description: s.description ?? '', priceCents: s.priceCents, estimatedMinutes: s.estimatedMinutes })
    setSelected(s); setModal('edit')
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    try {
      if (modal === 'create') { await create.mutateAsync(form); toast.success('Serviço criado!') }
      else { await update.mutateAsync({ id: selected!.id, req: form }); toast.success('Serviço atualizado!') }
      setModal(null)
    } catch { /* interceptor */ }
  }

  async function handleDelete() {
    try { await del.mutateAsync(deleteTarget!.id); toast.success('Serviço excluído!') }
    catch { /* interceptor */ }
    setDeleteTarget(null)
  }

  return (
    <div className="p-6">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-xl font-bold text-stone-900">Serviços Catálogo</h1>
        <button onClick={openCreate} className="flex items-center gap-2 px-4 py-2 bg-orange-600 text-white text-sm rounded-lg hover:bg-orange-700">
          <Plus size={15} /> Novo Serviço
        </button>
      </div>

      {isLoading ? <p className="text-stone-500 text-sm">Carregando...</p> : (
        <div className="bg-white rounded-xl border border-orange-200 overflow-hidden">
          <table className="w-full text-sm">
            <thead className="bg-orange-50 border-b border-orange-200">
              <tr>
                <th className="text-left px-4 py-3 text-stone-600 font-medium">Nome</th>
                <th className="text-left px-4 py-3 text-stone-600 font-medium">Descrição</th>
                <th className="text-left px-4 py-3 text-stone-600 font-medium">Preço</th>
                <th className="text-left px-4 py-3 text-stone-600 font-medium">Tempo Est.</th>
                <th className="px-4 py-3"></th>
              </tr>
            </thead>
            <tbody>
              {services.length === 0 ? (
                <tr><td colSpan={5} className="px-4 py-8 text-center text-stone-400">Nenhum serviço cadastrado.</td></tr>
              ) : services.map((s, i) => (
                <tr key={s.id} className={i % 2 === 0 ? 'bg-white' : 'bg-orange-50/40'}>
                  <td className="px-4 py-3 font-medium text-stone-800">{s.name}</td>
                  <td className="px-4 py-3 text-stone-500 max-w-xs truncate">{s.description ?? '—'}</td>
                  <td className="px-4 py-3 font-medium text-orange-600">{formatCurrency(s.priceCents)}</td>
                  <td className="px-4 py-3 text-stone-500">{s.estimatedMinutes} min</td>
                  <td className="px-4 py-3">
                    <div className="flex gap-2 justify-end">
                      <button onClick={() => openEdit(s)} className="text-stone-400 hover:text-orange-600"><Pencil size={14} /></button>
                      <button onClick={() => setDeleteTarget(s)} className="text-stone-400 hover:text-red-600"><Trash2 size={14} /></button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {modal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center">
          <div className="absolute inset-0 bg-black/40" onClick={() => setModal(null)} />
          <div className="relative bg-white rounded-xl shadow-xl p-6 w-full max-w-md mx-4">
            <h2 className="text-base font-bold text-stone-900 mb-4">{modal === 'create' ? 'Novo Serviço' : 'Editar Serviço'}</h2>
            <form onSubmit={handleSubmit} className="flex flex-col gap-3">
              <div>
                <label className="block text-xs font-medium text-stone-600 mb-1">Nome *</label>
                <input value={form.name} onChange={e => setForm(f => ({ ...f, name: e.target.value }))} required className="w-full px-3 py-2 border border-orange-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-orange-400" />
              </div>
              <div>
                <label className="block text-xs font-medium text-stone-600 mb-1">Descrição</label>
                <textarea value={form.description ?? ''} onChange={e => setForm(f => ({ ...f, description: e.target.value }))} rows={2} className="w-full px-3 py-2 border border-orange-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-orange-400 resize-none" />
              </div>
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-medium text-stone-600 mb-1">Preço (centavos) *</label>
                  <input type="number" min="0" value={form.priceCents} onChange={e => setForm(f => ({ ...f, priceCents: Number(e.target.value) }))} required className="w-full px-3 py-2 border border-orange-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-orange-400" />
                </div>
                <div>
                  <label className="block text-xs font-medium text-stone-600 mb-1">Tempo (min) *</label>
                  <input type="number" min="1" value={form.estimatedMinutes} onChange={e => setForm(f => ({ ...f, estimatedMinutes: Number(e.target.value) }))} required className="w-full px-3 py-2 border border-orange-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-orange-400" />
                </div>
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

      <ConfirmDialog
        open={!!deleteTarget}
        title="Excluir serviço"
        description={`Deseja excluir "${deleteTarget?.name}"?`}
        confirmLabel="Excluir"
        onConfirm={handleDelete}
        onCancel={() => setDeleteTarget(null)}
        loading={del.isPending}
      />
    </div>
  )
}
