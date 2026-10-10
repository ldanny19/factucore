ALTER TABLE comprobante
    ADD COLUMN estado_sri VARCHAR(50);

-- El resultado de autorización se conserva separado de la etapa del workflow.
UPDATE comprobante
   SET estado_sri = 'AUTORIZADO',
       estado_proceso = CASE
           WHEN ruta_ride IS NOT NULL THEN 'RIDE_GENERADO'
           ELSE 'AUTORIZACION_SRI'
       END
 WHERE estado_proceso = 'AUTORIZADO';

-- Las autorizaciones exitosas pueden identificarse por el número de autorización.
UPDATE comprobante
   SET estado_sri = 'AUTORIZADO'
 WHERE estado_proceso = 'ERROR'
   AND numero_autorizacion IS NOT NULL;

-- Normaliza el estado de rechazo heredado como resultado del SRI, no como etapa.
UPDATE comprobante
   SET estado_sri = 'RECHAZADO',
       estado_proceso = 'FIRMADO'
 WHERE estado_proceso = 'RECHAZADO';

-- Las devoluciones del SRI en la etapa de envío se identifican por la auditoría.
UPDATE comprobante c
   SET estado_sri = 'RECHAZADO',
       estado_proceso = 'FIRMADO'
 WHERE c.estado_proceso IN ('ERROR', 'RECHAZADO')
   AND EXISTS (
       SELECT 1
         FROM comprobante_auditoria a
        WHERE a.comprobante_id = c.id
          AND a.etapa = 'ENVIO_SRI'
          AND a.estado_nuevo = 'RECHAZADO'
   );

-- ERROR deja de ser un estado de proceso: se recupera la última etapa conocida.
UPDATE comprobante c
   SET estado_proceso = CASE
       WHEN c.numero_autorizacion IS NOT NULL THEN
           CASE WHEN c.ruta_ride IS NOT NULL THEN 'RIDE_GENERADO' ELSE 'AUTORIZACION_SRI' END
       ELSE COALESCE((
           SELECT CASE a.estado_anterior
               WHEN 'AUTORIZADO' THEN 'AUTORIZACION_SRI'
               WHEN 'ERROR' THEN 'FIRMADO'
               WHEN 'RECHAZADO' THEN 'FIRMADO'
               ELSE a.estado_anterior
           END
             FROM comprobante_auditoria a
            WHERE a.comprobante_id = c.id
              AND a.estado_anterior IS NOT NULL
              AND a.estado_anterior NOT IN ('ERROR', 'AUTORIZADO', 'RECHAZADO')
            ORDER BY a.fecha_inicio DESC
            LIMIT 1
       ), 'FIRMADO')
   END
 WHERE c.estado_proceso = 'ERROR';
