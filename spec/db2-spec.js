'use strict';
var config = require('../config'),
	db2 = require('../lib/db2').init(config);

function onFail(done) {
	return function (err) {
		console.log(err);
		done();
	};
}

describe('db2', function () {

	it('should execute query', function (done) {
		db2.executeQuery('select * from tsttbl').then( function (data) {
			done();
		}, onFail(done));
	});

	it('should execute query with params', function (done) {
		db2.executeQuery('select * from tsttbl where baz=?', [123.23]).then( function (data) {
			expect(data.length).toBe(1);
			done();
		}, onFail(done));
	});

	it('should execute update', function (done) {
		db2.executeUpdate('update tsttbl set foo=\'bar\' where testtblid=1732')
			.then(function (nUpdated) {
				done();
			}, onFail(done));
	});

	it('should execute update', function (done) {
		db2.executeUpdate('update tsttbl set foo=? where testtblid=?', ['ble', 2422])
			.then(function (nUpdated) {
				done();
			}, onFail(done));
	});
});