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
                var rw = document.getElementById('KncaApplet').getKeys(storage.alias, storage.path, storage.pwd, storage.keyType);
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

    function getNotBefore() {
        var storageAlias = $("#storageAlias").val();
        var storagePath = $("#storagePath").val();
        var password = $("#password").val();
        var alias = $("#keyAlias").val();
        if (storagePath !== null && storagePath !== "" && storageAlias !== null && storageAlias !== "") {
            if (password !== null && password !== "") {
                if (alias !== null && alias !== "") {
                    var rw = document.getElementById('KncaApplet').getNotBefore(storageAlias, storagePath, alias, password);
                    if (rw.getErrorCode() === "NONE") {
                        $("#notbefore").val(rw.getResult());
                    } else {
                        if (rw.getErrorCode() === "WRONG_PASSWORD" && rw.getResult() > -1) {
                            alert("Неправильный пароль! Количество оставшихся попыток: " + rw.getResult());
                        } else if (rw.getErrorCode() === "WRONG_PASSWORD") {
                            alert("Неправильный пароль!");
                        } else {
                            alert(rw.getErrorCode());
                        }
                    }
                } else {
                    alert("Вы не выбран ключ!");
                }
            } else {
                alert("Введите пароль к хранилищу");
            }
        } else {
            alert("Не выбран хранилище!");
        }
    }

    function getNotAfter() {
        var storageAlias = $("#storageAlias").val();
        var storagePath = $("#storagePath").val();
        var password = $("#password").val();
        var alias = $("#keyAlias").val();
        if (storagePath !== null && storagePath !== "" && storageAlias !== null && storageAlias !== "") {
            if (password !== null && password !== "") {
                if (alias !== null && alias !== "") {
                    var rw = document.getElementById('KncaApplet').getNotAfter(storageAlias, storagePath, alias, password);
                    if (rw.getErrorCode() === "NONE") {
                        $("#notafter").val(rw.getResult());
                    } else {
                        if (rw.getErrorCode() === "WRONG_PASSWORD" && rw.getResult() > -1) {
                            alert("Неправильный пароль! Количество оставшихся попыток: " + rw.getResult());
                        } else if (rw.getErrorCode() === "WRONG_PASSWORD") {
                            alert("Неправильный пароль!");
                        } else {
                            alert(rw.getErrorCode());
                        }
                    }
                } else {
                    alert("Вы не выбрали ключ!");
                }
            } else {
                alert("Введите пароль к хранилищу");
            }
        } else {
            alert("Не выбран хранилище!");
        }
    }

    function getSubjectDN() {
        var storageAlias = $("#storageAlias").val();
        var storagePath = $("#storagePath").val();
        var password = $("#password").val();
        var alias = $("#keyAlias").val();
        if (storagePath !== null && storagePath !== "" && storageAlias !== null && storageAlias !== "") {
            if (password !== null && password !== "") {
                if (alias !== null && alias !== "") {
                    var rw = document.getElementById('KncaApplet').getSubjectDN(storageAlias, storagePath, alias, password);

                    if (rw.getErrorCode() === "NONE") {
                        $("#subjectDn").text(rw.getResult());
                    } else {
                        if (rw.getErrorCode() === "WRONG_PASSWORD" && rw.getResult() > -1) {
                            alert("Неправильный пароль! Количество оставшихся попыток: " + rw.getResult());
                        } else if (rw.getErrorCode() === "WRONG_PASSWORD") {
                            alert("Неправильный пароль!");
                        } else {
                            alert(rw.getErrorCode());
                        }
                    }
                } else {
                    alert("Вы не выбрали ключ!");
                }
            } else {
                alert("Введите пароль к хранилищу");
            }
        } else {
            alert("Не выбран хранилище!");
        }
    }

    function getIssuerDN() {
        var storageAlias = $("#storageAlias").val();
        var storagePath = $("#storagePath").val();
        var password = $("#password").val();
        var alias = $("#keyAlias").val();
        if (storagePath !== null && storagePath !== "" && storageAlias !== null && storageAlias !== "") {
            if (password !== null && password !== "") {
                if (alias !== null && alias !== "") {
                    var rw = document.getElementById('KncaApplet').getIssuerDN(storageAlias, storagePath, alias, password);

                    if (rw.getErrorCode() === "NONE") {
                        $("#issuerDn").text(rw.getResult());
                    } else {
                        if (rw.getErrorCode() === "WRONG_PASSWORD" && rw.getResult() > -1) {
                            alert("Неправильный пароль! Количество оставшихся попыток: " + rw.getResult());
                        } else if (rw.getErrorCode() === "WRONG_PASSWORD") {
                            alert("Неправильный пароль!");
                        } else {
                            alert(rw.getErrorCode());
                        }
                    }
                } else {
                    alert("Вы не выбрали ключ!");
                }
            } else {
                alert("Введите пароль к хранилищу");
            }
        } else {
            alert("Не выбран хранилище!");
        }
    }

    function signPlainData() {
        var storageAlias = $("#storageAlias").val();
        var storagePath = $("#storagePath").val();
        var password = $("#password").val();
        var alias = $("#keyAlias").val();
        $("#identifier").text("Не проверено");
        if (storagePath !== null && storagePath !== "" && storageAlias !== null && storageAlias !== "") {
            if (password !== null && password !== "") {
                if (alias !== null && alias !== "") {
                    var data = $("#date").val();
                    if (data !== null && data !== "") {
                        var rw = document.getElementById('KncaApplet').signPlainData(storageAlias, storagePath, alias, password, data);
                        if (rw.getErrorCode() === "NONE") {
                            $("#signature").text(rw.getResult());
                        } else {
                            if (rw.getErrorCode() === "WRONG_PASSWORD" && rw.getResult() > -1) {
                                alert("Неправильный пароль! Количество оставшихся попыток: " + rw.getResult());
                            } else if (rw.getErrorCode() === "WRONG_PASSWORD") {
                                alert("Неправильный пароль!");
                            } else {
                                $("#signature").text("");
                                alert(rw.getErrorCode());
                            }
                        }
                    } else {
                        alert("Вы не ввели данные!")
                    }
                } else {
                    alert("Вы не выбрали ключ!");
                }
            } else {
                alert("Введите пароль к хранилищу");
            }
        } else {
            alert("Не выбран хранилище!");
        }
    }

    function createCMSSignature() {
        var storageAlias = $("#storageAlias").val();
        var storagePath = $("#storagePath").val();
        var password = $("#password").val();
        var alias = $("#keyAlias").val();
        $("#identifierCMS").text("Не проверено");
        if (storagePath !== null && storagePath !== "" && storageAlias !== null && storageAlias !== "") {
            if (password !== null && password !== "") {
                if (alias !== null && alias !== "") {
                    var data = $("#dateCMS").val();
                    var flag = $("#flag").is(':checked');

                    if (data !== null && data !== "") {
                        if (flag) {
                            var rw = document.getElementById('KncaApplet').createCMSSignature(storageAlias, storagePath, alias, password, data, true);
                        } else {
                            var rw = document.getElementById('KncaApplet').createCMSSignature(storageAlias, storagePath, alias, password, data, false);
                        }

                        if (rw.getErrorCode() === "NONE") {
                            $("#signatureCMS").text(rw.getResult());
                        } else {
                            if (rw.getErrorCode() === "WRONG_PASSWORD" && rw.getResult() > -1) {
                                alert("Неправильный пароль! Количество оставшихся попыток: " + rw.getResult());
                            } else if (rw.getErrorCode() === "WRONG_PASSWORD") {
                                alert("Неправильный пароль!");
                            } else {
                                $("#signatureCMS").text("");
                                alert(rw.getErrorCode());
                            }
                        }
                    } else {
                        alert("Вы не ввели данные!");
                    }
                } else {
                    alert("Вы не выбрали ключ!");
                }
            } else {
                alert("Введите пароль к хранилищу");
            }
        } else {
            alert("Не выбран хранилище!");
        }
    }

    function signXml() {
        var storageAlias = $("#storageAlias").val();
        var storagePath = $("#storagePath").val();
        var password = $("#password").val();
        var alias = $("#keyAlias").val();
        $("#identifierXML").text("Не проверено");
        if (storagePath !== null && storagePath !== "" && storageAlias !== null && storageAlias !== "") {
            if (password !== null && password !== "") {
                if (alias !== null && alias !== "") {
                    var data = document.getElementById("dateXML").value;
                    if (data !== null && data !== "") {
                        var rw = document.getElementById('KncaApplet').signXml(storageAlias, storagePath, alias, password, data);
                        if (rw.getErrorCode() === "NONE") {
                            document.getElementById("signatureXML").value = rw.getResult();
                        } else {
                            if (rw.getErrorCode() === "WRONG_PASSWORD" && rw.getResult() > -1) {
                                alert("Неправильный пароль! Количество оставшихся попыток: " + rw.getResult());
                            } else if (rw.getErrorCode() === "WRONG_PASSWORD") {
                                alert("Неправильный пароль!");
                            } else {
                                document.getElementById("signatureXML").value = "";
                                alert(rw.getErrorCode());
                            }
                        }
                    } else {
                        alert("Вы не ввели данные!");
                    }
                } else {
                    alert("Вы не выбрали ключ!");
                }
            } else {
                alert("Введите пароль к хранилищу");
            }
        } else {
            alert("Не выбран хранилище!");
        }
    }

    function verifyXml() {
        //                var signature = $("#signatureXML").text();
        var signature = document.getElementById("signatureXML").value;
        if (signature !== null && signature !== "") {
            var rw = document.getElementById('KncaApplet').verifyXml(signature);
            if (rw.getErrorCode() === "NONE") {
                if (rw.getResult()) {
                    $("#identifierXML").text("Валидная подпись");
                } else {
                    $("#identifierXML").text("Неправильная подпись");
                }
            } else {
                if (rw.getErrorCode() === "WRONG_PASSWORD" && rw.getResult() > -1) {
                    alert("Неправильный пароль! Количество оставшихся попыток: " + rw.getResult());
                } else if (rw.getErrorCode() === "WRONG_PASSWORD") {
                    alert("Неправильный пароль!");
                } else {
                    $("#identifierXML").text("Неправильная подпись");
                    alert(rw.getErrorCode());
                }
            }
        } else {
            alert("Не найден подписанный XML!");
        }
    }

    function verifyPlainData() {
        var storageAlias = $("#storageAlias").val();
        var storagePath = $("#storagePath").val();
        var password = $("#password").val();
        var alias = $("#keyAlias").val();
        if (storagePath !== null && storagePath !== "" && storageAlias !== null && storageAlias !== "") {
            if (password !== null && password !== "") {
                if (alias !== null && alias !== "") {
                    var data = $("#date").val();
                    var signature = $("#signature").val();
                    if (data !== null && data !== "" && signature !== null && signature !== "") {
                        var rw = document.getElementById('KncaApplet').verifyPlainData(storageAlias, storagePath, alias, password, data, signature);
                        if (rw.getErrorCode() === "NONE") {
                            if (rw.getResult()) {
                                $("#identifier").text("Валидная подпись");
                            } else {
                                $("#identifier").text("Неправильная подпись");
                            }
                        } else {
                            if (rw.getErrorCode() === "WRONG_PASSWORD" && rw.getResult() > -1) {
                                alert("Неправильный пароль! Количество оставшихся попыток: " + rw.getResult());
                            } else if (rw.getErrorCode() === "WRONG_PASSWORD") {
                                alert("Неправильный пароль!");
                            } else {
                                alert(rw.getErrorCode());
                            }
                        }
                    } else {
                        alert("Вы не ввели данные, или подписанные данные не найдены!");
                    }
                } else {
                    alert("Вы не выбрали ключ!");
                }
            } else {
                alert("Введите пароль к хранилищу");
            }
        } else {
            alert("Не выбран хранилище!");
        }
    }

    function verifyCMSSignature() {
        var data = $("#dateCMS").val();
        var signatureCMS = $("#signatureCMS").val();
        if (signatureCMS !== null && signatureCMS !== "") {
            var rw = null;
            rw = document.getElementById('KncaApplet').verifyCMSSignature(signatureCMS, data);
            if (rw.getErrorCode() === "NONE") {
                if (rw.getResult()) {
                    $("#identifierCMS").text("Валидная подпись");
                } else {
                    $("#identifierCMS").text("Неправильная подпись");
                }
            } else {
                $("#identifierCMS").text("Неправильная подпись");
                alert(rw.getErrorCode());
            }
        } else {
            alert("Вы не ввели данные, или подписанные данные не найдены!");
        }
    }

    function getRdnByOid() {
        var storageAlias = $("#storageAlias").val();
        var storagePath = $("#storagePath").val();
        var password = $("#password").val();
        var alias = $("#keyAlias").val();
        if (storagePath !== null && storagePath !== "" && storageAlias !== null && storageAlias !== "") {
            if (password !== null && password !== "") {
                if (alias !== null && alias !== "") {
                    var oid = "";
                    var selected = $("input[type='radio'][name='oid']:checked");
                    if (selected.length > 0) {
                        oid = selected.val();
                    }
                    var rw = document.getElementById('KncaApplet').getRdnByOid(storageAlias, storagePath, alias, password, oid, 0);
                    if (rw.getErrorCode() === "NONE") {
                        $("#rdnvalue").val(rw.getResult());
                    } else {
                        if (rw.getErrorCode() === "WRONG_PASSWORD" && rw.getResult() > -1) {
                            alert("Неправильный пароль! Количество оставшихся попыток: " + rw.getResult());
                        } else if (rw.getErrorCode() === "WRONG_PASSWORD") {
                            alert("Неправильный пароль!");
                        } else {
                            $("#rdnvalue").val("RDN не найден!");
                            alert(rw.getErrorCode());
                        }
                    }

                } else {
                    alert("Вы не выбрали ключ!");
                }
            } else {
                alert("Введите пароль к хранилищу");
            }
        } else {
            alert("Не выбран хранилище!");
        }
    }

    function selectFileToSign() {
        try {
            var rw = document.getElementById('KncaApplet').showFileChooser("ALL", "");
            if (rw.getErrorCode() === "NONE") {
                document.getElementById("filePath").value = rw.getResult();
            } else {
                alert(rw.getErrorCode());
            }
            return;
        } catch (e) {
            alert(e);
        }
    }

    function createCMSSignatureFromFile() {
        var storageAlias = $("#storageAlias").val();
        var storagePath = $("#storagePath").val();
        var password = $("#password").val();
        var alias = $("#keyAlias").val();
        var rw = null;


        $("#identifierCMSFile").text("Не проверено");
        if (storagePath !== null && storagePath !== "" && storageAlias !== null && storageAlias !== "") {
            if (password !== null && password !== "") {
                if (alias !== null && alias !== "") {

                    var filePath = $("#filePath").val();
                    var flag = $("#flagFile").is(':checked');

                    if (filePath !== null && filePath !== "") {
                        if (flag) {
                            rw = document.getElementById('KncaApplet').createCMSSignatureFromFile(storageAlias, storagePath, alias, password, filePath, true);
                        } else {
                            rw = document.getElementById('KncaApplet').createCMSSignatureFromFile(storageAlias, storagePath, alias, password, filePath, false);
                        }

                        if (rw.getErrorCode() === "NONE") {
                            $("#signatureCMSFile").text(rw.getResult());
                        } else {
                            if (rw.getErrorCode() === "WRONG_PASSWORD" && rw.getResult() > -1) {
                                alert("Неправильный пароль! Количество оставшихся попыток: " + rw.getResult());
                            } else if (rw.getErrorCode() === "WRONG_PASSWORD") {
                                alert("Неправильный пароль!");
                            } else {
                                $("#signatureCMS").text("");
                                alert(rw.getErrorCode());
                            }
                        }
                    } else {
                        alert("Вы не ввели путь к файлу");
                    }
                } else {
                    alert("Вы не выбрали ключ!");
                }
            } else {
                alert("Введите пароль к хранилищу");
            }
        } else {
            alert("Не выбрано хранилище!");
        }
    }

    function verifyCMSSignatureFromFile() {
        var signatureCMSFile = $("#signatureCMSFile").val();
        var filePath = $("#filePath").val();
        if (signatureCMS !== null && signatureCMS !== "") {
            var rw = null;
            rw = document.getElementById('KncaApplet').verifyCMSSignatureFromFile(signatureCMSFile, filePath);
            if (rw.getErrorCode() === "NONE") {
                if (rw.getResult()) {
                    $("#identifierCMSFile").text("Валидная подпись");
                } else {
                    $("#identifierCMSFile").text("Неправильная подпись");
                }
            } else {
                $("#identifierCMSFile").text("Неправильная подпись");
                alert(rw.getErrorCode());
            }
        } else {
            alert("Вы не ввели данные, или подписанные данные не найдены!");
        }
    }

    return {
        init: init,
        ready: ready,
        chooseStoragePath: function() {
            log('uninitialized; click knca init');
        }
    }
})();

// called from knca applet
function AppletIsReady() {
    knca.ready();
}
