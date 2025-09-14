(() => {
  const API = '/api/v1';
  const $ = (s) => document.querySelector(s);

  const ui = {
    nombre:   $('#f-nombre'),
    categoria:$('#f-categoria'),
    cantidad: $('#f-cantidad'),
    flanz:    $('#f-flanz'),
    fsor:     $('#f-fsor'),
    activo:   $('#f-activo'),
    tags:     $('#f-tags'),

    rows:     $('#rows'),
    gridMsg:  $('#grid-msg'),

    btnSave:  $('#btn-guardar'),
    btnClear: $('#btn-limpiar'),
    btnReload:$('#btn-reload'),
  };

  // fetch con salida de error clara
  async function http(url, opts = {}) {
    const res = await fetch(url, {
      ...opts,
      headers: { Accept: 'application/json', ...(opts.headers || {}) },
    });
    const isJson = (res.headers.get('content-type') || '').includes('application/json');

    if (!res.ok) {
      const body = isJson ? await res.json().catch(() => ({})) : await res.text().catch(() => '');
      const details = isJson ? JSON.stringify(body) : body;
      throw new Error(`HTTP ${res.status} ${res.statusText} → ${details}`);
    }
    return res.status === 204 ? null : (isJson ? res.json() : res.text());
  }

  const fmtDate = (d) => d ?? '-';

  const chip = (t) => {
    const s = document.createElement('span');
    s.className = 'badge text-bg-light me-1';
    s.textContent = t;
    return s;
  };

  async function loadCategorias() {
    try {
      const data = await http(`${API}/categoria`);
      ui.categoria.innerHTML = `<option value="">— elige —</option>`;
      for (const c of data) {
        const opt = document.createElement('option');
        opt.value = c.id;
        opt.textContent = c.nombre;
        ui.categoria.appendChild(opt);
      }
    } catch (e) {
      console.error('loadCategorias:', e);
      ui.categoria.innerHTML = `<option value="">(error)</option>`;
    }
  }

  async function loadAlbumes() {
    ui.rows.innerHTML = '';
    ui.gridMsg.textContent = 'Cargando…';
    try {
      const data = await http(`${API}/album`);
      ui.gridMsg.textContent = data.length ? '' : 'Sin registros.';

      for (const a of data) {
        const tr = document.createElement('tr');

        const tagsTd = document.createElement('td');
        const wrap = document.createElement('div');
        (a.tags || []).forEach((t) => wrap.appendChild(chip(t)));
        tagsTd.appendChild(wrap);

        tr.innerHTML = `
          <td>${a.id ?? ''}</td>
          <td>${a.nombre ?? ''}</td>
          <td>${a.categoria?.nombre ?? '-'}</td>
          <td></td>
          <td>${a.activo ? '✔️' : '—'}</td>
          <td>L: <strong>${fmtDate(a.fechaLanzamiento)}</strong><br>S: <strong>${fmtDate(a.fechaSorteo)}</strong></td>
          <td>${a.cantidadLaminas ?? 0}</td>
          <td>
            <button class="btn btn-sm btn-warning" disabled>Editar</button>
            <button class="btn btn-sm btn-danger" disabled>Eliminar</button>
          </td>
        `;
        // Reemplazo la 4ª celda por los chips de tags
        tr.children[3].replaceWith(tagsTd);

        ui.rows.appendChild(tr);
      }
    } catch (e) {
      console.error('loadAlbumes:', e);
      ui.gridMsg.textContent = e.message || 'Error cargando datos';
    }
  }

  async function guardar() {
    try {
      const nombre = ui.nombre.value.trim();
      const categoriaId = ui.categoria.value ? Number(ui.categoria.value) : null;
      if (!nombre || !categoriaId) {
        alert('Nombre y Categoría son obligatorios.');
        return;
      }
      const payload = {
        nombre,
        categoriaId,
        cantidadLaminas: Number(ui.cantidad.value || 0),
        activo: ui.activo.value === 'true',
        fechaLanzamiento: ui.flanz.value || null,
        fechaSorteo: ui.fsor.value || null,
        tags: (ui.tags.value || '')
          .split(',')
          .map((s) => s.trim())
          .filter(Boolean),
      };

      await http(`${API}/album`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      });

      limpiar();
      await loadAlbumes();
      alert('Álbum guardado');
    } catch (e) {
      console.error('guardar:', e);
      alert(`Error guardando: ${e.message}`);
    }
  }

  function limpiar() {
    ui.nombre.value = '';
    ui.categoria.value = '';
    ui.cantidad.value = '0';
    ui.flanz.value = '';
    ui.fsor.value = '';
    ui.activo.value = 'true';
    ui.tags.value = '';
  }

  ui.btnSave?.addEventListener('click', guardar);
  ui.btnClear?.addEventListener('click', limpiar);
  ui.btnReload?.addEventListener('click', loadAlbumes);

  // init
  loadCategorias().then(loadAlbumes);
})();
