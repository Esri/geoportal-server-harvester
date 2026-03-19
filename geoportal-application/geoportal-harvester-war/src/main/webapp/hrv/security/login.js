define(['./auth-base.js', '../config.js'], function(authBase, cfg) {
  const {
    HOME_URL, AUTHZ_ENDPOINT, REDIRECT_URI, CLIENT_ID, SCOPES,
    createPkce, randomUrlSafe, openPopup
  } = authBase;

  const errBox = document.getElementById('err');

  // Build /oauth2/authorize inputs in the PARENT and return both URL and PKCE
  async function buildAuthorize({ idpHint } = {}) {
    const { code_verifier, code_challenge } = await createPkce();
    const state = randomUrlSafe(24);

    // Optional: keep in parent for debugging/consistency
    sessionStorage.setItem('pkce_code_verifier', code_verifier);
    sessionStorage.setItem('pkce_state', state);

    const p = new URLSearchParams({
      client_id: CLIENT_ID,
      response_type: 'code',
      redirect_uri: REDIRECT_URI,
      scope: SCOPES.join(' '),
      code_challenge: code_challenge,
      code_challenge_method: 'S256',
      state
    });
    if (idpHint) p.set('idp', idpHint);

    return {
      url: `${AUTHZ_ENDPOINT}?${p.toString()}`,
      code_verifier,
      state
    };
  }

  // Receive tokens from popup (callback-popup.html -> postMessage)
  window.addEventListener('message', (ev) => {
    if (ev.origin !== window.location.origin) return;
    const data = ev.data || {};
    if (data.type === 'oauth_result') {
      if (data.error) {
        errBox.textContent = data.error;
        errBox.style.display = 'block';
        return;
      }
      // Store tokens in parent (SPA)
      sessionStorage.setItem('access_token',  data.access_token || '');
      if (data.refresh_token) sessionStorage.setItem('refresh_token', data.refresh_token);
      if (data.id_token)      sessionStorage.setItem('id_token',      data.id_token);
      sessionStorage.setItem('token_type',    data.token_type || 'Bearer');
      sessionStorage.setItem('expires_in',    String(data.expires_in || ''));

      // Navigate the PARENT to SPA Home
      window.location.replace(HOME_URL);
    }
  });

  // Buttons
  const btnLocal  = document.getElementById('btnLocal');
  const btnArcgis = document.getElementById('btnArcgis');


  if (cfg.app.arcGISAuthEnabled) {
    btnArcgis.style.display = "block";
  } else {
    btnArcgis.style.display = "none";
  }
  if (cfg.app.localAccountAuthEnabled) {
     btnLocal.style.display = "block";
   } else {
     btnLocal.style.display = "none";
   }

  // Local Account flow:
  //  1) Open same-origin custom-login.html (popup)
  //  2) After /login success, popup posts {type:'start_authorize'}
  //  3) Parent replies with {type:'navigate_authorize', url, code_verifier, state}
  btnLocal.addEventListener('click', () => {
    openPopup('custom-login.html', 'local_login');
  });

  // ArcGIS flow (popup):
  //  1) Open same-origin custom-login.html#arcgis
  //  2) Immediately send navigate_authorize to popup (it will store PKCE then navigate)
  btnArcgis.addEventListener('click', async () => {
    const popup = openPopup('custom-login.html#arcgis', 'arcgis_login');
    const auth  = await buildAuthorize({ idpHint: 'arcgis' });
    setTimeout(() => {
      popup.postMessage(
        { type: 'navigate_authorize', url: auth.url, code_verifier: auth.code_verifier, state: auth.state },
        window.location.origin
      );
    }, 100);
  });

  // Popup -> Parent requesting parent-built PKCE authorize URL
  window.addEventListener('message', async (ev) => {
    if (ev.origin !== window.location.origin) return;
    const msg = ev.data || {};
    if (msg.type === 'start_authorize') {
      const auth = await buildAuthorize({ idpHint: msg.idp || undefined });
      ev.source.postMessage(
        { type: 'navigate_authorize', url: auth.url, code_verifier: auth.code_verifier, state: auth.state },
        ev.origin
      );
    }
  });
});