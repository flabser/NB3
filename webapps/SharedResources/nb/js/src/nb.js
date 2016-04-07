/**
 * @author Medet
 */

'use strict';

var nb = {
    APP_NAME: location.hostname,
    MODULE: location.pathname.split('/')[1],
    LANG: (function() {
        var lang;
        var ck = document.cookie.match('(lang)=(.*?)($|;|,(?! ))');
        if (ck) {
            lang = ck[2];
        }

        if (lang === 'KAZ') {
            lang = 'kk';
        } else if (lang === 'ENG') {
            lang = 'en';
        } else {
            lang = 'ru';
        }

        return lang;
    })(),
    debug: false,
    api: {
        translations: 'p?id=common-captions',
        upload: 'UploadFile'
    },
    options: {
        dateFormat: 'yy-mm-dd',
        sideTreeStorageName: location.pathname.split('/')[1] + '-side-tree-toggle',
        searchReferStorageName: location.pathname.split('/')[1] + '-search_refer'
    },
    translations: {
        yes: 'Да',
        no: 'Нет',
        ok: 'Ok',
        cancel: 'Отмена',
        select: 'Выбрать',
        dialog_no_selected_value: 'Вы не сделали выбор'
    }
};

var nbApp = { /* local application namespace */ };

/**
 * ajax
 */
nb.ajax = function(options) {
    return $.ajax(options);
};

/**
 * getText
 */
nb.getText = function(stringKey, defaultText) {
    if (this.translations[stringKey]) {
        return this.translations[stringKey];
    } else {
        return (defaultText !== undefined) ? defaultText : stringKey;
    }
};

/**
 * fetchTranslations
 */
nb.fetchTranslations = function() {
    return $.ajax({
        type: 'get',
        dataType: 'json',
        url: nb.api.translations,
        success: function(response) {
            for (var key in response.captions) {
                nb.translations[key] = response.captions[key];
            }
        }
    });
};

/**
 * getForm
 */
nb.getForm = function(el) {
    if (!el) {
        return el;
    }

    if (typeof(el) === 'string' && (document[el] && document[el].nodeName === 'FORM')) {
        return document[el];
    } else {
        var $pf = $(el).parents('form')
        if ($pf.length) {
            el = $pf.get(0);
        }
    }

    return el.form || el;
};

/**
 * uiBlock
 */
nb.uiBlock = function() {
    var $el = $('#nb-block-ui');
    if ($el.length === 0) {
        $el = $('<div id="nb-block-ui" style="background:rgba(0,0,0,0.1);cursor:wait;position:fixed;top:0;left:0;bottom:0;right:0;z-index:999;"/>');
        $el.appendTo('body');
    }

    $el.css('display', 'block');
};

/**
 * uiUnblock
 */
nb.uiUnblock = function() {
    $('#nb-block-ui').css('display', 'none');
};

/**
 * isMobile
 */
nb.isMobile = function() {
    return /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent);
};

/**
 * template
 * @param templateId - hbs template
 * @param data - data for template
 */
nb.template = function(templateId, data) {
    return nb.templates[templateId].call(this, data);
};

/**
 * createElement
 */
nb.createElement = function(tagName, attributes, html) {
    var element = document.createElement(tagName);
    var el;
    for (el in attributes) {
        element.setAttribute(el, attributes[el]);
    }
    element.innerHTML = html;

    return element;
};

// Global ajax error handling
$(document).ajaxError(function(event, jqxhr, settings, thrownError) {
    if (jqxhr.responseJSON) {
        return;
    }

    var msg,
        bodyStIndex = jqxhr.responseText && jqxhr.responseText.indexOf('<body>');
    if (bodyStIndex > -1) {
        msg = jqxhr.responseText.substring(bodyStIndex, jqxhr.responseText.indexOf('</body>'))
    } else {
        msg = jqxhr.responseText;
    }

    if (msg) {
        nb.dialog.error({
            message: msg,
            height: 400,
            width: 600
        });
    } else {
        nb.notify({
            type: 'error',
            message: thrownError
        });
    }
});

// init
$(document).ready(function() {

    var oreo = localStorage.getItem(nb.options.sideTreeStorageName);
    var ary = [];
    if (oreo != null) {
        ary = oreo.split(',');
    } else {
        localStorage.setItem(nb.options.sideTreeStorageName, '');
    }
    $('[data-nav]', '.aside').each(function() {
        if (ary.indexOf($(this).data('nav')) != -1) {
            $(this).removeClass('side-tree-collapsed');
        }
    });

    $(':checkbox').bind('click', function() {
        var $checkbox = $(this);

        if (!$checkbox.data('toggle')) {
            return true;
        }

        var name = this.name || $checkbox.data('toggle');
        var $el = $('[name=' + name + ']:checkbox:visible:not([disabled])');

        if ($checkbox.is(':checked')) {
            $el.each(function() {
                this.checked = true;
            });
        } else {
            $el.each(function() {
                this.checked = false;
            });
        }
    });

    $('[data-role=side-tree-toggle]').click(function(e) {
        e.preventDefault();
        e.stopPropagation();
        var $parent = $(this).closest('.side-tree-collapsible');
        $parent.toggleClass('side-tree-collapsed');
        //
        var storageKey = nb.options.sideTreeStorageName;
        var navId = $parent.data('nav');
        var oreo = localStorage.getItem(storageKey);
        var ary = oreo.split(',');

        if ($parent.hasClass('side-tree-collapsed')) {
            var index = ary.indexOf(navId);
            if (index > -1) {
                ary.splice(index, 1);
            }
            localStorage.setItem(storageKey, ary.join(','));
        } else {
            ary.push(navId);
            localStorage.setItem(storageKey, ary.join(','));
        }
    });

    // toggle=panel
    $(document).on('click', '[data-toggle=panel]', function() {
        var targetSelector = $(this).data('target');
        var $panel;
        if (targetSelector) {
            $panel = $(targetSelector);
        } else {
            $panel = $(this).parents('.panel');
        }

        $panel.toggleClass('open');
    });

    // toggle=side-nav
    $(document).on('click', '[data-toggle=side-nav]', function(event) {
        event.preventDefault();
        $('body').toggleClass('side-nav-toggle');
    });

    if ($('#content-overlay').length) {
        $('#content-overlay').mousedown(function(event) {
            event.preventDefault();
            $('body').removeClass('side-nav-toggle search-open');
            if ($('.navbar-search input.q').length) {
                $('.navbar-search input.q')[0].blur();
            }
        });

        $('#content-overlay')[0].addEventListener('touchstart', function(event) {
            event.preventDefault();
            $('body').removeClass('side-nav-toggle search-open');
            if ($('.navbar-search input.q').length) {
                $('.navbar-search input.q')[0].blur();
            }
        }, false);
    }

    //
    $('.navbar-search input.q').on('focus', function() {
        $('body').addClass('search-open');
    });
    $('.navbar-search input.q').on('blur', function() {
        $('body').removeClass('search-open');
    });

    // data-role="dialog-list-item"
    $(document).on('dblclick', '[data-role=dialog-list-item]', function() {
        return nb.dialog.execute(this);
    });

    //
    $(window).resize(function() {
        if (window.innerWidth <= 1024 || nb.isMobile()) {
            $('body').addClass('phone');
        } else {
            $('body').removeClass('phone');
        }
    });

    if (nb.isMobile() || window.innerWidth <= 1024) {
        $('body').addClass('phone');
    }

    setTimeout(function() {
        $('body').removeClass('no_transition');
    }, 250);
});
