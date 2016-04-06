var knca = (function() {

    var wd = window.document,
        noty,
        initialized = false,
        isReady = false,
        t_o,
        LOCAL_STORAGE_PREFS_KEY = 'knca_storage_prefs';

    var storage = {
        alias: 'PKCS12',
        path: '',
        keyAlias: '',
        keyType: 'SIGN',
        pwd: '',
        keys: []
    };

    function init(options) {
        if (initialized) {
            log('already initialized');
            return;
        }

        if (!navigator.javaEnabled()) {
            nb.notify({
                type: 'error',
                message: nb.getText('java_unavailable', 'Поддержка Java в браузере не включена! Включите или <a href=\"http://java.com/ru/download/\" target=\"blank\">установите Java</a> и вновь обратитесь к этой странице.')
            }).show(5000);

            log('java disabled');
        } else {
            insertApplet();
            initialized = true;
        }
    }

    function ready() {
        if (!initialized) {
            log('uninitialized; call knca.init()');
            return false;
        }

        if (isReady) {
            return true;
        }

        clearTimeout(t_o);

        isReady = true;
        nb.uiUnblock();
        noty && noty.remove();
        noty = null;

        log('is ready');

        // add applet api
        window.knca.chooseStoragePath = chooseStoragePath;

        restorePrefsFromLocalStorage();

        log(storage);

        return true;
    }

    function log(msg) {
        console.log('knca > ', msg);

        nb.notify({
            type: 'info',
            message: 'knca > ' + msg
        }).show(800);
    }

    function insertApplet() {
        if (wd.getElementById('KncaApplet')) {
            return;
        }

        var htm = [];
        htm.push('<applet width="1" height="1"');
        htm.push(' codebase="."');
        htm.push(' code="kz.gov.pki.knca.applet.MainApplet"');
        htm.push(' archive="/SharedResources/knca/knca_applet.jar"');
        htm.push(' type="application/x-java-applet"');
        htm.push(' mayscript="true"');
        htm.push(' id="KncaApplet" name="KncaApplet">');
        htm.push('<param name="code" value="kz.gov.pki.knca.applet.MainApplet">');
        htm.push('<param name="archive" value="/SharedResources/knca/knca_applet.jar">');
        htm.push('<param name="mayscript" value="true">');
        htm.push('<param name="scriptable" value="true">');
        htm.push('<param name="language" value="ru">');
        htm.push('<param name="separate_jvm" value="true">');
        htm.push('</applet>');

        var d = wd.createElement('div');
        d.style.height = 0;
        d.style.visibility = 'hidden';
        d.innerHTML = htm.join('');
        wd.getElementsByTagName('body')[0].appendChild(d);

        log('applet inserted');

        nb.uiBlock();

        noty = nb.notify({
            type: 'process',
            message: nb.getText('wait_applet_loading', 'Подождите, идет загрузка Java-апплета...')
        }).show();

        t_o = setTimeout(function() {
            nb.notify({
                type: 'info',
                message: nb.getText('applet_loading_too_long', 'Процесс загрузки затянулся... :(')
            }).show(3000);
        }, 30 * 1000);
    }

    function savePrefsOnLocalStorage() {
        localStorage.setItem(LOCAL_STORAGE_PREFS_KEY, JSON.stringify({
            alias: storage.alias,
            keyType: storage.keyType,
            keyAlias: storage.keyAlias,
            path: storage.path
        }));

        log('save prefs to LocalStorage');
    }

    function restorePrefsFromLocalStorage() {
        var ls = localStorage.getItem(LOCAL_STORAGE_PREFS_KEY);
        if (ls) {
            ls = JSON.parse(ls);
            storage = {
                alias: ls.alias,
                keyType: ls.keyType,
                keyAlias: ls.keyAlias,
                path: ls.path
            };

            log('restored prefs from LocalStorage');
        }
    }

    function render() {

    }

    function invalidateStorage() {
        // storage.alias = 'NONE';
        // storage.path = '';
        // localStorage.removeItem(LOCAL_STORAGE_PREFS_KEY);
    }

    // applet api
    function chooseStoragePath() {
        if (storage.alias !== 'NONE') {
            var rw = wd.getElementById('KncaApplet').browseKeyStore(storage.alias, 'P12', storage.path);
            if (rw.getErrorCode() === 'NONE') {
                storage.path = rw.getResult();
                if (!storage.path) {
                    invalidateStorage();
                } else {
                    savePrefsOnLocalStorage();
                }
            } else {
                invalidateStorage();
                nb.notify({
                    type: 'error',
                    message: rw.getErrorCode()
                }).show('click');
            }
        } else {
            invalidateStorage();
        }
    }

    function _fillKeys() {
        var storageAlias = $("#storageAlias").val();
        var storagePath = $("#storagePath").val();
        var password = $("#password").val();
        var keyType = "";
        var selected = $("input[type='radio'][name='keyType']:checked");
        if (selected.length > 0) {
            keyType = selected.val();
        }

        if (storage.path && storage.alias) {
            if (storage.pwd) {
                var rw = wd.getElementById('KncaApplet').getKeys(storage.alias, storage.path, storage.pwd, storage.keyType);
                if (rw.getErrorCode() === 'NONE') {
                    var slots = rw.getResult().split('\n');
                    for (var i = 0; i < slots.length; i++) {
                        if (!slots[i]) {
                            continue;
                        }
                        keys.push(new Option(slots[i], i));
                    }

                    _keysOptionChanged();
                    render();
                } else {
                    if (rw.getErrorCode() === 'WRONG_PASSWORD' && rw.getResult() > -1) {
                        nb.notify({
                            type: 'error',
                            message: 'Неправильный пароль! Количество оставшихся попыток: ' + rw.getResult()
                        }).show(3000);
                    } else if (rw.getErrorCode() === 'WRONG_PASSWORD') {
                        nb.notify({
                            type: 'error',
                            message: 'Неправильный пароль!'
                        }).show(2000);
                    } else {
                        nb.notify({
                            type: 'error',
                            message: rw.getErrorCode()
                        }).show(2000);
                    }

                    storage.keys = [];
                    render();
                }
            } else {
                alert("Введите пароль к хранилищу");
            }
        } else {
            // TODO show select storage
            alert("Не выбран хранилище!");
        }
    }

    function _keysOptionChanged() {
        var str = $("#keys :selected").text();
        var alias = str.split("|")[3];
        $("#keyAlias").val(alias);
    }

    // plain data
    function signPlainData(data) {
        if (!data) {
            throw new Error('invalid_data_for_sign');
        }

        var rw = wd.getElementById('KncaApplet').signPlainData(storage.alias, storage.path, storage.keyAlias, storage.pwd, data);
        if (rw.getErrorCode() === 'NONE') {
            return rw.getResult();
        } else {
            throw new Error(rw.getErrorCode());
        }
    }

    function verifyPlainData(data, signature) {
        if (!signature) {
            throw new Error('invalid_signature_for_verify');
        }

        var rw = wd.getElementById('KncaApplet').verifyPlainData(storage.alias, storage.path, storage.keyAlias, storage.pwd, data, signature);
        if (rw.getErrorCode() === 'NONE') {
            if (rw.getResult()) {
                return true;
            } else {
                return false;
            }
        } else {
            throw new Error(rw.getErrorCode());
        }
    }

    // xml data
    function signXml(xmlData) {
        if (!xmlData) {
            throw new Error('invalid_data_for_verify');
        }

        var rw = wd.getElementById('KncaApplet').signXml(storage.alias, storage.path, storage.keyAlias, storage.pwd, data);
        if (rw.getErrorCode() === 'NONE') {
            if (rw.getResult()) {
                return true;
            } else {
                return false;
            }
        } else {
            throw new Error(rw.getErrorCode());
        }
    }

    function verifyXml(xmlSignature) {
        if (!xmlSignature) {
            throw new Error('invalid_xml_signature_for_verify');
        }

        var rw = wd.getElementById('KncaApplet').verifyXml(xmlSignature);
        if (rw.getErrorCode() === 'NONE') {
            if (rw.getResult()) {
                return true;
            } else {
                return false;
            }
        } else {
            throw new Error(rw.getErrorCode());
        }
    }

    // file
    function createCMSSignatureFromFile(fileInput) {
        if (!fileInput || !fileInput.files) {
            throw new Error('invalid_file_input_for_sign');
        }

        var filePath = fileInput.files[0].path;
        var attached = false;
        var rw = wd.getElementById('KncaApplet').createCMSSignatureFromFile(storage.alias, storage.path, storage.keyAlias, storage.pwd, filePath, attached);
        if (rw.getErrorCode() === 'NONE') {
            return rw.getResult();
        } else {
            throw new Error(rw.getErrorCode());
        }
    }

    function verifyCMSSignatureFromFile(signatureCMSFile, filePath) {
        if (!signatureCMSFile) {
            throw new Error('invalid_signature_for_verify');
        }

        var rw = wd.getElementById('KncaApplet').verifyCMSSignatureFromFile(signatureCMSFile, filePath);
        if (rw.getErrorCode() === 'NONE') {
            if (rw.getResult()) {
                return true;
            } else {
                return false;
            }
        } else {
            throw new Error(rw.getErrorCode());
        }
    }

    // CMSSignature
    function createCMSSignature(data) {
        if (!data) {
            throw new Error('invalid_data_for_sign');
        }

        var attached = false;
        var rw = wd.getElementById('KncaApplet').createCMSSignature(storage.alias, storage.path, storage.keyAlias, storage.pwd, data, attached);
        if (rw.getErrorCode() === 'NONE') {
            return rw.getResult();
        } else {
            throw new Error(rw.getErrorCode());
        }
    }

    function verifyCMSSignature(signatureCMS, data) {
        if (!signatureCMS) {
            throw new Error('invalid_signature_for_verify');
        }

        var rw = wd.getElementById('KncaApplet').verifyCMSSignature(signatureCMS, data);
        if (rw.getErrorCode() === 'NONE') {
            if (rw.getResult()) {
                return true;
            } else {
                return false;
            }
        } else {
            throw new Error(rw.getErrorCode());
        }
    }

    // storage data
    function getNotBefore() {
        var rw = wd.getElementById('KncaApplet').getNotBefore(storage.alias, storage.path, storage.keyAlias, storage.pwd);
        if (rw.getErrorCode() === 'NONE') {
            return rw.getResult();
        } else {
            throw new Error(rw.getErrorCode());
        }
    }

    function getNotAfter() {
        var rw = wd.getElementById('KncaApplet').getNotAfter(storage.alias, storage.path, storage.keyAlias, storage.pwd);
        if (rw.getErrorCode() === 'NONE') {
            return rw.getResult();
        } else {
            throw new Error(rw.getErrorCode());
        }
    }

    function getSubjectDN() {
        var rw = wd.getElementById('KncaApplet').getSubjectDN(storage.alias, storage.path, storage.keyAlias, storage.pwd);
        if (rw.getErrorCode() === 'NONE') {
            return rw.getResult();
        } else {
            throw new Error(rw.getErrorCode());
        }
    }

    function getIssuerDN() {
        var rw = wd.getElementById('KncaApplet').getIssuerDN(storage.alias, storage.path, storage.keyAlias, storage.pwd);
        if (rw.getErrorCode() === 'NONE') {
            return rw.getResult();
        } else {
            throw new Error(rw.getErrorCode());
        }
    }

    function getRdnByOid(oid) {
        var rw = wd.getElementById('KncaApplet').getRdnByOid(storage.alias, storage.path, storage.keyAlias, storage.pwd, oid, 0);
        if (rw.getErrorCode() === 'NONE') {
            return rw.getResult();
        } else {
            throw new Error(rw.getErrorCode());
        }
    }

    // public api
    return {
        init: init,
        ready: ready,
        chooseStoragePath: function() {
            log('uninitialized; click knca init');
        },
        signPlainData: signPlainData,
        verifyPlainData: verifyPlainData
    }
})();

// called from knca applet
function AppletIsReady() {
    knca.ready();
}
