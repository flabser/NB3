nb.upload = function(fileInput) {
    var inputName = fileInput.name;
    var formData = new FormData(fileInput.form);

    // console.log(fileInput.files[0].mozFullPath);
    // console.log(URL.createObjectURL(fileInput.files[0]));

    var $attNode = $(nb.template('attachments', {
        files: [{ name: fileInput.files[0].name, path: '' }]
    }));
    var $progress = $attNode.find('.upload-progress');

    return $.ajax({
        url: nb.api.upload + '?time=' + new Date().getTime(),
        method: 'POST',
        cache: false,
        contentType: false,
        processData: false,
        data: formData,
        xhr: function() {
            var _xhr = $.ajaxSettings.xhr();
            if (_xhr.upload) {
                _xhr.upload.addEventListener('progress', function(e) { nb.uploadProgress(e, $progress); }, false);
            }
            return _xhr;
        },
        beforeSend: function() {
            $('[data-upload-files=' + inputName + ']').prepend($attNode);
        },
        success: function(result, xhr) {
            $attNode.removeClass('uploading');
            $attNode.find('.file-name').addClass('blink-anim');
            $attNode.find('[name=fileid]').removeAttr('disabled');
            // init
            $('.btn-remove-file', $attNode).click(function() {
                $attNode.remove();
            });

            return result;
        },
        error: function(err) {
            $attNode.addClass('error');
            return err;
        },
        complete: function() {
            fileInput.form.reset();
            $('#progress_' + inputName).val(0);
        }
    });
};

nb.uploadProgress = function(e, $progress) {
    if (e.lengthComputable) {
        $progress.css('width', ((e.loaded / e.total) * 100) + '%');
    }
};

$(document).ready(function() {
    var fsId = new Date().getTime();

    $('[data-upload]').each(function() {
        var uploadBtn = this;
        var uploadName = $(this).data('upload');
        if (this.form.fsid && this.form.fsid.value) {
            fsId = this.form.fsid.value;
        }

        if ($('[type=file][name=' + uploadName + ']').length === 0) {
            if (this.form.fsid) {
                this.form.fsid.value = fsId;
            } else {
                $('<input type=hidden name=fsid value="' + fsId + '"/>').appendTo(this.form);
            }

            var $fileForm = $('<form class=hidden><input type=file name="' + uploadName + '" /><input type=hidden name=fsid value="' + fsId + '"/></form>').appendTo('body');
            var $fileInput = $fileForm.find('input[type=file]');

            $fileInput.on('change', function() {
                $(uploadBtn).attr('disabled', true);
                nb.upload($fileInput[0]).always(function() {
                    $(uploadBtn).attr('disabled', false);
                });

                if (!location.search.match('&fsid=')) {
                    history.replaceState(null, null, location.href + '&fsid=' + fsId);
                }
            });

            $(this).click(function() {
                $fileInput.click();
            });
        }

        $('.attachments-file', '[data-upload-files=' + uploadName + ']').each(function() {
            var $self = this;
            var resourceUrl = $('a[data-file]', $self).attr('href');
            $('.btn-remove-file', $self).on('click', function() {
                nb.xhrDelete(resourceUrl).then(function() {
                    $self.remove();
                });
            });
        });
    });
});
