// URLs base de cada microservicio
const MS_USUARIOS   = 'http://localhost:8080';
const MS_CONSULTAS  = 'http://localhost:8083';
const MS_REASIGNACION = 'http://localhost:8082';

// ─── USUARIOS (ms-usuarios del compañero) ───────────────────────────────────

export async function login(mail, pass) {
  const res = await fetch(`${MS_USUARIOS}/usuarios/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ mail, pass }),
  });
  if (!res.ok) throw new Error('Credenciales incorrectas');
  return res.json();
}

export async function listarUsuarios() {
  const res = await fetch(`${MS_USUARIOS}/usuarios`);
  if (!res.ok) throw new Error('Error al listar usuarios');
  return res.json();
}

export async function crearUsuario(usuario) {
  const res = await fetch(`${MS_USUARIOS}/usuarios`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(usuario),
  });
  if (!res.ok) throw new Error('Error al crear usuario');
}

export async function listarRoles() {
  const res = await fetch(`${MS_USUARIOS}/roles`);
  if (!res.ok) throw new Error('Error al listar roles');
  return res.json();
}

// ─── CONSULTAS (ms-consultas — el tuyo) ─────────────────────────────────────

export async function crearConsulta(dto) {
  const res = await fetch(`${MS_CONSULTAS}/consultas`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(dto),
  });
  if (!res.ok) throw new Error('Error al crear consulta');
  return res.json();
}

export async function listarTodasConsultas() {
  const res = await fetch(`${MS_CONSULTAS}/consultas`);
  if (!res.ok) throw new Error('Error al listar consultas');
  return res.json();
}

export async function listarConsultasPorUsuario(usuarioId) {
  const res = await fetch(`${MS_CONSULTAS}/consultas/usuario/${usuarioId}`);
  if (!res.ok) throw new Error('Error al listar consultas del usuario');
  return res.json();
}

export async function obtenerConsulta(id) {
  const res = await fetch(`${MS_CONSULTAS}/consultas/${id}`);
  if (!res.ok) throw new Error('Consulta no encontrada');
  return res.json();
}

// El paciente edita su nombre y síntomas
export async function editarConsultaPaciente(id, dto) {
  const res = await fetch(`${MS_CONSULTAS}/consultas/${id}/paciente`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(dto),
  });
  if (!res.ok) throw new Error('Error al editar consulta');
  return res.json();
}

// El admin actualiza estado, fecha, notas
export async function actualizarConsultaAdmin(id, dto) {
  const res = await fetch(`${MS_CONSULTAS}/consultas/${id}/admin`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(dto),
  });
  if (!res.ok) throw new Error('Error al actualizar consulta');
  return res.json();
}

export async function eliminarConsulta(id) {
  const res = await fetch(`${MS_CONSULTAS}/consultas/${id}`, { method: 'DELETE' });
  if (!res.ok) throw new Error('Error al eliminar consulta');
}

// ─── REASIGNACIÓN (ms-reasignacion del compañero) ───────────────────────────

export async function cancelarYReasignar(bloqueId, motivo) {
  const res = await fetch(
    `${MS_REASIGNACION}/api/reasignacion/cancelar-y-reasignar/${bloqueId}?motivo=${encodeURIComponent(motivo)}`,
    { method: 'POST' }
  );
  if (!res.ok) throw new Error('Error en la reasignación');
  return res.text();
}
