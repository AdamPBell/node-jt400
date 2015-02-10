node-jt400
=====

nodejs jt400 wrapper

## Configure

```javascript
var pool = require('node-jt400').pool({host: 'myhost', user: 'myuser', password: 'xxx'});
```

## SQL query

```javascript
pool.query('SELECT FIELD1, FIELD2 FROM FOO WHERE BAR=? AND BAZ=?', [1, 'a'])
.then(function (result) {
	var field1 = result[0].FIELD1;
	...
});

```
## SQL update

```javascript
pool.update('update FOO set BAR=? WHERE BAZ=?', [1, 'a'])
.then(function (nUpdated) {
    ...
});

```
## SQL insert

```javascript
//insert list in one statement
var tableName = 'foo',
    idColumn  = 'fooid',
    rows = [
        {FIELD1: 1, FIELD2: 'a'},
        {FIELD1: 1, FIELD2: 'a'}
    ];
pool.insertList(tableName, idColumn, rows)
.then(function (listOfGeneratedIds) {
    ...
});

```
