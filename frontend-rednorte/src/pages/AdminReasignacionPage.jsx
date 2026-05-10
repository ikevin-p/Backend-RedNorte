import React, { useEffect, useState } from 'react';
import Layout from '../components/Layout';
import EstadoBadge from '../components/EstadoBadge';
import { listarTodasConsultas, cancelarYReasignar, actualizarConsultaAdmin } from '../services/api';

export default function AdminReasignacionPage() {
  const [consultas, setConsultas] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState('');
  const [exito, setExito] = useState('');
  const [procesando, setProcesando] = useState(null);
  const [motivoMap, setMotivoMap] = useState({});

  const cargar = () => {
    setCargando(true);
    listarTodasConsultas()
      .then(setConsultas)
      .catch(() => setError('Error al cargar las consultas.'))
      .finally(() => setCargando(false));
  };

  useEffect(() => { cargar(); }, []);

  // Consultas agendadas (tienen bloquesAgendaId) → se pueden cancelar y reasignar
  const consultasAgendadas = consultas.filter(c => c.estado === 'AGENDADA' && c.bloquesAgendaId);
  // Consultas pendientes → esperando asignación
  const consultasPendientes = consultas.filter(c => c.estado === 'PENDIENTE');

  const handleCancelarYReasignar = async (consulta) => {
    const motivo = motivoMap[consulta.id] || 'Cancelación administrativa';
    setProcesando(consulta.id);
    setError('');
    setExito('');
    try {
      // 1. Marcar la consulta como CANCELADA en ms-consultas
      await actualizarConsultaAdmin(consulta.id, { estado: 'CANCELADA', notasAdmin: `Cancelada: ${motivo}` });
      // 2. Llamar a ms-reasignacion para reasignar el bloque
      const resultado = await cancelarYReasignar(consulta.bloquesAgendaId, motivo);
      setExito(`Consulta #${consulta.id} cancelada. ${resultado}`);
      cargar();
    } catch (err) {
      setError(`Error al procesar la reasignación: ${err.message}`);
    } finally {
      setProcesando(null);
    }
  };

  return (
    <Layout>
      <h2 style={{ fontFamily: "'DM Serif Display', serif", fontSize: 26, color: 'var(--azul)', marginBottom: 8 }}>
        Reasignación de citas
      </h2>
      <p style={{ color: 'var(--texto-suave)', fontSize: 14, marginBottom: 24 }}>
        Cancela una cita agendada y el sistema asigna automáticamente el bloque al siguiente paciente en lista de espera.
      </p>

      {error && <div className="alerta alerta-error">{error}</div>}
      {exito && <div className="alerta alerta-exito">{exito}</div>}

      {/* Consultas que se pueden cancelar y reasignar */}
      <div className="card">
        <h3 className="card-titulo">Citas agendadas — cancelar y reasignar</h3>
        {cargando ? (
          <div className="spinner">Cargando...</div>
        ) : consultasAgendadas.length === 0 ? (
          <p style={{ color: 'var(--texto-suave)', fontSize: 14 }}>
            No hay citas agendadas para reasignar en este momento.
          </p>
        ) : (
          <table className="tabla">
            <thead>
              <tr>
                <th>N°</th>
                <th>Paciente</th>
                <th>Especialidad</th>
                <th>Fecha cita</th>
                <th>Bloque ID</th>
                <th>Motivo cancelación</th>
                <th>Acción</th>
              </tr>
            </thead>
            <tbody>
              {consultasAgendadas.map(c => (
                <tr key={c.id}>
                  <td>#{c.id}</td>
                  <td>{c.nombrePaciente || '—'}</td>
                  <td style={{ textTransform: 'capitalize' }}>{c.especialidad}</td>
                  <td style={{ fontSize: 13 }}>
                    {c.fechaCita
                      ? new Date(c.fechaCita).toLocaleDateString('es-CL', { day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit' })
                      : '—'}
                  </td>
                  <td style={{ fontFamily: 'monospace', fontSize: 13 }}>{c.bloquesAgendaId}</td>
                  <td>
                    <input
                      type="text"
                      className="form-control"
                      style={{ fontSize: 13, padding: '6px 10px' }}
                      placeholder="Motivo de cancelación"
                      value={motivoMap[c.id] || ''}
                      onChange={e => setMotivoMap({ ...motivoMap, [c.id]: e.target.value })}
                    />
                  </td>
                  <td>
                    <button
                      className="btn btn-peligro btn-sm"
                      disabled={procesando === c.id}
                      onClick={() => handleCancelarYReasignar(c)}
                    >
                      {procesando === c.id ? 'Procesando...' : 'Cancelar y reasignar'}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {/* Consultas en espera */}
      <div className="card">
        <h3 className="card-titulo">Consultas pendientes de asignación</h3>
        {cargando ? (
          <div className="spinner">Cargando...</div>
        ) : consultasPendientes.length === 0 ? (
          <p style={{ color: 'var(--texto-suave)', fontSize: 14 }}>No hay consultas pendientes en este momento.</p>
        ) : (
          <table className="tabla">
            <thead>
              <tr>
                <th>N°</th>
                <th>Paciente</th>
                <th>Especialidad</th>
                <th>Síntomas</th>
                <th>Fecha solicitud</th>
                <th>Estado</th>
              </tr>
            </thead>
            <tbody>
              {consultasPendientes.map(c => (
                <tr key={c.id}>
                  <td>#{c.id}</td>
                  <td>{c.nombrePaciente || '—'}</td>
                  <td style={{ textTransform: 'capitalize' }}>{c.especialidad}</td>
                  <td style={{ fontSize: 13, color: 'var(--texto-suave)', maxWidth: 180 }}>
                    {c.sintomas ? c.sintomas.slice(0, 60) + (c.sintomas.length > 60 ? '...' : '') : '—'}
                  </td>
                  <td style={{ fontSize: 13, color: 'var(--texto-suave)' }}>
                    {c.fechaCreacion ? new Date(c.fechaCreacion).toLocaleDateString('es-CL') : '—'}
                  </td>
                  <td><EstadoBadge estado={c.estado} /></td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </Layout>
  );
}
