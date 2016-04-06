var knca = (function() {

    'use strict';

    var wd = window.document,
        noty,
        initialized = false,
        isReady = false,
        t_o,
        LOCAL_STORAGE_PREFS_KEY = 'knca_storage_prefs';

    var storage = {
        alias: 'NONE',
        path: '',
        keyAlias: '',
        keyType: 'ALL',
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

        restorePrefsFromLocalStorage();
        isReady = true;
        nb.uiUnblock();
        noty && noty.remove();
        noty = null;

        log('is ready');

        render();

        return true;
    }

    function log(msg) {
        console.log('knca > ', msg);
        console.log(storage);

        nb.notify({
            type: 'info',
            message: 'knca > ' + msg
        }).show(1000);
    }

    function insertApplet() {
        if (wd.getElementById('KncaApplet')) {
            log('applet already inserted');
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

    function savePrefsToLocalStorage() {
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

    function invalidateStorage() {
        // storage.alias = 'NONE';
        // storage.path = '';
        // localStorage.removeItem(LOCAL_STORAGE_PREFS_KEY);
        render();
    }

    function fillKeys() {
        var rw = wd.getElementById('KncaApplet').getKeys(storage.alias, storage.path, storage.pwd, storage.keyType);
        if (rw.getErrorCode() === 'NONE') {
            var slots = rw.getResult().split('\n');
            for (var i = 0; i < slots.length; i++) {
                if (slots[i]) {
                    keys.push(new Option(slots[i], i));
                }
            }

            render();
        } else {
            storage.keys = [];
            render();

            throw new Error(rw.getErrorCode());
        }
    }

    function render() {
        log(storage);
    }

    // process rw
    function appletResult(rw) {
        if (rw.getErrorCode() === 'NONE') {
            log(rw.getResult());
            return rw.getResult();
        } else {
            throw new Error(rw.getErrorCode());
        }
    }

    function signResult(rw) {
        if (rw.getErrorCode() === 'NONE') {
            log(rw.getResult());
            return rw.getResult();
        } else {
            throw new Error(rw.getErrorCode());
        }
    }

    function verifyResult(rw) {
        if (rw.getErrorCode() === 'NONE') {
            log(rw.getResult());
            if (rw.getResult()) {
                return true;
            } else {
                return false;
            }
        } else {
            throw new Error(rw.getErrorCode());
        }
    }

    /**
     * applet api 
     */

    // choose storage
    function chooseStoragePath() {
        var rw = wd.getElementById('KncaApplet').browseKeyStore(storage.alias, 'P12', storage.path);
        if (rw.getErrorCode() === 'NONE') {
            if (rw.getResult()) {
                storage.path = rw.getResult();
                savePrefsToLocalStorage();
            }
        } else {
            invalidateStorage();

            log(rw.getErrorCode());
            nb.notify({
                type: 'error',
                message: rw.getErrorCode()
            }).show('click');
        }
    }

    // plain data
    function signPlainData(data) {
        if (!data) {
            throw new Error('invalid_data');
        }

        return signResult(wd.getElementById('KncaApplet').signPlainData(storage.alias, storage.path, storage.keyAlias, storage.pwd, data));
    }

    function verifyPlainData(data, signature) {
        if (!signature) {
            throw new Error('invalid_signature');
        }

        return verifyResult(wd.getElementById('KncaApplet').verifyPlainData(storage.alias, storage.path, storage.keyAlias, storage.pwd, data, signature));
    }

    // xml data
    function signXml(xmlData) {
        if (!xmlData) {
            throw new Error('invalid_data');
        }

        return signResult(wd.getElementById('KncaApplet').signXml(storage.alias, storage.path, storage.keyAlias, storage.pwd, data));
    }

    function verifyXml(xmlSignature) {
        if (!xmlSignature) {
            throw new Error('invalid_signature');
        }

        return verifyResult(wd.getElementById('KncaApplet').verifyXml(xmlSignature));
    }

    // file
    function createCMSSignatureFromFile(fileInput, attached) {
        if (!fileInput || !fileInput.files) {
            throw new Error('invalid_data');
        }

        var filePath = fileInput.files[0].path;
        return signResult(wd.getElementById('KncaApplet').createCMSSignatureFromFile(storage.alias, storage.path, storage.keyAlias, storage.pwd, filePath, !!attached));
    }

    function verifyCMSSignatureFromFile(signatureCMSFile, filePath) {
        if (!signatureCMSFile) {
            throw new Error('invalid_signature');
        }

        return verifyResult(wd.getElementById('KncaApplet').verifyCMSSignatureFromFile(signatureCMSFile, filePath));
    }

    // CMSSignature
    function createCMSSignature(data, attached) {
        if (!data) {
            throw new Error('invalid_data');
        }

        return signResult(wd.getElementById('KncaApplet').createCMSSignature(storage.alias, storage.path, storage.keyAlias, storage.pwd, data, !!attached));
    }

    function verifyCMSSignature(signatureCMS, data) {
        if (!signatureCMS) {
            throw new Error('invalid_signature');
        }

        return verifyResult(wd.getElementById('KncaApplet').verifyCMSSignature(signatureCMS, data));
    }

    // storage data
    function getNotBefore() {
        return appletResult(wd.getElementById('KncaApplet').getNotBefore(storage.alias, storage.path, storage.keyAlias, storage.pwd));
    }

    function getNotAfter() {
        return appletResult(wd.getElementById('KncaApplet').getNotAfter(storage.alias, storage.path, storage.keyAlias, storage.pwd));
    }

    function getSubjectDN() {
        return appletResult(wd.getElementById('KncaApplet').getSubjectDN(storage.alias, storage.path, storage.keyAlias, storage.pwd));
    }

    function getIssuerDN() {
        return appletResult(wd.getElementById('KncaApplet').getIssuerDN(storage.alias, storage.path, storage.keyAlias, storage.pwd));
    }

    function getRdnByOid(oid) {
        return appletResult(wd.getElementById('KncaApplet').getRdnByOid(storage.alias, storage.path, storage.keyAlias, storage.pwd, oid, 0));
    }

    function resolveStorage() {
        var promise = $.Deferred();
        return promise.resolve(true);
    }

    // proxy
    function doAction(action, args) {
        if (!isReady) {
            log('not_ready');
            return false;
        }

        return resolveStorage().then(function(result) {
            log(result);

            switch (action) {
                case 'chooseStoragePath':
                    return chooseStoragePath();
                case 'signPlainData':
                    return signPlainData(args[0]);
                case 'verifyPlainData':
                    return verifyPlainData(args[0], args[1]);
                case 'createCMSSignatureFromFile':
                    return createCMSSignatureFromFile(args[0], args[1]);
                case 'verifyCMSSignatureFromFile':
                    return verifyCMSSignatureFromFile(args[0], args[1]);
            }
        });
    }

    // public api
    return {
        init: init,
        ready: ready,
        chooseStoragePath: function() {
            return doAction('chooseStoragePath', arguments);
        },
        signPlainData: function() {
            return doAction('signPlainData', arguments);
        },
        verifyPlainData: function() {
            return doAction('verifyPlainData', arguments);
        },
        createCMSSignatureFromFile: function() {
            return doAction('createCMSSignatureFromFile', arguments);
        },
        verifyCMSSignatureFromFile: function() {
            return doAction('verifyCMSSignatureFromFile', arguments);
        }
    };
})();

// called from knca applet
function AppletIsReady() {
    knca.ready();
}
