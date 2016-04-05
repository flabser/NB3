/**
 * xhrDelete
 */
nb.xhrDelete = function(url) {
    return $.ajax({
        type: 'DELETE',
        dataType: 'json',
        url: url
    });
};

/**
 * xhrDownload
 */
nb.xhrDownload = function(params) {

    params.method = params.method || 'POST';
    params.url = params.url || location.href;
    params.data = params.data || '';
    params.blockUi = params.blockUi === true;

    var noty;

    if (params.notify) {
        noty = nb.notify({
            message: params.notify
        });
    }

    var xhr = new XMLHttpRequest();
    xhr.open(params.method, params.url + '&' + params.data, true);
    xhr.responseType = 'arraybuffer';

    xhr.onload = function(oEvent) {
        if (params.blockUi) {
            nb.uiUnblock();
        }
        if (noty) {
            noty.remove();
        }

        var arrayBuffer = xhr.response;
        // check for a filename
        var filename = '';
        var disposition = xhr.getResponseHeader('Content-Disposition');
        if (disposition && disposition.indexOf('attachment') !== -1) {
            var filenameRegex = /filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/;
            var matches = filenameRegex.exec(disposition);
            if (matches != null && matches[1]) {
                filename = matches[1].replace(/['"]/g, '').replace('utf-8', '');
            }
        }

        var type = xhr.getResponseHeader('Content-Type');
        var blob = new Blob([arrayBuffer], { type: type });

        if (typeof window.navigator.msSaveBlob !== 'undefined') {
            // IE workaround for "HTML7007: One or more blob URLs were revoked by closing the blob for which they were created.
            // These URLs will no longer resolve as the data backing the URL has been freed."
            window.navigator.msSaveBlob(blob, filename);
        } else {
            var URL = window.URL || window.webkitURL;
            var downloadUrl = URL.createObjectURL(blob);

            if (filename) {
                // use HTML5 a[download] attribute to specify filename
                var a = document.createElement("a");
                // safari doesn't support this yet
                if (typeof a.download === 'undefined') {
                    window.location = downloadUrl;
                } else {
                    a.href = downloadUrl;
                    a.download = filename;
                    document.body.appendChild(a);
                    a.click();
                }
            } else {
                window.location = downloadUrl;
            }

            setTimeout(function() {
                URL.revokeObjectURL(downloadUrl);
            }, 100); // cleanup
        }

        if (typeof(params.success) === 'function') {
            params.success();
        }
    };

    xhr.send(null);

    if (noty) {
        noty.show();
    }
    if (params.blockUi) {
        nb.uiBlock();
    }
};
