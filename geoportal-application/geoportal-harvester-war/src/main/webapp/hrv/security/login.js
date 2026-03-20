define(['./auth-base.js', '../config.js'], function (authBase, cfg) {
  'use strict';

  const {
    HOME_URL,
    BASE,
    AUTHZ_ENDPOINT,
    REDIRECT_URI,
    CLIENT_ID,
    SCOPES,
    createPkce,
    randomUrlSafe,
    openPopup
  } = authBase;

  const errBox = document.getElementById('err');

  // Build /oauth2/authorize inputs in the PARENT and return both URL and PKCE
  async function buildAuthorize() {
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
        if (errBox) {
          errBox.textContent = data.error;
          errBox.style.display = 'block';
        }
        return;
      }

      // Store tokens in parent (SPA)
      sessionStorage.setItem('access_token', data.access_token ?? '');
      if (data.refresh_token) sessionStorage.setItem('refresh_token', data.refresh_token);
      if (data.id_token) sessionStorage.setItem('id_token', data.id_token);
      sessionStorage.setItem('token_type', data.token_type ?? 'Bearer');
      sessionStorage.setItem('expires_in', String(data.expires_in ?? ''));

      // Navigate the PARENT to SPA Home
      window.location.replace(HOME_URL);
    }
  });

  // Buttons
  const btnLocal = document.getElementById('btnLocal');
  const btnArcgis = document.getElementById('btnArcgis');

  if (cfg.app.arcGISAuthEnabled) {
    btnArcgis.style.display = 'block';
  } else {
    btnArcgis.style.display = 'none';
  }

  if (cfg.app.localAccountAuthEnabled) {
    btnLocal.style.display = 'block';
  } else {
    btnLocal.style.display = 'none';
  }

  // Local Account flow:
  // 1) Open same-origin custom-login.html (popup)
  // 2) After /login success, popup posts {type:'start_authorize'}
  // 3) Parent replies with {type:'navigate_authorize', url, code_verifier, state}
  btnLocal.addEventListener('click', () => {
    openPopup('custom-login.html', 'local_login');
  });

  // ArcGIS OAuth2 flow - directly redirects to ArcGIS Portal
  btnArcgis.addEventListener('click', async () => {
    try {
      console.log('ArcGIS sign-in initiated');

      // Generate PKCE and state
      const { code_verifier, code_challenge } = await createPkce();
      const state = randomUrlSafe(24);

      // Store in sessionStorage for later verification
      sessionStorage.setItem('pkce_code_verifier', code_verifier);
      sessionStorage.setItem('pkce_state', state);
      console.log('PKCE and state generated');

      // Build redirect URI for ArcGIS callback
      const redirectUri = BASE + '/login/oauth2/code/arcgis';
      console.log('Redirect URI:', redirectUri);

      // Get ArcGIS OAuth configuration from server
      const cfgUrl = BASE + '/rest/harvester/security/arcgis-config';
      console.log('Fetching ArcGIS config from:', cfgUrl);
      const response = await fetch(cfgUrl);
      if (!response.ok) {
        const errorBody = await response.text();
        console.error('ArcGIS config response:', response.status, errorBody);
        throw new Error(`Failed to load ArcGIS OAuth configuration (${response.status}): ${errorBody}`);
      }
      const arcgisConfig = await response.json();
      console.log('ArcGIS config loaded:', arcgisConfig);

      // Verify required config fields
      if (!arcgisConfig.clientId || !arcgisConfig.authorizationUri) {
        throw new Error('ArcGIS configuration is incomplete. Check environment variables: HRV_ARCGIS_CLIENTID, HRV_ARCGIS_AUTHORIZATIONURI');
      }

      // Build ArcGIS authorization URL
      const arcgisAuthParams = new URLSearchParams({
        client_id: arcgisConfig.clientId,
        response_type: 'code',
        redirect_uri: redirectUri,
        state: state,
        code_challenge: code_challenge,
        code_challenge_method: 'S256'
      });
      const arcgisAuthUrl = arcgisConfig.authorizationUri + '?' + arcgisAuthParams.toString();
      console.log('Redirecting to ArcGIS:', arcgisAuthUrl.substring(0, 100) + '...');

      // Redirect to ArcGIS (not a popup - direct navigation)
      window.location.href = arcgisAuthUrl;
    } catch (err) {
      const errorMsg = err && err.message ? err.message : String(err);
      if (errBox) {
        errBox.textContent = 'Error initiating ArcGIS sign-in:\n\n' + errorMsg;
        errBox.style.display = 'block';
      }
      console.error('ArcGIS sign-in error:', err);
    }
  });

  // Popup -> Parent requesting parent-built PKCE authorize URL
  window.addEventListener('message', async (ev) => {
    if (ev.origin !== window.location.origin) return;
    const msg = ev.data || {};

    if (msg.type === 'start_authorize') {
      const auth = await buildAuthorize();
      ev.source.postMessage(
        { type: 'navigate_authorize', url: auth.url, code_verifier: auth.code_verifier, state: auth.state },
        ev.origin
      );
    }
  });
});