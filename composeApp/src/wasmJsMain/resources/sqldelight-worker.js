/**
 * Functional SQLDelight Web Worker implementation skeleton.
 */
importScripts('https://cdnjs.cloudflare.com/ajax/libs/sql.js/1.10.3/sql-wasm.js');

let db = null;

initSqlJs().then(function(SQL) {
    db = new SQL.Database();
    postMessage({ event: 'ready' });
});

onmessage = function(e) {
    if (!db) return;
    const { action, sql, params } = e.data;

    try {
        if (action === 'exec') {
            db.run(sql, params);
            postMessage({ event: 'execDone' });
        } else if (action === 'query') {
            const res = db.exec(sql, params);
            postMessage({ event: 'queryDone', results: res });
        }
    } catch (err) {
        postMessage({ event: 'error', message: err.message });
    }
};
