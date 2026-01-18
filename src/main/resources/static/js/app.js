(() => {
  'use strict';

  const doc = document;
  const win = window;

  const config = {
    collapseDurationMs: 350,
    modalOpenDelayMs: 10,
    modalCloseDelayMs: 300,
    fadeDurationMs: 600,
    hobbyAlertTimeoutMs: 5000,
    searchDebounceMs: 200,
    searchMinChars: 2,
    searchLimit: 5
  };

  const state = {
    navbarBound: false,
    dropdownsBound: false,
    modalsBound: false,
    fileInputsBound: false,
    hobbyButtonsBound: false,
    certificateViewerBound: false,
    removeButtonsBound: false
  };

  const isElement = (value) => value && value.nodeType === 1;

  const toElement = (value) => {
    if (!value) return null;
    if (isElement(value)) return value;
    if (value.jquery) return value[0] || null;
    if (typeof value === 'string') return doc.querySelector(value);
    if (value instanceof NodeList) return value[0] || null;
    if (Array.isArray(value)) return value[0] || null;
    return null;
  };

  const toRoot = (container) => {
    if (!container) return doc;
    if (container === doc) return doc;
    if (isElement(container)) return container;
    if (container.jquery) return container[0] || doc;
    if (typeof container === 'string') return doc.querySelector(container) || doc;
    return doc;
  };

  const trim = (value) => (value || '').trim();

  const msg = (key, fallback) => {
    const messages = win.messages || {};
    if (Object.prototype.hasOwnProperty.call(messages, key)) {
      return messages[key];
    }
    return fallback;
  };

  const rememberDisplay = (element) => {
    if (!element || element.dataset.uiDisplay) return;
    const display = win.getComputedStyle(element).display;
    element.dataset.uiDisplay = display === 'none' ? 'block' : display;
  };

  const cancelAnimations = (element) => {
    if (typeof element.getAnimations !== 'function') return;
    element.getAnimations().forEach((animation) => animation.cancel());
  };

  const fade = (element, show, duration) => {
    if (!element) return;
    const time = typeof duration === 'number' ? duration : config.fadeDurationMs;
    rememberDisplay(element);
    if (show) {
      element.style.display = element.dataset.uiDisplay || 'block';
    }
    if (typeof element.animate !== 'function') {
      if (!show) element.style.display = 'none';
      return;
    }
    cancelAnimations(element);
    const from = show ? 0 : 1;
    const to = show ? 1 : 0;
    element.style.opacity = String(from);
    const animation = element.animate([{ opacity: from }, { opacity: to }], {
      duration: time,
      easing: 'linear'
    });
    animation.onfinish = () => {
      element.style.opacity = '';
      if (!show) {
        element.style.display = 'none';
      }
    };
  };

  const collectMatching = (selector, container) => {
    const root = toRoot(container);
    const elements = [];
    if (isElement(root) && root.matches(selector)) {
      elements.push(root);
    }
    if (root && root.querySelectorAll) {
      elements.push(...root.querySelectorAll(selector));
    }
    return elements;
  };

  const ensureFileWrapperDefaults = (wrapper) => {
    if (!wrapper) return;
    const caption = wrapper.querySelector('[data-file-caption]');
    if (!caption) return;
    if (!caption.dataset.default) {
      caption.dataset.default = caption.textContent || '';
    }
  };

  const supportsDateInput = (() => {
    const input = doc.createElement('input');
    input.setAttribute('type', 'date');
    return input.type === 'date';
  })();

  const onReady = (callback) => {
    if (typeof callback !== 'function') return;
    if (doc.readyState === 'loading') {
      doc.addEventListener('DOMContentLoaded', callback);
    } else {
      callback();
    }
  };

  const resume = {
    onReady,

    init() {
      if (resume && resume.interactions && typeof resume.interactions.init === 'function') {
        resume.interactions.init();
      }
      initSearchSuggestions();
    },

    initCertificateViewer() {
      if (state.certificateViewerBound) return;
      state.certificateViewerBound = true;
      doc.addEventListener('click', (event) => {
        if (!isElement(event.target)) return;
        const link = event.target.closest('a.certificate-link');
        if (!link) return;
        event.preventDefault();
        const modal = doc.getElementById('certificateViewer');
        if (!modal) return;
        const title = link.getAttribute('data-title') || '';
        const url = link.getAttribute('data-url') || '';
        const titleEl = modal.querySelector('.c-modal__title');
        if (titleEl) titleEl.innerHTML = title;
        const imgEl = modal.querySelector('.c-modal__body img');
        if (imgEl) imgEl.setAttribute('src', url);
        resume.interactions.showModal(modal);
      });
    },

    createDatePicker() {
      const elements = doc.querySelectorAll('.datepicker');
      if (!elements.length) return;
      elements.forEach((element) => {
        if (!isElement(element)) return;
        if (element.dataset.uiDatepicker === 'true') return;
        element.dataset.uiDatepicker = 'true';
        if (supportsDateInput) {
          element.type = 'date';
          return;
        }
        element.type = 'text';
        element.setAttribute('inputmode', 'numeric');
        element.setAttribute('pattern', '\\d{4}-\\d{2}-\\d{2}');
      });
    },

    createPhotoUploader() {
      const input = doc.getElementById('profilePhoto');
      if (!input) return;
      const wrapper = input.closest('.c-file');
      if (wrapper) resume.interactions.initFileInputs(wrapper);
      if (input.dataset.uiPhoto === 'true') return;
      input.dataset.uiPhoto = 'true';
      const photo = doc.getElementById('currentPhoto');
      if (photo && !photo.dataset.originalSrc) {
        photo.dataset.originalSrc = photo.getAttribute('src') || '';
      }
      let objectUrl = null;
      input.addEventListener('change', () => {
        const file = input.files && input.files[0];
        if (!file) {
          if (photo) {
            if (objectUrl) {
              win.URL.revokeObjectURL(objectUrl);
              objectUrl = null;
            }
            const original = photo.dataset.originalSrc || photo.getAttribute('src') || '';
            if (original) photo.setAttribute('src', original);
            photo.style.display = 'block';
          }
          return;
        }
        if (photo) {
          if (objectUrl) {
            win.URL.revokeObjectURL(objectUrl);
          }
          objectUrl = win.URL.createObjectURL(file);
          photo.setAttribute('src', objectUrl);
          photo.style.display = 'block';
        }
      });
    },

    createCertificateUploader(csrfToken, uploadUrl) {
      let resolvedUploadUrl = uploadUrl || '/edit/certificates/upload';
      if (csrfToken && resolvedUploadUrl.indexOf('_csrf=') === -1) {
        const separator = resolvedUploadUrl.indexOf('?') === -1 ? '?' : '&';
        resolvedUploadUrl += `${separator}_csrf=${csrfToken}`;
      }
      const input = doc.getElementById('certificateFile');
      if (!input) return;
      const wrapper = input.closest('.c-file');
      const status = wrapper ? wrapper.querySelector('[data-file-status]') : null;
      if (wrapper) resume.interactions.initFileInputs(wrapper);
      input.dataset.uploadUrl = resolvedUploadUrl;
      if (input.dataset.uiUpload === 'true') return;
      input.dataset.uiUpload = 'true';

      input.addEventListener('change', async () => {
        const file = input.files && input.files[0];
        if (!file) return;
        if (status) status.textContent = msg('uploading', 'Uploading...');
        const formData = new FormData();
        formData.append('certificateFile', file);
        try {
          const response = await fetch(input.dataset.uploadUrl || resolvedUploadUrl, {
            method: 'POST',
            body: formData,
            credentials: 'same-origin',
            headers: { 'X-Requested-With': 'XMLHttpRequest' }
          });
          if (!response.ok) throw new Error('Upload failed');
          const data = await response.json();
          const nameInput = doc.getElementById('certificateName');
          if (data && data.certificateName != null && nameInput && trim(nameInput.value) === '') {
            nameInput.value = data.certificateName;
          }
          const uploader = doc.getElementById('certificateUploader');
          if (uploader) {
            if (data && data.smallUrl) {
              uploader.setAttribute('data-small-url', data.smallUrl);
            }
            if (data && data.largeUrl) {
              uploader.setAttribute('data-large-url', data.largeUrl);
            }
          }
          const issuerInput = doc.getElementById('certificateIssuer');
          if (data && data.issuer != null && issuerInput && trim(issuerInput.value) === '') {
            issuerInput.value = data.issuer;
          }
          if (status) status.textContent = msg('uploadSuccess', 'Upload complete');
        } catch (error) {
          if (status) status.textContent = msg('uploadError', 'Upload error');
          resume.showErrorDialog(msg('errorUploadCertificate', 'Upload failed'));
        }
      });
    },

    interactions: {
      init() {
        resume.interactions.initNavbar();
        resume.interactions.initDropdowns();
        resume.interactions.initModals();
        resume.interactions.initFileInputs();
        resume.interactions.initHobbyButtons();
        resume.interactions.initRemoveButtons();
      },

      initNavbar() {
        if (state.navbarBound) return;
        state.navbarBound = true;
        doc.addEventListener('click', (event) => {
          if (!isElement(event.target)) return;
          const trigger = event.target.closest('[data-ui-toggle="collapse"]');
          if (!trigger) return;
          event.preventDefault();
          const targetSelector = trigger.getAttribute('data-ui-target');
          if (!targetSelector) return;
          const collapse = doc.querySelector(targetSelector);
          if (!collapse || collapse.classList.contains('collapsing')) return;
          const isOpen = collapse.classList.contains('is-open');

          if (isOpen) {
            collapse.style.height = `${collapse.scrollHeight}px`;
            collapse.offsetHeight;
            collapse.classList.add('collapsing');
            collapse.classList.remove('is-open');
            collapse.style.display = 'block';
            collapse.style.height = '0px';
            win.setTimeout(() => {
              collapse.classList.remove('collapsing');
              collapse.style.height = '';
              collapse.style.display = '';
            }, config.collapseDurationMs);
          } else {
            collapse.classList.add('collapsing');
            collapse.style.display = 'block';
            collapse.style.height = '0px';
            const height = collapse.scrollHeight;
            collapse.offsetHeight;
            collapse.style.height = `${height}px`;
            win.setTimeout(() => {
              collapse.classList.remove('collapsing');
              collapse.classList.add('is-open');
              collapse.style.height = '';
              collapse.style.display = '';
            }, config.collapseDurationMs);
          }

          trigger.classList.toggle('is-collapsed', isOpen);
          trigger.setAttribute('aria-expanded', isOpen ? 'false' : 'true');
        });
      },

      initDropdowns() {
        if (state.dropdownsBound) return;
        state.dropdownsBound = true;

        const closeAll = () => {
          doc.querySelectorAll('.c-dropdown.is-open').forEach((dropdown) => {
            dropdown.classList.remove('is-open');
            const toggle = dropdown.querySelector('.c-dropdown__toggle');
            if (toggle) toggle.setAttribute('aria-expanded', 'false');
          });
        };

        doc.addEventListener('click', (event) => {
          if (!isElement(event.target)) return;
          const toggle = event.target.closest('.c-dropdown__toggle');
          if (toggle) {
            event.preventDefault();
            const dropdown = toggle.closest('.c-dropdown');
            const wasOpen = dropdown && dropdown.classList.contains('is-open');
            closeAll();
            if (dropdown && !wasOpen) {
              dropdown.classList.add('is-open');
              toggle.setAttribute('aria-expanded', 'true');
            }
            return;
          }

          const menuLink = event.target.closest('.c-dropdown__menu a');
          if (menuLink) {
            const dropdown = menuLink.closest('.c-dropdown');
            if (dropdown) {
              dropdown.classList.remove('is-open');
              const toggle = dropdown.querySelector('.c-dropdown__toggle');
              if (toggle) toggle.setAttribute('aria-expanded', 'false');
            }
            return;
          }

          if (!event.target.closest('.c-dropdown')) {
            closeAll();
          }
        });

        doc.addEventListener('keydown', (event) => {
          if (event.key === 'Escape') {
            closeAll();
          }
        });
      },

      initModals() {
        if (state.modalsBound) return;
        state.modalsBound = true;

        doc.addEventListener('click', (event) => {
          if (!isElement(event.target)) return;
          const dismiss = event.target.closest('[data-ui-dismiss="modal"]');
          if (!dismiss) return;
          const modal = dismiss.closest('.c-modal');
          if (modal) {
            resume.interactions.hideModal(modal);
          }
        });

        doc.addEventListener('keydown', (event) => {
          if (event.key !== 'Escape') return;
          const openModals = doc.querySelectorAll('.c-modal.is-open');
          if (!openModals.length) return;
          const last = openModals[openModals.length - 1];
          resume.interactions.hideModal(last);
        });
      },

      initRemoveButtons() {
        if (state.removeButtonsBound) return;
        state.removeButtonsBound = true;

        doc.addEventListener('click', (event) => {
          if (!isElement(event.target)) return;
          const trigger = event.target.closest('[data-ui-remove]');
          if (!trigger) return;
          event.preventDefault();
          const target = trim(trigger.getAttribute('data-ui-remove'));
          if (target) {
            resume.ui.removeBlock(target);
            return;
          }
          const item = trigger.closest('.ui-item');
          if (item) item.remove();
        });
      },

      showModal(modal) {
        const modalEl = toElement(modal);
        if (!modalEl) return;
        if (modalEl.classList.contains('is-open')) return;
        const body = doc.body;
        if (!body.classList.contains('c-modal-open')) {
          const scrollbarWidth = win.innerWidth - doc.documentElement.clientWidth;
          if (scrollbarWidth > 0) {
            body.dataset.uiScrollbarWidth = String(scrollbarWidth);
            body.style.paddingRight = `${scrollbarWidth}px`;
          }
        }
        if (!modalEl.hasAttribute('tabindex')) {
          modalEl.setAttribute('tabindex', '-1');
        }
        modalEl.setAttribute('aria-hidden', 'false');
        modalEl.setAttribute('aria-modal', 'true');
        modalEl.style.display = 'block';
        body.classList.add('c-modal-open');

        doc.querySelectorAll('.c-modal-backdrop').forEach((backdrop) => backdrop.remove());
        const backdrop = doc.createElement('div');
        backdrop.className = 'c-modal-backdrop is-fade';
        body.appendChild(backdrop);

        win.setTimeout(() => {
          modalEl.classList.add('is-open');
          backdrop.classList.add('is-open');
          modalEl.focus();
        }, config.modalOpenDelayMs);

        backdrop.addEventListener('click', () => resume.interactions.hideModal(modalEl), { once: true });
      },

      hideModal(modal) {
        const modalEl = toElement(modal);
        if (!modalEl) return;
        const backdrops = doc.querySelectorAll('.c-modal-backdrop');
        modalEl.classList.remove('is-open');
        backdrops.forEach((backdrop) => backdrop.classList.remove('is-open'));
        modalEl.setAttribute('aria-hidden', 'true');
        modalEl.removeAttribute('aria-modal');

        win.setTimeout(() => {
          modalEl.style.display = 'none';
          backdrops.forEach((backdrop) => backdrop.remove());
          if (!doc.querySelector('.c-modal.is-open')) {
            const body = doc.body;
            body.classList.remove('c-modal-open');
            if (body.dataset.uiScrollbarWidth) {
              body.style.paddingRight = '';
              delete body.dataset.uiScrollbarWidth;
            }
          }
        }, config.modalCloseDelayMs);
      },

      initFileInputs(container) {
        if (!state.fileInputsBound) {
          state.fileInputsBound = true;

          doc.addEventListener('change', (event) => {
            if (!isElement(event.target)) return;
            const input = event.target.closest('.c-file__input');
            if (!input) return;
            const wrapper = input.closest('.c-file');
            ensureFileWrapperDefaults(wrapper);
            const caption = wrapper ? wrapper.querySelector('[data-file-caption]') : null;
            const defaultCaption = caption ? (caption.dataset.default || '') : '';
            const fileName = input.files && input.files[0] ? input.files[0].name : '';
            if (caption) {
              caption.textContent = fileName || defaultCaption;
            }
            const status = wrapper ? wrapper.querySelector('[data-file-status]') : null;
            if (status) status.textContent = '';
          });

          doc.addEventListener('click', (event) => {
            if (!isElement(event.target)) return;
            const clear = event.target.closest('[data-file-clear]');
            if (!clear) return;
            const wrapper = clear.closest('.c-file');
            ensureFileWrapperDefaults(wrapper);
            const input = wrapper ? wrapper.querySelector('.c-file__input') : null;
            if (input) resume.interactions.clearFileInput(input);
          });
        }

        collectMatching('.c-file', container).forEach((wrapper) => {
          ensureFileWrapperDefaults(wrapper);
        });
      },

      clearFileInput(input) {
        const element = toElement(input);
        if (!element) return;
        element.value = '';
        element.dispatchEvent(new Event('change', { bubbles: true }));
      },

      initHobbyButtons() {
        if (state.hobbyButtonsBound) return;
        state.hobbyButtonsBound = true;
        doc.addEventListener('change', (event) => {
          const input = event.target;
          if (!(input instanceof HTMLInputElement) || input.type !== 'checkbox') return;
          const button = input.closest('.hobby-btn');
          if (button) {
            button.classList.toggle('is-active', input.checked);
          }
        });
      }
    },

    showErrorDialog(message) {
      win.alert(message);
    },

    post(path, params) {
      const form = doc.createElement('form');
      form.setAttribute('method', 'post');
      form.setAttribute('action', path);
      Object.keys(params || {}).forEach((key) => {
        const value = params[key];
        if (value === undefined || value === null) return;
        const input = doc.createElement('input');
        input.setAttribute('type', 'hidden');
        input.setAttribute('name', key);
        input.setAttribute('value', String(value));
        form.appendChild(input);
      });
      doc.body.appendChild(form);
      form.submit();
    },

    logout(csrfToken) {
      resume.post('/logout', { _csrf: csrfToken });
    },

    moreProfiles(searchQuery) {
      const container = doc.getElementById('profileContainer');
      if (!container) return;
      const page = parseInt(container.getAttribute('data-profile-number'), 10);
      const total = parseInt(container.getAttribute('data-profile-total'), 10);
      if (!Number.isFinite(page) || !Number.isFinite(total)) return;
      const nextPage = page + 1;
      if (nextPage >= total) return;

      const url = new URL('/fragment/more', win.location.origin);
      url.searchParams.set('page', String(nextPage));
      if (searchQuery !== undefined && trim(searchQuery) !== '') {
        url.searchParams.set('query', searchQuery);
      }

      const loadMoreContainer = doc.getElementById('loadMoreContainer');
      const indicator = doc.getElementById('loadMoreIndicator');
      if (loadMoreContainer) loadMoreContainer.style.display = 'none';
      if (indicator) indicator.style.display = 'block';

      fetch(url.toString(), {
        credentials: 'same-origin',
        headers: { 'X-Requested-With': 'XMLHttpRequest' }
      })
        .then((response) => {
          if (!response.ok) throw new Error('Request failed');
          return response.text();
        })
        .then((data) => {
          if (indicator) indicator.style.display = 'none';
          container.insertAdjacentHTML('beforeend', data);
          container.setAttribute('data-profile-number', String(nextPage));
          if (nextPage >= total - 1) {
            if (indicator) indicator.remove();
            if (loadMoreContainer) loadMoreContainer.remove();
          } else if (loadMoreContainer) {
            loadMoreContainer.style.display = 'block';
          }
        })
        .catch(() => {
          if (indicator) indicator.style.display = 'none';
          resume.showErrorDialog(msg('errorAjax', 'Request failed'));
        });
    },

    ui: {
      template: null,

      getTemplate() {
        if (resume.ui.template == null) {
          const source = doc.getElementById('ui-block-template');
          if (!source || !win.ResumeTemplate) return null;
          resume.ui.template = win.ResumeTemplate.compile(source.innerHTML);
        }
        return resume.ui.template;
      },

      nextBlockIndex(container) {
        if (!container || !container.querySelectorAll) return 0;
        const items = container.querySelectorAll('.ui-item');
        let maxIndex = -1;
        items.forEach((item) => {
          const id = item.getAttribute('id') || '';
          const idMatch = id.match(/^ui-item-(\d+)$/);
          if (idMatch) {
            maxIndex = Math.max(maxIndex, parseInt(idMatch[1], 10));
            return;
          }
          const field = item.querySelector('[name^="items["]');
          if (!field) return;
          const name = field.getAttribute('name') || '';
          const nameMatch = name.match(/^items\[(\d+)\]/);
          if (nameMatch) {
            maxIndex = Math.max(maxIndex, parseInt(nameMatch[1], 10));
          }
        });
        return maxIndex >= 0 ? maxIndex + 1 : items.length;
      },

      normalizeListIndices(container, listName) {
        const root = toRoot(container);
        const prefix = listName || 'items';
        if (!root || !root.querySelectorAll) return;
        const items = root.querySelectorAll('.ui-item');
        const nameRegex = new RegExp(`^${prefix}\\[(\\d+)\\]\\.(.+)$`);
        items.forEach((item, index) => {
          item.querySelectorAll('input[name], select[name], textarea[name]').forEach((field) => {
            const name = field.getAttribute('name') || '';
            const match = name.match(nameRegex);
            if (!match) return;
            const nextName = `${prefix}[${index}].${match[2]}`;
            if (nextName !== name) {
              field.setAttribute('name', nextName);
            }
          });
        });
      },

      addBlock() {
        const template = resume.ui.getTemplate();
        const container = doc.getElementById('ui-block-container');
        if (!template || !container) return;
        const blockIndex = resume.ui.nextBlockIndex(container);
        const context = { blockIndex };
        if (win.languageLevelDefault !== undefined) {
          context.levelDefault = win.languageLevelDefault;
        }
        if (win.languageLevelMax !== undefined) {
          context.levelMax = win.languageLevelMax;
        }
        container.insertAdjacentHTML('beforeend', template(context));
        resume.createDatePicker();
        resume.initLevelSliders(container);
      },

      updateSelect(thisObj) {
        let value = '';
        let refId = '';
        if (thisObj && typeof thisObj.val === 'function') {
          value = thisObj.val();
          refId = thisObj.attr ? thisObj.attr('data-ref-select') : '';
        } else {
          const element = toElement(thisObj);
          if (element) {
            value = element.value;
            refId = element.getAttribute('data-ref-select');
          }
        }
        if (!value && refId) {
          const ref = doc.getElementById(refId);
          if (ref) ref.value = '';
        }
      },

      removeBlock(target) {
        let element = null;
        if (typeof target === 'string') {
          const value = trim(target);
          if (value) {
            element = doc.getElementById(value) || doc.querySelector(value);
          }
        } else {
          element = toElement(target);
        }
        if (element) {
          element.remove();
        }
      }
    },

    certificates: {
      showUploadDialog() {
        const uploader = doc.getElementById('certificateUploader');
        if (uploader) {
          uploader.setAttribute('data-small-url', '');
          uploader.setAttribute('data-large-url', '');
        }
        const nameInput = doc.getElementById('certificateName');
        if (nameInput) nameInput.value = '';
        const issuerInput = doc.getElementById('certificateIssuer');
        if (issuerInput) issuerInput.value = '';
        const fileInput = doc.getElementById('certificateFile');
        if (fileInput) {
          const wrapper = fileInput.closest('.c-file');
          if (wrapper) resume.interactions.initFileInputs(wrapper);
          resume.interactions.clearFileInput(fileInput);
        }
        resume.interactions.showModal('#certificateUploader');
      },

      add() {
        const nameInput = doc.getElementById('certificateName');
        const certificateName = nameInput ? nameInput.value : '';
        if (trim(certificateName) === '') {
          win.alert('certificateName is null');
          return;
        }
        const uploader = doc.getElementById('certificateUploader');
        const smallUrl = uploader ? uploader.getAttribute('data-small-url') : '';
        const largeUrl = uploader ? uploader.getAttribute('data-large-url') : '';
        if (!smallUrl || !largeUrl) {
          resume.showErrorDialog(msg('errorUploadCertificate', 'Upload failed'));
          return;
        }
        const template = resume.ui.getTemplate();
        const container = doc.getElementById('ui-block-container');
        if (!template || !container) return;
        const blockIndex = container.querySelectorAll('.ui-item').length;
        const issuerInput = doc.getElementById('certificateIssuer');
        const context = {
          blockIndex,
          name: certificateName,
          issuer: issuerInput ? issuerInput.value : '',
          smallUrl,
          largeUrl
        };
        container.insertAdjacentHTML('beforeend', template(context));
        resume.interactions.hideModal('#certificateUploader');
        if (nameInput) nameInput.value = '';
        if (issuerInput) issuerInput.value = '';
        const fileInput = doc.getElementById('certificateFile');
        if (fileInput) resume.interactions.clearFileInput(fileInput);
      }
    },

    hobbies: {
      errorTimeout: null,

      save() {
        const selected = Array.from(doc.querySelectorAll('.hobby-btn.is-active'));
        const container = doc.getElementById('ui-block-container');
        const maxHobbies = container ? parseInt(container.getAttribute('data-max-hobbies'), 10) : NaN;
        if (Number.isFinite(maxHobbies) && selected.length > maxHobbies) {
          const errorAlert = doc.getElementById('errorAlert');
          const closeButton = errorAlert ? errorAlert.querySelector('button') : null;
          const closeFunction = () => {
            fade(errorAlert, false);
            if (resume.hobbies.errorTimeout != null) {
              win.clearTimeout(resume.hobbies.errorTimeout);
              resume.hobbies.errorTimeout = null;
            }
          };
          if (closeButton) {
            closeButton.onclick = closeFunction;
          }
          fade(errorAlert, true);
          resume.hobbies.errorTimeout = win.setTimeout(closeFunction, config.hobbyAlertTimeoutMs);
          return;
        }

        const hobbies = selected
          .map((item) => item.getAttribute('data-hobby'))
          .filter(Boolean)
          .join(',');

        const hobbyContainer = doc.getElementById('hobbyContainer');
        const csrfToken = hobbyContainer ? hobbyContainer.getAttribute('data-csrf-value') : '';
        const postUrl = hobbyContainer ? hobbyContainer.getAttribute('data-post-url') : '';
        resume.post(postUrl || '/edit/hobbies', {
          hobbies,
          _csrf: csrfToken
        });
      }
    }
  };

  const initSearchSuggestions = () => {
    const input = doc.getElementById('nav-search-input');
    const list = doc.getElementById('nav-search-suggest');
    if (!input || !list) return;
    let timer = null;
    let requestId = 0;

    const hide = () => {
      list.innerHTML = '';
      list.style.display = 'none';
    };

    const renderItems = (items) => {
      list.innerHTML = '';
      if (!items || !items.length) {
        const empty = doc.createElement('li');
        empty.className = 'empty';
        empty.textContent = msg('suggestEmpty', 'No results');
        list.appendChild(empty);
      } else {
        items.forEach((item) => {
          const text = item.fullName || item.uid;
          const link = doc.createElement('a');
          link.href = `/${item.uid}`;
          link.textContent = text;
          const li = doc.createElement('li');
          li.appendChild(link);
          list.appendChild(li);
        });
      }
      list.style.display = 'block';
    };

    const fetchSuggestions = (query) => {
      const currentRequest = ++requestId;
      const url = new URL('/api/suggest', win.location.origin);
      url.searchParams.set('q', query);
      url.searchParams.set('limit', String(config.searchLimit));

      fetch(url.toString(), {
        credentials: 'same-origin',
        headers: {
          Accept: 'application/json',
          'X-Requested-With': 'XMLHttpRequest'
        }
      })
        .then((response) => {
          if (!response.ok) throw new Error('Request failed');
          return response.json();
        })
        .then((items) => {
          if (currentRequest !== requestId) return;
          renderItems(items);
        })
        .catch(() => {
          if (currentRequest !== requestId) return;
          hide();
        });
    };

    input.addEventListener('input', () => {
      const query = trim(input.value);
      if (query.length < config.searchMinChars) {
        hide();
        return;
      }
      if (timer) {
        win.clearTimeout(timer);
      }
      timer = win.setTimeout(() => {
        fetchSuggestions(query);
      }, config.searchDebounceMs);
    });

    input.addEventListener('blur', () => {
      win.setTimeout(hide, config.searchDebounceMs);
    });

    const form = doc.getElementById('nav-search-form');
    if (form) {
      form.addEventListener('submit', hide);
    }
  };

  win.resume = resume;

  onReady(() => {
    resume.init();
  });
})();
