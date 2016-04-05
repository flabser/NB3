nb.getSelectedEntityIDs = function(checkboxName) {
    var $checked = $('input[name=' + (checkboxName || 'docid') + ']:checked');
    if ($checked.length === 0) {
        return [];
    }

    var result = [];
    $checked.each(function() {
        result.push(this.value);
    });

    return result;
};

nb.setSearchReferToSessionStorage = function() {
    if (location.href.indexOf('id=search') == -1) {
        sessionStorage.setItem(nb.options.searchReferStorageName, location.href);
    }
};

nb.resetSearchFromRefer = function() {
    var refer = sessionStorage.getItem(nb.options.searchReferStorageName);
    if (refer) {
        sessionStorage.removeItem(nb.options.searchReferStorageName)
        location.href = refer;
    } else {
        history.back();
    }
};

// init
$(document).ready(function() {
    $('form[name=ft-search]').on('submit', function() {
        nb.setSearchReferToSessionStorage();
        return true;
    });

    $('form[name=ft-search]').on('reset', function() {
        $('[type=search]', this).attr('value', '').focus();
    });

    $('[data-action=reset_search]').click(function(event) {
        nb.resetSearchFromRefer();
    });
});
