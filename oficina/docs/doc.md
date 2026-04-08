# DOCUMENTO DE ARQUITETURA DE DOMÍNIO (DDD)
## SISTEMA DE OFICINA MECÂNICA

## Sumário
* [1. Introdução](#1-introdução)
* [2. Dicionário de Linguagem Ubíqua](#2-dicionário-de-linguagem-ubíqua)
* [3. Event Storming](#3-event-storming)
* [4. Swimlane da Ordem de Serviço](#4-swimlane-da-ordem-de-serviço)

## 1. Introdução

Este documento descreve a modelagem de domínio da primeira versão (MVP) do back-end para um Sistema Integrado de Atendimento e Execução de Serviços de uma oficina mecânica de médio porte. O objetivo é substituir os processos manuais que causavam erros de priorização, falhas no controle de peças e dificuldade de acompanhamento de status. O escopo engloba fluxos de Ordem de Serviço (OS), gestão de clientes, veículos e controle de estoque de peças e insumos.

## 2. Dicionário de Linguagem Ubíqua

A Linguagem Ubíqua estabelece o vocabulário compartilhado entre os desenvolvedores e os especialistas do domínio da oficina.

*   **Cliente**: Pessoa física ou jurídica (identificada por CPF ou CNPJ) que solicita os serviços da oficina.
*   **Veículo**: Automóvel do cliente que passará por manutenção, identificado de forma única por sua placa, contendo também marca, modelo e ano.
*   **Ordem de Serviço (OS)**: Entidade central do fluxo de atendimento, que registra a identificação do cliente, o veículo, os serviços solicitados, as peças necessárias e o progresso do trabalho.
*   **Serviço**: Ação ou reparo executado no veículo, como "troca de óleo" ou "alinhamento".
*   **Peças e Insumos**: Materiais físicos necessários para a execução dos serviços, que dependem de um controle de estoque rigoroso.
*   **Reserva de Peça**: Bloqueio do estoque de uma peça para uma OS, criada quando o técnico submete o plano de serviço. Confirmada pelo almoxarife ao autorizar a saída física.
*   **Orçamento**: Documento gerado automaticamente com base nos serviços e peças atrelados à OS, que deve ser enviado ao cliente para aprovação antes da execução.
*   **Aprovação Interna**: Etapa em que o administrador avalia o plano técnico antes de o orçamento ser enviado ao cliente.
*   **Status da OS**: O estado atual do ciclo de vida do atendimento, que avança automaticamente conforme ações no sistema. Pode ser: Recebida, Em diagnóstico, Aguardando aprovação interna, Aguardando aprovação do cliente, Em execução, Finalizada, Entregue ou Cancelada.
*   **Código de Acompanhamento**: UUID público da OS que permite ao cliente consultar o progresso sem autenticação.
*   **Ponto de Reposição**: Quantidade mínima de estoque de uma peça abaixo da qual é gerado um alerta para o almoxarife.
*   **Entrada de Mercadoria**: Registro de recebimento de peças que incrementa o estoque físico.

## 3. Event Storming

Abaixo estão os mapeamentos de Ações e Eventos de Domínio, cobrindo todos os atores do sistema.

| Ator | Ação de Domínio | Evento de Domínio |
|---|---|---|
| Atendente | Registra OS com cliente, veículo e serviços | OS Recebida |
| Técnico | Inicia diagnóstico | OS Em diagnóstico |
| Técnico | Submete plano (serviços + peças) | OS Aguardando aprovação interna / Reservas de peças criadas |
| Admin | Aprova plano interno | OS pronta para orçamento ao cliente |
| Admin | Reprova plano interno | OS Cancelada |
| Atendente | Envia orçamento ao cliente | OS Aguardando aprovação do cliente |
| Atendente | Retorna OS ao diagnóstico | OS Em diagnóstico |
| Cliente | Aprova orçamento | OS aguardando saída de peças |
| Cliente | Reprova orçamento | OS Cancelada |
| Almoxarife | Confirma saída física das peças | OS Em execução / Reservas confirmadas / Estoque deduzido |
| Técnico | Conclui serviços | OS Finalizada |
| Atendente | Registra entrega do veículo | OS Entregue |
| Admin | Consulta métricas | Tempo médio de execução por serviço calculado |
| Admin | Registra entrada de mercadoria | Estoque de peça incrementado |
| Sistema | Detecta peça abaixo do ponto de reposição | Alerta de estoque baixo gerado |

## Diagrama Event Storming

![Event Storming](event.png)

## Link para o diagrama interativo no Excalidraw:
https://excalidraw.com/#json=hNqS470KXMvDfNgdykG-P,66YHxm_zQ_vM3HjqrPO7qg

## 4. Swimlane da Ordem de Serviço

```
RECEBIDA
  │  (atendente cria OS)
  ↓
EM_DIAGNOSTICO
  │  (técnico inicia diagnóstico)
  ↓
EM_DIAGNOSTICO → técnico submete plano → AGUARDANDO_APROVACAO_INTERNA
                                               │
                          ┌────────────────────┤
                          ↓ admin reprova       ↓ admin aprova
                       CANCELADA         atendente envia orçamento
                                               ↓
                                    AGUARDANDO_APROVACAO (cliente)
                                               │
                          ┌────────────────────┤
                          ↓ cliente reprova     ↓ cliente aprova
                       CANCELADA         almoxarife confirma saída
                                               ↓
                                          EM_EXECUCAO
                                               │
                                        técnico conclui
                                               ↓
                                          FINALIZADA
                                               │
                                     atendente registra entrega
                                               ↓
                                            ENTREGUE
```

**Caminho de retorno:** atendente pode usar `voltar-diagnostico` para mover `AGUARDANDO_APROVACAO` de volta a `EM_DIAGNOSTICO` (ex.: cliente solicita revisão do orçamento).
