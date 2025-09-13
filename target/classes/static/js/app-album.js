// app-album.js — CRUD Álbumes

const API_BASE = '/api/v1';

// ---------------- helpers ----------------
const $ = s => document.querySelector(s);

async function api(path, { method='GET', body } = {}) {
  const res = await fetch(`${API_BASE}${path}`, {
    method,
    headers: { 'Content-Type':'application/json' },
    body: body ? JSON.stringify(body) : undefined,
  });
  if (!res.ok) {
    let msg = `HTTP ${res.status}`;
    try {
      const e = await res.json();
      msg = e?.details || e?.message || msg;
    } catch {}
    throw new Error(msg);
  }
  return res.status === 204 ? null : res.json();
}

function toISODate(v) {
  if (!v) return null;
  if (/^\d{4}-\d{2}-\d{2}$/.test(v)) return v; // yyyy-MM-dd
  const p = v.replaceAll('/', '-').split('-');
  if (p.length === 3) {
    const [dd, mm, yyyy] = p;
    return `${yyyy}-${String(mm).padStart(2,'0')}-${String(dd).padStart(2,'0')}`;
  }
  return v;
}

function escapeHtml(s) {
  return String(s)
    .replaceAll('&','&amp;').replaceAll('<','&lt;').replaceAll('>','&gt;')
    .replaceAll('"','&quot;').replaceAll("'",'&#39;');
}

// --------------- DOM refs ----------------
const $form   = $('#albumForm');
const $id     = $('#id');
const $nombre = $('#nombre');
const $catId  = $('#categoriaId');
const $cant   = $('#cantidadLaminas');
const $act    = $('#activo');
const $fL     = $('#fechaLanzamiento');
const $fS     = $('#fechaSorteo');
const $tags   = $('#tags');

const $save   = $('#saveBtn');
const $reset  = $('#resetBtn');
const $reload = $('#reloadBtn');
const $tbody  = $('#tbody');

// ------------- DTO builder ---------------
function buildDto() {
  return {
    nombre: ($nombre.value || '').trim(),
    categoria: $catId.value ? { id: Number($catId.value) } : null,
    activo: ($act.value === 'true' || $act.value === '1'),
    fechaLanzamiento: toISODate($fL.value || null),
    fechaSorteo:      toISODate($fS.value || null),
    tags: ($tags.value || '').split(',').map(t => t.trim()).filter(Boolean),
    cantidadLaminas: Number($cant.value) || 0,
  };
}

// --------------- actions -----------------
async function guardar(e) {
  e?.preventDefault?.();
  try {
    const dto = buildDto();
    const id = $id.value || null;
    if (id) {
      await api(`/album/${id}`, { method:'PUT', body:dto });
    } else {
      await api('/album/', { method:'POST', body:dto });
    }
    limpiar();
    await listar();
    alert('Álbum guardado');
  } catch (err) {
    alert('Error guardando: ' + err.message);
    console.error(err);
  }
}

function limpiar() {
  $id.value=''; $nombre.value=''; $catId.value=''; $cant.value='0';
  $act.value='true'; $fL.value=''; $fS.value=''; $tags.value='';
  $save.textContent='Guardar';
}

async function eliminar(id) {
  if (!confirm('¿Eliminar álbum ' + id + '?')) return;
  try {
    await api(`/album/${id}`, { method:'DELETE' });
    await listar();
  } catch (err) {
    alert('Error eliminando: ' + err.message);
  }
}

async function cargar(id) {
  try {
    const a = await api(`/album/${id}`);
    $id.value = a.id ?? '';
    $nombre.value = a.nombre ?? '';
    $catId.value = a.categoria?.id ?? '';
    $cant.value = (a.cantidadLaminas ?? a.cantidad_laminas ?? 0);
    $act.value = String(Boolean(a.activo));
    $fL.value = a.fechaLanzamiento || a.fecha_lanzamiento || '';
    $fS.value = a.fechaSorteo || a.fecha_sorteo || '';
    $tags.value = (Array.isArray(a.tags) ? a.tags.join(', ') : (a.tags || ''));
    $save.textContent = 'Actualizar';
    window.scrollTo({ top: 0, behavior: 'smooth' });
  } catch (err) {
    alert('No se pudo cargar: ' + err.message);
  }
}

async function listar() {
  if ($tbody) $tbody.innerHTML = `<tr><td colspan="8" class="empty">Cargando…</td></tr>`;
  try {
    const items = await api('/album/');
    if (!items.length) {
      $tbody.innerHTML = `<tr><td colspan="8" class="empty">Sin registros.</td></tr>`;
      return;
    }
    $tbody.innerHTML = items.map(a => {
      const tags = Array.isArray(a.tags) ? a.tags.map(t=>`<span class="pill">${escapeHtml(t)}</span>`).join('') : escapeHtml(a.tags||'');
      const fechas = `
        <div class="muted" style="font-size:12px">
          L: ${escapeHtml(a.fechaLanzamiento || a.fecha_lanzamiento || '-') }<br>
          S: ${escapeHtml(a.fechaSorteo || a.fecha_sorteo || '-') }
        </div>`;
      const cat = a.categoria?.nombre ?? a.categoria?.id ?? '';
      const cant = (a.cantidadLaminas ?? a.cantidad_laminas ?? 0);
      return `
        <tr>
          <td>${a.id ?? ''}</td>
          <td>${escapeHtml(a.nombre ?? '')}</td>
          <td>${escapeHtml(String(cat))}</td>
          <td>${tags}</td>
          <td>${a.activo ? '✅' : '❌'}</td>
          <td>${fechas}</td>
          <td>${cant}</td>
          <td class="actions">
            <button class="btn warning" data-edit="${a.id}">Editar</button>
            <button class="btn danger" data-del="${a.id}">Eliminar</button>
          </td>
        </tr>`;
    }).join('');
  } catch (err) {
    $tbody.innerHTML = `<tr><td colspan="8" class="empty">Error al cargar: ${escapeHtml(err.message)}</td></tr>`;
    console.error(err);
  }
}

// --------------- events ------------------
$form?.addEventListener('submit', guardar);
$reset?.addEventListener('click', limpiar);
$reload?.addEventListener('click', listar);

document.addEventListener('click', (e) => {
  const del = e.target.closest('[data-del]');
  const edit = e.target.closest('[data-edit]');
  if (del) eliminar(del.dataset.del);
  if (edit) cargar(edit.dataset.edit);
});

// init
document.addEventListener('DOMContentLoaded', listar);
