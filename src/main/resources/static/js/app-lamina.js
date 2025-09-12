// js/app-lamina.js
(() => {
  'use strict';

  // -------- Helpers de DOM ----------
  const $ = (id) => document.getElementById(id);
  const getVal = (id) => {
    const el = $(id);
    return el ? el.value.trim() : '';
  };
  const setVal = (id, v) => {
    const el = $(id);
    if (el) el.value = v ?? '';
  };

  // -------- HTTP (usa api.js si existe; si no, fallback con fetch) ----------
  const http = window.api || {
    async get(path) {
      const r = await fetch(path);
      if (!r.ok) throw new Error(`GET ${path} → ${r.status}`);
      return r.json();
    },
    async post(path, body) {
      const r = await fetch(path, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body),
      });
      if (!r.ok) throw new Error(`POST ${path} → ${r.status}`);
      return r.json();
    },
    async put(path, body) {
      const r = await fetch(path, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body),
      });
      if (!r.ok) throw new Error(`PUT ${path} → ${r.status}`);
      // algunos endpoints devuelven 200 con body, otros 204; maneja ambos
      return r.status !== 204 ? r.json() : null;
    },
    async del(path) {
      const r = await fetch(path, { method: 'DELETE' });
      if (!r.ok) throw new Error(`DELETE ${path} → ${r.status}`);
      return null;
    },
  };

  // -------- Estado --------
  let editId = null;                 // id de lámina en edición
  let albumsById = {};               // mapa id -> nombre de álbum
  let laminasCache = [];             // cache del listado

  // -------- Carga de Álbumes (combo) --------
  async function loadAlbums() {
    const sel = $('lam_album');
    if (!sel) return;

    sel.innerHTML = '<option value="">— elige álbum —</option>';

    const albums = await http.get('/api/v1/album/');
    albumsById = {};
    (albums || []).forEach(a => {
      albumsById[a.id] = a.nombre;
      const opt = document.createElement('option');
      opt.value = a.id;
      opt.textContent = a.nombre;
      sel.appendChild(opt);
    });
  }

  // -------- Listado de Láminas --------
  async function loadLaminas() {
    const tbody = $('lam_tbody');
    if (!tbody) return;

    laminasCache = await http.get('/api/v1/lamina/') || [];
    tbody.innerHTML = laminasCache.map(rowHtml).join('');
  }

  function rowHtml(r) {
    const albName = r?.album?.id != null
      ? (albumsById[r.album.id] ?? `#${r.album.id}`)
      : '-';

    const tipo = (r?.tipoLamina?.nombre)
      ?? (r?.tipoLamina?.id)
      ?? (r?.tipoLaminaId)
      ?? '-';

    return `
      <tr>
        <td>${r.id}</td>
        <td>${r.numero}</td>
        <td>${albName}</td>
        <td>${tipo}</td>
        <td class="space-x-1">
          <button class="lam-edit btn btn-xs btn-warning" data-id="${r.id}">Editar</button>
          <button class="lam-del btn btn-xs btn-danger" data-id="${r.id}">Eliminar</button>
        </td>
      </tr>
    `.trim();
  }

  // -------- Formulario --------
  function clearForm() {
    editId = null;
    setVal('lam_numero', '');
    setVal('lam_album', '');
    setVal('lam_tipo', '');
    const btn = $('lam_guardar');
    if (btn) btn.textContent = 'Guardar';
  }

  function readForm() {
    const numero = Number(getVal('lam_numero'));
    const albumId = Number(getVal('lam_album'));
    const tipoRaw = getVal('lam_tipo');
    const tipoId = tipoRaw === '' ? null : Number(tipoRaw);

    if (!Number.isFinite(numero)) throw new Error('El campo "Número" es obligatorio.');
    if (!Number.isFinite(albumId)) throw new Error('Debes elegir un "Álbum".');

    // API acepta "tipoLaminaId": null o número
    return {
      numero,
      album: { id: albumId },
      tipoLaminaId: (tipoId === null || Number.isNaN(tipoId)) ? null : tipoId
    };
  }

  async function onSave() {
    const btn = $('lam_guardar');
    try {
      if (btn) btn.disabled = true;
      const body = readForm();

      if (editId == null) {
        await http.post('/api/v1/lamina/', body);
      } else {
        await http.put(`/api/v1/lamina/${editId}`, body);
      }

      clearForm();
      await loadLaminas();
    } catch (err) {
      console.error(err);
      alert(err?.message || 'Error al guardar la lámina.');
    } finally {
      if (btn) btn.disabled = false;
    }
  }

  function onTableClick(e) {
    const t = e.target;
    if (!(t instanceof HTMLElement)) return;
    const id = t.dataset.id;
    if (!id) return;

    if (t.classList.contains('lam-edit')) {
      const row = laminasCache.find(x => String(x.id) === String(id));
      if (!row) return;

      editId = row.id;
      setVal('lam_numero', row.numero ?? '');
      setVal('lam_album', row?.album?.id ?? '');
      setVal('lam_tipo', row?.tipoLaminaId ?? row?.tipoLamina?.id ?? '');

      const btn = $('lam_guardar');
      if (btn) btn.textContent = 'Actualizar';
      window.scrollTo({ top: 0, behavior: 'smooth' });

    } else if (t.classList.contains('lam-del')) {
      onDelete(id);
    }
  }

  async function onDelete(id) {
    if (!confirm(`¿Eliminar la lámina #${id}?`)) return;
    try {
      await http.del(`/api/v1/lamina/${id}`);
      await loadLaminas();
    } catch (err) {
      console.error(err);
      alert('No se pudo eliminar la lámina.');
    }
  }

  // -------- Eventos --------
  function wireEvents() {
    const btnSave = $('lam_guardar');
    const btnClear = $('lam_limpiar');
    const btnReload = $('lam_recargar');
    const tbody = $('lam_tbody');

    if (btnSave) btnSave.addEventListener('click', onSave);
    if (btnClear) btnClear.addEventListener('click', clearForm);
    if (btnReload) btnReload.addEventListener('click', loadLaminas);
    if (tbody) tbody.addEventListener('click', onTableClick);
  }

  // -------- Init --------
  document.addEventListener('DOMContentLoaded', async () => {
    wireEvents();
    try {
      await loadAlbums();   // primero álbumes para mostrar nombres
      await loadLaminas();  // luego listado
    } catch (err) {
      console.error(err);
      alert('No se pudieron cargar los datos iniciales.');
    }
  });
})();
