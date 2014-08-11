module.exports = function (grunt) {
	var _ = require('underscore');

	var paths = {
		public: grunt.option('public') || process.env.OUTPUT_DIR || 'target/classes/public',
		private: grunt.option('private') || 'src/main/client'
	};

	var files = {
		assets: {
			expand: true,
			cwd: '<%= paths.private %>',
			src: ['partials/**', '*.*', 'img/**', 'css/**', 'js/lib/**'],
			dest: '<%= paths.public %>'
		},
		js: [
			'<%= paths.private %>/js/app.js'
		]
	};

	grunt.initConfig({
		paths: paths,
		pkg: grunt.file.readJSON('package.json'),
		clean: ['<%= paths.public %>'],
		copy: {
			main: {
				files: [files.assets]
			}
		},
		concat: {
			main: {
				files: [
					{
						src: files.js,
						dest: '<%= paths.public %>/js/app.js'
					}
				]
			}
		},
	});

	grunt.loadNpmTasks('grunt-contrib-copy');
	grunt.loadNpmTasks('grunt-contrib-concat');

	var distTasks = ['copy', 'concat'];
	grunt.registerTask('default', distTasks);
}
