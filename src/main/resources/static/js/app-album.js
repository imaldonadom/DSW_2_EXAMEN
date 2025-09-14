(() => {
  const API = '/api/v1';
  const $ = sel => document.querySelector(sel);

  const ui = {
    nombre:  $('#f-nombre'),
    categoria: $('#f-categoria'),
    cantidad: $('#f-cantidad'),
    flanz:   $('#f-flanz'),
    fsor:    $('#f-fsor'),
    activo:  $('#f-activo'),
    tags:    $('#f-tags'),

    rows:    $('#rows'),
    gridMsg: $('#grid-msg'),

    btnSave: $('#btn-guardar'),
    btnClear: $('#btn-limpiar'),
    btnReload: $('#btn-reload')
  };

  async function http(url, opts={}) {
    const res = await fetch(url, {
      headers: { 'Content-Type': 'application/json' },
      ...opts
    });
    if (!res.ok) {
      const txt = await res.text().catch(()=>'');
      throw new Error(`HTTP ${res.status}: ${txt || res.statusText}`);
    }
    return res.status === 204 ? null : await res.json();
  }

  function fmtDate(d) { // d puede ser "2026-02-01" o null
    return d ? d : '-';
  }

  function chip(text) {
    const span = document.createElement('span');
    span.className = 'badge text-bg-light me-1';
    span.textContent = text;
    return span;
  }

  async function loadCategorias() {
    try {
      const data = await http(`${API}/categoria`);
      ui.categoria.innerHTML = '';
      // placeholder
      const opt0 = document.createElement('option');
      opt0.value = '';
      opt0.textContent = '— elige —';
      ui.categoria.appendChild(opt0);

      for (const c of data) {
        const opt = document.createElement('option');
        opt.value = c.id;
        opt.textContent = c.nombre;
        ui.categoria.appendChild(opt);
      }
    } catch (e) {
      console.error(e);
      ui.categoria.innerHTML = `<option value="">(error)</option>`;
    }
  }

  async function loadAlbumes() {
    ui.rows.innerHTML = '';
    ui.gridMsg.textContent = 'Cargando...';
    try {
      const data = await http(`${API}/album`);
      ui.gridMsg.textContent = data.length ? '' : 'Sin registros.';
      for (const a of data) {
        const tr = document.createElement('tr');
        const td = (...cells) => cells.map(html => {
          const c = document.createElement('td');
          if (html instanceof Node) c.appendChild(html); else c.innerHTML = html;
          return c;
        });

        // tags
        const tagsCell = document.createElement('div');
        (a.tags || []).forEach(t => tagsCell.appendChild(chip(t)));

        // fechas (2 líneas)
        const fechas = document.createElement('div');
        fechas.innerHTML = `L: <strong>${fmtDate(a.fechaLanzamiento)}</strong><br>S: <strong>${fmtDate(a.fechaSorteo)}</strong>`;

        tr.append(
          ...td(
            a.id,
            a.nombre ?? '',
            a.categoria ? a.categoria.nombre : '-',
            tagsCell,
            a.activo ? '✔️' : '—',
            fechas,
            a.cantidadLaminas ?? 0,
            `<button class="btn btn-sm btn-warning" disabled>Editar</button>
             <button class="btn btn-sm btn-danger" disabled>Eliminar</button>`
          )
        );
        ui.rows.appendChild(tr);
      }
    } catch (e) {
      console.error(e);
      ui.gridMsg.textContent = 'Error cargando datos';
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
        fechaLanzamiento: ui.flanz.value || null, // yyyy-MM-dd
        fechaSorteo: ui.fsor.value || null,
        tags: (ui.tags.value || '')
                .split(',')
                .map(s => s.trim())
                .filter(Boolean)
      };
      await http(`${API}/album`, { method:'POST', body: JSON.stringify(payload) });
      limpiar();
      await loadAlbumes();
      alert('Álbum guardado');
    } catch (e) {
      console.error(e);
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

  ui.btnSave.addEventListener('click', guardar);
  ui.btnClear.addEventListener('click', limpiar);
  ui.btnReload.addEventListener('click', loadAlbumes);

  // init
  loadCategorias().then(loadAlbumes);
})();
