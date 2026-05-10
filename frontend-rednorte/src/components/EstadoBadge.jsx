import React from 'react';

const CLASES = {
  PENDIENTE:  'badge badge-pendiente',
  AGENDADA:   'badge badge-agendada',
  REASIGNADA: 'badge badge-reasignada',
  CANCELADA:  'badge badge-cancelada',
  ATENDIDA:   'badge badge-atendida',
};

export default function EstadoBadge({ estado }) {
  return (
    <span className={CLASES[estado] || 'badge badge-pendiente'}>
      {estado}
    </span>
  );
}
