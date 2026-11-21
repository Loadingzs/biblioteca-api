const test = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');
const root = path.resolve(__dirname, '../../main/resources/static');
const shared = fs.readFileSync(path.join(root, 'js/scripts.js'), 'utf8');

function browser(status = 200, token = 'token-de-teste', role = 'ADMIN') {
  const storage = new Map([['token', token], ['userEmail', 'teste@example.com'], ['userRole', role]]);
  if (!token) storage.delete('token');
  const redirects = [];
  const requests = [];
  const classes = [];
  const context = vm.createContext({
    Headers, console, setTimeout, clearTimeout,
    localStorage: {
      getItem: key => storage.get(key) ?? null,
      setItem: (key, value) => storage.set(key, value),
      removeItem: key => storage.delete(key),
    },
    window: { location: { pathname: '/livros.html', replace: target => redirects.push(target) } },
    document: {
      addEventListener() {},
      querySelectorAll: () => [],
      body: { classList: { add: name => classes.push(name) } },
    },
    fetch: async (url, options) => {
      requests.push({url, options});
      return { status, ok: status === 200 };
    },
  });
  vm.runInContext(shared, context);
  return {context, storage, requests, redirects, classes};
}

test('anexa Bearer e preserva cabeçalhos da chamada', async () => {
  const state = browser();
  await vm.runInContext("fetchWithAuth('/api/livros', {method:'POST', headers:{'X-Test':'ok'}, body:'{}'})", state.context);
  assert.equal(state.requests[0].options.headers.get('Authorization'), 'Bearer token-de-teste');
  assert.equal(state.requests[0].options.headers.get('X-Test'), 'ok');
  assert.equal(state.requests[0].options.headers.get('Content-Type'), 'application/json');
});

test('sem token volta ao login sem consultar a API', async () => {
  const state = browser(200, null);
  await assert.rejects(vm.runInContext("fetchWithAuth('/api/livros')", state.context), /Autenticação necessária/);
  assert.deepEqual(state.redirects, ['login.html']);
  assert.equal(state.requests.length, 0);
});

test('401 remove a sessão e pede novo login', async () => {
  const state = browser(401);
  await assert.rejects(vm.runInContext("fetchWithAuth('/api/livros')", state.context), /sessão expirou/);
  assert.equal(state.storage.size, 0);
  assert.deepEqual(state.redirects, ['login.html']);
});

test('403 mantém a sessão e informa falta de permissão', async () => {
  const state = browser(403);
  await assert.rejects(vm.runInContext("fetchWithAuth('/api/usuarios')", state.context), /restrito/);
  assert.equal(state.storage.get('token'), 'token-de-teste');
  assert.equal(state.redirects.length, 0);
});

test('leitor recebe catálogo sem controles administrativos', () => {
  const state = browser(200, 'token', 'USER');
  assert.equal(vm.runInContext('checkAuth()', state.context), true);
  assert.deepEqual(state.classes, ['read-only']);
  state.context.window.location.pathname = '/usuarios.html';
  assert.equal(vm.runInContext('checkAuth()', state.context), false);
  assert.deepEqual(state.redirects, ['livros.html']);
});

test('texto de usuário é escapado antes de entrar nas tabelas HTML', () => {
  const state = browser();
  assert.equal(vm.runInContext(`escapeHtml('<img src=x onerror="alert(1)">')`, state.context),
    '&lt;img src=x onerror=&quot;alert(1)&quot;&gt;');
});

for (const file of fs.readdirSync(root).filter(name => name.endsWith('.html'))) {
  test('scripts válidos e sem declarações globais conflitantes: ' + file, () => {
    const html = fs.readFileSync(path.join(root, file), 'utf8');
    const scripts = [...html.matchAll(/<script([^>]*)>([\s\S]*?)<\/script>/g)];
    const code = scripts.map(([, attrs, inline]) => attrs.includes('js/scripts.js') ? shared : inline).join('\n');
    assert.doesNotThrow(() => new vm.Script(code, {filename:file}));
  });
}
