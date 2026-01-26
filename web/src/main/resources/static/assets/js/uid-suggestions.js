document.addEventListener('DOMContentLoaded', function () {
  var uidInput = document.getElementById('uid');
  var firstNameInput = document.getElementById('firstName');
  var lastNameInput = document.getElementById('lastName');
  var autoValue = '';

  function canAutofill() {
    return uidInput && (uidInput.value === '' || uidInput.value === autoValue);
  }

  function requestUidHint() {
    if (!firstNameInput || !lastNameInput || !uidInput) {
      return;
    }
    if (!canAutofill()) {
      return;
    }
    var first = firstNameInput.value || '';
    var last = lastNameInput.value || '';
    if (!first.trim() && !last.trim()) {
      return;
    }
    var url = '/register/uid-hint?firstName=' + encodeURIComponent(first)
      + '&lastName=' + encodeURIComponent(last);
    fetch(url)
      .then(function (res) { return res.text(); })
      .then(function (text) {
        if (text && canAutofill()) {
          uidInput.value = text;
          autoValue = text;
        }
      })
      .catch(function () { });
  }

  if (uidInput) {
    uidInput.addEventListener('input', function () {
      if (uidInput.value !== autoValue) {
        autoValue = '';
      }
    });
  }
  if (firstNameInput) {
    firstNameInput.addEventListener('blur', requestUidHint);
  }
  if (lastNameInput) {
    lastNameInput.addEventListener('blur', requestUidHint);
  }

  document.querySelectorAll('.uid-suggestion').forEach(function (link) {
    link.addEventListener('click', function (event) {
      event.preventDefault();
      var targetId = link.getAttribute('data-target');
      var value = link.getAttribute('data-value');
      var input = document.getElementById(targetId);
      if (input) {
        input.value = value;
        input.focus();
      }
    });
  });
});
