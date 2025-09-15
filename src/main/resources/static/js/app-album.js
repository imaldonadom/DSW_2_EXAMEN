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
    btnReload: $('#btn-reload'),
  };

  // estado edición
  let editingId = null;

  // --------------- utils http ---------------
  async function http(url, opts = {}) {
    const res = await fetch(url, {
      headers: { 'Content-Type': 'application/json' },
      ...opts
    });
    if (!res.ok) {
      // intenta parsear error del backend
      let errText = '';
      try { errText = await res.text(); } catch {}
      throw new Error(`HTTP ${res.status} → ${errText || res.statusText}`);
    }
    return res.status === 204 ? null : await res.json();
  }

  function fmtDate(d) { return d ? d : '-'; }

  function chip(text) {
    const span = document.createElement('span');
    span.className = 'badge text-bg-light me-1';
    span.textContent = text;
    return span;
  }

  // --------------- categorías ---------------
  let categoriasLoaded = null;

  function loadCategorias() {
    // memoiza la carga para que podamos esperar desde edición
    if (!categoriasLoaded) {
      categoriasLoaded = (async () => {
        try {
          const data = await http(`${API}/categoria`);
          ui.categoria.innerHTML = '';
          const opt0 = document.createElement('option');
          opt0.value = '';
          opt0.textContent = '— elige —';
          ui.categoria.appendChild(opt0);

          for (const c of data) {
            const opt = document.createElement('option');
            opt.value = String(c.id);       // <-- value como string
            opt.textContent = c.nombre;
            ui.categoria.appendChild(opt);
          }
        } catch (e) {
          console.error(e);
          ui.categoria.innerHTML = `<option value="">(error)</option>`;
        }
      })();
    }
    return categoriasLoaded;
  }

  // --------------- tabla álbumes ---------------
  async function loadAlbumes() {
    ui.rows.innerHTML = '';
    ui.gridMsg.textContent = 'Cargando...';
    try {
      const data = await http(`${API}/album`);
      ui.gridMsg.textContent = data.length ? '' : 'Sin registros.';

      for (const a of data) {
        const tr = document.createElement('tr');

        // helpers para <td>
        const td = (...cells) => cells.map(html => {
          const c = document.createElement('td');
          if (html instanceof Node) c.appendChild(html); else c.innerHTML = html;
          return c;
        });

        // tags
        const tagsCell = document.createElement('div');
        (a.tags || []).forEach(t => tagsCell.appendChild(chip(t)));

        // fechas
        const fechas = document.createElement('div');
        fechas.innerHTML = `L: <strong>${fmtDate(a.fechaLanzamiento)}</strong><br>S: <strong>${fmtDate(a.fechaSorteo)}</strong>`;

        // acciones
        const actions = document.createElement('div');

        const btnEdit = document.createElement('button');
        btnEdit.className = 'btn btn-sm btn-warning me-1';
        btnEdit.textContent = 'Editar';
        btnEdit.onclick = () => startEdit(a);

        const btnDel = document.createElement('button');
        btnDel.className = 'btn btn-sm btn-danger';
        btnDel.textContent = 'Eliminar';
        btnDel.onclick = () => eliminar(a.id);

        actions.append(btnEdit, btnDel);

        tr.append(
          ...td(
            a.id,
            a.nombre ?? '',
            a.categoria ? a.categoria.nombre : '-',   // muestra nombre
            tagsCell,
            a.activo ? '✔️' : '—',
            fechas,
            a.cantidadLaminas ?? 0,
            actions
          )
        );

        ui.rows.appendChild(tr);
      }
    } catch (e) {
      console.error(e);
      ui.gridMsg.textContent = 'Error cargando datos';
    }
  }

  // --------------- crear/editar/eliminar ---------------
  async function guardar() {
    try {
      const nombre = ui.nombre.value.trim();
      const categoriaId = ui.categoria.value ? Number(ui.categoria.value) : null; // <-- a número
      if (!nombre || !categoriaId) {
        alert('Nombre y Categoría son obligatorios.');
        return;
      }
      const payload = {
        nombre,
        categoriaId,                                        // <-- id numérico
        cantidadLaminas: Number(ui.cantidad.value || 0),
        activo: ui.activo.value === 'true',
        fechaLanzamiento: ui.flanz.value || null,           // yyyy-MM-dd
        fechaSorteo: ui.fsor.value || null,
        tags: (ui.tags.value || '')
          .split(',')
          .map(s => s.trim())
          .filter(Boolean)
      };

      if (editingId) {
        await http(`${API}/album/${editingId}`, { method:'PUT', body: JSON.stringify(payload) });
        alert('Álbum actualizado');
      } else {
        await http(`${API}/album`, { method:'POST', body: JSON.stringify(payload) });
        alert('Álbum guardado');
      }
      limpiar();
      await loadAlbumes();
    } catch (e) {
      console.error(e);
      alert(e.message);
    }
  }

  function limpiar() {
    editingId = null;
    ui.btnSave.textContent = 'Guardar';
    ui.nombre.value = '';
    ui.categoria.value = '';
    ui.cantidad.value = '0';
    ui.flanz.value = '';
    ui.fsor.value = '';
    ui.activo.value = 'true';
    ui.tags.value = '';
  }

  async function eliminar(id) {
    if (!confirm('¿Eliminar álbum? Esta acción no se puede deshacer.')) return;
    try {
      await http(`${API}/album/${id}`, { method:'DELETE' });
      await loadAlbumes();
    } catch (e) {
      console.error(e);
      alert(e.message);
    }
  }

  // --------------- edición ---------------
  function startEdit(a) {
    editingId = a.id;
    ui.btnSave.textContent = 'Actualizar';

    ui.nombre.value = a.nombre ?? '';
    ui.cantidad.value = String(a.cantidadLaminas ?? 0);
    ui.flanz.value = a.fechaLanzamiento ?? '';
    ui.fsor.value = a.fechaSorteo ?? '';
    ui.activo.value = a.activo ? 'true' : 'false';
    ui.tags.value = (a.tags || []).join(', ');

    // asegura categorías cargadas y setea el valor como STRING
    loadCategorias().then(() => {
      ui.categoria.value = a.categoria ? String(a.categoria.id) : '';
    });
  }

  // --------------- eventos ---------------
  ui.btnSave.addEventListener('click', guardar);
  ui.btnClear.addEventListener('click', limpiar);
  ui.btnReload.addEventListener('click', loadAlbumes);

  // --------------- init ---------------
  loadCategorias().then(loadAlbumes);
})();
