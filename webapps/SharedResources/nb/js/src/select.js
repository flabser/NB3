nb.getSelectOptions = function(selectOptions) {
    var options = selectOptions;
    var cacheDataSource = [];
    var allDataLoaded = false;
    var meta = {};

    var dataAdapter = function(data) {
        var items = [],
            meta = {},
            list = {},
            buff = {};

        if (data.objects.length) {
            meta = data.objects[0].meta;
            list = data.objects[0].list;

            for (var k in list) {
                buff = {
                    id: list[k].id,
                    text: list[k].name
                };
                if (options.fields) {
                    for (var fi in options.fields) {
                        buff[options.fields[fi]] = list[k][options.fields[fi]];
                    }
                }
                items.push(buff);
            }
        }

        return {
            meta: meta,
            items: items
        }
    };

    var filterItems = function(data, term) {
        if (!term || !term.trim().length) {
            return data;
        }

        var _term = term.trim().toLowerCase();
        var items = [];
        for (var i in data.items) {
            if (data.items[i].text.toLowerCase().indexOf(_term) > -1) {
                items.push(data.items[i]);
            }
        }

        return {
            meta: data.meta,
            items: items
        }
    };

    return {
        allowClear: true,
        minimumInputLength: 0,
        minimumResultsForSearch: options.minimumResultsForSearch || 20,
        placeholder: options.placeholder || '',
        templateResult: options.templateResult,
        // templateSelection: options.templateResult,
        language: nb.LANG,
        ajax: {
            url: options.url,
            dataType: 'json',
            delay: 0,
            data: function(params) {
                var _data = {
                    page: params.page,
                    keyword: params.term
                };

                for (var k in options.data) {
                    _data[options.data[k]] = this[0].form[options.data[k]].value;
                }

                return _data;
            },
            processResults: function(data, params) {
                var _data = data;
                params.page = params.page || 1;
                meta = data.meta;
                allDataLoaded = 1 == meta.totalPages;

                return {
                    results: _data.items,
                    pagination: {
                        more: params.page < meta.totalPages
                    }
                };
            },
            transport: function(params, success, failure) {
                var cachedData,
                    key = params.url,
                    checkCache = options.cache && meta.totalPages == 1;

                if (checkCache) {
                    cachedData = cacheDataSource[key];
                }

                if (cachedData && cachedData.items.length) {
                    var result = filterItems(cachedData, params.data.keyword);
                    // console.log('cachedData', result, params);
                    return success(result);
                } else {
                    var $request = $.ajax(params);
                    $request.then(function(data) {
                        var _data = dataAdapter(data);
                        cacheDataSource[key] = _data;
                        // console.log('ajax load', params.data, _data);
                        return success(_data);
                    });
                    $request.fail(failure);
                    return $request;
                }
            },
            cache: true
        }
    };
};

$(document).ready(function() {
    $('select[name]:not(.native)').each(function() {
        var appSelectOptions = nbApp.selectOptions && nbApp.selectOptions[this.name];
        if (appSelectOptions) {
            var $select2El = $(this).select2(nb.getSelectOptions(appSelectOptions));

            $select2El.on('select2:unselecting', function(e) {
                $(this).data('unselecting', true);

                if (typeof(appSelectOptions.onSelect) === 'function') {
                    appSelectOptions.onSelect(e);
                }
            }).on('select2:opening', function(e) {
                if ($(this).data('unselecting')) {
                    $(this).removeData('unselecting');
                    e.preventDefault();
                }
            });

            if (typeof(appSelectOptions.onSelect) === 'function') {
                $select2El.on('select2:select', function(e) {
                    if (appSelectOptions.onSelect) {
                        appSelectOptions.onSelect(e);
                    }
                });
            }
        } else {
            if (nb.isMobile()) {
                if (this.multiple) {
                    $(this).select2({
                        minimumResultsForSearch: 20
                    }).on('select2:unselecting', function() {
                        $(this).data('unselecting', true);
                    }).on('select2:opening', function(e) {
                        if ($(this).data('unselecting')) {
                            $(this).removeData('unselecting');
                            e.preventDefault();
                        }
                    });
                }
            } else {
                $(this).select2({
                    minimumResultsForSearch: 20
                }).on('select2:unselecting', function() {
                    $(this).data('unselecting', true);
                }).on('select2:opening', function(e) {
                    if ($(this).data('unselecting')) {
                        $(this).removeData('unselecting');
                        e.preventDefault();
                    }
                });
            }
        }
    });

    // need dummy input if no select value
    $('select[name]').on('change', function() {
        if ($(this).val()) {
            $('[data-role=dummy-select][name=' + this.name + ']', $(this).parent()).remove();
        } else {
            $('<input type=hidden data-role=dummy-select name=' + this.name + ' value="">').appendTo($(this).parent());
        }
    });
});
