// static/js/app-laminas.js – eliminación por fila / selección / todo el álbum (robusto)
(() => {
  const API = '/api/v1';

  // ---------- utils ----------
  const q  = (s, r=document) => r.querySelector(s);
  const qa = (s, r=document) => Array.from(r.querySelectorAll(s));
  const alertErr = (m) => window.alert(m);

  // ---------- refs ----------
  let inpCant, selAlbum, selTipo, tbody, chkAll;

  function pick() {
    inpCant  = q('#inp-cantidad') || q('#cantidad') || q('input[name="cantidad"]');
    selAlbum = q('#sel-album')    || qa('select')[0];
    selTipo  = q('#sel-tipo')     || qa('select')[1];
    tbody    = q('tbody');
    chkAll   = q('#chk-all') || qa('thead input[type="checkbox"]')[0];

    if (!selAlbum || !selTipo || !tbody) {
      throw new Error('Faltan elementos esenciales en la página (álbum/tipo/tabla).');
    }
  }

  function option(v,t){const o=document.createElement('option');o.value=v;o.textContent=t;return o;}

  // ---------- combos ----------
  async function loadCombos() {
    try {
      const [ra, rt] = await Promise.all([fetch(`${API}/album`), fetch(`${API}/tipo-lamina`)]);
      if (!ra.ok) throw new Error('No se pudieron cargar álbumes');
      if (!rt.ok) throw new Error('No se pudieron cargar tipos');

      const albums = await ra.json();
      const tipos  = await rt.json();

      selAlbum.innerHTML = ''; selAlbum.appendChild(option('', '— elige —'));
      for (const a of albums) selAlbum.appendChild(option(a.id, a.nombre));

      selTipo.innerHTML = '';
      for (const t of tipos) selTipo.appendChild(option(t.id, t.nombre));
    } catch (e) { alertErr(`Error inicializando combos.\n${e.message ?? e}`); }
  }

  // ---------- grilla ----------
  function makeRow(it){
    const tr = document.createElement('tr');

    const tdSel = document.createElement('td');
    const cb = document.createElement('input');
    cb.type = 'checkbox'; cb.className = 'chk-row'; cb.dataset.id = it.id;
    tdSel.appendChild(cb);

    const tdId   = document.createElement('td'); tdId.textContent   = it.id ?? '';
    const tdNum  = document.createElement('td'); tdNum.textContent  = it.numero ?? '';
    const tdAlb  = document.createElement('td'); tdAlb.textContent  = it.album?.nombre ?? '';
    const tdTipo = document.createElement('td'); tdTipo.textContent = it.tipo?.nombre ?? '';

    const tdAcc = document.createElement('td');
    const b = document.createElement('button');
    b.type = 'button';
    b.textContent = 'Eliminar';
    b.className = 'btn btn-outline-danger btn-sm btn-del-one';
    b.dataset.id = it.id;
    tdAcc.appendChild(b);

    tr.append(tdSel, tdId, tdNum, tdAlb, tdTipo, tdAcc);
    return tr;
  }

  async function loadGrid() {
    const albumId = selAlbum.value;
    if (!albumId) { tbody.innerHTML = ''; return; }
    try {
      const r = await fetch(`${API}/lamina?albumId=${albumId}`);
      if (!r.ok) throw new Error('No fue posible cargar láminas.');
      const data = await r.json();
      tbody.innerHTML = '';
      data.forEach(it => tbody.appendChild(makeRow(it)));
      if (chkAll) chkAll.checked = false;
    } catch (e) { alertErr(`Error cargando listado.\n${e.message ?? e}`); }
  }

  // ---------- acciones ----------
  async function guardar() {
    const albumId  = selAlbum.value;
    const tipoId   = selTipo.value;
    const cantidad = parseInt((inpCant?.value || '0'), 10);
    if (!albumId)  return alertErr('Selecciona un álbum.');
    if (!tipoId)   return alertErr('Selecciona un tipo.');
    if (!cantidad || cantidad <= 0) return alertErr('Cantidad inválida.');
    try {
      const r = await fetch(`${API}/lamina/generar?albumId=${albumId}&tipoId=${tipoId}&cantidad=${cantidad}`, { method: 'POST' });
      if (!r.ok) throw new Error(`No fue posible guardar. (HTTP ${r.status})`);
      const res = await r.json();
      alert(`OK. Insertadas: ${res.inserted}, omitidas: ${res.skipped}.`);
      await loadGrid();
    } catch (e) { alertErr(`Error al guardar.\n${e.message ?? e}`); }
  }

  function selectedIds(){
    return qa('.chk-row').filter(c=>c.checked).map(c=>Number(c.dataset.id)).filter(Boolean);
  }

  async function deleteOne(id){
    try{
      const r = await fetch(`${API}/lamina/${id}`, { method: 'DELETE' });
      if (!r.ok) throw new Error(`No fue posible eliminar la lámina. (HTTP ${r.status})`);
      await loadGrid();
    }catch(e){ alertErr(`Error eliminando lámina.\n${e.message ?? e}`); }
  }

  async function deleteSelection(){
    const ids = selectedIds();
    if (ids.length === 0) return alertErr('No hay filas seleccionadas.');
    try{
      const r = await fetch(`${API}/lamina/delete-selection`,{
        method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify(ids)
      });
      if(!r.ok) throw new Error(`No fue posible eliminar selección. (HTTP ${r.status})`);
      await loadGrid();
    }catch(e){ alertErr(`Error eliminando selección.\n${e.message ?? e}`); }
  }

  async function deleteAllByAlbum(){
    const albumId = selAlbum.value;
    if (!albumId) return alertErr('Selecciona un álbum.');
    if (!confirm('¿Eliminar TODAS las láminas de este álbum?')) return;
    try{
      const r = await fetch(`${API}/lamina/by-album/${albumId}`, { method:'DELETE' });
      if(!r.ok) throw new Error(`No fue posible eliminar todo el álbum. (HTTP ${r.status})`);
      await loadGrid();
    }catch(e){ alertErr(`Error al eliminar todo el álbum.\n${e.message ?? e}`); }
  }

  // ---------- wiring (delegación global) ----------
  function wire(){
    selAlbum?.addEventListener('change', loadGrid);
    if (chkAll) chkAll.addEventListener('change', ()=> {
      const on = chkAll.checked; qa('.chk-row').forEach(c => c.checked = on);
    });

    document.addEventListener('click', (ev) => {
      const t = ev.target;

      // Top bar
      if (t.closest('#btn-del-sel')) { ev.preventDefault(); deleteSelection(); return; }
      if (t.closest('#btn-del-all')) { ev.preventDefault(); deleteAllByAlbum(); return; }
      if (t.closest('#btn-reload'))   { ev.preventDefault(); loadGrid(); return; }
      if (t.closest('#btn-guardar'))  { ev.preventDefault(); guardar(); return; }

      // Por fila
      const delOne = t.closest('.btn-del-one');
      if (delOne) { ev.preventDefault(); const id = Number(delOne.dataset.id); if(id) deleteOne(id); }
    });
  }

  // ---------- init ----------
  document.addEventListener('DOMContentLoaded', async () => {
    try {
      pick();
      await loadCombos();
      await loadGrid();
      wire();
    } catch (e) { alertErr(`Error inicializando pantalla de láminas.\n${e.message ?? e}`); }
  });
})();
