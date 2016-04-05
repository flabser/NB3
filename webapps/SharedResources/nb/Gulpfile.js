var argv = require('yargs').argv;
var gulp = require('gulp');
var gulpif = require('gulp-if');
var jshint = require('gulp-jshint');
var concat = require('gulp-concat');
var rename = require('gulp-rename');
var uglify = require('gulp-uglify');
var csso = require('gulp-csso');
var wrap = require('gulp-wrap');
var declare = require('gulp-declare');
var handlebars = require('gulp-handlebars');

// uglify argument --production
// gulp --production
//
// remove all node modules
// npm remove yargs gulp gulp-if gulp-jshint gulp-concat gulp-rename gulp-uglify gulp-csso gulp-wrap gulp-declare gulp-handlebars jshint

var modules = ['Administrator', 'Accountant', 'Staff', 'Reference', 'MunicipalProperty', 'PropertyLeasing', 'Registry'];
var _styles = {};
var _templates = {};
var _scripts = {};
var webAppsPath = '../../';
var isProduction = argv.production;

// create module task
for (var i = 0; i < modules.length; i++) {
    var module = modules[i];
    // _styles
    _styles[module] = ['../css/normalize.css',
        '../vendor/select2/css/select2.min.css',
        'css/nb.min.css',
        webAppsPath + module + '/css/**/*.css',
        '!' + webAppsPath + module + '/css/*.min.css'
    ];
    // _templates
    _templates[module] = [webAppsPath + module + '/js/templates/*.hbs'];
    // _scripts
    _scripts[module] = ['js/nb.build.js',
        '../vendor/select2/js/select2.full.min.js',
        '../vendor/select2/js/i18n/ru.js',
        webAppsPath + module + '/js/**/*.js',
        '!' + webAppsPath + module + '/js/app.bundle.js'
    ];

    (function() { // scope
        var m = module;
        gulp.task(m + '_styles', function() {
            gulp.src(_styles[m])
                .pipe(concat('all.min.css'))
                .pipe(csso())
                .pipe(gulp.dest(webAppsPath + m + '/css'));
        });

        gulp.task(m + '_templates', function() {
            gulp.src(_templates[m])
                .pipe(handlebars({
                    handlebars: require('handlebars')
                }))
                .pipe(wrap('Handlebars.template(<%= contents %>)'))
                .pipe(declare({
                    namespace: 'nb.templates',
                    noRedeclare: true,
                }))
                .pipe(concat('templates.js'))
                .pipe(gulp.dest(webAppsPath + m + '/js/templates/compiled'));
        });

        gulp.task(m + '_scripts', function() {
            gulp.src(_scripts[m])
                .pipe(concat('app.bundle.js'))
                .pipe(gulpif(isProduction, uglify()))
                .pipe(gulp.dest(webAppsPath + m + '/js'));
        });
    })();
}

// nb task
var js_files = ['../vendor/handlebars/handlebars.runtime-v4.0.5.js',
    '../vendor/jquery/ui-i18n/*.js',
    'js/src/nb.js',
    'js/src/**/*.js'
];
var css_files = ['./css/**/*.css',
    '!./css/nb.min.css'
];
var hbs_templates = ['js/src/templates/*.hbs'];

gulp.task('lint', function() {
    gulp.src(js_files)
        .pipe(jshint())
        .pipe(jshint.reporter('default'));
});

gulp.task('templates', function() {
    gulp.src(hbs_templates)
        .pipe(handlebars({
            handlebars: require('handlebars')
        }))
        .pipe(wrap('Handlebars.template(<%= contents %>)'))
        .pipe(declare({
            namespace: 'nb.templates',
            noRedeclare: true,
        }))
        .pipe(concat('templates.js'))
        .pipe(gulp.dest('./js/src/templates/compiled'));
});

gulp.task('scripts', function() {
    gulp.src(js_files)
        .pipe(concat('nb.build.js'))
        .pipe(gulp.dest('./js'))
        .pipe(rename('nb.min.js'))
        .pipe(uglify())
        .pipe(gulp.dest('./js'));
});

gulp.task('styles', function() {
    gulp.src(css_files)
        .pipe(concat('nb.min.css'))
        .pipe(csso())
        .pipe(gulp.dest('./css'));
});

gulp.task('default', ['styles', 'templates', 'lint', 'scripts'], function() {

    gulp.watch(css_files, function() {
        gulp.run('styles');
    });

    gulp.watch(hbs_templates, function() {
        gulp.run('templates');
    });

    gulp.watch(js_files, function() {
        gulp.run('scripts');
    });

    // create module watch
    for (var i = 0; i < modules.length; i++) {
        var module = modules[i];

        console.log('watch', module, '\n_styles:\n', _styles[module], '\n_templates:\n', _templates[module], '\n_scripts:\n', _scripts[module]);
        console.log('---------------------------------');

        (function() {
            var m = module;
            gulp.watch(_styles[m], function() {
                gulp.run(m + '_styles');
            });
            gulp.watch(_templates[m], function() {
                gulp.run(m + '_templates');
            });
            gulp.watch(_scripts[m], function() {
                gulp.run(m + '_scripts');
            });
        })();
    }
});
