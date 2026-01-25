(() => {
  'use strict';

  const doc = document;
  const win = window;
  const resume = win.resume;

  if (!resume) {
    return;
  }

  const base = resolveBase();
  const PLACEHOLDER_PHOTO = buildPath('/img/profile-placeholder.png');
  const RESERVED_PATHS = new Set([
    'welcome',
    'search',
    'login',
    'register',
    'restore',
    'account',
    'me',
    'fragment',
    'api',
    'error',
    'css',
    'js',
    'img',
    'fonts',
    'uploads',
    'media',
    'favicon',
    'favicon.ico'
  ]);

  const state = {
    requestId: 0,
    abortController: null,
    staticDataPromise: null,
    languageTypeLabels: null
  };

  const locale = doc.documentElement.lang || win.navigator.language || 'en';
  const fullDateFormatter = new Intl.DateTimeFormat(locale, {
    month: 'short',
    day: '2-digit',
    year: 'numeric',
    timeZone: 'UTC'
  });
  const monthYearFormatter = new Intl.DateTimeFormat(locale, {
    month: 'short',
    year: 'numeric',
    timeZone: 'UTC'
  });

  function resolveBase() {
    const searchForm = doc.getElementById('nav-search-form');
    const action = searchForm ? searchForm.getAttribute('action') : null;
    const brand = doc.querySelector('.c-navbar__brand');
    const href = action || (brand ? brand.getAttribute('href') : null);
    if (!href) {
      return { origin: win.location.origin, basePath: '' };
    }
    const url = new URL(href, win.location.origin);
    let basePath = url.pathname || '';
    if (basePath.endsWith('/search')) {
      basePath = basePath.slice(0, -'/search'.length);
    } else if (basePath.endsWith('/welcome')) {
      basePath = basePath.slice(0, -'/welcome'.length);
    }
    basePath = normalizeBasePath(basePath);
    return { origin: win.location.origin, basePath };
  }

  function normalizeBasePath(path) {
    if (!path || path === '/') return '';
    return path.endsWith('/') ? path.slice(0, -1) : path;
  }

  function stripBasePath(pathname) {
    if (!base.basePath) return pathname;
    if (pathname === base.basePath) {
      return '/';
    }
    if (pathname.startsWith(`${base.basePath}/`)) {
      const stripped = pathname.slice(base.basePath.length);
      return stripped || '/';
    }
    return pathname;
  }

  function buildPath(path) {
    const value = path && path.startsWith('/') ? path : `/${path || ''}`;
    return `${base.basePath}${value}`;
  }

  function buildUrl(path) {
    return new URL(buildPath(path), base.origin);
  }

  function normalizePath(pathname) {
    if (!pathname) return '/';
    if (pathname.length > 1 && pathname.endsWith('/')) {
      return pathname.slice(0, -1);
    }
    return pathname;
  }

  function parsePage(value) {
    const page = parseInt(value, 10);
    return Number.isFinite(page) && page >= 0 ? page : 0;
  }

  function normalizeQuery(value) {
    return String(value || '').trim();
  }

  function decodeSegment(value) {
    if (!value) return '';
    try {
      return decodeURIComponent(value);
    } catch (err) {
      return value;
    }
  }

  function isProfilePath(pathname) {
    if (!pathname || pathname === '/') return false;
    const trimmed = pathname.startsWith('/') ? pathname.slice(1) : pathname;
    if (!trimmed || trimmed.includes('/') || trimmed.includes('.')) return false;
    return !RESERVED_PATHS.has(trimmed);
  }

  function parseRoute(input) {
    const url = input instanceof URL ? input : new URL(String(input), base.origin);
    const pathname = normalizePath(stripBasePath(url.pathname));
    const params = url.searchParams;
    if (pathname === '/' || pathname === '') {
      return {
        type: 'welcome',
        query: normalizeQuery(params.get('query')),
        page: parsePage(params.get('page'))
      };
    }
    if (pathname === '/welcome') {
      return {
        type: 'welcome',
        query: normalizeQuery(params.get('query')),
        page: parsePage(params.get('page'))
      };
    }
    if (pathname === '/search') {
      return {
        type: 'search',
        query: normalizeQuery(params.get('q')),
        page: parsePage(params.get('page'))
      };
    }
    if (isProfilePath(pathname)) {
      return { type: 'profile', uid: decodeSegment(pathname.slice(1)) };
    }
    return null;
  }

  function startRequest() {
    state.requestId += 1;
    if (state.abortController) {
      state.abortController.abort();
    }
    state.abortController = new AbortController();
    return { id: state.requestId, signal: state.abortController.signal };
  }

  function isCurrentRequest(id) {
    return id === state.requestId;
  }

  function fetchJson(url, signal) {
    return fetch(url.toString(), {
      credentials: 'same-origin',
      headers: {
        Accept: 'application/json',
        'X-Requested-With': 'XMLHttpRequest'
      },
      signal
    }).then((response) => {
      if (!response.ok) {
        const error = new Error('Request failed');
        error.status = response.status;
        throw error;
      }
      return response.json();
    });
  }

  function buildApiUrl(path, params) {
    const url = buildUrl(path);
    if (params) {
      Object.keys(params).forEach((key) => {
        const value = params[key];
        if (value === undefined || value === null || value === '') return;
        url.searchParams.set(key, String(value));
      });
    }
    return url;
  }

  function cloneTemplate(id) {
    const template = doc.getElementById(id);
    if (!template || !(template instanceof HTMLTemplateElement)) {
      return null;
    }
    return template.content.cloneNode(true);
  }

  function replaceView(templateId, containerClass) {
    const layout = doc.querySelector('.layout-content');
    if (!layout) return null;
    const fragment = cloneTemplate(templateId);
    if (!fragment) return null;
    layout.textContent = '';
    layout.removeAttribute('id');
    layout.className = `layout-content${containerClass ? ` ${containerClass}` : ''}`;
    layout.appendChild(fragment);
    return layout;
  }

  function setTitleFromLayout(layout, fallback) {
    if (!layout) return;
    const titleHolder = layout.querySelector('[data-spa-title]');
    const title = titleHolder ? titleHolder.getAttribute('data-spa-title') : null;
    if (title) {
      doc.title = title;
      return;
    }
    if (fallback) {
      doc.title = fallback;
    }
  }

  function applyTemplateText(template, data) {
    let out = template || '';
    if (!data) return out;
    Object.keys(data).forEach((key) => {
      const value = data[key];
      const pattern = new RegExp(`{{\\s*${key}\\s*}}`, 'g');
      out = out.replace(pattern, String(value));
    });
    return out;
  }

  function clearElement(element) {
    if (!element) return;
    while (element.firstChild) {
      element.removeChild(element.firstChild);
    }
  }

  function setHidden(element, hidden) {
    if (!element) return;
    element.hidden = Boolean(hidden);
  }

  function setText(element, value) {
    if (!element) return;
    element.textContent = value == null ? '' : String(value);
  }

  function setHref(element, value) {
    if (!element) return;
    element.setAttribute('href', value || '#');
  }

  function setSrc(element, value) {
    if (!element) return;
    element.setAttribute('src', value || '');
  }

  function updateSearchInput(value) {
    const input = doc.getElementById('nav-search-input');
    if (input) {
      input.value = value || '';
    }
  }

  function formatFullDate(value) {
    const date = parseIsoDate(value);
    return date ? fullDateFormatter.format(date) : '';
  }

  function formatMonthYear(value) {
    const date = parseIsoDate(value);
    return date ? monthYearFormatter.format(date) : '';
  }

  function parseIsoDate(value) {
    if (!value) return null;
    const parts = String(value).split('-');
    if (parts.length < 2) return null;
    const year = parseInt(parts[0], 10);
    const month = parseInt(parts[1], 10);
    const day = parseInt(parts[2] || '1', 10);
    if (!Number.isFinite(year) || !Number.isFinite(month)) return null;
    const safeDay = Number.isFinite(day) ? day : 1;
    return new Date(Date.UTC(year, month - 1, safeDay));
  }

  function buildFullName(profile) {
    const fullName = normalizeQuery(profile && profile.fullName);
    if (fullName) return fullName;
    const parts = [];
    if (profile && profile.firstName) parts.push(profile.firstName);
    if (profile && profile.lastName) parts.push(profile.lastName);
    return parts.join(' ').trim();
  }

  function renderRoute(route) {
    if (!route) return;
    win.scrollTo(0, 0);
    if (route.type === 'welcome') {
      renderWelcome(route);
      return;
    }
    if (route.type === 'search') {
      renderSearch(route);
      return;
    }
    if (route.type === 'profile') {
      renderProfile(route);
    }
  }

  async function renderWelcome(route) {
    const layout = replaceView('spa-welcome-view', '');
    if (!layout) {
      fallbackToServer(win.location.href);
      return;
    }
    setTitleFromLayout(layout, 'My Resume');
    updateSearchInput(route.query || '');

    const container = layout.querySelector('[data-spa="profile-container"]');
    const loadMoreContainer = layout.querySelector('[data-spa="load-more-container"]');
    const loadMoreButton = layout.querySelector('[data-spa="load-more-button"]');
    const indicator = layout.querySelector('[data-spa="load-more-indicator"]');

    const viewState = {
      page: route.page,
      query: route.query,
      loading: false,
      hasNext: false
    };

    if (loadMoreButton) {
      loadMoreButton.addEventListener('click', (event) => {
        event.preventDefault();
        if (viewState.loading || !viewState.hasNext) return;
        loadPage(viewState.page + 1, true);
      });
    }

    await loadPage(viewState.page, false);

    async function loadPage(page, append) {
      if (!container) return;
      viewState.loading = true;
      if (indicator) indicator.style.display = 'block';
      if (loadMoreContainer) loadMoreContainer.style.display = 'none';

      const request = startRequest();
      try {
        const data = await fetchJson(buildWelcomeUrl(page, viewState.query), request.signal);
        if (!isCurrentRequest(request.id)) return;
        viewState.page = Number.isFinite(data.page) ? data.page : page;
        viewState.hasNext = Boolean(data.hasNext);
        renderProfileList(container, data.items || [], append);
        if (viewState.hasNext && loadMoreContainer) {
          loadMoreContainer.style.display = 'block';
        }
      } catch (error) {
        if (error && error.name === 'AbortError') return;
        if (!append) {
          fallbackToServer(win.location.href);
          return;
        }
        showError('Request failed');
      } finally {
        viewState.loading = false;
        if (indicator) indicator.style.display = 'none';
        if (!viewState.hasNext && loadMoreContainer) {
          loadMoreContainer.style.display = 'none';
        }
      }
    }
  }

  async function renderSearch(route) {
    const query = normalizeQuery(route.query);
    if (!query) {
      navigateTo(buildPath('/welcome'), true);
      return;
    }
    const layout = replaceView('spa-search-view', '');
    if (!layout) {
      fallbackToServer(win.location.href);
      return;
    }
    setTitleFromLayout(layout, null);
    updateSearchInput(query);

    const container = layout.querySelector('[data-spa="profile-container"]');
    const emptyState = layout.querySelector('[data-spa="search-empty"]');
    const title = layout.querySelector('[data-spa="search-title"]');
    const template = title ? title.getAttribute('data-spa-template') : '';

    const request = startRequest();
    try {
      const data = await fetchJson(buildSearchUrl(query, route.page), request.signal);
      if (!isCurrentRequest(request.id)) return;
      const items = Array.isArray(data.items) ? data.items : [];
      renderProfileList(container, items, false);
      if (title) {
        const count = items.length;
        title.textContent = applyTemplateText(template, { count, query });
      }
      if (emptyState) {
        emptyState.style.display = items.length ? 'none' : 'block';
      }
    } catch (error) {
      if (error && error.name === 'AbortError') return;
      fallbackToServer(win.location.href);
    }
  }

  async function renderProfile(route) {
    const layout = replaceView('spa-profile-view', 'l-container');
    if (!layout) {
      fallbackToServer(win.location.href);
      return;
    }
    updateSearchInput('');
    const request = startRequest();
    try {
      const data = await fetchJson(buildUrl(`/api/profiles/${encodeURIComponent(route.uid)}`), request.signal);
      if (!isCurrentRequest(request.id)) return;
      await renderProfileDetails(layout, data);
    } catch (error) {
      if (error && error.name === 'AbortError') return;
      fallbackToServer(win.location.href);
    }
  }

  function buildWelcomeUrl(page, query) {
    if (normalizeQuery(query)) {
      return buildSearchUrl(query, page);
    }
    return buildApiUrl('/api/profiles', { page });
  }

  function buildSearchUrl(query, page) {
    return buildApiUrl('/api/search', { q: query, page });
  }

  function renderProfileList(container, items, append) {
    if (!container) return;
    if (!append) {
      clearElement(container);
    }
    items.forEach((item) => {
      const fragment = buildProfileItem(item);
      if (fragment) {
        container.appendChild(fragment);
      }
    });
  }

  function buildProfileItem(item) {
    if (!item) return null;
    const fragment = cloneTemplate('spa-profile-item');
    if (!fragment) return null;
    const uid = item.uid || '';
    const link = buildPath(`/${uid}`);
    const fullName = buildFullName(item);
    const age = Number.isFinite(item.age) ? item.age : '';
    const title = fullName && age !== '' ? `${fullName}, ${age}` : fullName || '';
    const locationParts = [];
    if (item.city) locationParts.push(item.city);
    if (item.country) locationParts.push(item.country);
    const location = locationParts.join(', ');

    fragment.querySelectorAll('[data-spa-link="profile"]').forEach((anchor) => {
      setHref(anchor, link);
    });

    const photo = fragment.querySelector('[data-spa-photo]');
    if (photo) {
      setSrc(photo, item.smallPhoto || PLACEHOLDER_PHOTO);
      photo.setAttribute('alt', fullName || '');
    }

    setText(fragment.querySelector('[data-spa-text="profile-title"]'), title);
    setText(fragment.querySelector('[data-spa-text="profile-objective"]'), item.objective || '');
    setText(fragment.querySelector('[data-spa-text="profile-location"]'), location);
    setText(fragment.querySelector('[data-spa-text="profile-summary"]'), item.summary || '');
    return fragment;
  }

  async function renderProfileDetails(layout, profile) {
    const fullName = buildFullName(profile);
    doc.title = fullName || 'My Resume';

    const uid = profile.uid || '';
    const ownProfile = profile.ownProfile === true;
    const completed = profile.completed !== false;

    const photoLink = layout.querySelector('[data-spa="photo-link"]');
    const photo = layout.querySelector('[data-spa="photo"]');
    const photoSrc = profile.largePhoto || PLACEHOLDER_PHOTO;

    if (photo) {
      setSrc(photo, photoSrc);
      photo.setAttribute('alt', fullName || 'photo');
    }

    if (ownProfile) {
      setHref(photoLink, buildPath(`/${uid}/edit`));
    } else {
      if (photoLink) {
        photoLink.removeAttribute('href');
      }
    }

    const nameLinkWrap = layout.querySelector('[data-spa="name-link-wrap"]');
    const nameLink = layout.querySelector('[data-spa="name-link"]');
    const nameText = layout.querySelector('[data-spa="name-text"]');

    if (ownProfile) {
      setHidden(nameLinkWrap, false);
      setHidden(nameText, true);
      if (nameLink) {
        setHref(nameLink, buildPath(`/${uid}/edit`));
        setText(nameLink, fullName);
      }
    } else {
      setHidden(nameLinkWrap, true);
      setHidden(nameText, false);
      setText(nameText, fullName);
    }

    setHidden(layout.querySelector('[data-spa="profile-incomplete"]'), completed);

    const city = profile.city || '';
    const country = profile.country || '';
    setText(layout.querySelector('[data-spa="city"]'), city);
    setText(layout.querySelector('[data-spa="country"]'), country);
    setHidden(layout.querySelector('[data-spa="location-comma"]'), !(city && country));

    setText(layout.querySelector('[data-spa="age"]'), Number.isFinite(profile.age) ? profile.age : '');

    const birthdayWrap = layout.querySelector('[data-spa="birthday-wrap"]');
    if (profile.birthDay) {
      setText(layout.querySelector('[data-spa="birthday"]'), formatFullDate(profile.birthDay));
      setHidden(birthdayWrap, false);
    } else {
      setHidden(birthdayWrap, true);
    }

    renderContacts(layout.querySelector('[data-spa="contacts"]'), profile);

    const objective = profile.objective || '';
    const summary = profile.summary || '';
    setText(layout.querySelector('[data-spa="objective"]'), objective);
    setText(layout.querySelector('[data-spa="summary"]'), summary);

    const info = normalizeQuery(profile.info);
    const infoSection = layout.querySelector('[data-spa-section="info"]');
    const infoMobileSection = layout.querySelector('[data-spa-section="info-mobile"]');
    if (info) {
      setText(layout.querySelector('[data-spa="info-text"]'), info);
      setText(layout.querySelector('[data-spa="info-mobile-text"]'), info);
      setHidden(infoSection, false);
      setHidden(infoMobileSection, false);
    } else {
      setHidden(infoSection, true);
      setHidden(infoMobileSection, true);
    }

    const languages = Array.isArray(profile.languages) ? profile.languages : [];
    const hobbies = Array.isArray(profile.hobbies) ? profile.hobbies : [];
    const skills = Array.isArray(profile.skills) ? profile.skills : [];
    const practics = Array.isArray(profile.practics) ? profile.practics : [];
    const certificates = Array.isArray(profile.certificates) ? profile.certificates : [];
    const courses = Array.isArray(profile.courses) ? profile.courses : [];
    const educations = Array.isArray(profile.educations) ? profile.educations : [];

    await renderLanguages(layout, languages);
    renderHobbies(layout, hobbies);
    renderSkills(layout, skills);
    renderPractics(layout, practics);
    renderCertificates(layout, certificates);
    renderCourses(layout, courses);
    renderEducations(layout, educations);

    const editLinks = [
      { selector: '[data-spa="languages-edit"]', href: buildPath(`/${uid}/edit/languages`) },
      { selector: '[data-spa="hobbies-edit"]', href: buildPath(`/${uid}/edit/hobbies`) },
      { selector: '[data-spa="info-edit"]', href: buildPath(`/${uid}/edit/info`) },
      { selector: '[data-spa="objective-edit"]', href: buildPath(`/${uid}/edit/profile#inputObjective`) },
      { selector: '[data-spa="skills-edit"]', href: buildPath(`/${uid}/edit/skills`) },
      { selector: '[data-spa="practics-edit"]', href: buildPath(`/${uid}/edit/practics`) },
      { selector: '[data-spa="certificates-edit"]', href: buildPath('/edit/certificates') },
      { selector: '[data-spa="courses-edit"]', href: buildPath('/edit/courses') },
      { selector: '[data-spa="educations-edit"]', href: buildPath('/edit/education') },
      { selector: '[data-spa="languages-mobile-edit"]', href: buildPath('/account/login') },
      { selector: '[data-spa="hobbies-mobile-edit"]', href: buildPath('/account/login') },
      { selector: '[data-spa="info-mobile-edit"]', href: buildPath('/account/login') }
    ];

    editLinks.forEach((item) => {
      const link = layout.querySelector(item.selector);
      if (!link) return;
      if (ownProfile) {
        setHref(link, item.href);
        setHidden(link, false);
      } else {
        setHidden(link, true);
      }
    });

    if (certificates.length) {
      ensureCertificateViewer();
      if (typeof resume.initCertificateViewer === 'function') {
        resume.initCertificateViewer();
      }
    }
  }

  function renderContacts(container, profile) {
    if (!container) return;
    clearElement(container);

    const phone = normalizeQuery(profile.phone);
    const email = normalizeQuery(profile.email);
    const contacts = profile.contacts || {};

    if (phone) {
      appendContact(container, 'fa-phone', `tel:${phone}`, phone, false);
    }
    if (email) {
      appendContact(container, 'fa-envelope', `mailto:${email}`, email, false);
    }
    if (contacts.facebook) {
      appendContact(container, 'fa-facebook', contacts.facebook, contacts.facebook, true);
    }
    if (contacts.linkedin) {
      appendContact(container, 'fa-linkedin', contacts.linkedin, contacts.linkedin, true);
    }
    if (contacts.github) {
      appendContact(container, 'fa-github', contacts.github, contacts.github, true);
    }
    if (contacts.stackoverflow) {
      appendContact(container, 'fa-stack-overflow', contacts.stackoverflow, contacts.stackoverflow, true);
    }
  }

  function appendContact(container, iconClass, href, text, external) {
    if (!container || !href || !text) return;
    const link = doc.createElement('a');
    link.className = 'c-list__item';
    setHref(link, href);
    if (external) link.setAttribute('target', '_blank');
    const icon = doc.createElement('i');
    icon.className = `fa ${iconClass}`;
    const label = doc.createElement('span');
    label.textContent = text;
    link.appendChild(icon);
    link.appendChild(doc.createTextNode(' '));
    link.appendChild(label);
    container.appendChild(link);
  }

  async function renderLanguages(layout, languages) {
    const section = layout.querySelector('[data-spa-section="languages"]');
    const mobileSection = layout.querySelector('[data-spa-section="languages-mobile"]');
    const list = layout.querySelector('[data-spa="languages-list"]');
    const listMobile = layout.querySelector('[data-spa="languages-mobile-list"]');
    if (!languages.length) {
      setHidden(section, true);
      setHidden(mobileSection, true);
      return;
    }
    const labels = await resolveLanguageLabels(languages);
    fillLanguageList(list, languages, labels);
    fillLanguageList(listMobile, languages, labels);
    setHidden(section, false);
    setHidden(mobileSection, false);
  }

  function renderHobbies(layout, hobbies) {
    const section = layout.querySelector('[data-spa-section="hobbies"]');
    const mobileSection = layout.querySelector('[data-spa-section="hobbies-mobile"]');
    const list = layout.querySelector('[data-spa="hobbies-list"]');
    const listMobile = layout.querySelector('[data-spa="hobbies-mobile-list"]');
    if (!hobbies.length) {
      setHidden(section, true);
      setHidden(mobileSection, true);
      return;
    }
    fillHobbyList(list, hobbies);
    fillHobbyList(listMobile, hobbies);
    setHidden(section, false);
    setHidden(mobileSection, false);
  }

  function renderSkills(layout, skills) {
    const section = layout.querySelector('[data-spa-section="skills"]');
    const list = layout.querySelector('[data-spa="skills-list"]');
    if (!skills.length) {
      setHidden(section, true);
      return;
    }
    fillSkillsList(list, skills);
    setHidden(section, false);
  }

  function renderPractics(layout, practics) {
    const section = layout.querySelector('[data-spa-section="practics"]');
    const list = layout.querySelector('[data-spa="practics-list"]');
    if (!practics.length) {
      setHidden(section, true);
      return;
    }
    fillPracticsList(list, practics);
    setHidden(section, false);
  }

  function renderCertificates(layout, certificates) {
    const section = layout.querySelector('[data-spa-section="certificates"]');
    const list = layout.querySelector('[data-spa="certificates-list"]');
    if (!certificates.length) {
      setHidden(section, true);
      return;
    }
    fillCertificatesList(list, certificates);
    setHidden(section, false);
  }

  function renderCourses(layout, courses) {
    const section = layout.querySelector('[data-spa-section="courses"]');
    const list = layout.querySelector('[data-spa="courses-list"]');
    if (!courses.length) {
      setHidden(section, true);
      return;
    }
    fillCoursesList(list, courses);
    setHidden(section, false);
  }

  function renderEducations(layout, educations) {
    const section = layout.querySelector('[data-spa-section="educations"]');
    const list = layout.querySelector('[data-spa="educations-list"]');
    if (!educations.length) {
      setHidden(section, true);
      return;
    }
    fillEducationsList(list, educations);
    setHidden(section, false);
  }

  function fillLanguageList(container, languages, labels) {
    if (!container) return;
    clearElement(container);
    languages.forEach((language) => {
      if (!language) return;
      const item = doc.createElement('div');
      const strong = doc.createElement('strong');
      strong.textContent = language.name || '';
      item.appendChild(strong);
      item.appendChild(doc.createTextNode(': '));
      const level = doc.createElement('span');
      level.textContent = language.level || '';
      item.appendChild(level);
      if (language.hasLanguageType && language.type) {
        const label = labels && labels.get(language.type) ? labels.get(language.type) : language.type;
        if (label) {
          item.appendChild(doc.createTextNode(' ('));
          const type = doc.createElement('i');
          type.textContent = label;
          item.appendChild(type);
          item.appendChild(doc.createTextNode(')'));
        }
      }
      item.appendChild(doc.createElement('br'));
      container.appendChild(item);
    });
  }

  function fillHobbyList(container, hobbies) {
    if (!container) return;
    clearElement(container);
    hobbies.forEach((hobby) => {
      if (!hobby) return;
      const row = doc.createElement('tr');
      const iconCell = doc.createElement('td');
      const icon = doc.createElement('i');
      const css = hobby.cssClassName ? ` hobby-${hobby.cssClassName}` : '';
      icon.className = `fa hobby${css}`;
      iconCell.appendChild(icon);
      const nameCell = doc.createElement('td');
      nameCell.textContent = hobby.name || '';
      row.appendChild(iconCell);
      row.appendChild(nameCell);
      container.appendChild(row);
    });
  }

  function fillSkillsList(container, skills) {
    if (!container) return;
    clearElement(container);
    skills.forEach((skill) => {
      if (!skill) return;
      const row = doc.createElement('tr');
      const category = doc.createElement('td');
      const value = doc.createElement('td');
      category.textContent = skill.category || '';
      value.textContent = skill.value || '';
      row.appendChild(category);
      row.appendChild(value);
      container.appendChild(row);
    });
  }

  function fillPracticsList(container, practics) {
    if (!container) return;
    clearElement(container);
    practics.forEach((practic) => {
      if (!practic) return;
      const item = doc.createElement('li');
      const badge = doc.createElement('div');
      badge.className = 'timeline-badge danger';
      const icon = doc.createElement('i');
      icon.className = 'fa fa-briefcase';
      badge.appendChild(icon);

      const panel = doc.createElement('div');
      panel.className = 'timeline-panel';

      const heading = doc.createElement('div');
      heading.className = 'timeline-heading';

      const title = doc.createElement('h4');
      title.className = 'timeline-title';
      const position = practic.position || '';
      const company = practic.company || '';
      if (position && company) {
        title.textContent = `${position} at ${company}`;
      } else {
        title.textContent = position || company;
      }
      heading.appendChild(title);

      const dates = doc.createElement('p');
      const datesSmall = doc.createElement('small');
      datesSmall.className = 'dates';
      const dateIcon = doc.createElement('i');
      dateIcon.className = 'fa fa-calendar';
      datesSmall.appendChild(dateIcon);
      datesSmall.appendChild(doc.createTextNode(' '));

      const beginSpan = doc.createElement('span');
      beginSpan.textContent = formatMonthYear(practic.beginDate);
      datesSmall.appendChild(beginSpan);
      datesSmall.appendChild(doc.createTextNode(' - '));

      if (practic.finish) {
        const finishSpan = doc.createElement('span');
        finishSpan.textContent = formatMonthYear(practic.finishDate);
        datesSmall.appendChild(finishSpan);
      } else {
        const current = doc.createElement('strong');
        current.className = 'c-label c-label--danger';
        current.textContent = 'Current';
        datesSmall.appendChild(current);
      }

      dates.appendChild(datesSmall);
      heading.appendChild(dates);
      panel.appendChild(heading);

      const body = doc.createElement('div');
      body.className = 'timeline-body';

      const responsibilities = doc.createElement('p');
      const responsibilitiesLabel = doc.createElement('strong');
      responsibilitiesLabel.textContent = 'Responsibilities included:';
      const responsibilitiesText = doc.createElement('span');
      responsibilitiesText.textContent = practic.responsibilities || '';
      responsibilities.appendChild(responsibilitiesLabel);
      responsibilities.appendChild(doc.createTextNode(' '));
      responsibilities.appendChild(responsibilitiesText);
      body.appendChild(responsibilities);

      if (practic.demo) {
        const demo = doc.createElement('p');
        const demoLabel = doc.createElement('strong');
        demoLabel.textContent = 'Demo:';
        const demoLink = doc.createElement('a');
        demoLink.setAttribute('href', practic.demo);
        demoLink.textContent = practic.demo;
        demo.appendChild(demoLabel);
        demo.appendChild(doc.createTextNode(' '));
        demo.appendChild(demoLink);
        body.appendChild(demo);
      }

      if (practic.src) {
        const source = doc.createElement('p');
        const sourceLabel = doc.createElement('strong');
        sourceLabel.textContent = 'Source code:';
        const sourceLink = doc.createElement('a');
        sourceLink.setAttribute('href', practic.src);
        sourceLink.textContent = practic.src;
        source.appendChild(sourceLabel);
        source.appendChild(doc.createTextNode(' '));
        source.appendChild(sourceLink);
        body.appendChild(source);
      }

      panel.appendChild(body);

      item.appendChild(badge);
      item.appendChild(panel);
      container.appendChild(item);
    });
  }

  function fillCertificatesList(container, certificates) {
    if (!container) return;
    clearElement(container);
    certificates.forEach((certificate) => {
      if (!certificate) return;
      const link = doc.createElement('a');
      link.className = 'c-thumbnail certificate-link';
      link.setAttribute('href', '#');
      link.setAttribute('data-title', certificate.name || '');
      link.setAttribute('data-url', certificate.largeUrl || '');

      const img = doc.createElement('img');
      img.className = 'u-img-responsive';
      img.setAttribute('src', certificate.smallUrl || '');
      img.setAttribute('alt', certificate.name || '');

      const label = doc.createElement('span');
      label.textContent = certificate.name || '';

      link.appendChild(img);
      link.appendChild(label);
      container.appendChild(link);
    });
  }

  function fillCoursesList(container, courses) {
    if (!container) return;
    clearElement(container);
    courses.forEach((course) => {
      if (!course) return;
      const item = doc.createElement('li');
      const badge = doc.createElement('div');
      badge.className = 'timeline-badge success';
      const icon = doc.createElement('i');
      icon.className = 'fa fa-book';
      badge.appendChild(icon);

      const panel = doc.createElement('div');
      panel.className = 'timeline-panel';
      const heading = doc.createElement('div');
      heading.className = 'timeline-heading';

      const title = doc.createElement('h4');
      title.className = 'timeline-title';
      const name = course.name || '';
      const school = course.school || '';
      if (name && school) {
        title.textContent = `${name} at ${school}`;
      } else {
        title.textContent = name || school;
      }
      heading.appendChild(title);

      const dates = doc.createElement('p');
      const datesSmall = doc.createElement('small');
      datesSmall.className = 'dates';
      const dateIcon = doc.createElement('i');
      dateIcon.className = 'fa fa-calendar';
      datesSmall.appendChild(dateIcon);
      datesSmall.appendChild(doc.createTextNode(' '));
      const label = doc.createElement('strong');
      label.textContent = 'Finish Date:';
      datesSmall.appendChild(label);
      datesSmall.appendChild(doc.createTextNode(' '));

      if (course.finish) {
        const finishSpan = doc.createElement('span');
        finishSpan.textContent = formatMonthYear(course.finishDate);
        datesSmall.appendChild(finishSpan);
      } else {
        const current = doc.createElement('strong');
        current.className = 'c-label c-label--danger';
        current.textContent = 'Not finished yet';
        datesSmall.appendChild(current);
      }

      dates.appendChild(datesSmall);
      heading.appendChild(dates);
      panel.appendChild(heading);

      item.appendChild(badge);
      item.appendChild(panel);
      container.appendChild(item);
    });
  }

  function fillEducationsList(container, educations) {
    if (!container) return;
    clearElement(container);
    educations.forEach((education) => {
      if (!education) return;
      const item = doc.createElement('li');
      const badge = doc.createElement('div');
      badge.className = 'timeline-badge warning';
      const icon = doc.createElement('i');
      icon.className = 'fa fa-graduation-cap';
      badge.appendChild(icon);

      const panel = doc.createElement('div');
      panel.className = 'timeline-panel';
      const heading = doc.createElement('div');
      heading.className = 'timeline-heading';

      const title = doc.createElement('h4');
      title.className = 'timeline-title';
      title.textContent = education.summary || '';
      heading.appendChild(title);

      const dates = doc.createElement('p');
      const datesSmall = doc.createElement('small');
      datesSmall.className = 'dates';
      const dateIcon = doc.createElement('i');
      dateIcon.className = 'fa fa-calendar';
      datesSmall.appendChild(dateIcon);
      datesSmall.appendChild(doc.createTextNode(' '));
      const beginSpan = doc.createElement('span');
      beginSpan.textContent = education.beginYear != null ? String(education.beginYear) : '';
      datesSmall.appendChild(beginSpan);
      datesSmall.appendChild(doc.createTextNode(' - '));

      if (education.finish) {
        const finishSpan = doc.createElement('span');
        finishSpan.textContent = education.finishYear != null ? String(education.finishYear) : '';
        datesSmall.appendChild(finishSpan);
      } else {
        const current = doc.createElement('strong');
        current.className = 'c-label c-label--danger';
        current.textContent = 'Current';
        datesSmall.appendChild(current);
      }

      dates.appendChild(datesSmall);
      heading.appendChild(dates);

      const body = doc.createElement('div');
      body.className = 'timeline-body';
      const bodyText = doc.createElement('p');
      const faculty = education.faculty || '';
      const university = education.university || '';
      if (faculty && university) {
        bodyText.textContent = `${faculty}, ${university}`;
      } else {
        bodyText.textContent = faculty || university;
      }
      body.appendChild(bodyText);

      panel.appendChild(heading);
      panel.appendChild(body);

      item.appendChild(badge);
      item.appendChild(panel);
      container.appendChild(item);
    });
  }

  async function resolveLanguageLabels(languages) {
    const needsLabels = languages.some((language) => language && language.hasLanguageType && language.type);
    if (!needsLabels) return null;
    const labels = await ensureStaticData();
    return labels || null;
  }

  function ensureStaticData() {
    if (state.staticDataPromise) return state.staticDataPromise;
    const url = buildUrl('/api/static-data');
    state.staticDataPromise = fetchJson(url)
      .then((data) => {
        const map = new Map();
        const types = data && Array.isArray(data.languageTypes) ? data.languageTypes : [];
        types.forEach((type) => {
          if (!type || !type.code) return;
          map.set(type.code, type.label || type.code);
        });
        state.languageTypeLabels = map;
        return map;
      })
      .catch(() => {
        const fallback = new Map();
        state.languageTypeLabels = fallback;
        return fallback;
      });
    return state.staticDataPromise;
  }

  function ensureCertificateViewer() {
    if (doc.getElementById('certificateViewer')) return;
    const fragment = cloneTemplate('spa-certificate-viewer');
    if (!fragment) return;
    doc.body.appendChild(fragment);
  }

  function showError(message) {
    if (resume && typeof resume.showErrorDialog === 'function') {
      resume.showErrorDialog(message);
      return;
    }
    win.alert(message);
  }

  function fallbackToServer(target) {
    const url = target instanceof URL ? target.toString() : String(target || win.location.href);
    win.location.href = url;
  }

  function shouldHandleLink(link) {
    if (!link) return false;
    if (link.target && link.target !== '_self') return false;
    if (link.hasAttribute('download')) return false;
    const rel = link.getAttribute('rel') || '';
    if (rel.split(/\s+/).includes('external')) return false;
    const href = link.getAttribute('href') || '';
    if (!href || href.startsWith('#') || href.startsWith('javascript:')) return false;
    const url = new URL(href, win.location.href);
    if (url.origin !== win.location.origin) return false;
    return Boolean(parseRoute(url));
  }

  function navigateTo(href, replace) {
    const url = href instanceof URL ? href : new URL(String(href), win.location.origin);
    const route = parseRoute(url);
    if (!route) {
      fallbackToServer(url);
      return;
    }
    if (replace) {
      win.history.replaceState({}, '', url.toString());
    } else {
      win.history.pushState({}, '', url.toString());
    }
    renderRoute(route);
  }

  function bindNavigation() {
    if (bindNavigation.bound) return;
    bindNavigation.bound = true;

    doc.addEventListener('click', (event) => {
      if (event.defaultPrevented) return;
      if (event.button !== 0) return;
      if (event.metaKey || event.ctrlKey || event.shiftKey || event.altKey) return;
      const target = event.target;
      if (!(target instanceof Element)) return;
      const link = target.closest('a');
      if (!shouldHandleLink(link)) return;
      event.preventDefault();
      navigateTo(link.href, false);
    });

    win.addEventListener('popstate', () => {
      const route = parseRoute(win.location.href);
      if (route) {
        renderRoute(route);
      }
    });

    const form = doc.getElementById('nav-search-form');
    if (form) {
      form.addEventListener('submit', (event) => {
        const input = doc.getElementById('nav-search-input');
        if (!input) return;
        event.preventDefault();
        const query = normalizeQuery(input.value);
        if (!query) {
          navigateTo(buildPath('/welcome'), true);
          return;
        }
        const url = buildUrl('/search');
        url.searchParams.set('q', query);
        navigateTo(url, false);
      });
    }
  }

  function init() {
    const route = parseRoute(win.location.href);
    if (!route) return;
    bindNavigation();
    renderRoute(route);
  }

  resume.onReady(() => {
    init();
  });
})();
