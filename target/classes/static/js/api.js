const API = 'http://localhost:8080/api/v1';

async function http(url, options = {}) {
  const res = await fetch(url, {
    headers: { 'Content-Type':'application/json' },
    ...options
  });
  if (!res.ok) {
    let msg = await res.text();
    try { msg = JSON.parse(msg); } catch(e){}
    const detail = typeof msg === 'string' ? msg : (msg.details || msg.message || res.statusText);
    throw new Error(detail);
  }
  return res.status === 204 ? null : res.json();
}

function pick(obj, keys, fallback=undefined){
  for (const k of keys) if (obj && obj[k] !== undefined && obj[k] !== null) return obj[k];
  return fallback;
}

function normalizeDateStr(s){
  if (!s) return '';
  const m = /^(\d{4})-(\d{2})-(\d{2})$/.exec(s);        // yyyy-mm-dd
  if (m) return `${m[3]}-${m[2]}-${m[1]}`;
  const m2 = /^(\d{2})[\/\-](\d{2})[\/\-](\d{4})$/.exec(s); // dd/mm/aaaa o dd-mm-aaaa
  if (m2) return `${m2[1]}-${m2[2]}-${m2[3]}`;
  return s;
}
