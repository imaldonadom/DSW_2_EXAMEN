// ----------------------
// Config
// ----------------------
const API = {
  // si tu controller usa /api/v1/albums cambia esta línea:
  albums: '/api/albums',
  // endpoints de colección (ya los tienes en el back)
  totales: (cid, aid) => `/api/coleccion?albumId=${aid}&coleccionistaId=${cid}`, // read list (tu controller)
  totalesNums: (cid, aid) => `/api/coleccion/totales?albumId=${aid}&coleccionistaId=${cid}`,
  upsertUnit: (cid, aid) => `/api/coleccionistas/${cid}/albums/${aid}/laminas/unitario`,
  importCsv:  (cid, aid) => `/api/coleccionistas/${cid}/albums/${aid}/laminas/masivo`
};

const COLECCIONISTA_ID = 1;

// ----------------------
// Helpers
// ----------------------
const $ = (q) => document.querySelector(q);
const $$ = (q) => document.querySelectorAll(q);

function option(value, text) {
  const o = document.createElement('option');
  o.value = String(value);
  o.textContent = text;
  return o;
}

async function fetchJSON(url, opts = {}) {
  const r = await fetch(url, opts);
  if (!r.ok) {
    const text = await r.text().catch(()=>'');
    throw new Error(text || `${r.status} ${r.statusText}`);
  }
  // algunos endpoints devuelven 204
  if (r.status === 204) return null;
  return await r.json();
}

// ----------------------
// Estado UI
// ----------------------
let state = {
  albumId: null,
  albums: [],
};

// ----------------------
// Carga de álbumes
// ----------------------
async function loadAlbums() {
  const sel = $('#albumSelect');

  // Limpia
  sel.innerHTML = '';

  // Trae álbumes
  let albums = await fetchJSON(API.albums); // [{id,nombre,cantidadLaminas,...}]
  // ordena por nombre
  albums = (albums || []).sort((a,b)=>a.nombre.localeCompare(b.nombre));

  // pinta correctamente: value = id real
  for (const a of albums) {
    sel.appendChild(option(a.id, a.nombre));
  }

  state.albums = albums;

  if (albums.length) {
    state.albumId = albums[0].id;
    sel.value = String(state.albumId);
    // pinta cantidad total del álbum si viene en el DTO
    if (albums[0].cantidadLaminas != null) {
      $('#capacidadInput').value = albums[0].cantidadLaminas;
    }
    await reloadPanel();
  }
}

// cuando cambia el select, guardamos el **id** real
$('#albumSelect').addEventListener('change', async (e) => {
  state.albumId = Number(e.target.value);
  const a = state.albums.find(x => x.id === state.albumId);
  $('#capacidadInput').value = a?.cantidadLaminas ?? 0;
  await reloadPanel();
});

// ----------------------
// Panel totales + grilla
// ----------------------
async function reloadPanel() {
  const aid = state.albumId;
  if (!aid) return;

  // totales
  try {
    const t = await fetchJSON(API.totalesNums(COLECCIONISTA_ID, aid));
    // {capacidad, coleccionadas, faltan, duplicadas} según tu controller
    $('#totCap').textContent = t.capacidad ?? 0;
    $('#totCol').textContent = t.coleccionadas ?? 0;
    $('#totFal').textContent = t.faltan ?? 0;
    $('#totDup').textContent = t.duplicadas ?? 0;
  } catch (e) {
    console.warn('totales', e.message);
    $('#totCap').textContent = '0';
    $('#totCol').textContent = '0';
    $('#totFal').textContent = '0';
    $('#totDup').textContent = '0';
  }

  // listado (si quieres pintar la tabla aquí)
  try {
    const rows = await fetchJSON(API.totales(COLECCIONISTA_ID, aid));
    // TODO: pintar filas si corresponde
    // (tu HTML original ya tenía el mensaje "click +/- para ajustar", así que dejo el render de filas tal cual lo tengas)
  } catch (e) {
    console.warn('list', e.message);
  }
}

// ----------------------
// Alta unitaria
// ----------------------
$('#btnAddUnit').addEventListener('click', async () => {
  const aid = state.albumId;
  if (!aid) return alert('Selecciona un álbum');

  const num = Number($('#numInput').value || 0);
  const cant = Number($('#cantInput').value || 1);
  if (!num || cant < 1) return alert('Número/cantidad inválidos');

  try {
    await fetchJSON(API.upsertUnit(COLECCIONISTA_ID, aid), {
      method: 'POST',
      headers: {'Content-Type':'application/json'},
      body: JSON.stringify({
        coleccionistaId: COLECCIONISTA_ID,
        albumId: aid,
        laminaNumero: num,
        cantidad: cant
      })
    });
    await reloadPanel();
  } catch (e) {
    alert(`Error: ${e.message}`);
  }
});

// ----------------------
// Import CSV (numero,cantidad)
// ----------------------
$('#btnImportCsv').addEventListener('click', async () => {
  const aid = state.albumId;
  if (!aid) return alert('Selecciona un álbum');

  const input = $('#csvInput');
  if (!input.files?.length) return alert('Selecciona un CSV');

  const fd = new FormData();
  fd.append('file', input.files[0]);

  try {
    const r = await fetchJSON(API.importCsv(COLECCIONISTA_ID, aid), {
      method: 'POST',
      body: fd
    });
    // r: {procesadas, ok, error, errores:[...]}
    await reloadPanel();
    alert(`Cargadas OK: ${r.ok}, con error: ${r.error}`);
  } catch (e) {
    alert(`Import falló: ${e.message}`);
  } finally {
    input.value = '';
  }
});

// ----------------------
// Inicio
// ----------------------
window.addEventListener('DOMContentLoaded', loadAlbums);
