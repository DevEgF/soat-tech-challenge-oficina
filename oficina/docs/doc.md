# DOCUMENTO DE ARQUITETURA DE DOMÍNIO (DDD)
## SISTEMA DE OFICINA MECÂNICA

## Sumário
* [1. Introdução](#1-introdução)
* [2. Dicionário de Linguagem Ubíqua](#2-dicionário-de-linguagem-ubíqua)
* [3. Event Storming](#3-event-storming)

## 1. Introdução

Este documento descreve a modelagem de domínio da primeira versão (MVP) do back-end para um Sistema Integrado de Atendimento e Execução de Serviços de uma oficina mecânica de médio porte. O objetivo é substituir os processos manuais que causavam erros de priorização, falhas no controle de peças e dificuldade de acompanhamento de status. O escopo engloba fluxos de Ordem de Serviço (OS), gestão de clientes, veículos e controle de estoque de peças e insumos.

## 2. Dicionário de Linguagem Ubíqua

A Linguagem Ubíqua estabelece o vocabulário compartilhado entre os desenvolvedores e os especialistas do domínio da oficina.

*   **Cliente**: Pessoa física ou jurídica (identificada por CPF ou CNPJ) que solicita os serviços da oficina.
*   **Veículo**: Automóvel do cliente que passará por manutenção, identificado de forma única por sua placa, contendo também marca, modelo e ano.
*   **Ordem de Serviço (OS)**: Entidade central do fluxo de atendimento, que registra a identificação do cliente, o veículo, os serviços solicitados, as peças necessárias e o progresso do trabalho.
*   **Serviço**: Ação ou reparo executado no veículo, como "troca de óleo" ou "alinhamento".
*   **Peças e Insumos**: Materiais físicos necessários para a execução dos serviços, que dependem de um controle de estoque rigoroso.
*   **Orçamento**: Documento gerado automaticamente com base nos serviços e peças atrelados à OS, que deve ser enviado ao cliente para aprovação antes da execução.
*   **Status da OS**: O estado atual do ciclo de vida do atendimento, que avança automaticamente conforme ações no sistema. Pode ser: Recebida, Em diagnóstico, Aguardando aprovação, Em execução, Finalizada e Entregue.
*   **Procedimentos Técnicos**: Checklists ou instruções detalhadas e padronizadas que guiam o mecânico na execução correta de cada Serviço, garantindo a qualidade, a segurança e a rastreabilidade do trabalho realizado.

## 3. Event Storming

Abaixo estão os mapeamentos de Ações e Eventos de Domínio.

| Ação de Domínio | Evento de Domínio       |
| --- |-------------------------|
| Cliente solicita serviço | OS Recebida             |
| Mecânico inicia diagnóstico | OS Em diagnóstico       |
| Sistema gera orçamento | OS Aguardando aprovação |
| Mecânico inicia execução | OS Em execução          |
| Cliente rejeita orçamento | OS Cancelada            |
| Mecânico finaliza serviço | OS Finalizada           |
| Cliente retira veículo | OS Entregue             |

## Diagrama Event Storming

![Event Storming](event.png)

## Link para o diagrama interativo no Excalidraw:
https://excalidraw.com/#json=hNqS470KXMvDfNgdykG-P,66YHxm_zQ_vM3HjqrPO7qg
