// static/js/app-laminas.js (robusto con fallback de endpoints)
(() => {
  // Preferimos /api/v1/*
  const APIv1 = '/api/v1';

  // ---------- utils ----------
  const q  = (s, r=document) => r.querySelector(s);
  const qa = (s, r=document) => Array.from(r.querySelectorAll(s));
  const alertErr = (m) => window.alert(m);

  async function fetchFirstOk(urls, init) {
    let lastErr;
    for (const url of urls) {
      try {
        const r = await fetch(url, init);
        if (r.ok) return await r.json();
        lastErr = `HTTP ${r.status} ${url}`;
      } catch (e) {
        lastErr = `${e?.message ?? e}`;
      }
    }
    throw new Error(lastErr ?? 'no reachable endpoint');
  }

  // ---------- refs ----------
  let inpCant, selAlbum, selTipo, tbody, chkAll;
  function pick() {
    inpCant  = q('#inp-cantidad') || q('#cantidad') || q('input[name="cantidad"]');
    selAlbum = q('#sel-album')    || qa('select')[0];
    selTipo  = q('#sel-tipo')     || qa('select')[1];
    tbody    = q('tbody');
    chkAll   = q('#chk-all') || qa('thead input[type="checkbox"]')[0];
    if (!selAlbum || !selTipo || !tbody) {
      throw new Error('Faltan elementos en la página (álbum/tipo/tabla).');
    }
  }

  function option(v,t){const o=document.createElement('option');o.value=v;o.textContent=t;return o;}

  // ---------- combos ----------
  async function loadCombos() {
    try {
      // Álbumes
      const albums = await fetchFirstOk([
        `${APIv1}/album`,      // OK segun Swagger
        `/api/album`,          // alias antiguo
        `/api/albums`          // alias plural antiguo
      ]);
      // Tipos
      const tipos = await fetchFirstOk([
        `${APIv1}/tipo-lamina`,
        `/api/tipo-lamina`,
        `/api/tipo-laminas`
      ]);

      selAlbum.innerHTML = '';
      selAlbum.appendChild(option('', '— elige álbum —'));
      for (const a of albums) selAlbum.appendChild(option(a.id, a.nombre));

      selTipo.innerHTML = '';
      selTipo.appendChild(option('', '— tipo —'));
      for (const t of tipos) selTipo.appendChild(option(t.id, t.nombre));
    } catch (e) {
      alertErr(`Error inicializando combos.\nNo se pudieron cargar álbumes/tipos.\nfalló: ${e.message}`);
    }
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
    const tdAlb  = document.createElement('td'); tdAlb.textContent  = it.album?.nombre ?? it.album ?? '';
    const tdTipo = document.createElement('td'); tdTipo.textContent = it.tipo?.nombre ?? it.tipo ?? '';

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
      const data = await fetchFirstOk([
        `${APIv1}/lamina?albumId=${albumId}`,  // preferido
        `/api/laminas?albumId=${albumId}`      // alias viejo
      ]);
      tbody.innerHTML = '';
      data.forEach(it => tbody.appendChild(makeRow(it)));
      if (chkAll) chkAll.checked = false;
    } catch (e) {
      alertErr(`Error cargando listado.\nNo fue posible cargar láminas.\n${e.message}`);
    }
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
      // POST /api/v1/lamina/generar?albumId=&tipoId=&cantidad=
      const urls = [`${APIv1}/lamina/generar?albumId=${albumId}&tipoId=${tipoId}&cantidad=${cantidad}`];
      let ok = false, last;
      for (const u of urls) {
        const r = await fetch(u, { method: 'POST' });
        if (r.ok) { ok = true; break; }
        last = `HTTP ${r.status}`;
      }
      if (!ok) throw new Error(last ?? 'no endpoint');

      await loadGrid();
    } catch (e) { alertErr(`Error al guardar.\n${e.message}`); }
  }

  function selectedIds(){
    return qa('.chk-row').filter(c=>c.checked).map(c=>Number(c.dataset.id)).filter(Boolean);
  }

  async function deleteOne(id){
    try{
      const urls = [`${APIv1}/lamina/${id}`];
      let ok = false, last;
      for (const u of urls) {
        const r = await fetch(u, { method: 'DELETE' });
        if (r.ok) { ok=true; break; }
        last = `HTTP ${r.status}`;
      }
      if(!ok) throw new Error(last ?? 'no endpoint');
      await loadGrid();
    }catch(e){ alertErr(`Error eliminando lámina.\n${e.message}`); }
  }

  async function deleteSelection(){
    const ids = selectedIds();
    if (ids.length === 0) return alertErr('No hay filas seleccionadas.');
    try{
      const r = await fetch(`${APIv1}/lamina/delete-selection`,{
        method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify(ids)
      });
      if(!r.ok) throw new Error(`HTTP ${r.status}`);
      await loadGrid();
    }catch(e){ alertErr(`Error eliminando selección.\n${e.message}`); }
  }

  async function deleteAllByAlbum(){
    const albumId = selAlbum.value;
    if (!albumId) return alertErr('Selecciona un álbum.');
    if (!confirm('¿Eliminar TODAS las láminas de este álbum?')) return;
    try{
      const r = await fetch(`${APIv1}/lamina/by-album/${albumId}`, { method:'DELETE' });
      if(!r.ok) throw new Error(`HTTP ${r.status}`);
      await loadGrid();
    }catch(e){ alertErr(`Error al eliminar todo el álbum.\n${e.message}`); }
  }

  // ---------- wiring ----------
  function wire(){
    selAlbum?.addEventListener('change', loadGrid);
    if (chkAll) chkAll.addEventListener('change', ()=> {
      const on = chkAll.checked; qa('.chk-row').forEach(c => c.checked = on);
    });

    document.addEventListener('click', (ev) => {
      const t = ev.target;
      if (t.closest('#btn-del-sel')) { ev.preventDefault(); deleteSelection(); return; }
      if (t.closest('#btn-del-all')) { ev.preventDefault(); deleteAllByAlbum(); return; }
      if (t.closest('#btn-reload'))   { ev.preventDefault(); loadGrid(); return; }
      if (t.closest('#btn-guardar'))  { ev.preventDefault(); guardar(); return; }

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
    } catch (e) {
      alertErr(`Error inicializando pantalla de láminas.\n${e.message}`);
    }
  });
})();
