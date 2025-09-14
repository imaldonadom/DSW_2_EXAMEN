(() => {
  const $ = s => document.querySelector(s);

  const api = async (url, opt={}) => {
    const res = await fetch(url, {
      headers: { 'Content-Type': 'application/json' },
      ...opt
    });
    if (!res.ok) throw new Error(`${res.status} ${res.statusText} -> ${await res.text().catch(()=>'-')}`);
    return res.json();
  };

  const els = {
    cant:   $('#fCant'),
    album:  $('#fAlbum'),
    tipo:   $('#fTipo'),
    save:   $('#btnSave'),
    clear:  $('#btnClear'),
    reload: $('#btnReload'),
    tpl:    $('#btnTpl'),
    csv:    $('#fCsv'),
    tbody:  $('#tbody')
  };

  const tipoNombre = (id) => {
    if (!id || id === '' || id === '1') return 'Normal';
    if (String(id) === '2') return 'Brillante';
    if (String(id) === '3') return 'Dorada';
    return '—';
  };

  const loadAlbumes = async () => {
    els.album.innerHTML = '';
    const data = await api('/api/v1/album');
    els.album.append(new Option('— elige —', ''));
    data.forEach(a => els.album.append(new Option(`${a.nombre} (${a.id})`, a.id)));
  };

  const load = async () => {
    // Si tienes GET /api/v1/lamina, úsalo. Si no, muestra “Sin registros.”
    try {
      const list = await api('/api/v1/lamina'); // ajusta si tu endpoint es distinto
      if (!list.length) {
        els.tbody.innerHTML = `<tr><td colspan="5" class="text-center py-4">Sin registros.</td></tr>`;
        return;
      }
      els.tbody.innerHTML = list.map(l => `
        <tr>
          <td>${l.id}</td>
          <td>${l.numero}</td>
          <td>${l.album?.nombre ?? '-'}</td>
          <td>${tipoNombre(l.tipo?.id)}</td>
          <td><button class="btn btn-sm btn-outline-danger" data-evt="del" data-id="${l.id}">Eliminar</button></td>
        </tr>
      `).join('');
    } catch {
      els.tbody.innerHTML = `<tr><td colspan="5" class="text-center py-4">Sin registros.</td></tr>`;
    }
  };

  els.save.addEventListener('click', async () => {
    const n = Number(els.cant.value || 0);
    const albumId = els.album.value ? Number(els.album.value) : null;
    const tipoId = els.tipo.value ? Number(els.tipo.value) : 1;
    if (!albumId || n <= 0) return alert('Selecciona álbum y cantidad > 0');

    if (!confirm(`Crear ${n} láminas para álbum #${albumId}?`)) return;

    // Si tienes un endpoint bulk, úsalo. Aquí “simulamos” N POST consecutivos.
    try {
      for (let i = 1; i <= n; i++) {
        const body = JSON.stringify({ numero: i, albumId, tipoId: tipoId === 1 ? null : tipoId });
        await api('/api/v1/lamina', { method: 'POST', body });
      }
      await load();
    } catch (e) {
      alert('Error guardando: ' + e.message);
    }
  });

  els.clear.addEventListener('click', () => {
    els.cant.value = 60;
    els.album.value = '';
    els.tipo.value = '';
  });

  els.reload.addEventListener('click', load);

  els.tpl.addEventListener('click', () => {
    // columnas por campo
    const csv = 'numero,albumId,tipoId\n1,1,\n2,1,2\n3,1,3\n';
    const blob = new Blob([csv], { type:'text/csv;charset=utf-8;' });
    const a = document.createElement('a');
    a.href = URL.createObjectURL(blob);
    a.download = 'plantilla_laminas.csv';
    a.click();
    URL.revokeObjectURL(a.href);
  });

  els.csv.addEventListener('change', async (ev) => {
    const file = ev.target.files[0];
    if (!file) return;
    const text = await file.text();
    const rows = text.trim().split(/\r?\n/);
    const header = rows.shift().split(',').map(s=>s.trim());
    const idx = {
      numero: header.indexOf('numero'),
      albumId: header.indexOf('albumId'),
      tipoId: header.indexOf('tipoId')
    };
    if (idx.numero < 0 || idx.albumId < 0 || idx.tipoId < 0) {
      return alert('La plantilla debe tener columnas: numero,albumId,tipoId');
    }

    try {
      for (const r of rows) {
        const c = r.split(',').map(s=>s.trim());
        const body = JSON.stringify({
          numero: Number(c[idx.numero]),
          albumId: Number(c[idx.albumId]),
          tipoId: c[idx.tipoId] ? Number(c[idx.tipoId]) : null
        });
        await api('/api/v1/lamina', { method: 'POST', body });
      }
      await load();
    } catch (e) {
      alert('Error importando CSV: ' + e.message);
    } finally {
      els.csv.value = '';
    }
  });

  // init
  (async () => {
    await loadAlbumes();
    await load();
  })();
})();
