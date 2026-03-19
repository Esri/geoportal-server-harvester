define([], function() {
  const errBox = document.getElementById('err');

  // Show Spring error messages (?error)
  (function () {
    const params = new URLSearchParams(window.location.search);
    if (params.has('error')) {
      errBox.textContent = 'Authentication failed. Please check your username and password.';
      errBox.style.display = 'block';
    }
  })();

  // After Spring form login success -> ?loggedin
  (function () {
    const params = new URLSearchParams(window.location.search);
    if (params.has('loggedin')) {
      // Ask the PARENT to build PKCE + authorize URL and send it back
      window.opener && window.opener.postMessage({ type: 'start_authorize' }, window.location.origin);
    }
  })();

  // If we opened this page specifically for ArcGIS (#arcgis), we can
  // proactively request start_authorize immediately:
  (function () {
    if (window.location.hash === '#arcgis') {
      window.opener && window.opener.postMessage({ type: 'start_authorize', idp: 'arcgis' }, window.location.origin);
    }
  })();

  // Receive authorize URL & PKCE from parent, then navigate
  window.addEventListener('message', (ev) => {
    if (ev.origin !== window.location.origin) return;
    const msg = ev.data || {};
    if (msg.type === 'navigate_authorize' && msg.url) {
      // IMPORTANT: persist PKCE in POPUP storage before navigating
      if (msg.code_verifier) sessionStorage.setItem('pkce_code_verifier', msg.code_verifier);
      if (msg.state)         sessionStorage.setItem('pkce_state',         msg.state);
      window.location.assign(msg.url);
    }
  });
});