import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Layout from '../components/Layout';
import { useAuth } from '../context/AuthContext';
import { crearConsulta } from '../services/api';

const ESPECIALIDADES = [
  'cardiologia',
  'dermatologia',
  'gastroenterologia',
  'ginecologia',
  'medicina general',
  'neurologia',
  'oftalmologia',
  'ortopedia',
  'otorrinolaringologia',
  'pediatria',
  'psiquiatria',
  'traumatologia',
  'urologia',
];

export default function NuevaConsultaPage() {
  const { usuario } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({
    nombrePaciente: '',
    sintomas: '',
    especialidad: '',
  });
  const [error, setError] = useState('');
  const [cargando, setCargando] = useState(false);

  const handleChange = e => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async e => {
    e.preventDefault();
    setError('');
    if (!form.especialidad) { setError('Selecciona una especialidad.'); return; }
    setCargando(true);
    try {
      await crearConsulta({
        usuarioId: usuario.id,
        nombrePaciente: form.nombrePaciente,
        sintomas: form.sintomas,
        especialidad: form.especialidad,
      });
      navigate('/mis-consultas');
    } catch {
      setError('Error al crear la consulta. Intenta de nuevo.');
    } finally {
      setCargando(false);
    }
  };

  return (
    <Layout>
      <h2 style={{ fontFamily: "'DM Serif Display', serif", fontSize: 26, color: 'var(--azul)', marginBottom: 24 }}>
        Nueva consulta médica
      </h2>

      <div className="card" style={{ maxWidth: 560 }}>
        {error && <div className="alerta alerta-error">{error}</div>}

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Tu nombre completo</label>
            <input
              type="text"
              name="nombrePaciente"
              className="form-control"
              value={form.nombrePaciente}
              onChange={handleChange}
              placeholder="Ej: Juan Pérez González"
              required
            />
          </div>

          <div className="form-group">
            <label>Especialidad médica</label>
            <select
              name="especialidad"
              className="form-control"
              value={form.especialidad}
              onChange={handleChange}
              required
            >
              <option value="">Selecciona una especialidad...</option>
              {ESPECIALIDADES.map(e => (
                <option key={e} value={e} style={{ textTransform: 'capitalize' }}>
                  {e.charAt(0).toUpperCase() + e.slice(1)}
                </option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label>Describe tus síntomas</label>
            <textarea
              name="sintomas"
              className="form-control"
              value={form.sintomas}
              onChange={handleChange}
              placeholder="Describe detalladamente lo que estás sintiendo, desde cuándo y con qué frecuencia..."
              required
            />
          </div>

          <div style={{ display: 'flex', gap: 12, marginTop: 8 }}>
            <button type="submit" className="btn btn-primario" disabled={cargando}>
              {cargando ? 'Enviando...' : 'Enviar consulta'}
            </button>
            <button
              type="button"
              className="btn btn-secundario"
              onClick={() => navigate('/mis-consultas')}
            >
              Cancelar
            </button>
          </div>
        </form>
      </div>
    </Layout>
  );
}
