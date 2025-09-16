// ==============================
// Examen 3 – Coleccionista (JS)
// ==============================

(() => {
  // === CONFIG ===
  const COLECCIONISTA_ID = 1;

  // === SELECTORES ===
  const $selAlbum      = byId('selAlbum');
  const $txtTotalAlbum = byId('txtTotalAlbum');
  const $btnPickCsv    = byId('btnPickCsv');
  const $btnImportCsv  = byId('btnImportCsv');
  const $btnReload     = byId('btnReload');
  const $fileCsv       = byId('fileCsv');

  const $statTotal     = byId('statTotal');
  const $statCollected = byId('statCollected');
  const $statMissing   = byId('statMissing');
  const $statDupes     = byId('statDupes');

  const $inpNumero     = byId('inpNumero');
  const $inpCantidad   = byId('inpCantidad');
  const $btnAddUnit    = byId('btnAddUnit');

  const $gridBody      = byId('gridBody');

  // === HELPERS ===
  function byId(id) { return document.getElementById(id); }

  async function http(url, opts = {}) {
    const res = await fetch(url, opts);
    if (!res.ok) {
      const txt = await res.text().catch(() => '');
      throw new Error(txt || res.statusText);
    }
    // algunos endpoints devuelven 204
    const ct = res.headers.get('content-type') || '';
    return ct.includes('application/json') ? res.json() : res.text();
  }

  function albumId() {
    return Number($selAlbum.value);
  }

  function fmt(n) { return Number(n || 0).toLocaleString(); }

  function setStats({ totalAlbum, coleccionadas, faltan, duplicadas }) {
    $statTotal.textContent     = fmt(totalAlbum);
    $statCollected.textContent = fmt(coleccionadas);
    $statMissing.textContent   = fmt(faltan);
    $statDupes.textContent     = fmt(duplicadas);
    $txtTotalAlbum.value       = fmt(totalAlbum);
  }

  function rowHtml(item) {
    // item: {id, numero, album, tipo, cantidad}
    return `
      <tr>
        <td class="text-muted">${item.id}</td>
        <td>${item.numero}</td>
        <td>${item.album ?? ''}</td>
        <td>${item.tipo ?? ''}</td>
        <td class="text-end">${fmt(item.cantidad)}</td>
        <td class="text-nowrap">
          <button class="btn btn-sm btn-light" data-act="dec" data-id="${item.id}">−</button>
          <button class="btn btn-sm btn-light" data-act="inc" data-id="${item.id}">+</button>
          <button class="btn btn-sm btn-outline-danger" data-act="del" data-id="${item.id}">Eliminar</button>
        </td>
      </tr>
    `;
  }

  function bindRowActions() {
    $gridBody.querySelectorAll('button[data-act]').forEach(btn => {
      btn.addEventListener('click', async (ev) => {
        const id  = ev.currentTarget.getAttribute('data-id');
        const act = ev.currentTarget.getAttribute('data-act');
        try {
          if (act === 'inc') {
            await http(`/api/coleccionistas/${COLECCIONISTA_ID}/albums/${albumId()}/laminas/${id}/incrementar`, { method: 'PATCH' });
          } else if (act === 'dec') {
            await http(`/api/coleccionistas/${COLECCIONISTA_ID}/albums/${albumId()}/laminas/${id}/decrementar`, { method: 'PATCH' });
          } else if (act === 'del') {
            await http(`/api/coleccionistas/${COLECCIONISTA_ID}/albums/${albumId()}/laminas/${id}`, { method: 'DELETE' });
          }
          await recargarTodo();
        } catch (e) {
          alert('Error: ' + e.message);
        }
      });
    });
  }

  // === CARGA DE ÁLBUMES (opcional – si el backend expone lista) ===
  async function cargarAlbums() {
    // si ya hay opciones en el select, respétalas
    if ($selAlbum.options.length > 0 && $selAlbum.options[0].value) return;

    try {
      // Ajusta a tu endpoint real; varios proyectos lo exponen así:
      // GET /api/albums  -> [{id, nombre, cantidadLaminas}, ...]
      const data = await http('/api/albums');
      $selAlbum.innerHTML = data.map(a =>
        `<option value="${a.id}">${a.nombre}</option>`
      ).join('');
    } catch {
      /* si no existe el endpoint, seguimos con las opciones actuales */
    }
  }

  // === CARGAS PRINCIPALES ===
  async function cargarTotales() {
    const data = await http(`/api/coleccionistas/${COLECCIONISTA_ID}/albums/${albumId()}/laminas/totales`);
    setStats(data);
  }

  async function cargarListado() {
    const data = await http(`/api/coleccionistas/${COLECCIONISTA_ID}/albums/${albumId()}/laminas`);
    $gridBody.innerHTML = data.map(rowHtml).join('') || `
      <tr><td colspan="6" class="text-center text-muted">Sin datos</td></tr>`;
    bindRowActions();
  }

  async function recargarTodo() {
    await Promise.all([cargarTotales(), cargarListado()]);
  }

  // === UNITARIO ===
  async function agregarUnitario() {
    const numero   = Number($inpNumero.value);
    const cantidad = Number($inpCantidad.value || 1);
    if (!numero || numero < 1) {
      alert('Ingresa el número de lámina.');
      $inpNumero.focus();
      return;
    }
    try {
      await http(
        `/api/coleccionistas/${COLECCIONISTA_ID}/albums/${albumId()}/laminas/unitario`,
        {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ laminaNumero: numero, cantidad })
        }
      );
      $inpCantidad.value = '1';
      await recargarTodo();
    } catch (e) {
      alert('Error al agregar: ' + e.message);
    }
  }

  // === CSV ===
  function pickCsv() { $fileCsv.click(); }

  async function importarCsv() {
    const f = $fileCsv.files?.[0];
    if (!f) { alert('Selecciona un CSV.'); return; }
    const fd = new FormData();
    fd.append('file', f);
    try {
      const resp = await http(
        `/api/coleccionistas/${COLECCIONISTA_ID}/albums/${albumId()}/laminas/masivo`,
        { method: 'POST', body: fd }
      );
      const ok   = resp.ok ?? resp.OK ?? resp.inserted ?? resp.cargadas ?? '';
      const err  = resp.error ?? resp.errors ?? resp.fallidas ?? '';
      alert(`Cargadas OK: ${ok || 'desconocido'}${err ? `, con error: ${err}` : ''}`);
      // limpia file input para que dispare de nuevo el change
      $fileCsv.value = '';
      await recargarTodo();
    } catch (e) {
      alert('Error importando CSV: ' + e.message);
    }
  }

  // === EVENTOS ===
  $selAlbum?.addEventListener('change', recargarTodo);
  $btnReload?.addEventListener('click', recargarTodo);

  $btnAddUnit?.addEventListener('click', agregarUnitario);
  $inpNumero?.addEventListener('keydown', (e) => { if (e.key === 'Enter') agregarUnitario(); });
  $inpCantidad?.addEventListener('keydown', (e) => { if (e.key === 'Enter') agregarUnitario(); });

  $btnPickCsv?.addEventListener('click', pickCsv);
  $fileCsv?.addEventListener('change', () => { $btnImportCsv?.removeAttribute('disabled'); });
  $btnImportCsv?.addEventListener('click', importarCsv);

  // === INICIO ===
  (async () => {
    try {
      await cargarAlbums();  // no rompe si no existe el endpoint
      await recargarTodo();
    } catch (e) {
      alert('Error inicial: ' + e.message);
    }
  })();

})();
