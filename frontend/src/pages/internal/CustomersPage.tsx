import { useState } from 'react'
import { toast } from 'sonner'
import { Plus, Pencil, Trash2 } from 'lucide-react'
import { useCustomers, useCreateCustomer, useUpdateCustomer, useDeleteCustomer } from '@/api/customers'
import { ConfirmDialog } from '@/components/ConfirmDialog'
import type { CustomerResponse, CustomerRequest } from '@/lib/types'

const EMPTY: CustomerRequest = { taxId: '', name: '', email: '', phone: '' }

export default function CustomersPage() {
  const { data: customers = [], isLoading } = useCustomers()
  const create = useCreateCustomer()
  const update = useUpdateCustomer()
  const del = useDeleteCustomer()

  const [modal, setModal] = useState<'create' | 'edit' | null>(null)
  const [selected, setSelected] = useState<CustomerResponse | null>(null)
  const [form, setForm] = useState<CustomerRequest>(EMPTY)
  const [deleteTarget, setDeleteTarget] = useState<CustomerResponse | null>(null)

  function openCreate() { setForm(EMPTY); setSelected(null); setModal('create') }
  function openEdit(c: CustomerResponse) { setForm({ taxId: c.taxId, name: c.name, email: c.email ?? '', phone: c.phone ?? '' }); setSelected(c); setModal('edit') }

  function field(k: keyof CustomerRequest): React.InputHTMLAttributes<HTMLInputElement> {
    return { value: form[k] ?? '', onChange: e => setForm(f => ({ ...f, [k]: e.target.value })) }
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    try {
      if (modal === 'create') { await create.mutateAsync(form); toast.success('Cliente criado!') }
      else { await update.mutateAsync({ id: selected!.id, req: form }); toast.success('Cliente atualizado!') }
      setModal(null)
    } catch { /* interceptor */ }
  }

  async function handleDelete() {
    try { await del.mutateAsync(deleteTarget!.id); toast.success('Cliente excluído!') }
    catch { /* interceptor */ }
    setDeleteTarget(null)
  }

  return (
    <div className="p-6">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-xl font-bold text-stone-900">Clientes</h1>
        <button onClick={openCreate} className="flex items-center gap-2 px-4 py-2 bg-orange-600 text-white text-sm rounded-lg hover:bg-orange-700">
          <Plus size={15} /> Novo Cliente
        </button>
      </div>

      {isLoading ? <p className="text-stone-500 text-sm">Carregando...</p> : (
        <div className="bg-white rounded-xl border border-orange-200 overflow-hidden">
          <table className="w-full text-sm">
            <thead className="bg-orange-50 border-b border-orange-200">
              <tr>
                <th className="text-left px-4 py-3 text-stone-600 font-medium">Nome</th>
                <th className="text-left px-4 py-3 text-stone-600 font-medium">CPF/CNPJ</th>
                <th className="text-left px-4 py-3 text-stone-600 font-medium">Email</th>
                <th className="text-left px-4 py-3 text-stone-600 font-medium">Telefone</th>
                <th className="px-4 py-3"></th>
              </tr>
            </thead>
            <tbody>
              {customers.length === 0 ? (
                <tr><td colSpan={5} className="px-4 py-8 text-center text-stone-400">Nenhum cliente cadastrado.</td></tr>
              ) : customers.map((c, i) => (
                <tr key={c.id} className={i % 2 === 0 ? 'bg-white' : 'bg-orange-50/40'}>
                  <td className="px-4 py-3 font-medium text-stone-800">{c.name}</td>
                  <td className="px-4 py-3 text-stone-600">{c.taxId}</td>
                  <td className="px-4 py-3 text-stone-500">{c.email ?? '—'}</td>
                  <td className="px-4 py-3 text-stone-500">{c.phone ?? '—'}</td>
                  <td className="px-4 py-3">
                    <div className="flex gap-2 justify-end">
                      <button onClick={() => openEdit(c)} className="text-stone-400 hover:text-orange-600"><Pencil size={14} /></button>
                      <button onClick={() => setDeleteTarget(c)} className="text-stone-400 hover:text-red-600"><Trash2 size={14} /></button>
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
            <h2 className="text-base font-bold text-stone-900 mb-4">{modal === 'create' ? 'Novo Cliente' : 'Editar Cliente'}</h2>
            <form onSubmit={handleSubmit} className="flex flex-col gap-3">
              <FormInput label="CPF/CNPJ *" {...field('taxId')} required />
              <FormInput label="Nome *" {...field('name')} required />
              <FormInput label="Email" type="email" {...field('email')} />
              <FormInput label="Telefone" {...field('phone')} />
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
        title="Excluir cliente"
        description={`Deseja excluir "${deleteTarget?.name}"? Esta ação não pode ser desfeita.`}
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
