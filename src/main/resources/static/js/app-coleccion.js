(() => {
  const API = '/api/v1';
  const COLECCIONISTA_ID = 1; // Fijo para el examen

  // Helpers
  const q  = (s, r=document)=> r.querySelector(s);
  const qa = (s, r=document)=> Array.from(r.querySelectorAll(s));
  const alertErr = (m)=> window.alert(m);

  // DOM
  const selAlbum       = q('#sel-album');
  const inpCapacidadUI = q('#inp-capacidad');

  const totCapacidad   = q('#tot-capacidad');
  const totColec       = q('#tot-coleccionadas');
  const totFaltan      = q('#tot-faltan');
  const totDup         = q('#tot-duplicadas');

  const tbody          = q('#tbody-detalle');

  const btnDescargar   = q('#btn-descargar');
  const fileCsv        = q('#file-csv');
  const btnImportar    = q('#btn-importar');
  const btnRecargar    = q('#btn-recargar');

  const inpNro         = q('#inp-nro');
  const inpCantidad    = q('#inp-cantidad');
  const btnAgregar     = q('#btn-agregar');

  // Estado en memoria
  let albumes = [];
  let mapNumeroToLaminaId = new Map();   // numero -> laminaId
  let capacidadAlbum = 0;

  // ------------------------------
  // Cargar combos
  // ------------------------------
  async function loadAlbums() {
    const r = await fetch(`${API}/album`);
    if (!r.ok) throw new Error('No se pudieron cargar álbumes');
    const data = await r.json();
    albumes = data || [];

    selAlbum.innerHTML = '';
    for (const a of albumes) {
      // Intentamos detectar capacidad si viene en API (cantidadLaminas / cantLaminas / etc.)
      const cap = a.cantidadLaminas ?? a.cantLaminas ?? a.capacidad ?? a.cantidad ?? null;
      const opt = document.createElement('option');
      opt.value = a.id;
      opt.textContent = a.nombre;
      if (cap != null) opt.dataset.capacidad = String(cap);
      selAlbum.appendChild(opt);
    }
    if (selAlbum.options.length > 0) selAlbum.selectedIndex = 0;
  }

  // Cargar láminas del álbum (para mapear numero -> id)
  async function loadLaminasMap(albumId) {
    const r = await fetch(`${API}/lamina?albumId=${albumId}`);
    if (!r.ok) throw new Error('No fue posible cargar láminas');
    const list = await r.json();
    mapNumeroToLaminaId.clear();
    for (const it of list) {
      // it.numero y it.id deberían venir
      if (it?.numero != null && it?.id != null) {
        mapNumeroToLaminaId.set(Number(it.numero), Number(it.id));
      }
    }
  }

  // ------------------------------
  // Totales + Detalle
  // ------------------------------
  async function loadTotales() {
    const albumId = selAlbum.value;
    if (!albumId) return;
    const r = await fetch(`${API}/coleccion/totales?albumId=${albumId}&coleccionistaId=${COLECCIONISTA_ID}`);
    if (!r.ok) throw new Error('No fue posible cargar totales');
    const t = await r.json();

    const cap = Number(capacidadAlbum || t.capacidad || 0);
    totCapacidad.value   = String(cap);
    totColec.value       = String(t.coleccionadas ?? 0);
    totFaltan.value      = String(t.faltan ?? Math.max(0, cap - (t.coleccionadas ?? 0)));
    totDup.value         = String(t.duplicadas ?? 0);
  }

  async function loadDetalle() {
    const albumId = selAlbum.value;
    if (!albumId) return;
    const r = await fetch(`${API}/coleccion?albumId=${albumId}&coleccionistaId=${COLECCIONISTA_ID}`);
    if (!r.ok) throw new Error('No fue posible cargar detalle');
    const data = await r.json();

    tbody.innerHTML = '';
    for (const it of data) {
      const tr = document.createElement('tr');
      tr.innerHTML = `
        <td class="mono">${it.id ?? ''}</td>
        <td class="mono">${it.lamina?.numero ?? it.numero ?? ''}</td>
        <td>${it.album?.nombre ?? ''}</td>
        <td>${it.lamina?.tipo?.nombre ?? it.tipo ?? ''}</td>
        <td class="mono">${it.cantidad ?? 0}</td>
        <td class="text-end">
          <div class="btn-group btn-group-sm">
            <button class="btn btn-outline-secondary" data-acc="inc" data-id="${it.id}">+1</button>
            <button class="btn btn-outline-secondary" data-acc="dec" data-id="${it.id}">−1</button>
            <button class="btn btn-outline-danger" data-acc="del" data-id="${it.id}">Eliminar</button>
          </div>
        </td>
      `;
      tbody.appendChild(tr);
    }
  }

  async function recargarTodo() {
    const albumId = selAlbum.value;
    if (!albumId) return;

    // Capacidad desde option si existe; si no, la inferimos desde láminas distintas
    const opt = selAlbum.selectedOptions[0];
    capacidadAlbum = Number(opt?.dataset?.capacidad ?? 0);

    // Mapa de láminas (numero -> id)
    await loadLaminasMap(albumId);

    // Si no teníamos capacidad, intenta inferir de la tabla de láminas
    if (!capacidadAlbum && mapNumeroToLaminaId.size > 0) {
      capacidadAlbum = mapNumeroToLaminaId.size;
    }
    inpCapacidadUI.value = capacidadAlbum || '';

    await Promise.all([loadTotales(), loadDetalle()]);
  }

  // ------------------------------
  // Acciones detalle (+1, -1, Eliminar)
  // ------------------------------
  tbody.addEventListener('click', async (ev) => {
    const btn = ev.target.closest('button[data-acc]');
    if (!btn) return;
    const acc = btn.dataset.acc;
    const id  = btn.dataset.id;

    try {
      if (acc === 'inc' || acc === 'dec') {
        const delta = (acc === 'inc') ? 1 : -1;
        const r = await fetch(`${API}/coleccion/${id}?delta=${delta}`, { method: 'PATCH' });
        if (!r.ok) throw new Error('No fue posible actualizar');
      } else if (acc === 'del') {
        if (!confirm('¿Eliminar registro de la colección?')) return;
        const r = await fetch(`${API}/coleccion/${id}`, { method: 'DELETE' });
        if (!r.ok) throw new Error('No fue posible eliminar');
      }
      await Promise.all([loadTotales(), loadDetalle()]);
    } catch (e) {
      alertErr(e.message ?? e);
    }
  });

  // ------------------------------
  // Agregar unitario
  // ------------------------------
  btnAgregar.addEventListener('click', async () => {
    const albumId = selAlbum.value;
    if (!albumId) return alertErr('Selecciona un álbum');

    const nro = Number(inpNro.value || 0);
    const cant = Number(inpCantidad.value || 0);
    if (!nro || nro <= 0)  return alertErr('Número inválido');
    if (!cant || cant <= 0) return alertErr('Cantidad inválida');

    const laminaId = mapNumeroToLaminaId.get(nro);
    if (!laminaId) return alertErr(`No existe lámina N° ${nro} en el álbum`);

    try {
      const r = await fetch(`${API}/coleccion`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          albumId: Number(albumId),
          coleccionistaId: COLECCIONISTA_ID,
          laminaId,
          cantidad: cant
        })
      });
      if (!r.ok) throw new Error('No fue posible guardar');
      inpCantidad.value = '1';
      inpNro.value = '';
      await Promise.all([loadTotales(), loadDetalle()]);
    } catch (e) {
      alertErr(e.message ?? e);
    }
  });

  // ------------------------------
  // Plantilla CSV (cliente)
  // ------------------------------
  btnDescargar.addEventListener('click', () => {
    const albumId = selAlbum.value;
    if (!albumId) return alertErr('Selecciona un álbum');

    const cap = Number(capacidadAlbum || 0);
    if (!cap) return alertErr('No conozco la capacidad del álbum');

    let csv = 'numero,cantidad\n';
    for (let i = 1; i <= cap; i++) csv += `${i},0\n`;

    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const url  = URL.createObjectURL(blob);
    const a    = document.createElement('a');
    a.href = url;
    a.download = 'coleccion_plantilla.csv';
    a.click();
    URL.revokeObjectURL(url);
  });

  // ------------------------------
  // Importar CSV
  // ------------------------------
  btnImportar.addEventListener('click', async () => {
    const albumId = selAlbum.value;
    if (!albumId) return alertErr('Selecciona un álbum');
    const f = fileCsv.files?.[0];
    if (!f) return alertErr('Selecciona un archivo CSV');

    try {
      const text = await f.text();
      const lines = text.replace(/\r/g, '').split('\n').map(s => s.trim()).filter(Boolean);
      if (lines.length === 0) return alertErr('CSV vacío');

      if (!/^numero\s*,\s*cantidad\s*$/i.test(lines[0])) {
        return alertErr('Cabecera inválida. Debe ser: numero,cantidad');
      }
      const items = [];
      for (let i = 1; i < lines.length; i++) {
        const [nStr, cStr] = lines[i].split(',');
        const numero   = Number((nStr || '').trim());
        const cantidad = Number((cStr || '').trim());
        if (!numero || numero <= 0) continue;
        if (!Number.isFinite(cantidad) || cantidad < 0) continue;
        items.push({ numero, cantidad });
      }
      if (items.length === 0) return alertErr('No hay filas válidas en el CSV');

      const r = await fetch(`${API}/coleccion/bulk?albumId=${albumId}&coleccionistaId=${COLECCIONISTA_ID}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(items)
      });
      if (!r.ok) throw new Error('No fue posible importar CSV');
      fileCsv.value = '';
      await Promise.all([loadTotales(), loadDetalle()]);
    } catch (e) {
      alertErr(e.message ?? e);
    }
  });

  btnRecargar.addEventListener('click', recargarTodo);
  selAlbum.addEventListener('change', recargarTodo);

  // Init
  document.addEventListener('DOMContentLoaded', async () => {
    try {
      await loadAlbums();
      await recargarTodo();
    } catch (e) {
      alertErr(`Error inicializando.\n${e.message ?? e}`);
    }
  });
})();
