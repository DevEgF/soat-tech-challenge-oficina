import { useQuery } from '@tanstack/react-query'
import { api } from '@/api/client'
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts'
import type { AverageServiceTimeResponse } from '@/lib/types'

export default function MetricsPage() {
  const { data = [], isLoading } = useQuery({
    queryKey: ['metrics'],
    queryFn: async () => {
      const { data } = await api.get<AverageServiceTimeResponse[]>('/api/admin/metricas/tempo-medio-execucao-servicos')
      return data
    },
  })

  const chartData = data.map(d => ({
    name: d.serviceName.length > 20 ? d.serviceName.slice(0, 20) + '…' : d.serviceName,
    minutos: Math.round(d.averageMinutes),
    amostras: d.sampleCount,
  }))

  return (
    <div className="p-6 max-w-4xl">
      <h1 className="text-xl font-bold text-stone-900 mb-6">Métricas</h1>

      <div className="bg-white rounded-xl border border-orange-200 p-5 mb-5">
        <h2 className="text-sm font-semibold text-stone-700 mb-4">Tempo Médio de Execução por Serviço</h2>
        {isLoading ? (
          <p className="text-stone-500 text-sm">Carregando...</p>
        ) : data.length === 0 ? (
          <p className="text-stone-400 text-sm py-8 text-center">Nenhuma métrica disponível ainda.</p>
        ) : (
          <ResponsiveContainer width="100%" height={300}>
            <BarChart data={chartData} layout="vertical" margin={{ left: 10, right: 20, top: 0, bottom: 0 }}>
              <CartesianGrid strokeDasharray="3 3" horizontal={false} />
              <XAxis type="number" tick={{ fontSize: 11 }} unit=" min" />
              <YAxis type="category" dataKey="name" tick={{ fontSize: 11 }} width={140} />
              <Tooltip
                formatter={(v) => [`${v} min`, 'Tempo médio']}
                contentStyle={{ fontSize: 12, borderRadius: 8 }}
              />
              <Bar dataKey="minutos" fill="#ea580c" radius={[0, 4, 4, 0]} />
            </BarChart>
          </ResponsiveContainer>
        )}
      </div>

      {data.length > 0 && (
        <div className="bg-white rounded-xl border border-orange-200 overflow-hidden">
          <table className="w-full text-sm">
            <thead className="bg-orange-50 border-b border-orange-200">
              <tr>
                <th className="text-left px-4 py-3 text-stone-600 font-medium">Serviço</th>
                <th className="text-left px-4 py-3 text-stone-600 font-medium">Tempo Médio</th>
                <th className="text-left px-4 py-3 text-stone-600 font-medium">Amostras</th>
              </tr>
            </thead>
            <tbody>
              {data.map((d, i) => (
                <tr key={d.catalogServiceId} className={i % 2 === 0 ? 'bg-white' : 'bg-orange-50/40'}>
                  <td className="px-4 py-3 font-medium text-stone-800">{d.serviceName}</td>
                  <td className="px-4 py-3 text-orange-600 font-semibold">{Math.round(d.averageMinutes)} min</td>
                  <td className="px-4 py-3 text-stone-500">{d.sampleCount}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}
