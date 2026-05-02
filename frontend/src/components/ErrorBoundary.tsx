import { Component, type ReactNode } from 'react'

interface State { hasError: boolean }

export class ErrorBoundary extends Component<{ children: ReactNode }, State> {
  state: State = { hasError: false }
  static getDerivedStateFromError() { return { hasError: true } }
  render() {
    if (this.state.hasError) {
      return (
        <div className="flex flex-col items-center justify-center min-h-screen gap-4">
          <p className="text-stone-600">Algo deu errado. Por favor, recarregue a página.</p>
          <button onClick={() => window.location.reload()} className="px-4 py-2 bg-orange-600 text-white rounded-lg text-sm">
            Recarregar
          </button>
        </div>
      )
    }
    return this.props.children
  }
}
