// js/app-lamina.js

// Fallback de tipos si el endpoint no existe
const TIPOS_FALLBACK = [
  { id: 1, nombre: 'Normal' },
  { id: 2, nombre: 'Brillante' },
  { id: 3, nombre: 'Dorada' }
];

let tipos = [];
let albums = [];

async function loadTipos() {
  try {
    const res = await api.get('/api/v1/tipo-lamina');
    tipos = Array.isArray(res) && res.length ? res : TIPOS_FALLBACK;
  } catch {
    tipos = TIPOS_FALLBACK;
  }
  const sel = document.getElementById('lamTipo');
  sel.innerHTML = tipos.map(t => `<option value="${t.id}">${t.nombre}</option>`).join('');
  sel.value = '1'; // por defecto Normal
}

async function loadAlbums() {
  try {
    albums = await api.get('/api/v1/album');
  } catch {
    albums = [];
  }
  const sel = document.getElementById('lamAlbum');
  sel.innerHTML = `<option value="">— elige —</option>` +
    albums.map(a => `<option value="${a.id}">${a.nombre}</option>`).join('');
}

async function loadLaminas() {
  const tbody = document.getElementById('lamRows');
  tbody.innerHTML = `<tr><td colspan="5" class="text-center text-muted py-4">Cargando…</td></tr>`;
  try {
    const data = await api.get('/api/v1/lamina');
    if (!data.length) {
      tbody.innerHTML = `<tr><td colspan="5" class="text-center text-muted py-4">Sin registros.</td></tr>`;
      return;
    }
    const rows = data.map(l => {
      const albNom = l.album?.nombre ?? (albums.find(a => a.id === l.albumId)?.nombre) ?? `#${l.album?.id ?? l.albumId}`;
      const tipoNom = l.tipo?.nombre ?? (tipos.find(t => t.id === l.tipoId)?.nombre) ?? 'Normal';
      return `
        <tr>
          <td>${l.id}</td>
          <td>${l.numero}</td>
          <td>${albNom}</td>
          <td>${tipoNom}</td>
          <td class="text-end">
            <button class="btn btn-sm btn-danger" onclick="delLamina(${l.id})">Eliminar</button>
          </td>
        </tr>
      `;
    }).join('');
    tbody.innerHTML = rows;
  } catch (e) {
    console.error(e);
    tbody.innerHTML = `<tr><td colspan="5" class="text-center text-danger py-4">Error cargando datos</td></tr>`;
  }
}

function clearForm() {
  document.getElementById('lamCantidad').value = 60;
  document.getElementById('lamAlbum').value = '';
  document.getElementById('lamTipo').value = '1';
  document.getElementById('lamCsv').value = '';
}

// Crear N láminas consecutivas 1..cantidad (si existen, el backend debería ignorar/validar)
async function saveCantidad() {
  const cantidad = +document.getElementById('lamCantidad').value || 0;
  const albumId = document.getElementById('lamAlbum').value;
  const tipoId = document.getElementById('lamTipo').value || 1;

  if (!albumId) {
    alert('Elige un álbum');
    return;
  }
  if (cantidad < 1) {
    alert('Cantidad inválida');
    return;
  }

  // Para evitar muchos errores por duplicado, pedimos las existentes y saltamos repetidos
  const existentes = await api.get('/api/v1/lamina');
  const ya = new Set(
    existentes.filter(x => (x.album?.id ?? x.albumId) == albumId)
              .map(x => x.numero)
  );

  const toCreate = [];
  for (let n = 1; n <= cantidad; n++) {
    if (!ya.has(n)) {
      toCreate.push({ numero: n, album: {id: +albumId}, tipo: {id: +tipoId} });
    }
  }

  if (!toCreate.length) {
    alert('No hay láminas nuevas para crear (parecen existir todas).');
    return;
  }

  try {
    // sin endpoint bulk, hacemos uno a uno
    for (const item of toCreate) {
      await api.post('/api/v1/lamina', item);
    }
    alert(`Creadas ${toCreate.length} láminas nuevas`);
    loadLaminas();
  } catch (e) {
    console.error(e);
    alert('Error creando láminas.');
  }
}

async function delLamina(id) {
  if (!confirm('¿Eliminar lámina?')) return;
  try {
    await api.del(`/api/v1/lamina/${id}`);
    loadLaminas();
  } catch {
    alert('No se pudo eliminar.');
  }
}

// Plantilla CSV
document.getElementById('btnTpl').addEventListener('click', () => {
  const blob = new Blob(
    [`numero,albumId,tipoId\n1,1,1\n2,1,2\n3,1,3\n`],
    { type: 'text/csv;charset=utf-8' }
  );
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url; a.download = 'laminas_template.csv';
  document.body.appendChild(a); a.click(); a.remove();
  URL.revokeObjectURL(url);
});

// Importar CSV (columnas: numero,albumId,tipoId)
document.getElementById('lamCsv').addEventListener('change', async (e) => {
  const file = e.target.files?.[0];
  if (!file) return;

  const text = await file.text();
  const lines = text.split(/\r?\n/).map(l => l.trim()).filter(Boolean);
  const header = lines.shift();
  if (!/^numero\s*,\s*albumId\s*,\s*tipoId$/i.test(header)) {
    alert('Cabecera CSV inválida. Debe ser: numero,albumId,tipoId');
    return;
  }

  const registros = lines.map(l => {
    const [numero, albumId, tipoId] = l.split(',').map(x => x.trim());
    return { numero: +numero, album: {id:+albumId}, tipo: {id:+tipoId||1} };
  }).filter(r => r.numero > 0 && r.album.id > 0);

  if (!registros.length) {
    alert('El CSV está vacío.');
    return;
  }

  try {
    for (const r of registros) {
      await api.post('/api/v1/lamina', r);
    }
    alert(`Importadas ${registros.length} láminas`);
    loadLaminas();
  } catch (e) {
    console.error(e);
    alert('Error al importar CSV.');
  }
});

document.getElementById('btnLamSave').addEventListener('click', saveCantidad);
document.getElementById('btnLamClear').addEventListener('click', clearForm);
document.getElementById('btnLamReload').addEventListener('click', loadLaminas);

(async function init(){
  await loadTipos();
  await loadAlbums();
  await loadLaminas();
})();
