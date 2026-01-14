var resume = {
	initCertificateViewer : function() {
		$('a.certificate-link').click(function(e) {
			e.preventDefault();
			var title = $(this).attr("data-title");
			$('#certificateViewer .c-modal__title').html(title);
			$('#certificateViewer .c-modal__body img').attr('src',$(this).attr("data-url"));
			resume.interactions.showModal('#certificateViewer');
		});
	},
	createDatePicker : function() {
		if (typeof flatpickr !== 'function') {
			return;
		}
		var elements = document.querySelectorAll('.datepicker');
		if (elements.length === 0) {
			return;
		}
		elements.forEach(function(element) {
			if (element._flatpickr) {
				return;
			}
			flatpickr(element, {
				dateFormat: 'Y-m-d',
				allowInput: true
			});
		});
	},
	initLevelSliders : function(container) {
		if (!Array.isArray(window.languageLevelLabels)) {
			return;
		}
		var $container = container ? $(container) : $(document);
		$container.find('.c-slider__input').each(function() {
			var $input = $(this);
			if ($input.data('uiSlider')) {
				return;
			}
			$input.data('uiSlider', true);
			resume.interactions.updateSliderProgress($input[0]);
			$input.on('input change', function() {
				resume.interactions.updateSliderProgress(this);
			});
			var $labels = $input.closest('.c-slider').find('[data-slider-labels]');
			if ($labels.length && !$labels.data('uiLabels')) {
				window.languageLevelLabels.forEach(function(label) {
					$labels.append('<span class="c-slider__label">' + label + '</span>');
				});
				$labels.data('uiLabels', true);
			}
		});
	},
	createPhotoUploader : function(){
		var $input = $('#profilePhoto');
		if ($input.length === 0) {
			return;
		}
		resume.interactions.initFileInputs($input.closest('.c-file'));
		var $photo = $('#currentPhoto');
		if ($photo.length) {
			$photo.attr('data-original-src', $photo.attr('src'));
		}
		$input.on('change', function() {
			var file = this.files && this.files[0];
			if (!file) {
				if ($photo.length) {
					$photo.attr('src', $photo.attr('data-original-src'));
					$photo.css('display', 'block');
				}
				return;
			}
			if ($photo.length) {
				var url = URL.createObjectURL(file);
				$photo.attr('src', url);
				$photo.css('display', 'block');
			}
		});
	},
	createCertificateUploader : function(csrfToken, uploadUrl){
		var resolvedUploadUrl = uploadUrl || '/edit/certificates/upload';
		if(csrfToken && resolvedUploadUrl.indexOf('_csrf=') === -1) {
			var separator = resolvedUploadUrl.indexOf('?') === -1 ? '?' : '&';
			resolvedUploadUrl += separator + '_csrf='+csrfToken;
		}
		var $input = $('#certificateFile');
		if ($input.length === 0) {
			return;
		}
		var $wrapper = $input.closest('.c-file');
		var $status = $wrapper.find('[data-file-status]');
		resume.interactions.initFileInputs($wrapper);
		$input.off('change.uiUpload').on('change.uiUpload', function() {
			var file = this.files && this.files[0];
			if (!file) {
				return;
			}
			$status.text('Завантаження...');
			var formData = new FormData();
			formData.append('certificateFile', file);
			$.ajax({
				url: resolvedUploadUrl,
				type: 'POST',
				data: formData,
				processData: false,
				contentType: false
			}).done(function(response) {
				if (response && response.certificateName != null && $('#certificateName').val().trim() === '') {
					$('#certificateName').val(response.certificateName);
				}
				if (response && response.smallUrl) {
					$('#certificateUploader').attr('data-small-url', response.smallUrl);
				}
				if (response && response.largeUrl) {
					$('#certificateUploader').attr('data-large-url', response.largeUrl);
				}
				if (response && response.issuer != null && $('#certificateIssuer').val().trim() === '') {
					$('#certificateIssuer').val(response.issuer);
				}
				$status.text('Файл завантажено');
			}).fail(function() {
				$status.text('Помилка завантаження');
				resume.showErrorDialog(messages.errorUploadCertificate);
			});
		});
	},
	interactions : {
		init : function() {
			resume.interactions.initNavbar();
			resume.interactions.initDropdowns();
			resume.interactions.initModals();
			resume.interactions.initFileInputs();
			resume.interactions.initHobbyButtons();
		},
		initNavbar : function() {
			$(document).on('click', '[data-ui-toggle=\"collapse\"]', function(e) {
				e.preventDefault();
				var target = $(this).attr('data-ui-target');
				if (!target) {
					return;
				}
				var $collapse = $(target);
				if ($collapse.length === 0 || $collapse.hasClass('collapsing')) {
					return;
				}
				var isOpen = $collapse.hasClass('is-open');
				var duration = 350;
				if (isOpen) {
					$collapse.css('height', $collapse[0].scrollHeight + 'px');
					$collapse[0].offsetHeight;
					$collapse.addClass('collapsing')
						.removeClass('is-open')
						.css('display', 'block')
						.css('height', 0);
					setTimeout(function() {
						$collapse.removeClass('collapsing').css({ height: '', display: '' });
					}, duration);
				} else {
					$collapse.addClass('collapsing')
						.css('display', 'block')
						.css('height', 0);
					var height = $collapse[0].scrollHeight;
					$collapse[0].offsetHeight;
					$collapse.css('height', height + 'px');
					setTimeout(function() {
						$collapse.removeClass('collapsing')
							.addClass('is-open')
							.css({ height: '', display: '' });
					}, duration);
				}
				$(this).toggleClass('is-collapsed', isOpen).attr('aria-expanded', isOpen ? 'false' : 'true');
			});
		},
		initDropdowns : function() {
			var closeAll = function() {
				$('.c-dropdown.is-open').removeClass('is-open').find('.c-dropdown__toggle').attr('aria-expanded', 'false');
			};
			$(document).on('click', '.c-dropdown__toggle', function(e) {
				e.preventDefault();
				var $dropdown = $(this).closest('.c-dropdown');
				var wasOpen = $dropdown.hasClass('is-open');
				closeAll();
				if (!wasOpen) {
					$dropdown.addClass('is-open');
					$(this).attr('aria-expanded', 'true');
				}
			});
			$(document).on('click', '.c-dropdown__menu a', function() {
				var $dropdown = $(this).closest('.c-dropdown');
				if ($dropdown.length) {
					$dropdown.removeClass('is-open').find('.c-dropdown__toggle').attr('aria-expanded', 'false');
				}
			});
			$(document).on('click', function(e) {
				if ($(e.target).closest('.c-dropdown').length === 0) {
					closeAll();
				}
			});
			$(document).on('keydown', function(e) {
				if (e.key === 'Escape') {
					closeAll();
				}
			});
		},
		initModals : function() {
			$(document).on('click', '[data-ui-dismiss=\"modal\"]', function() {
				var $modal = $(this).closest('.c-modal');
				if ($modal.length) {
					resume.interactions.hideModal($modal);
				}
			});
			$(document).on('keydown', function(e) {
				if (e.key !== 'Escape') {
					return;
				}
				var $open = $('.c-modal.is-open').last();
				if ($open.length) {
					resume.interactions.hideModal($open);
				}
			});
		},
		showModal : function(modal) {
			var $modal = typeof modal === 'string' ? $(modal) : $(modal);
			if ($modal.length === 0) {
				return;
			}
			if ($modal.hasClass('is-open')) {
				return;
			}
			var $body = $('body');
			if (!$body.hasClass('c-modal-open')) {
				var scrollbarWidth = window.innerWidth - document.documentElement.clientWidth;
				if (scrollbarWidth > 0) {
					$body.data('ui-scrollbar-width', scrollbarWidth).css('padding-right', scrollbarWidth + 'px');
				}
			}
			if (!$modal.attr('tabindex')) {
				$modal.attr('tabindex', '-1');
			}
			$modal.attr('aria-hidden', 'false').attr('aria-modal', 'true');
			$modal.css('display', 'block');
			$body.addClass('c-modal-open');
			$('.c-modal-backdrop').remove();
			var $backdrop = $('<div class=\"c-modal-backdrop is-fade\"></div>');
			$body.append($backdrop);
			setTimeout(function() {
				$modal.addClass('is-open');
				$backdrop.addClass('is-open');
				$modal.trigger('focus');
			}, 10);
			$backdrop.on('click', function() {
				resume.interactions.hideModal($modal);
			});
		},
		hideModal : function(modal) {
			var $modal = typeof modal === 'string' ? $(modal) : $(modal);
			if ($modal.length === 0) {
				return;
			}
			var $backdrop = $('.c-modal-backdrop');
			$modal.removeClass('is-open');
			$backdrop.removeClass('is-open');
			$modal.attr('aria-hidden', 'true').removeAttr('aria-modal');
			setTimeout(function() {
				$modal.css('display', 'none');
				$backdrop.remove();
				if ($('.c-modal.is-open').length === 0) {
					var $body = $('body');
					$body.removeClass('c-modal-open');
					if ($body.data('ui-scrollbar-width')) {
						$body.css('padding-right', '').removeData('ui-scrollbar-width');
					}
				}
			}, 300);
		},
		updateSliderProgress : function(input) {
			var min = parseFloat(input.min || 0);
			var max = parseFloat(input.max || 0);
			var value = parseFloat(input.value || 0);
			if (max <= min) {
				input.style.setProperty('--c-slider-progress', '0%');
				return;
			}
			var percent = ((value - min) / (max - min)) * 100;
			input.style.setProperty('--c-slider-progress', percent + '%');
		},
		initFileInputs : function(container) {
			var $root = container ? $(container) : $(document);
			var $targets = $root.is('.c-file') ? $root.add($root.find('.c-file')) : $root.find('.c-file');
			$targets.each(function() {
				var $wrapper = $(this);
				var $input = $wrapper.find('.c-file__input');
				var $caption = $wrapper.find('[data-file-caption]');
				var $clear = $wrapper.find('[data-file-clear]');
				var defaultCaption = $caption.data('default') || $caption.text();
				$caption.data('default', defaultCaption);
				$input.off('change.uiFile').on('change.uiFile', function() {
					var fileName = this.files && this.files[0] ? this.files[0].name : '';
					$caption.text(fileName || defaultCaption);
					$wrapper.find('[data-file-status]').text('');
				});
				$clear.off('click.uiFile').on('click.uiFile', function() {
					resume.interactions.clearFileInput($input);
					$caption.text(defaultCaption);
					$wrapper.find('[data-file-status]').text('');
				});
			});
		},
		clearFileInput : function($input) {
			if (!$input || $input.length === 0) {
				return;
			}
			$input.val('');
			var $target = $input;
			if ($input.val()) {
				var $clone = $input.clone(true);
				$input.replaceWith($clone);
				$target = $clone;
			}
			$target.trigger('change');
		},
		initHobbyButtons : function() {
			$(document).on('change', '.hobby-btn input[type=\"checkbox\"]', function() {
				$(this).closest('.hobby-btn').toggleClass('is-active', this.checked);
			});
		}
	},
	showErrorDialog : function(message) {
		alert(message);
	},
	post : function(path, params) {
		var form = document.createElement("form");
		form.setAttribute("method", 'post');
		form.setAttribute("action", path);
		for ( var key in params) {
			if (params.hasOwnProperty(key)) {
				var value = params[key];
				if (value != undefined) {
					var hiddenField = document.createElement("input");
					hiddenField.setAttribute("type", "hidden");
					hiddenField.setAttribute("name", key);
					hiddenField.setAttribute("value", params[key]);
					form.appendChild(hiddenField);
				}
			}
		}
		document.body.appendChild(form);
		form.submit();
	},
	
	logout : function (csrfToken){
		resume.post('/logout', {
			_csrf : csrfToken
		});
	},
	
	moreProfiles : function(searchQuery) {
		var page = parseInt($('#profileContainer').attr('data-profile-number')) + 1;
		var total= parseInt($('#profileContainer').attr('data-profile-total'));
		if (page >= total) {
			return;
		}
		var url = '/fragment/more?page=' + page;
		if(searchQuery != undefined && searchQuery.trim() != '') {
			url += '&query='+searchQuery;
		}
		
		$('#loadMoreContainer').css('display', 'none');
		$('#loadMoreIndicator').css('display', 'block');
		$.ajax({
			url : url,
			success : function(data) {
				$('#loadMoreIndicator').css('display', 'none');
				$('#profileContainer').append(data);
				$('#profileContainer').attr('data-profile-number', page);
				if (page >= total-1) {
					$('#loadMoreIndicator').remove();
					$('#loadMoreContainer').remove();
				} else {
					$('#loadMoreContainer').css('display', 'block');
				}
			},
			error : function(data) {
				resume.showErrorDialog(messages.errorAjax);
			}
		});
	},

	ui : {
		// http://handlebarsjs.com/
		template : null,

		getTemplate : function() {
			if (resume.ui.template == null) {
				var source = $("#ui-block-template").html();
				resume.ui.template = Handlebars.compile(source);
			}
			return resume.ui.template;
		},

		addBlock : function() {
			var template = resume.ui.getTemplate();
			var container = $('#ui-block-container');
			var blockIndex = container.find('.ui-item').length;
			var context = {
				blockIndex : blockIndex
			};
			if (window.languageLevelDefault !== undefined) {
				context.levelDefault = window.languageLevelDefault;
			}
			if (window.languageLevelMax !== undefined) {
				context.levelMax = window.languageLevelMax;
			}
			container.append(template(context));

			resume.createDatePicker();
			resume.initLevelSliders(container);
		},
		
		updateSelect : function(thisObj) {
			if(thisObj.val() == '') {
				var idSelectRef = thisObj.attr('data-ref-select');
				$('#'+idSelectRef).val('');
			}
		}
	},

	certificates : {
		showUploadDialog : function() {
			$('#certificateUploader').attr('data-small-url', '').attr('data-large-url', '');
			$('#certificateName').val('');
			$('#certificateIssuer').val('');
			if($('#certificateFile').length) {
				var $file = $('#certificateFile');
				var $wrapper = $file.closest('.c-file');
				var $caption = $wrapper.find('[data-file-caption]');
				resume.interactions.clearFileInput($file);
				$caption.text($caption.data('default') || $caption.text());
				$wrapper.find('[data-file-status]').text('');
			}
			resume.interactions.showModal('#certificateUploader');
		},
		
		add : function (){
			var certificateName = $('#certificateName').val();
			//https://www.tjvantoll.com/2012/08/05/html5-form-validation-showing-all-error-messages/
			if(certificateName.trim() == '') {
				alert('certificateName is null')
				return;
			}
			var smallUrl = $('#certificateUploader').attr('data-small-url');
			var largeUrl = $('#certificateUploader').attr('data-large-url');
			if(!smallUrl || !largeUrl) {
				resume.showErrorDialog(messages.errorUploadCertificate);
				return;
			}
			var template = resume.ui.getTemplate();
			var container = $('#ui-block-container');
			var blockIndex = container.find('.ui-item').length;
			var context = {
				blockIndex : blockIndex,
				name : $('#certificateName').val(),
				issuer : $('#certificateIssuer').val(),
				smallUrl : smallUrl,
				largeUrl : largeUrl
			};
			container.append(template(context));
			resume.interactions.hideModal('#certificateUploader');
			$('#certificateName').val('');
			$('#certificateIssuer').val('');
			if($('#certificateFile').length) {
				var $file = $('#certificateFile');
				var $wrapper = $file.closest('.c-file');
				var $caption = $wrapper.find('[data-file-caption]');
				resume.interactions.clearFileInput($file);
				$caption.text($caption.data('default') || $caption.text());
				$wrapper.find('[data-file-status]').text('');
			}
		}
	},

	hobbies : {
		errorTimeout : null,

		save : function() {
			var hobbies = '';
			var selectedHobbyButtons = $('.hobby-btn.is-active');
			var maxHobbies = parseInt($('#ui-block-container').attr('data-max-hobbies'));
			if (selectedHobbyButtons.length > maxHobbies) {
				var closeFunction = function() {
					$('#errorAlert').fadeOut('slow');
					if (resume.hobbies.errorTimeout != null) {
						clearTimeout(resume.hobbies.errorTimeout);
						resume.hobbies.errorTimeout = null;
					}
				};
				$('#errorAlert button').unbind('click');
				$('#errorAlert button').click(closeFunction);
				$('#errorAlert').fadeIn('slow');
				resume.hobbies.errorTimeout = setTimeout(closeFunction, 5000);
				return;
			}
			for (var i = 0; i < selectedHobbyButtons.length; i++) {
				hobbies += $(selectedHobbyButtons[i]).attr('data-hobby');
				if (i != selectedHobbyButtons.length - 1) {
					hobbies += ',';
				}
			}
			var _csrfToken = $('#hobbyContainer').attr('data-csrf-value');
			var postUrl = $('#hobbyContainer').attr('data-post-url') || '/edit/hobbies';
			resume.post(postUrl, {
				hobbies : hobbies,
				_csrf : _csrfToken
			});
		}
	}
};

// --- Search suggestions in navbar ---
$(function() {
	if (resume && resume.interactions && typeof resume.interactions.init === 'function') {
		resume.interactions.init();
	}
	var $input = $('#nav-search-input');
	var $list = $('#nav-search-suggest');
	if ($input.length === 0 || $list.length === 0) {
		return;
	}
	var timer = null;

	function hide() {
		$list.hide().empty();
	}

	$input.on('input', function() {
		var q = $(this).val().trim();
		if (q.length < 2) {
			hide();
			return;
		}
		clearTimeout(timer);
		timer = setTimeout(function() {
			$.getJSON('/api/suggest', { q: q, limit: 5 })
				.done(function(items) {
					$list.empty();
					if (!items || items.length === 0) {
						$list.append('<li class="empty">Нічого не знайдено</li>');
					} else {
						$.each(items, function(_, it) {
							var text = it.fullName || it.uid;
							var link = $('<a/>', { href: '/' + it.uid, text: text });
							$list.append($('<li/>').append(link));
						});
					}
					$list.show();
				})
				.fail(hide);
		}, 200);
	});

	$input.on('blur', function() {
		setTimeout(hide, 200);
	});

	$('#nav-search-form').on('submit', hide);
});
