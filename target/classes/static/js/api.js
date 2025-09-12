// Helpers mínimos para llamar a la API REST
window.API = {
  base: '/api/v1',
  async request(path, { method = 'GET', body, headers } = {}) {
    const res = await fetch(`${this.base}${path}`, {
      method,
      headers: { 'Content-Type': 'application/json', ...(headers || {}) },
      body: body ? JSON.stringify(body) : undefined,
    });
    if (!res.ok) {
      let err;
      try { err = await res.json(); } catch {}
      const msg = err?.details || err?.message || `HTTP ${res.status}`;
      throw new Error(msg);
    }
    return res.status === 204 ? null : res.json();
  },

  album: {
    list() { return API.request('/album/'); },
  },

  lamina: {
    list()          { return API.request('/lamina/'); },
    get(id)         { return API.request(`/lamina/${id}`); },
    create(dto)     { return API.request('/lamina/', { method: 'POST', body: dto }); },
    update(id, dto) { return API.request(`/lamina/${id}`, { method: 'PUT', body: dto }); },
    delete(id)      { return API.request(`/lamina/${id}`, { method: 'DELETE' }); },
  },

  tipos: {
    list() { return API.request('/tipo-lamina/'); } 
  }
};
