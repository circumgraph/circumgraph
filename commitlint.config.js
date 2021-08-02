module.exports = {
	rules: {
		'body-leading-blank': [ 1, 'always' ],
		'footer-leading-blank': [ 1, 'always' ],
		'scope-case': [ 2, 'always', 'lower-case' ],
		'subject-case': [
			2,
			'never',
			[ 'lower-case' ]
		],
		'subject-empty': [ 2, 'never' ],
		'subject-full-stop': [ 2, 'never', '.' ],
		'type-case': [ 2, 'always', 'lower-case' ],
		'type-empty': [ 2, 'never' ],
		'type-enum': [
			2,
			'always',
			[
				'build',
				'chore',
				'docs',
				'feat',
				'fix',
				'refactor',
				'revert',
				'style',
				'test'
			]
		],
		'scope-enum': [
			2,
			'always',
			[
				'app',
				'model',
				'graphql',
				'schema-graphql',
				'storage',
				'values'
			]
		]
	}
};
