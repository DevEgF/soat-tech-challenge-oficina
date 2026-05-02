# MR - Evolucao da Ordem de Servico

Este documento justifica as alteracoes deste MR, consolidando decisoes de API, dominio e fluxo operacional da OS.

## Objetivo do MR

- Permitir que o tecnico monte/ajuste o plano de diagnostico antes da submissao final.
- Tratar servicos e pecas como itens opcionais no plano da OS.
- Preservar a regra de negocio do EventStorm:
  - reserva de pecas no envio do diagnostico/plano;
  - baixa real de estoque apenas apos aprovacao do cliente e confirmacao do almoxarifado.
- Registrar observacoes tecnicas de diagnostico para rastreabilidade.

## Fluxo de negocio adotado

1. OS criada (status `RECEIVED`).
2. Tecnico inicia diagnostico (status `IN_DIAGNOSIS`).
3. Tecnico atualiza plano (`servicos`, `pecas`, `observacoes`) quantas vezes precisar em `IN_DIAGNOSIS`.
4. Tecnico submete plano para aprovacao interna:
   - recalculo do orcamento final;
   - criacao/atualizacao das reservas pendentes de pecas.
5. Cliente aprova orcamento.
6. Almoxarife confirma saida:
   - somente neste momento ocorre o consumo no estoque e inicio da execucao.

## Alteracoes de API neste MR

### Novo endpoint (tecnico)

- `PUT /api/technician/ordens-servico/{id}/plano`
  - Atualiza plano tecnico da OS em diagnostico.
  - Payload:
    - `services[]` (opcional)
    - `parts[]` (opcional)
    - `diagnosisNotes` (opcional)
  - Regra: permitido apenas quando OS estiver em `IN_DIAGNOSIS`.

### Endpoints de consulta para montagem do diagnostico (tecnico)

- `GET /api/technician/servicos-catalogo`
- `GET /api/technician/servicos-catalogo/{id}`
- `GET /api/technician/pecas`
- `GET /api/technician/pecas/{id}`

Motivo: permitir que o tecnico monte o plano de diagnostico na tela de detalhe da OS sem depender de credenciais administrativas.

### Endpoint existente mantido com papel de gatilho

- `POST /api/technician/ordens-servico/{id}/submeter-plano`
  - Continua sendo o gatilho de reserva de pecas.
  - Nao executa baixa de estoque.

### Contrato de resposta da OS ampliado

- `WorkOrderResponse` passou a expor:
  - `diagnosisNotes`

## Alteracoes de dominio e persistencia

- `WorkOrder` passou a suportar atualizacao de plano em diagnostico:
  - metodo `updateDiagnosisPlan(...)`
  - recalculo de totais ao atualizar o plano.
- Inclusao de campo de observacao tecnica:
  - dominio: `diagnosisNotes`
  - entidade JPA: `observacoes_diagnostico`
  - migration Flyway: `V5__os_plano_diagnostico_observacoes.sql`

## Observacao de release

- Ja existia migration `V4__rename_enum_values_to_english.sql` no projeto.
- Para evitar conflito de versao no Flyway, a migration deste MR foi publicada como `V5`.
- Mapeadores e repositorio atualizados para persistir e retornar observacoes.

## Justificativa tecnica

- Evita acoplamento de montagem do plano com sua submissao final.
- Permite revisoes no diagnostico sem efeitos colaterais de estoque antes do momento correto.
- Mantem aderencia ao EventStorm e reduz risco de divergencia entre orcamento e consumo.
- Melhora auditabilidade com observacoes tecnicas explicitamente armazenadas.

## Compatibilidade e impacto

- Sem quebra de endpoints existentes.
- Evolucao incremental de contrato (campo adicional em resposta).
- Regra de estoque permanece consistente com o fluxo ja implementado no almoxarifado.

## Ajuste de UX no detalhe da OS

- A composicao de orcamento foi movida para o detalhe (`/internal/ordens-servico/:id`).
- Antes de iniciar diagnostico, a secao de servicos/pecas/orcamento fica oculta.
- Apos `Iniciar Diagnostico`, o tecnico pode:
  - selecionar servicos e pecas opcionais;
  - ajustar quantidades;
  - registrar observacoes;
  - salvar o plano parcial sem submeter.
- A submissao do plano continua sendo a acao que dispara reserva de pecas.

## Evolucao de almoxarifado e disponibilidade de pecas

### Novos endpoints

- `GET /api/warehouse/reservas-pendentes`
  - Lista todas as reservas pendentes (nao apenas por OS), facilitando visibilidade de OS aguardando aprovacao/liberacao.

- `GET /api/technician/pecas/disponibilidade`
  - Retorna disponibilidade considerando estoque atual e quantidade ja reservada em outras OS.
  - Campos: estoque, reservado pendente, disponivel e ponto de reposicao.

### Regra de UX aplicada

- No diagnostico, pecas sem saldo livre aparecem como **indisponiveis**.
- Se a quantidade selecionada ultrapassar o disponivel, exibimos alerta de inconsistência.
- Ao selecionar quantidade que leva ao ponto de reposicao, exibimos aviso de reposicao ainda durante a montagem da reserva.

## Ajustes de status e visibilidade para cliente

- Aprovacao interna agora promove a OS diretamente para `PENDING_APPROVAL` (aguardando cliente).
- Fluxo publico de acompanhamento bloqueia exibicao para OS ainda internas:
  - `RECEIVED`
  - `IN_DIAGNOSIS`
  - `PENDING_INTERNAL_APPROVAL`
- Aprovacao do cliente executa automaticamente:
  1. confirmacao/baixa das reservas pendentes de pecas;
  2. transicao para `IN_EXECUTION`.
- Cancelamento interno e rejeicao do cliente continuam levando para `CANCELLED`.
- Acoes finais mantidas:
  - `IN_EXECUTION` -> `Concluir Serviços` -> `FINALIZED`
  - `FINALIZED` -> `Registrar Entrega` -> `DELIVERED`

## Notificacao automatica de finalizacao (Resend)

- Ao concluir servicos (`IN_EXECUTION` -> `FINALIZED`), o backend tenta enviar email automaticamente ao cliente.
- O front nao recebe nem depende desse envio; a notificacao ocorre no backend de forma transparente.
- Campos usados na mensagem:
  - nome do cliente cadastrado;
  - modelo/marca do veiculo cadastrado.
- Configuracao por variaveis de ambiente:
  - `APP_RESEND_API_KEY`
  - `APP_RESEND_FROM_EMAIL`
- Se a chave nao estiver configurada ou o cliente nao tiver email, o backend apenas registra log e segue o fluxo normal (nao bloqueia a transicao de status).
