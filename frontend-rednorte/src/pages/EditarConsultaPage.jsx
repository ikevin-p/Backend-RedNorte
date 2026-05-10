import React, { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import Layout from '../components/Layout';
import EstadoBadge from '../components/EstadoBadge';
import { obtenerConsulta, editarConsultaPaciente } from '../services/api';

export default function EditarConsultaPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [consulta, setConsulta] = useState(null);
  const [form, setForm] = useState({ nombrePaciente: '', sintomas: '' });
  const [cargando, setCargando] = useState(true);
  const [guardando, setGuardando] = useState(false);
  const [error, setError] = useState('');
  const [exito, setExito] = useState('');

  useEffect(() => {
    obtenerConsulta(id)
      .then(c => {
        setConsulta(c);
        setForm({ nombrePaciente: c.nombrePaciente || '', sintomas: c.sintomas || '' });
      })
      .catch(() => setError('No se pudo cargar la consulta.'))
      .finally(() => setCargando(false));
  }, [id]);

  const handleSubmit = async e => {
    e.preventDefault();
    setError('');
    setExito('');
    setGuardando(true);
    try {
      await editarConsultaPaciente(id, form);
      setExito('Consulta actualizada correctamente.');
    } catch {
      setError('Error al guardar los cambios.');
    } finally {
      setGuardando(false);
    }
  };

  if (cargando) return <Layout><div className="spinner">Cargando...</div></Layout>;
  if (!consulta) return <Layout><div className="alerta alerta-error">{error}</div></Layout>;

  return (
    <Layout>
      <div style={{ display: 'flex', alignItems: 'center', gap: 16, marginBottom: 24 }}>
        <h2 style={{ fontFamily: "'DM Serif Display', serif", fontSize: 26, color: 'var(--azul)' }}>
          Editar consulta #{consulta.id}
        </h2>
        <EstadoBadge estado={consulta.estado} />
      </div>

      <div className="card" style={{ maxWidth: 560 }}>
        {error && <div className="alerta alerta-error">{error}</div>}
        {exito && <div className="alerta alerta-exito">{exito}</div>}

        <div className="form-group">
          <label>Especialidad</label>
          <input
            type="text"
            className="form-control"
            value={consulta.especialidad}
            disabled
            style={{ background: 'var(--gris-bg)', cursor: 'not-allowed' }}
          />
        </div>

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Tu nombre completo</label>
            <input
              type="text"
              className="form-control"
              value={form.nombrePaciente}
              onChange={e => setForm({ ...form, nombrePaciente: e.target.value })}
              placeholder="Tu nombre completo"
              required
            />
          </div>

          <div className="form-group">
            <label>Descripción de síntomas</label>
            <textarea
              className="form-control"
              value={form.sintomas}
              onChange={e => setForm({ ...form, sintomas: e.target.value })}
              placeholder="Describe tus síntomas..."
              required
            />
          </div>

          <div style={{ display: 'flex', gap: 12 }}>
            <button type="submit" className="btn btn-primario" disabled={guardando}>
              {guardando ? 'Guardando...' : 'Guardar cambios'}
            </button>
            <button type="button" className="btn btn-secundario" onClick={() => navigate('/mis-consultas')}>
              Volver
            </button>
          </div>
        </form>
      </div>
    </Layout>
  );
}
