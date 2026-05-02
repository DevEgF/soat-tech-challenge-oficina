-- Rename work order status enum values from Portuguese to English
UPDATE ordens_servico SET status = 'RECEIVED'                  WHERE status = 'RECEBIDA';
UPDATE ordens_servico SET status = 'IN_DIAGNOSIS'              WHERE status = 'EM_DIAGNOSTICO';
UPDATE ordens_servico SET status = 'PENDING_INTERNAL_APPROVAL' WHERE status = 'AGUARDANDO_APROVACAO_INTERNA';
UPDATE ordens_servico SET status = 'PENDING_APPROVAL'          WHERE status = 'AGUARDANDO_APROVACAO';
UPDATE ordens_servico SET status = 'IN_EXECUTION'              WHERE status = 'EM_EXECUCAO';
UPDATE ordens_servico SET status = 'FINALIZED'                 WHERE status = 'FINALIZADA';
UPDATE ordens_servico SET status = 'DELIVERED'                 WHERE status = 'ENTREGUE';
UPDATE ordens_servico SET status = 'CANCELLED'                 WHERE status = 'CANCELADA';

-- Rename part reservation status enum values from Portuguese to English
UPDATE reservas_peca_os SET status = 'PENDING'   WHERE status = 'PENDENTE';
UPDATE reservas_peca_os SET status = 'CONFIRMED'  WHERE status = 'CONFIRMADA';
UPDATE reservas_peca_os SET status = 'CANCELLED'  WHERE status = 'CANCELADA';
