import { useState } from 'react'
import { toast } from 'sonner'
import { Plus, Pencil, Trash2 } from 'lucide-react'
import { useVehicles, useCreateVehicle, useUpdateVehicle, useDeleteVehicle } from '@/api/vehicles'
import { useCustomers } from '@/api/customers'
import { ConfirmDialog } from '@/components/ConfirmDialog'
import type { VehicleResponse, VehicleRequest } from '@/lib/types'

const EMPTY: VehicleRequest = { customerId: '', plate: '', brand: '', model: '', year: new Date().getFullYear() }

export default function VehiclesPage() {
  const { data: vehicles = [], isLoading } = useVehicles()
  const { data: customers = [] } = useCustomers()
  const create = useCreateVehicle()
  const update = useUpdateVehicle()
  const del = useDeleteVehicle()

  const [modal, setModal] = useState<'create' | 'edit' | null>(null)
  const [selected, setSelected] = useState<VehicleResponse | null>(null)
  const [form, setForm] = useState<VehicleRequest>(EMPTY)
  const [deleteTarget, setDeleteTarget] = useState<VehicleResponse | null>(null)

  function openCreate() { setForm(EMPTY); setSelected(null); setModal('create') }
  function openEdit(v: VehicleResponse) {
    setForm({ customerId: v.customerId, plate: v.plate, brand: v.brand, model: v.model, year: v.year })
    setSelected(v); setModal('edit')
  }

  function field(k: keyof Omit<VehicleRequest, 'year' | 'customerId'>): React.InputHTMLAttributes<HTMLInputElement> {
    return { value: form[k], onChange: e => setForm(f => ({ ...f, [k]: e.target.value })) }
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    try {
      if (modal === 'create') { await create.mutateAsync(form); toast.success('Veículo criado!') }
      else { await update.mutateAsync({ id: selected!.id, req: form }); toast.success('Veículo atualizado!') }
      setModal(null)
    } catch { /* interceptor */ }
  }

  async function handleDelete() {
    try { await del.mutateAsync(deleteTarget!.id); toast.success('Veículo excluído!') }
    catch { /* interceptor */ }
    setDeleteTarget(null)
  }

  const customerName = (id: string) => customers.find(c => c.id === id)?.name ?? id.slice(0, 8)

  return (
    <div className="p-6">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-xl font-bold text-stone-900">Veículos</h1>
        <button onClick={openCreate} className="flex items-center gap-2 px-4 py-2 bg-orange-600 text-white text-sm rounded-lg hover:bg-orange-700">
          <Plus size={15} /> Novo Veículo
        </button>
      </div>

      {isLoading ? <p className="text-stone-500 text-sm">Carregando...</p> : (
        <div className="bg-white rounded-xl border border-orange-200 overflow-hidden">
          <table className="w-full text-sm">
            <thead className="bg-orange-50 border-b border-orange-200">
              <tr>
                <th className="text-left px-4 py-3 text-stone-600 font-medium">Placa</th>
                <th className="text-left px-4 py-3 text-stone-600 font-medium">Marca/Modelo</th>
                <th className="text-left px-4 py-3 text-stone-600 font-medium">Ano</th>
                <th className="text-left px-4 py-3 text-stone-600 font-medium">Cliente</th>
                <th className="px-4 py-3"></th>
              </tr>
            </thead>
            <tbody>
              {vehicles.length === 0 ? (
                <tr><td colSpan={5} className="px-4 py-8 text-center text-stone-400">Nenhum veículo cadastrado.</td></tr>
              ) : vehicles.map((v, i) => (
                <tr key={v.id} className={i % 2 === 0 ? 'bg-white' : 'bg-orange-50/40'}>
                  <td className="px-4 py-3 font-medium text-stone-800">{v.plate}</td>
                  <td className="px-4 py-3 text-stone-600">{v.brand} {v.model}</td>
                  <td className="px-4 py-3 text-stone-500">{v.year}</td>
                  <td className="px-4 py-3 text-stone-500">{customerName(v.customerId)}</td>
                  <td className="px-4 py-3">
                    <div className="flex gap-2 justify-end">
                      <button onClick={() => openEdit(v)} className="text-stone-400 hover:text-orange-600"><Pencil size={14} /></button>
                      <button onClick={() => setDeleteTarget(v)} className="text-stone-400 hover:text-red-600"><Trash2 size={14} /></button>
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
            <h2 className="text-base font-bold text-stone-900 mb-4">{modal === 'create' ? 'Novo Veículo' : 'Editar Veículo'}</h2>
            <form onSubmit={handleSubmit} className="flex flex-col gap-3">
              <div>
                <label className="block text-xs font-medium text-stone-600 mb-1">Cliente *</label>
                <select
                  value={form.customerId}
                  onChange={e => setForm(f => ({ ...f, customerId: e.target.value }))}
                  required
                  className="w-full px-3 py-2 border border-orange-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-orange-400"
                >
                  <option value="">Selecione...</option>
                  {customers.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
                </select>
              </div>
              <FormInput label="Placa *" {...field('plate')} required />
              <FormInput label="Marca *" {...field('brand')} required />
              <FormInput label="Modelo *" {...field('model')} required />
              <div>
                <label className="block text-xs font-medium text-stone-600 mb-1">Ano *</label>
                <input type="number" min="1900" value={form.year} onChange={e => setForm(f => ({ ...f, year: Number(e.target.value) }))} required className="w-full px-3 py-2 border border-orange-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-orange-400" />
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
        title="Excluir veículo"
        description={`Deseja excluir "${deleteTarget?.plate}"?`}
        confirmLabel="Excluir"
        onConfirm={handleDelete}
        onCancel={() => setDeleteTarget(null)}
        loading={del.isPending}
      />
    </div>
  )
}

function FormInput({ label, ...props }: { label: string } & React.InputHTMLAttributes<HTMLInputElement>) {
  return (
    <div>
      <label className="block text-xs font-medium text-stone-600 mb-1">{label}</label>
      <input {...props} className="w-full px-3 py-2 border border-orange-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-orange-400" />
    </div>
  )
}
