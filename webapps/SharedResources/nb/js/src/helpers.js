Handlebars.registerHelper('mapValue', function(context, options) {
    return context[options];
});

Handlebars.registerHelper('unescapeXml', function(data) {
    return data.replace('&amp;', '&');
});

Handlebars.registerPartial('pagination', function(data) {
    return nb.html.pagination(data);
});

nb.html = {};

nb.html.pagination = function(data) {

    var result = '',
        maxPageControl = 7,
        maxPage = data.meta.totalPages,
        curPage = data.meta.page,
        url = data.url;

    if (maxPage > 1) {
        var perPage = parseInt(maxPageControl / 2, 10);
        var startPage = (curPage - perPage);
        var stopPage = (curPage + perPage);

        if (startPage <= perPage) {
            startPage = 1;
        } else if (curPage == maxPage) {
            startPage = maxPage - maxPageControl;
        }

        if (stopPage > (maxPage - perPage)) {
            stopPage = maxPage;
        } else if (curPage == 1) {
            stopPage = maxPageControl + 1;
        }

        if ((maxPageControl + perPage) >= maxPage) {
            startPage = 1;
            stopPage = maxPage;
        }

        try {
            result = '<div class=pagination>';
            result += '<span class=text-muted>' + data.models.length + '/' + data.meta.count + ' </span>';

            if (startPage > 1) {
                result += '<a href="' + url + '&page=1">1</a>';
                result += '<span>...</span>';
            }

            for (var p = startPage; p <= stopPage; p++) {
                if (p == curPage) {
                    result += '<a class="page-active" href="' + url + '&page=' + p + '">' + p + '</a>';
                } else {
                    result += '<a href="' + url + '&page=' + p + '">' + p + '</a>';
                }
            }

            if (stopPage < maxPage) {
                result += '<span>...</span>';
                result += '<a href="' + url + '&page=' + maxPage + '">' + maxPage + '</a>';
            }

            /*if ((startPage > 1) || (stopPage < maxPage)) {
                result += '<li class="page"><div class="gotopage">';
                result += 'Перейти на страницу <input type="number" min="1" max="' + maxPage + '" class="goToPage" name="goToPage" value="" />';
                result += '</div></li>';
            }*/

            result += '</div>';

            /*$("[name='goToPage']").click(function() {
                $(".gotopage").css("display", "block");
            }).blur(function() {
                $(".gotopage").css("display", "");
            }).keydown(function(e) {
                if (e.keyCode == 13) {
                    var goToPage = parseInt($(this).val(), 10);
                    if ((goToPage >= 0) && (goToPage <= maxPage)) {
                        getPage(goToPage);
                    } else {
                        window.status = "error page number";
                    }
                }
            });*/
        } catch (e) {
            alert(e);
        }
    }

    return result;
};

jQuery.fn.numericField = function() {
    var triadnum_p = /(\d)(?=(\d\d\d)+([^\d]|$))/g;

    return this.each(function() {
        var fv;
        var $field = jQuery(this);

        $field.each(function() {
            fv = $(this).val();
            $(this).val($(this).val().replace(triadnum_p, '$1 '));

            if (!($(this).attr('readonly'))) {
                // hidden field
                /*var $di = $('<input type=hidden name="' + this.name + '" value="' + this.value + '" />');
                $(this).removeAttr('name');
                $di.appendTo(this.form);*/

                $(this).keyup(function(e) {
                    if ([37, 38, 39, 40].indexOf(e.keyCode) == -1) {
                        var $val = $(this).val().replace(/[^0-9,\.]/g, '').replace(triadnum_p, '$1 ');
                        $(this).val($val);
                    } else {
                        return true;
                    }
                }).keydown(function(e) {
                    /*if ((e.keyCode >= 48 && e.keyCode <= 57) || (e.keyCode >= 96 && e.keyCode <= 105)) {
                        return true;
                    }
                    if ([8, 9, 46, 37, 38, 39, 40, 116].indexOf(e.keyCode) !== -1) {
                        return true;
                    }*/
                    return true;
                });
                /*.blur(function() {
                    $di.val($(this).val().replace(/[^0-9,\.]/g, ''));
                }).change(function() {
                    $di.val($(this).val().replace(/[^0-9,\.]/g, ''));
                });*/
            }
        });
    });
};
