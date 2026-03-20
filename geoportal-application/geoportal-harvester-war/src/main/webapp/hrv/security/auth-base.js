define([], function() {
  // ===== Context & BASE discovery (works for WAR context or ROOT) =====
  const ORIGIN = window.location.origin || (window.location.protocol + '//' + window.location.host);

  function getContextPath() {
    const parts = window.location.pathname.split('/').filter(Boolean);
    // If this page is at /<context>/file.html, the first segment is the WAR context.
    return parts.length > 0 ? '/' + parts[0] : '';
  }

  const CONTEXT = getContextPath();              // e.g., "/geoportal-harvester-war" or ""
  const BASE    = ORIGIN + CONTEXT;              // e.g., "http://localhost:8080/geoportal-harvester-war"

  // ===== App routes (customize as needed) =====
  const HOME_URL       = `${BASE}/#/home`;
  const AUTHZ_ENDPOINT = `${BASE}/oauth2/authorize`; // SAS authorize
  const TOKEN_ENDPOINT = `${BASE}/oauth2/token`;     // SAS token
  const ARCGIS_ENDPOINT = `${BASE}/login/oauth2/code/arcgis`; // ArcGIS OAuth2 callback
  const REDIRECT_URI   = `${BASE}/callback-popup.html`; // must be registered in RegisteredClient
  const CLIENT_ID      = 'harvester-ui-client';         // keep in sync with server config
  const SCOPES         = ['openid', 'profile', 'api.read'];

  // ===== PKCE helpers =====
  async function sha256(input) {
    const data = new TextEncoder().encode(input);
    const hash = await crypto.subtle.digest('SHA-256', data);
    return new Uint8Array(hash);
  }
  function b64url(bytes) {
    return btoa(String.fromCharCode(...bytes)).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/,'');
  }
  function randomUrlSafe(len=64) {
    const a = new Uint8Array(len);
    crypto.getRandomValues(a);
    return b64url(a).substring(0, len);
  }
  async function createPkce() {
    const code_verifier  = b64url(crypto.getRandomValues(new Uint8Array(64)));
    const challengeBytes = await sha256(code_verifier);
    const code_challenge = b64url(challengeBytes);
    return { code_verifier, code_challenge };
  }

  // ===== Popup helpers =====
  function openPopup(url, name = 'oauth_popup') {
    const w = 520, h = 320;
    const y = window.top.outerHeight / 2 + window.top.screenY - (h / 1.5);
    const x = window.top.outerWidth  / 2 + window.top.screenX - (w / 2);
    return window.open(url, name, `width=${w},height=${h},left=${x},top=${y},resizable,scrollbars`);
  }

  // Send tokens to opener and close (used by callback-popup.html)
  function sendTokensToOpenerAndClose(payload) {
    try {
      window.opener && window.opener.postMessage({ type: 'oauth_result', ...payload }, window.location.origin);
    } finally {
      try { window.close(); } catch (e) {}
      setTimeout(() => { if (!window.closed) window.location.replace('about:blank'); }, 400);
    }
  }

  return {
    ORIGIN,
    CONTEXT,
    BASE,
    HOME_URL,
    AUTHZ_ENDPOINT,
    TOKEN_ENDPOINT,
    ARCGIS_ENDPOINT,
    REDIRECT_URI,
    CLIENT_ID,
    SCOPES,
    randomUrlSafe,
    createPkce,
    openPopup,
    sendTokensToOpenerAndClose
  };
});