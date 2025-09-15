// static/js/app-laminas.js  (eliminar + tope por álbum)
(() => {
  const API = '/api/v1';

  // -------------------- utils --------------------
  const q  = (s, root = document) => root.querySelector(s);
  const qa = (s, root = document) => Array.from(root.querySelectorAll(s));
  const alertErr = (msg) => window.alert(msg);

  // estado en memoria
  let currentList = [];                 // láminas del álbum seleccionado
  const albumLimitById = Object.create(null); // { albumId: cantidadLaminas }

  // Busca controles aunque cambien IDs/names
  function pickElements() {
    const inpCant =
      q('#inp-cantidad') ||
      q('#cantidad') ||
      q('input[name="cantidad"]') ||
      qa('input').find(i => i.type !== 'file' && i.type !== 'hidden') ||
      null;

    const allSelects = qa('select');
    let selAlbum =
      allSelects.find(s => /album/i.test(s.id || '') || /album/i.test(s.name || '')) ||
      allSelects[0] || null;

    let selTipo =
      allSelects.find(s =>
        /tipo/i.test(s.id || '') || /tipo/i.test(s.name || '') ||
        /lamina/i.test(s.id || '') || /lamina/i.test(s.name || '')
      ) || allSelects[1] || null;

    const btnPlant  = q('#btn-plantilla') || qa('button').find(b => /plantilla|descargar/i.test(b.textContent)) || null;
    const fileCsv   = q('#file-csv')      || q('input[type="file"]') || null;
    const btnImport = q('#btn-import')    || qa('button').find(b => /importar/i.test(b.textContent)) || null;
    const btnSave   = q('#btn-guardar')   || qa('button').find(b => /guardar/i.test(b.textContent)) || null;
    const btnReload = q('#btn-reload')    || qa('button').find(b => /recargar/i.test(b.textContent)) || null;
    const tbody     = q('table tbody')    || q('tbody') || null;

    return { inpCant, selAlbum, selTipo, btnPlant, fileCsv, btnImport, btnSave, btnReload, tbody };
  }

  let inpCant, selAlbum, selTipo, btnPlant, fileCsv, btnImport, btnSave, btnReload, tbody;

  function ensureDom() {
    ({ inpCant, selAlbum, selTipo, btnPlant, fileCsv, btnImport, btnSave, btnReload, tbody } = pickElements());
    if (!selAlbum || !selTipo) throw new Error('No se encontraron los <select> de Álbum o Tipo de Lámina.');
    if (!tbody) throw new Error('No se encontró <tbody> para renderizar el listado.');
  }

  function option(id, text) {
    const o = document.createElement('option');
    o.value = id;
    o.textContent = text;
    return o;
  }

  // -------------------- combos --------------------
  async function loadCombos() {
    try {
      const [albRes, tipRes] = await Promise.all([
        fetch(`${API}/album`),
        fetch(`${API}/tipo-lamina`)
      ]);
      if (!albRes.ok) throw new Error('No se pudieron cargar álbumes.');
      if (!tipRes.ok) throw new Error('No se pudieron cargar tipos.');

      const albums = await albRes.json();
      const tipos  = await tipRes.json();

      selAlbum.innerHTML = '';
      selAlbum.appendChild(option('', '— elige —'));
      for (const a of albums) {
        selAlbum.appendChild(option(a.id, a.nombre));
        // guardamos límite si viene en el DTO (si no viene, tratamos como infinito)
        albumLimitById[a.id] = (a.cantidadLaminas ?? Number.POSITIVE_INFINITY);
      }

      selTipo.innerHTML = '';
      for (const t of tipos) selTipo.appendChild(option(t.id, t.nombre));
    } catch (e) {
      alertErr(`Error inicializando pantalla de láminas.\n${e.message ?? e}`);
    }
  }

  // -------------------- listado --------------------
  function wireRowActions() {
    qa('button[data-del]').forEach(btn => {
      btn.addEventListener('click', async () => {
        const id = btn.getAttribute('data-del');
        if (!id) return;
        if (!confirm('¿Eliminar esta lámina?')) return;
        try {
          const r = await fetch(`${API}/lamina/${id}`, { method: 'DELETE' });
          if (!r.ok) throw new Error('No fue posible eliminar.');
          await loadGrid();
        } catch (e) {
          alertErr(`Error eliminando.\n${e.message ?? e}`);
        }
      });
    });
  }

  async function loadGrid() {
    const albumId = selAlbum.value;
    if (!albumId) {
      currentList = [];
      tbody.innerHTML = '';
      return;
    }
    try {
      const r = await fetch(`${API}/lamina?albumId=${albumId}`);
      if (!r.ok) throw new Error('No fue posible cargar láminas.');
      const data = await r.json();
      currentList = Array.isArray(data) ? data : [];

      tbody.innerHTML = '';
      for (const it of currentList) {
        const tr = document.createElement('tr');
        tr.innerHTML = `
          <td>${it.id ?? ''}</td>
          <td>${it.numero ?? ''}</td>
          <td>${it.album?.nombre ?? ''}</td>
          <td>${it.tipo?.nombre ?? ''}</td>
          <td><button type="button" class="btn btn-outline-danger btn-sm" data-del="${it.id}">Eliminar</button></td>`;
        tbody.appendChild(tr);
      }
      wireRowActions();
    } catch (e) {
      alertErr(`Error cargando listado.\n${e.message ?? e}`);
    }
  }

  // -------------------- acciones --------------------
  function getAlbumLimit(albumId) {
    const limit = albumLimitById[albumId];
    return Number.isFinite(limit) ? limit : Number.POSITIVE_INFINITY;
  }

  async function guardar() {
    const albumId  = selAlbum.value;
    const tipoId   = selTipo.value;
    const agregar  = parseInt((inpCant?.value || '0'), 10);

    if (!albumId) return alertErr('Selecciona un álbum.');
    if (!tipoId)   return alertErr('Selecciona un tipo de lámina.');
    if (!agregar || agregar <= 0) return alertErr('Cantidad inválida.');

    const limit     = getAlbumLimit(albumId);
    const maxActual = currentList.length ? Math.max(...currentList.map(x => +x.numero || 0)) : 0;

    if (!Number.isFinite(limit)) {
      // sin límite definido, vamos normal
      return doGenerar(maxActual + agregar);
    }

    if (maxActual >= limit) {
      return alertErr(`El álbum ya alcanzó su máximo (${limit}).`);
    }

    const disponible = limit - maxActual;
    const toAdd = Math.min(agregar, disponible);
    if (toAdd < agregar) {
      if (!confirm(`El álbum tiene tope ${limit}. Solo se pueden agregar ${toAdd} más. ¿Continuar?`)) return;
    }

    await doGenerar(maxActual + toAdd);
  }

  async function doGenerar(totalDeseado) {
    const albumId = selAlbum.value;
    const tipoId  = selTipo.value;
    try {
      const r = await fetch(
        `${API}/lamina/generar?albumId=${albumId}&tipoId=${tipoId}&cantidad=${totalDeseado}`,
        { method: 'POST' }
      );
      if (!r.ok) {
        // intenta leer json de error
        let msg = 'No fue posible guardar.';
        try { msg = (await r.json()).message || msg; } catch { /* ignore */ }
        throw new Error(msg);
      }
      const res = await r.json();
      alert(`OK. Insertadas: ${res.inserted}, omitidas: ${res.skipped}.`);
      await loadGrid();
    } catch (e) {
      alertErr(`Error al guardar.\n${e.message ?? e}`);
    }
  }

  async function descargarPlantilla() {
    const cantidad = parseInt((inpCant?.value || '60'), 10);
    try {
      const r = await fetch(`${API}/lamina/plantilla?cantidad=${cantidad}`);
      if (!r.ok) throw new Error('No fue posible descargar la plantilla.');
      const blob = await r.blob();
      const url  = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = 'laminas_plantilla.csv';
      document.body.appendChild(a);
      a.click();
      a.remove();
      URL.revokeObjectURL(url);
    } catch (e) {
      alertErr(`Error descargando plantilla.\n${e.message ?? e}`);
    }
  }

  async function importarCsv() {
    const albumId = selAlbum.value;
    if (!albumId) return alertErr('Selecciona un álbum antes de importar.');
    const file = fileCsv?.files?.[0];
    if (!file) return alertErr('Selecciona un archivo CSV.');

    try {
      const text = await file.text();
      const lines = text.replace(/\r/g, '').split('\n').map(x => x.trim());
      const nonEmpty = lines.filter(Boolean);
      if (nonEmpty.length === 0) return alertErr('CSV vacío.');
      if (!/^numero\s*,\s*tipoId\s*$/i.test(nonEmpty[0])) {
        return alertErr('Cabecera inválida. Debe ser: numero,tipoId');
      }

      // parse
      const raw = [];
      for (let i = 1; i < nonEmpty.length; i++) {
        const [nStr, tStr] = nonEmpty[i].split(',');
        if (!nStr?.trim()) continue;
        const numero = parseInt(nStr.trim(), 10);
        const tipoId = tStr && tStr.trim() ? parseInt(tStr.trim(), 10) : null;
        raw.push({ numero, tipoId });
      }

      // validaciones de tope
      const limit = getAlbumLimit(albumId);
      const existingNumbers = new Set(currentList.map(x => +x.numero || 0));

      // 1..limit
      const inside = raw.filter(x => x.numero >= 1 && (Number.isFinite(limit) ? x.numero <= limit : true));
      // separa los que agregan nuevos números vs los que actualizan
      const nuevos = inside.filter(x => !existingNumbers.has(x.numero));
      const updates = inside.filter(x =>  existingNumbers.has(x.numero));

      let allowedNew = nuevos;
      if (Number.isFinite(limit)) {
        const remaining = limit - existingNumbers.size;
        if (remaining <= 0) {
          if (!updates.length) return alertErr(`El álbum ya está completo (${limit}). No hay espacio para nuevas láminas.`);
          allowedNew = [];
        } else if (nuevos.length > remaining) {
          if (!confirm(`Hay ${nuevos.length} números nuevos pero solo caben ${remaining}. Se importarán los primeros ${remaining}. ¿Continuar?`)) {
            return;
          }
          allowedNew = nuevos.slice(0, remaining);
        }
      }

      const items = [...updates, ...allowedNew];

      const r = await fetch(`${API}/lamina/bulk?albumId=${albumId}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(items)
      });
      if (!r.ok) {
        let msg = 'No fue posible importar CSV.';
        try { msg = (await r.json()).message || msg; } catch { /* ignore */ }
        throw new Error(msg);
      }
      const res = await r.json();
      alert(`Importado. Insertadas: ${res.inserted}, actualizadas: ${res.updated}, omitidas: ${res.skipped}.`);
      await loadGrid();
    } catch (e) {
      alertErr(`Error importando CSV.\n${e.message ?? e}`);
    }
  }

  // -------------------- wiring & init --------------------
  function wire() {
    selAlbum?.addEventListener('change', loadGrid);
    btnSave?.addEventListener('click', guardar);
    btnPlant?.addEventListener('click', descargarPlantilla);
    btnImport?.addEventListener('click', importarCsv);
    btnReload?.addEventListener('click', loadGrid);
  }

  document.addEventListener('DOMContentLoaded', async () => {
    try {
      ensureDom();
      await loadCombos();
      await loadGrid();
      wire();
    } catch (e) {
      alertErr(`Error inicializando pantalla de láminas.\n${e.message ?? e}`);
    }
  });
})();
