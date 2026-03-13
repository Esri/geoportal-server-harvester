import {
  TOKEN_ENDPOINT, CLIENT_ID, REDIRECT_URI,
  sendTokensToOpenerAndClose
} from './auth-base.js';

(async () => {
  function fail(message) {
    sendTokensToOpenerAndClose({ error: message || 'Unknown error' });
  }

  try {
    // Parse ?code=...&state=...
    const params = new URLSearchParams(window.location.search);
    const code   = params.get('code');
    const state  = params.get('state');
    if (!code) return fail('Missing authorization code');

    // Verify state vs what the parent stored before starting the flow
    const expectedState = sessionStorage.getItem('pkce_state');
    if (expectedState && state && expectedState !== state) {
      return fail('State mismatch');
    }

    // Retrieve PKCE verifier
    const verifier = sessionStorage.getItem('pkce_code_verifier');
    if (!verifier) return fail('Missing PKCE code_verifier');

    // Exchange code -> tokens at SAS token endpoint
    const body = new URLSearchParams({
      grant_type: 'authorization_code',
      client_id: CLIENT_ID,
      redirect_uri: REDIRECT_URI,
      code,
      code_verifier: verifier
    });

    const res = await fetch(TOKEN_ENDPOINT, {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: body.toString(),
      credentials: 'include'
    });

    if (!res.ok) {
      const text = await res.text().catch(() => '');
      return fail(`Token error ${res.status}: ${text || res.statusText}`);
    }

    const tokens = await res.json();

    // Clean up transient PKCE values
    sessionStorage.removeItem('pkce_code_verifier');
    sessionStorage.removeItem('pkce_state');

    // Send tokens to the opener and close the popup
    sendTokensToOpenerAndClose(tokens);
  } catch (e) {
    fail(e instanceof Error ? e.message : String(e));
  }
})();