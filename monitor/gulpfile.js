const gulp = require('gulp');
const gutil = require('gulp-util');
const browserify = require('browserify');
const tsify = require('tsify');
const watchify = require('watchify');
const buffer = require('vinyl-buffer');
const source = require('vinyl-source-stream');

const www = 'src/main/resources/www/';

function build() {
  return browserify('js/main.ts', {
      standalone: 'Monitor',
      debug: true
    })
    .plugin(tsify);
}

const watchedBrowserify = watchify(build());

function bundle() {
  return watchedBrowserify
    .bundle()
    .on('error', (e) => gutil.log(gutil.colors.red(e.message)))
    .pipe(source('main.js'))
    .pipe(buffer())
    .pipe(gulp.dest(www));
}

watchedBrowserify.on('update', bundle);
watchedBrowserify.on('log', gutil.log);

gulp.task('ol-css', function() {
  gulp.src('node_modules/openlayers/dist/ol.css')
    .pipe(gulp.dest(www));
});

gulp.task('dev', ['ol-css'], function() {
  return build()
    .bundle()
    .pipe(source('main.js'))
    .pipe(gulp.dest(www));
});

gulp.task('watch', ['ol-css'], bundle);
gulp.task('default', ['dev']);
