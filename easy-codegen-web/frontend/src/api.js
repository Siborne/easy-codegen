// 后端 API 封装：dev 模式走 Vite 代理(/api -> 9000)，生产模式同源托管
async function request(path, options = {}) {
  const resp = await fetch('/api' + path, {
    headers: { 'Content-Type': 'application/json' },
    ...options
  });
  if (!resp.ok) {
    throw new Error('HTTP ' + resp.status);
  }
  return resp.json();
}

export const api = {
  getConfig: () => request('/config'),
  saveConfig: (config) => request('/config', { method: 'POST', body: JSON.stringify(config) }),
  testConnection: () => request('/connection/test', { method: 'POST' }),
  listTables: () => request('/tables', { method: 'POST' }),
  generate: (config, tables, types) =>
    request('/generate', {
      method: 'POST',
      body: JSON.stringify({ config, tables, types })
    }),
  preview: (config, tables, types) =>
    request('/preview', {
      method: 'POST',
      body: JSON.stringify({ config, tables, types })
    }),
  download: async (config, tables, types) => {
    const resp = await fetch('/api/generate/download', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ config, tables, types })
    });
    const contentType = resp.headers.get('Content-Type') || '';
    if (contentType.includes('application/json')) {
      const err = await resp.json();
      throw new Error(err.message || '下载失败');
    }
    return resp.blob();
  },
  listTemplates: () => request('/templates'),
  saveTemplate: (name, config) =>
    request('/templates/' + encodeURIComponent(name), { method: 'POST', body: JSON.stringify(config) }),
  loadTemplate: (name) => request('/templates/' + encodeURIComponent(name)),
  deleteTemplate: (name) => request('/templates/' + encodeURIComponent(name), { method: 'DELETE' })
};
