'use strict';
var jt400 = require('../lib/jt400').useInMemoryDb();

function onFail(that, done) {
	return function (err) {
		that.fail(err);
		done();
	};
}

describe('hsql in memory', function () {

	beforeEach(function (done) {
		jt400.update('create table testtbl (ID VARCHAR(10), NAME VARCHAR(300), PRIMARY KEY(ID))')
		.then(function () {
			return jt400.update('insert into testtbl values(\'TEST\', \'Foo bar baz\')');
		})
		.then(function () {
			done();
		})
		.fail(onFail(this, done));
	});

	afterEach(function (done){
		jt400.update('drop table testtbl')
		.then(function () {done();})
		.fail(onFail(this, done));
	});

	it('should select form testtbl', function (done) {
		jt400.query('select * from testtbl')
		.then(function (res) {
			expect(res.length).toBe(1);
			done();
		})
		.fail(onFail(this, done));
	});

	it('should insert list', function (done) {
		jt400.insertList('testtbl', 'ID', [
			{ID: '123', NAME: 'foo'},
			{ID: '124', NAME: 'bar'}
		])
		.then(function (res) {
			return jt400.query('select * from testtbl');
		})
		.then(function (res) {
			expect(res.length).toBe(3);
			done();
		})
		.fail(onFail(this, done));
	});

	it('should mock pgm call', function (done) {
		var callFoo = jt400.pgm('foo', {name: 'bar', size: 10}, {name: 'baz', size: 9, decimals: 2}),
			input = {bar: 'a', baz: 10};
		callFoo(input).then(function (res) {
			expect(res).toEqual(input);
			done();
		})
		.fail(onFail(this, done));
	});
});
