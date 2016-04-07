var knca = (function() {

    'use strict';

    var wd = window.document,
        noty,
        initialized = false,
        isReady = false,
        t_o,
        appletPath = '/SharedResources/knca/',
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

    function isValidStorage() {
        return storage.alias && storage.path && storage.keyAlias && storage.keyType && storage.pwd;
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

    // fillKeys
    function fillKeys() {
        storage.keys = [];
        var rw = applet().getKeys(storage.alias, storage.path, storage.pwd, storage.keyType);
        if (rw.getErrorCode() === 'NONE') {
            var slots = rw.getResult().split('\n');
            for (var i = 0; i < slots.length; i++) {
                if (slots[i]) {
                    storage.keys.push(slots[i]);
                }
            }
        } else {
            throw new Error(rw.getErrorCode());
        }
    }

    // choose storage
    function chooseStorageP12() {
        var rw = applet().browseKeyStore(storage.alias, 'P12', storage.path);
        if (rw.getErrorCode() === 'NONE') {
            if (rw.getResult()) {
                storage.path = rw.getResult();
                return true;
            }
        }

        return false;
    }

    // plain data
    function signPlainData(data) {
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
        if (!filePath) {
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
        var edsNode = wd.getElementById('eds-property');
        if (!edsNode) {
            // html
            var htm = [];
            htm.push('<header>' + nb.getText('eds_title', 'ЭЦП') + '</header>');
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
            htm.push('  <p class="eds-info"></p>');
            htm.push('  <input type="password" name="pwd" placeholder="Password" required style="display:none"/>');
            htm.push('  <select name="keys" class="native" style="display:none"></select>');
            htm.push('</section>');
            htm.push('<footer>');
            htm.push('  <button class="btn" type="button" name="cancel">' + nb.getText('cancel', 'Отмена') + '</button>');
            htm.push('  <button class="btn btn-primary" type="submit" name="ok" disabled></button>');
            htm.push('</footer>');

            edsNode = wd.createElement('form');
            edsNode.id = 'eds-property';
            edsNode.className = 'eds';
            edsNode.innerHTML = htm.join('');
            wd.getElementsByTagName('body')[0].appendChild(edsNode);
            //
            var overlay = wd.createElement('div');
            overlay.className = 'eds-overlay';
            edsNode.parentNode.insertBefore(overlay, edsNode.nextSibling);
            //
            $(edsNode).on('submit', function(event) {
                event.preventDefault();
            });
            $(edsNode).find('[name=chooseStorage]').on('click', function() {
                chooseStorageP12();
                render();
            });
            $(edsNode).find('[name=pwd]').on('change, blur', function() {
                storage.pwd = this.value;
                try {
                    fillKeys();
                    render();
                } catch (e) {
                    edsNode.classList.add('has-error');

                    nb.notify({
                        type: 'error',
                        message: e.message
                    }).show(3000);
                }
            });
            $(edsNode).find('[name=cancel]').on('click', function() {
                hidePropertyModal();
                currentPromise.reject('cancel');
            });
            $(edsNode).find('[name=ok]').on('click', function() {
                if (isValidStorage()) {
                    savePrefsToLocalStorage();
                    try {
                        currentPromise.resolve().then(function() {
                            hidePropertyModal();
                        });
                    } catch (e) {
                        log(e.message);
                    }
                } else {
                    log('invalid storage');
                    log(storage);
                }
            });
        }

        // ['RSA'|name|?|alias]
        if (storage.keys && storage.keys.length) {
            var key;
            var keysEl = $(edsNode).find('[name=keys]');
            keysEl.empty();
            for (var k in storage.keys) {
                key = storage.keys[k].split('|');
                $('<option value=' + key[3] + '>' + key[1] + '</option>').appendTo(keysEl);
                storage.keyAlias = key[3];
            }
            keysEl.show();
        } else {
            $(edsNode).find('[name=keys]').empty().hide();
        }

        // show/hide
        $(edsNode).find('[name=ok]').attr('disabled', !isValidStorage());
        if (storage.path && !storage.pwd) {
            $(edsNode).find('[name=pwd]').show();
        }
    }

    function showPropertyModal(okBtnText) {
        $(wd.getElementById('eds-property')).find('[name=ok]').text(okBtnText);
        wd.getElementById('eds-property').classList.add('open');
    }

    function hidePropertyModal() {
        wd.getElementById('eds-property').classList.remove('open');
    }

    //
    var currentPromise;

    function resolveStorage(okBtnText) {
        var promise = $.Deferred();
        if (isReady) {
            if (isValidStorage()) {
                return promise.resolve();
            } else {
                // show select property dialog
                log('show select property dialog');
                currentPromise = promise;
                showPropertyModal(okBtnText);
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
            return resolveStorage(nb.getText('sign', 'Подписать')).then(function() {
                return signPlainData(data);
            });
        },
        verifyPlainData: function(data, signature) {
            return resolveStorage(nb.getText('verify_sign', 'Проверить')).then(function() {
                return verifyPlainData(data, signature);
            });
        },
        signXml: function(xmlData) {
            return resolveStorage(nb.getText('sign', 'Подписать')).then(function() {
                return signXml(xmlData);
            });
        },
        verifyXml: function(xmlSignature) {
            return resolveStorage(nb.getText('verify_sign', 'Проверить')).then(function() {
                return verifyXml(xmlSignature);
            });
        },
        createCMSSignatureFromFile: function(filePath, attached) {
            return resolveStorage(nb.getText('sign', 'Подписать')).then(function() {
                return createCMSSignatureFromFile(filePath, attached);
            });
        },
        verifyCMSSignatureFromFile: function(signatureCMSFile, filePath) {
            return resolveStorage(nb.getText('verify_sign', 'Проверить')).then(function() {
                return verifyCMSSignatureFromFile(signatureCMSFile, filePath);
            });
        }
    };
})();

// called from knca applet
function AppletIsReady() {
    knca.ready();
}

$(document).ready(function() {
    $('[data-action=sign]').click(function() {
        var tfs = document.getElementById('text_for_sign').innerText;
        knca.signPlainData(tfs).then(function(sign) {
            document.getElementById('text_sign').innerHTML = sign;
        })
    });
});
