'use strict';
var jt400 = require('../lib/jt400');

function wrap(fn) {
	return function () {
		return fn();
	};
}

function onFail(that, done) {
	return function (err) {
		that.fail(err);
		done();
	};
}

describe('jt400', function () {
	var idList;

	beforeEach(function (done) {
		jt400.update('delete from tsttbl')
		.then(function () {
			var records = [{foo: 'bar', bar: 123, baz: '123.23'},
							{foo: 'bar2', bar: 124, baz: '321.32'}];
			return jt400.insertList('tsttbl', 'testtblid', records);
		})
		.then(function (idListResult) {
			idList = idListResult;
			done();
		})
		.fail(onFail(this, done));
	});

	it('should insert records', function () {
		expect(idList.length).toBe(2);
		expect(idList[0]).toBeGreaterThan(1);
	});

	it('should execute query', function (done) {
		jt400.query('select * from tsttbl').then( function (data) {
			expect(data.length).toBe(2);
			done();
		}, onFail(this, done));
	});

	it('should execute query with params', function (done) {
		jt400.query('select * from tsttbl where baz=?', [123.23]).then( function (data) {
			expect(data.length).toBe(1);
			done();
		}, onFail(this, done));
	});

	it('should execute update', function (done) {
		jt400.update('update tsttbl set foo=\'bar3\' where foo=\'bar\'')
			.then(function (nUpdated) {
				expect(nUpdated).toBe(1);
				done();
			}, onFail(this, done));
	});

	it('should execute update', function (done) {
		jt400.update('update tsttbl set foo=? where testtblid=?', ['ble', 0])
			.then(function (nUpdated) {
				expect(nUpdated).toBe(0);
				done();
			}, onFail(this, done));
	});


});