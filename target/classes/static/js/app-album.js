// js/app-album.js

// --- helpers de fecha dd-MM-yyyy <-> yyyy-MM-dd ---------------
function toIso(dmy) {
  if (!dmy || !dmy.trim()) return null;
  const [dd, mm, yyyy] = dmy.split('-');
  if (!yyyy) return null;
  return `${yyyy}-${mm.padStart(2, '0')}-${dd.padStart(2, '0')}`;
}
function toDmy(iso) {
  if (!iso || !iso.trim()) return '';
  const [yyyy, mm, dd] = iso.split('-');
  return `${dd}-${mm}-${yyyy}`;
}

// --- fallback de categorías por si no hay endpoint -------------
const CATEGORIAS_FALLBACK = [
  { id: 1, nombre: 'Fútbol' },
  { id: 2, nombre: 'Música' },
  { id: 3, nombre: 'Cómics' },
  { id: 4, nombre: 'Película' },
  { id: 5, nombre: 'Manga' }
];
let categorias = [];

async function loadCategorias() {
  try {
    const res = await api.get('/api/v1/categoria');
    categorias = Array.isArray(res) && res.length ? res : CATEGORIAS_FALLBACK;
  } catch {
    categorias = CATEGORIAS_FALLBACK;
  }

  const sel = document.getElementById('albCategoria');
  sel.innerHTML = categorias.map(c => `<option value="${c.id}">${c.nombre}</option>`).join('');
}

// --- listado de álbumes ----------------------------------------
async function loadAlbums() {
  const tbody = document.getElementById('albRows');
  tbody.innerHTML = `<tr><td colspan="8" class="text-center text-muted py-4">Cargando…</td></tr>`;
  try {
    const data = await api.get('/api/v1/album');
    if (!data.length) {
      tbody.innerHTML = `<tr><td colspan="8" class="text-center text-muted py-4">Sin datos.</td></tr>`;
      return;
    }
    const rows = data.map(a => {
      const catNombre = a.categoria?.nombre ?? (categorias.find(c => c.id === a.categoriaId)?.nombre) ?? '—';
      const fechas = `L: ${a.fechaLanzamiento ? toDmy(a.fechaLanzamiento) : '—'}<br>S: ${a.fechaSorteo ? toDmy(a.fechaSorteo) : '—'}`;
      const tags = (a.tags && a.tags.length)
        ? a.tags.map(t => `<span class="tag me-1">${t}</span>`).join(' ')
        : '—';
      const activo = a.activo ? `<span class="pill-on">●</span>` : `<span class="pill-off">●</span>`;
      return `
        <tr>
          <td>${a.id}</td>
          <td>${a.nombre}</td>
          <td>${catNombre}</td>
          <td>${tags}</td>
          <td>${activo}</td>
          <td class="text-muted small">${fechas}</td>
          <td>${a.cantidadLaminas ?? 0}</td>
          <td class="text-end">
            <button class="btn btn-sm btn-warning me-2" onclick="editAlbum(${a.id})">Editar</button>
            <button class="btn btn-sm btn-danger" onclick="delAlbum(${a.id})">Eliminar</button>
          </td>
        </tr>`;
    }).join('');
    tbody.innerHTML = rows;
  } catch (e) {
    console.error(e);
    tbody.innerHTML = `<tr><td colspan="8" class="text-center text-danger py-4">Error cargando datos</td></tr>`;
  }
}

// --- cargar datos en el formulario ------------------------------
async function editAlbum(id) {
  try {
    const a = await api.get(`/api/v1/album/${id}`);
    document.getElementById('albId').value = a.id;
    document.getElementById('albNombre').value = a.nombre ?? '';
    const catId = a.categoria?.id ?? a.categoriaId ?? '';
    document.getElementById('albCategoria').value = `${catId}`;
    document.getElementById('albCantidad').value = a.cantidadLaminas ?? 0;
    document.getElementById('albActivo').value = a.activo ? 'true' : 'false';
    document.getElementById('albFecLan').value = a.fechaLanzamiento ? toDmy(a.fechaLanzamiento) : '';
    document.getElementById('albFecSor').value = a.fechaSorteo ? toDmy(a.fechaSorteo) : '';
    document.getElementById('albTags').value = (a.tags ?? []).join(', ');
    window.scrollTo({ top: 0, behavior: 'smooth' });
  } catch (e) {
    alert('No fue posible cargar el álbum.');
  }
}

function clearForm() {
  document.getElementById('albId').value = '';
  document.getElementById('albNombre').value = '';
  document.getElementById('albCategoria').selectedIndex = 0;
  document.getElementById('albCantidad').value = 0;
  document.getElementById('albActivo').value = 'true';
  document.getElementById('albFecLan').value = '';
  document.getElementById('albFecSor').value = '';
  document.getElementById('albTags').value = '';
}

// --- guardar (create/update) -----------------------------------
async function saveAlbum() {
  const id = document.getElementById('albId').value || null;
  const body = {
    nombre: document.getElementById('albNombre').value.trim(),
    categoria: { id: +document.getElementById('albCategoria').value },
    cantidadLaminas: +document.getElementById('albCantidad').value || 0,
    activo: document.getElementById('albActivo').value === 'true',
    fechaLanzamiento: toIso(document.getElementById('albFecLan').value),
    fechaSorteo: toIso(document.getElementById('albFecSor').value),
    tags: document.getElementById('albTags').value
      .split(',')
      .map(s => s.trim())
      .filter(Boolean)
  };

  try {
    if (id) {
      await api.put(`/api/v1/album/${id}`, body);
      alert('Álbum actualizado');
    } else {
      await api.post('/api/v1/album', body);
      alert('Álbum guardado');
    }
    clearForm();
    loadAlbums();
  } catch (e) {
    console.error(e);
    alert('Error guardando álbum');
  }
}

// --- eliminar ---------------------------------------------------
async function delAlbum(id) {
  if (!confirm('¿Eliminar álbum?')) return;
  try {
    await api.del(`/api/v1/album/${id}`);
    loadAlbums();
  } catch (e) {
    alert('No se pudo eliminar.');
  }
}

// --- init -------------------------------------------------------
document.getElementById('btnSave').addEventListener('click', saveAlbum);
document.getElementById('btnClear').addEventListener('click', clearForm);
document.getElementById('btnReload').addEventListener('click', loadAlbums);

(async function init(){
  await loadCategorias();
  await loadAlbums();
})();
