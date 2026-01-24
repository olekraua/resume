(() => {
  'use strict';

  const win = window;
  const doc = win.document;

  const selectors = {
    input: '.c-slider__input',
    labels: '[data-slider-labels]'
  };

  const isElement = (value) => value && value.nodeType === 1;

  const toRoot = (container) => {
    if (!container) return doc;
    if (container === doc) return doc;
    if (isElement(container)) return container;
    if (container.jquery) return container[0] || doc;
    if (typeof container === 'string') return doc.querySelector(container) || doc;
    if (container instanceof NodeList) return container[0] || doc;
    if (Array.isArray(container)) return container[0] || doc;
    return doc;
  };

  const collectInputs = (container) => {
    const root = toRoot(container);
    const inputs = [];
    if (isElement(root) && root.matches(selectors.input)) {
      inputs.push(root);
    }
    if (root && root.querySelectorAll) {
      inputs.push(...root.querySelectorAll(selectors.input));
    }
    return inputs;
  };

  const toNumber = (value, fallback) => {
    const num = Number.parseFloat(value);
    return Number.isFinite(num) ? num : fallback;
  };

  const parseLabelData = (element) => {
    if (!element) return null;
    const raw = element.getAttribute('data-labels');
    if (!raw) return null;
    try {
      const parsed = JSON.parse(raw);
      if (Array.isArray(parsed)) return parsed;
    } catch (error) {
      // Fall through to comma-separated parsing.
    }
    const parts = raw
      .split(',')
      .map((item) => item.trim())
      .filter(Boolean);
    return parts.length ? parts : null;
  };

  const resolveLabels = (labelsEl, input) => {
    if (Array.isArray(win.languageLevelLabels) && win.languageLevelLabels.length) {
      return win.languageLevelLabels;
    }
    return parseLabelData(labelsEl) || parseLabelData(input);
  };

  const ensureLabels = (labelsEl, labels) => {
    if (!labelsEl || labelsEl.dataset.uiLabels === 'true') return;
    labels.forEach((label) => {
      const span = doc.createElement('span');
      span.className = 'c-slider__label';
      span.textContent = String(label);
      labelsEl.appendChild(span);
    });
    labelsEl.dataset.uiLabels = 'true';
  };

  const updateProgress = (input) => {
    if (!input) return;
    const min = toNumber(input.min, 0);
    const max = toNumber(input.max, 0);
    const value = toNumber(input.value, 0);
    if (max <= min) {
      input.style.setProperty('--c-slider-progress', '0%');
      return;
    }
    const percent = ((value - min) / (max - min)) * 100;
    input.style.setProperty('--c-slider-progress', `${percent}%`);
  };

  const bindInput = (input) => {
    if (!input || input.dataset.uiSlider === 'true') return;
    input.dataset.uiSlider = 'true';
    updateProgress(input);
    const update = () => updateProgress(input);
    input.addEventListener('input', update, { passive: true });
    input.addEventListener('change', update);
    const slider = input.closest('.c-slider');
    const labelsEl = slider ? slider.querySelector(selectors.labels) : null;
    const labels = resolveLabels(labelsEl, input);
    if (labels && labels.length) {
      ensureLabels(labelsEl, labels);
    }
  };

  const init = (container) => {
    collectInputs(container).forEach((input) => bindInput(input));
  };

  const resume = win.resume || (win.resume = {});
  resume.initLevelSliders = init;
  resume.ui = resume.ui || {};
  resume.ui.sliders = {
    init,
    updateProgress
  };
  if (resume.interactions && typeof resume.interactions === 'object') {
    resume.interactions.updateSliderProgress = updateProgress;
  }
})();
