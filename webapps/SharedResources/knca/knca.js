var knca = (function() {

    'use strict';

    var wd = window.document,
        noty,
        initialized = false,
        isReady = false,
        t_o,
        appletPath = '/SharedResources/knca/',
        LOCAL_STORAGE_PREFS_KEY = 'knca_storage_prefs';
    var currentPromise;

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
        if (applet()) {
            log('applet already inserted');
            return;
        }

        var htm = [];
        htm.push('<applet width="1" height="1"');
        htm.push(' code="kz.gov.pki.knca.applet.MainApplet"');
        htm.push(' codebase="' + appletPath + '"');
        htm.push(' archive="knca_applet.jar"');
        htm.push(' type="application/x-java-applet"');
        htm.push(' mayscript="true"');
        htm.push(' id="KncaApplet" name="KncaApplet">');
        htm.push('<param name="code" value="kz.gov.pki.knca.applet.MainApplet">');
        htm.push('<param name="codebase" value="' + appletPath + '">');
        htm.push('<param name="archive" value="knca_applet.jar">');
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
        var rw = applet().getKeys(storage.alias, storage.path, storage.pwd, storage.keyType);
        if (rw.getErrorCode() === 'NONE') {
            var slots = rw.getResult().split('\n');
            for (var i = 0; i < slots.length; i++) {
                if (slots[i]) {
                    keys.push(slots[i]);
                }
            }

            render();
        } else {
            storage.keys = [];
            render();

            throw new Error(rw.getErrorCode());
        }
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

    function applet() {
        return wd.KncaApplet;
    }

    /**
     * applet api 
     */

    // choose storage
    function chooseStorageP12() {
        var rw = applet().browseKeyStore(storage.alias, 'P12', storage.path);
        if (rw.getErrorCode() === 'NONE') {
            if (rw.getResult()) {
                storage.path = rw.getResult();
                fillKeys();
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

        return signResult(applet().signPlainData(storage.alias, storage.path, storage.keyAlias, storage.pwd, data));
    }

    function verifyPlainData(data, signature) {
        if (!signature) {
            throw new Error('invalid_signature');
        }

        return verifyResult(applet().verifyPlainData(storage.alias, storage.path, storage.keyAlias, storage.pwd, data, signature));
    }

    // xml data
    function signXml(xmlData) {
        if (!xmlData) {
            throw new Error('invalid_data');
        }

        return signResult(applet().signXml(storage.alias, storage.path, storage.keyAlias, storage.pwd, data));
    }

    function verifyXml(xmlSignature) {
        if (!xmlSignature) {
            throw new Error('invalid_signature');
        }

        return verifyResult(applet().verifyXml(xmlSignature));
    }

    // file
    function createCMSSignatureFromFile(filePath, attached) {
        if (!fileInput) {
            throw new Error('invalid_data');
        }

        return signResult(applet().createCMSSignatureFromFile(storage.alias, storage.path, storage.keyAlias, storage.pwd, filePath, !!attached));
    }

    function verifyCMSSignatureFromFile(signatureCMSFile, filePath) {
        if (!signatureCMSFile) {
            throw new Error('invalid_signature');
        }

        return verifyResult(applet().verifyCMSSignatureFromFile(signatureCMSFile, filePath));
    }

    // CMSSignature
    function createCMSSignature(data, attached) {
        if (!data) {
            throw new Error('invalid_data');
        }

        return signResult(applet().createCMSSignature(storage.alias, storage.path, storage.keyAlias, storage.pwd, data, !!attached));
    }

    function verifyCMSSignature(signatureCMS, data) {
        if (!signatureCMS) {
            throw new Error('invalid_signature');
        }

        return verifyResult(applet().verifyCMSSignature(signatureCMS, data));
    }

    // storage data
    function getNotBefore() {
        return appletResult(applet().getNotBefore(storage.alias, storage.path, storage.keyAlias, storage.pwd));
    }

    function getNotAfter() {
        return appletResult(applet().getNotAfter(storage.alias, storage.path, storage.keyAlias, storage.pwd));
    }

    function getSubjectDN() {
        return appletResult(applet().getSubjectDN(storage.alias, storage.path, storage.keyAlias, storage.pwd));
    }

    function getIssuerDN() {
        return appletResult(applet().getIssuerDN(storage.alias, storage.path, storage.keyAlias, storage.pwd));
    }

    function getRdnByOid(oid) {
        return appletResult(applet().getRdnByOid(storage.alias, storage.path, storage.keyAlias, storage.pwd, oid, 0));
    }

    //
    function render() {
        if (!wd.getElementById('eds-property')) {
            // html
            var htm = [];
            htm.push('<header>Sign property</header>');
            htm.push('<section>');
            htm.push('  <select name="storageAlias" class="native" style="display:none">');
            htm.push('    <option value="PKCS12" selected="selected">Ваш Компьютер</option>');
            htm.push('    <option value="AKKaztokenStore">Казтокен</option>');
            htm.push('    <option value="AKKZIDCardStore">Личное Удостоверение</option>');
            htm.push('    <option value="AKEToken72KStore">EToken Java 72k</option>');
            htm.push('    <option value="AKJaCartaStore">AK JaCarta</option>');
            htm.push('  </select>');
            htm.push('  <input type="radio" value="SIGN" name="keyType" checked="checked"/>');
            htm.push('  <button class="btn" name="chooseStorage" type="button">Выбрать ЭЦП</button>');
            // htm.push('  <label class="btn" for="edsFile" type="button">Выбрать ЭЦП</label>');
            // htm.push('  <input type="file" id="edsFile" style="display:none"/>');
            htm.push('  <input type="password" name="pwd" placeholder="Password" style="display:none"/>');
            htm.push('  <select name="keys" class="native" style="display:none"></select>');
            htm.push('</section>');
            htm.push('<footer>');
            htm.push('  <button class="btn" type="button" name="cancel">Cancel</button>');
            htm.push('  <button class="btn btn-primary" type="button" name="ok" disabled>Ok</button>');
            htm.push('</footer>');

            var el = wd.createElement('div');
            el.id = 'eds-property';
            el.className = 'eds';
            el.innerHTML = htm.join('');
            wd.getElementsByTagName('body')[0].appendChild(el);
            //
            var overlay = wd.createElement('div');
            overlay.className = 'eds-overlay';
            el.parentNode.insertBefore(overlay, el.nextSibling);
            //
            $(el).find('[name=chooseStorage]').on('click', function() {
                chooseStorageP12();
            });
            $(el).find('[name=cancel]').on('click', function() {
                hidePropertyModal();
                currentPromise.reject('cancel');
            });
            $(el).find('[name=ok]').on('click', function() {
                currentPromise.resolve();
            });
        }

        // show/hide mode
        console.log('render');
    }

    function showPropertyModal() {
        wd.getElementById('eds-property').classList.add('open');
    }

    function hidePropertyModal() {
        wd.getElementById('eds-property').classList.remove('open');
    }

    function resolveStorage() {
        var promise = $.Deferred();
        if (isReady) {
            if (storage.alias && storage.path && storage.keyAlias && storage.keyType && storage.pwd) {
                return promise.resolve();
            } else {
                // show select property dialog
                log('show select property dialog');
                currentPromise = promise;
                showPropertyModal();
            }
        } else {
            log('not_ready');
            return promise.reject('knca_not_ready');
        }

        return promise;
    }

    // public api
    return {
        init: init,
        ready: ready,
        signPlainData: function(data) {
            return resolveStorage().then(function() {
                return signPlainData(data);
            });
        },
        verifyPlainData: function(data, signature) {
            return resolveStorage().then(function() {
                return verifyPlainData(data, signature);
            });
        },
        createCMSSignatureFromFile: function(filePath, attached) {
            return resolveStorage().then(function() {
                return createCMSSignatureFromFile(filePath, attached);
            });
        },
        verifyCMSSignatureFromFile: function(signatureCMSFile, filePath) {
            return resolveStorage().then(function() {
                return verifyCMSSignatureFromFile(signatureCMSFile, filePath);
            });
        }
    };
})();

// called from knca applet
function AppletIsReady() {
    knca.ready();
}

$(document).ready(knca.init);
