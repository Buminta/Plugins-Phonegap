var PhoneGap = cordova;
(function() {
    var SQLiteFactory, SQLitePlugin, SQLitePluginCallback, SQLitePluginTransaction, SQLitePluginTransactionCB, get_unique_id, pcb, root, transaction_callback_queue, transaction_queue;
    root = this;
    SQLitePlugin = function(openargs, openSuccess, openError) {
        var dbname;
        if (!(openargs && openargs['name'])) {
            throw new Error("Cannot create a SQLitePlugin instance without a db name");
        }
        dbname = openargs.name;
        this.openargs = openargs;
        this.dbname = dbname;
        this.openSuccess = openSuccess;
        this.openError = openError;
        this.openSuccess || (this.openSuccess = function() {
            return
        });
        this.openError || (this.openError = function(e) {
            return
        });
        this.open(this.openSuccess, this.openError);
    };
    SQLitePlugin.prototype.databaseFeatures = {
        isSQLitePluginDatabase : true
    };
    SQLitePlugin.prototype.openDBs = {};
    SQLitePlugin.prototype.transaction = function(fn, error, success) {
        var t;
        t = new SQLitePluginTransaction(this.dbname);
        fn(t);
        t.complete(success, error);
    };
    SQLitePlugin.prototype.open = function(success, error) {
        if (!(this.dbname in this.openDBs)) {
            this.openDBs[this.dbname] = true;
            cordova.exec(success, error, "SQLitePlugin", "open", [this.openargs]);
        }
    };
    SQLitePlugin.prototype.close = function(success, error) {
        if (this.dbname in this.openDBs) {
            delete this.openDBs[this.dbname];
            cordova.exec(null, null, "SQLitePlugin", "close", [this.dbname]);
        }
    };
    pcb = function() {
        return 1;
    };
    SQLitePlugin.prototype.executeSql = function(statement, params, success, error) {
        pcb = success;
        cordova.exec((function() {
            return 1;
        }), error, "SQLitePlugin", "executePragmaStatement", [this.dbname, statement, params]);
    };
    SQLitePlugin.prototype.executePragmaStatement = function(statement, success, error) {
        pcb = success;
        cordova.exec((function() {
            return 1;
        }), error, "SQLitePlugin", "executePragmaStatement", [this.dbname, statement]);
    };
    SQLitePluginCallback = {
        p1 : function(id, result) {
            var mycb;
            mycb = pcb;
            pcb = function() {
                return 1;
            };
            mycb(result);
        }
    };
    get_unique_id = function() {
        var id, id2;
        id = new Date().getTime();
        id2 = new Date().getTime();
        while (id === id2) {
            id2 = new Date().getTime();
        }
        return id2 + "000";
    };
    transaction_queue = [];
    transaction_callback_queue = {};
    SQLitePluginTransaction = function(dbname) {
        this.dbname = dbname;
        this.executes = [];
        this.trans_id = get_unique_id();
        this.__completed = false;
        this.__submitted = false;
        this.optimization_no_nested_callbacks = false;
        transaction_queue[this.trans_id] = [];
        transaction_callback_queue[this.trans_id] = new Object();
    };
    SQLitePluginTransactionCB = {};
    SQLitePluginTransactionCB.queryCompleteCallback = function(transId, queryId, result) {
        var query, x;
        query = null;
        for (x in transaction_queue[transId]) {
            if (transaction_queue[transId][x]["query_id"] === queryId) {
                query = transaction_queue[transId][x];
                if (transaction_queue[transId].length === 1) {
                    transaction_queue[transId] = [];
                } else {
                    transaction_queue[transId].splice(x, 1);
                }
                break;
            }
        }
        if (query && query["callback"])
            return query["callback"](result);
    };
    SQLitePluginTransactionCB.queryErrorCallback = function(transId, queryId, result) {
        var query, x;
        query = null;
        for (x in transaction_queue[transId]) {
            if (transaction_queue[transId][x]["query_id"] === queryId) {
                query = transaction_queue[transId][x];
                if (transaction_queue[transId].length === 1) {
                    transaction_queue[transId] = [];
                } else {
                    transaction_queue[transId].splice(x, 1);
                }
                break;
            }
        }
        if (query && query["err_callback"])
            return query["err_callback"](result);
    };
    SQLitePluginTransactionCB.txCompleteCallback = function(transId) {
        if ( typeof transId !== "undefined") {
            if (transId && transaction_callback_queue[transId] && transaction_callback_queue[transId]["success"]) {
                return transaction_callback_queue[transId]["success"]();
            }
        } else {
            return
        }
    };
    SQLitePluginTransactionCB.txErrorCallback = function(transId, error) {
        if ( typeof transId !== "undefined") {
            if (transId && transaction_callback_queue[transId]["error"]) {
                transaction_callback_queue[transId]["error"](error);
            }
            delete transaction_queue[transId];
            return
            delete transaction_callback_queue[transId];
        } else {
            return
        }
    };
    SQLitePluginTransaction.prototype.add_to_transaction = function(trans_id, query, params, callback, err_callback) {
        var new_query;
        new_query = new Object();
        new_query["trans_id"] = trans_id;
        if (callback || !this.optimization_no_nested_callbacks) {
            new_query["query_id"] = get_unique_id();
        } else {
            if (this.optimization_no_nested_callbacks)
                new_query["query_id"] = "";
        }
        new_query["query"] = query;
        if (params) {
            new_query["params"] = params;
        } else {
            new_query["params"] = [];
        }
        new_query["callback"] = callback;
        new_query["err_callback"] = err_callback;
        if (!transaction_queue[trans_id])
            transaction_queue[trans_id] = [];
        transaction_queue[trans_id].push(new_query);
    };
    SQLitePluginTransaction.prototype.executeSql = function(sql, values, success, error) {
        var errorcb, successcb, txself;
        errorcb =
            void 0;
        successcb =
            void 0;
        txself =
            void 0;
        txself = this;
        successcb = null;
        if (success) {
            successcb = function(execres) {
                var res, saveres;
                res =
                    void 0;
                saveres =
                    void 0;
                saveres = execres;
                res = {
                    rows : {
                        item : function(i) {
                            return saveres[i];
                        },
                        length : saveres.length
                    },
                    rowsAffected : saveres.rowsAffected,
                    insertId : saveres.insertId || null
                };
                return success(txself, res);
            };
        } else {
        }
        errorcb = null;
        if (error) {
            errorcb = function(res) {
                return error(txself, res);
            };
        }
        this.add_to_transaction(this.trans_id, sql, values, successcb, errorcb);
    };
    SQLitePluginTransaction.prototype.complete = function(success, error) {
        var errorcb, successcb, txself;
        if (this.__completed)
            throw new Error("Transaction already run");
        if (this.__submitted)
            throw new Error("Transaction already submitted");
        this.__submitted = true;
        txself = this;
        successcb = function() {
            if (transaction_queue[txself.trans_id].length > 0 && !txself.optimization_no_nested_callbacks) {
                txself.__submitted = false;
                return txself.complete(success, error);
            } else {
                this.__completed = true;
                if (success)
                    return success(txself);
            }
        };
        errorcb = function(res) {
            return null;
        };
        if (error) {
            errorcb = function(res) {
                return error(txself, res);
            };
        }
        transaction_callback_queue[this.trans_id]["success"] = successcb;
        transaction_callback_queue[this.trans_id]["error"] = errorcb;
        cordova.exec(null, null, "SQLitePlugin", "executeSqlBatch", [this.dbname, transaction_queue[this.trans_id]]);
    };
    SQLiteFactory = {
        opendb : function() {
            var errorcb, first, okcb, openargs;
            if (arguments.length < 1)
                return null;
            first = arguments[0];
            openargs = null;
            okcb = null;
            errorcb = null;
            if (first.constructor === String) {
                openargs = {
                    name : first
                };
                if (arguments.length >= 5) {
                    okcb = arguments[4];
                    if (arguments.length > 5)
                        errorcb = arguments[5];
                }
            } else {
                openargs = first;
                if (arguments.length >= 2) {
                    okcb = arguments[1];
                    if (arguments.length > 2)
                        errorcb = arguments[2];
                }
            }
            return new SQLitePlugin(openargs, okcb, errorcb);
        }
    };
    root.SQLitePluginCallback = SQLitePluginCallback;
    root.SQLitePluginTransactionCB = SQLitePluginTransactionCB;
    return root.sqlitePlugin = {
        sqliteFeatures : {
            isSQLitePlugin : true
        },
        openDatabase : SQLiteFactory.opendb
    };
})();



function Downloader() {
    this.trans_id = 0;
}

Downloader.prototype.downloadFile = function(fileUrl, win, fail) {
    if (!fail)
        win = params;
    PhoneGap.exec(win, fail, "Downloader", "downloadFile", fileUrl);
};
Downloader.prototype.testDownload = function(fileUrl, win, fail) {
    PhoneGap.exec(win, fail, "Downloader", "testDownload", [fileUrl]);
};
Downloader.prototype.pause = function() {
    PhoneGap.exec(null, null, "Downloader", "pause", [this.trans_id]);
};
Downloader.prototype.stop = function() {
    PhoneGap.exec(null, null, "Downloader", "stop", [this.trans_id]);
};
Downloader.prototype.play = function(token) {
    if (token)  PhoneGap.exec(null, null, "Downloader", "play", [this.trans_id,token]);
    else PhoneGap.exec(null, null, "Downloader", "play", [this.trans_id]);
};
Downloader.prototype.deleteDir = function(url, callback, err) {
    PhoneGap.exec(callback, err, "Downloader", "deleteDir", [url]);
};
PhoneGap.addConstructor(function() {
    PhoneGap.addPlugin("downloader", new Downloader());
    PluginManager.addService("Downloader", "org.plugins.Downloader");
});

SoftKeyBoard = {
    show: function(win, fail){
        return cordova.exec(function(args){
            if (win !== undefined) {
                win(args);
            }
        }, function(args){
            if (fail !== undefined) {
                fail(args);
            }
        }, "SoftKeyBoard", "show", []);
    },

    hide: function(win, fail){
        return cordova.exec(function(args){
            if (win !== undefined) {
                win(args);
            }
        }, function(args){
            if (fail !== undefined) {
                fail(args);
            }
        }, "SoftKeyBoard", "hide", []);
    },

    isShowing: function(win, fail){
        return cordova.exec(function(args){
            if (win !== undefined) {
                win(args);
            }
        }, function(args){
            if (fail !== undefined) {
                fail(args);
            }
        }, "SoftKeyBoard", "isShowing", []);
    }
};


function ExtFunctionsPlugin() {
}

ExtFunctionsPlugin.prototype.checkMD5 = function(list, callback, err) {
    PhoneGap.exec(callback, err, "ExtFunctionsPlugin", "checkMD5", [list]);
}
ExtFunctionsPlugin.prototype.maxOrderForShelf = function(list, callback, err) {
    PhoneGap.exec(callback, err, "ExtFunctionsPlugin", "maxOrderForShelf", [list]);
}
ExtFunctionsPlugin.prototype.maxOrderByShelf = function(list, callback, err) {
    PhoneGap.exec(callback, err, "ExtFunctionsPlugin", "maxOrderByShelf", [list[0],list[1]]);
}
ExtFunctionsPlugin.prototype.installAPK = function(uri, callback, err) {
    PhoneGap.exec(callback, err, "ExtFunctionsPlugin", "installAPK", [uri]);
}
ExtFunctionsPlugin.prototype.closeServiceAPK = function(callback) {
    PhoneGap.exec(callback, null, "ExtFunctionsPlugin", "closeServiceAPK", []);
}
ExtFunctionsPlugin.prototype.showNotify = function(title, text, process, id) {
    PhoneGap.exec(null, null, "ExtFunctionsPlugin", "notify", [title, text, process, id]);
}
ExtFunctionsPlugin.prototype.hideNotify = function(text,progress, id) {
    PhoneGap.exec(null, null, "ExtFunctionsPlugin", "notifyOff", [text,progress, id]);
}
ExtFunctionsPlugin.prototype.getSerial = function(success, fail) {
    PhoneGap.exec(success, fail, "ExtFunctionsPlugin", "getSerial", []);
}
ExtFunctionsPlugin.prototype.getRomVersion = function(success, fail) {
    PhoneGap.exec(success, fail, "ExtFunctionsPlugin", "getRomVersion", []);
}

ExtFunctionsPlugin.prototype.RC4Encode = function(key, success, fail) {
    PhoneGap.exec(success, fail, "ExtFunctionsPlugin", "encodeRC4", key);
}
ExtFunctionsPlugin.prototype.RC4Decode = function(key, success, fail) {
    PhoneGap.exec(success, fail, "ExtFunctionsPlugin", "decodeRC4", key);
}
ExtFunctionsPlugin.prototype.moveDir = function(dirA, dirB, success, fail) {
    PhoneGap.exec(success, fail, "ExtFunctionsPlugin", "moveDir", [dirA, dirB]);
}
ExtFunctionsPlugin.prototype.renameFile = function(pathA, pathB, success, fail) {
    PhoneGap.exec(success, fail, "ExtFunctionsPlugin", "renameFile", [pathA, pathB]);
}


PhoneGap.addConstructor(function() {
    PhoneGap.addPlugin("ExtFunctionsPlugin", new ExtFunctionsPlugin());
    PluginManager.addService("ExtFunctionsPlugin", "org.plugins.ExtFunctionsPlugin");
});
var OpenBookShelf = function() {
};
PhoneGap.addConstructor(function() {
    PhoneGap.addPlugin('OpenBookShelf', new OpenBookShelf());
    PluginManager.addService("OpenBookShelf", "org.plugins.OpenBookShelf");
});
OpenBookShelf.prototype.openBook = function(path, successCallback, errorCallback) {
    return PhoneGap.exec(successCallback, errorCallback, "OpenBookShelf", "openBook", [path]);
}
OpenBookShelf.prototype.reload = function(shelf, successCallback, errorCallback) {
    return PhoneGap.exec(successCallback, errorCallback, "OpenBookShelf", "reload", [shelf]);
}
OpenBookShelf.prototype.openApp = function(packageName, successCallback, errorCallback) {
    return PhoneGap.exec(successCallback, errorCallback, "OpenBookShelf", "openApp", [packageName]);
}
OpenBookShelf.prototype.checkAppInstall = function(packageName, successCallback, errorCallback) {
    return PhoneGap.exec(successCallback, errorCallback, "OpenBookShelf", "checkAppInstall", [packageName]);
}
var ExtractZipFilePlugin = function() {
};
PhoneGap.addConstructor(function() {
    PhoneGap.addPlugin('ExtractZipFile', new ExtractZipFilePlugin());
});
ExtractZipFilePlugin.prototype.extractFile = function(file, successCallback, errorCallback) {
    return PhoneGap.exec(successCallback, errorCallback, "ExtractZipFile", "extract", [file]);
};