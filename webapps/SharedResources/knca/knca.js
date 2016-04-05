var knca = (function() {

    var wd = window.document,
        noty,
        initialized = false,
        isReady = false,
        t_o,
        LOCALE_STORAGE_PATH_KEY = 'sign_storage_path';

    var storage = {
        alias: 'PKCS12',
        keyAlias: '627b042ba9d21a10f724d7a6b5c68cdb21fbd899',
        keyType: 'SIGN',
        path: '/home/medin/Загрузки/keulimjai/RSA_627b042ba9d21a10f724d7a6b5c68cdb21fbd899.p12',
        pwd: '123456'
    };

    function init() {
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

        // add api
        window.knca.chooseStoragePath = chooseStoragePath;

        // restore path from storage


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

    function invalidateStorage() {
        // storage.alias = 'NONE';
        // storage.path = '';
        localeStorage.setItem(LOCALE_STORAGE_PATH_KEY, storage.path);
    }

    function chooseStoragePath() {
        if (storage.alias !== 'NONE') {
            var rw = wd.getElementById('KncaApplet').browseKeyStore(storage.alias, 'P12', storage.path);
            if (rw.getErrorCode() === 'NONE') {
                storage.path = rw.getResult();
                if (!storage.path) {
                    invalidateStorage();
                } else {
                    localeStorage.setItem(LOCALE_STORAGE_PATH_KEY, storage.path);
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

    return {
        init: init,
        ready: ready
    }
})();

function AppletIsReady() {
    knca.ready();
}
