/*global cordova, module*/

module.exports = {
    download: function (url, path, fileName, title, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "Download", "download", [url, path, fileName,title]);
    }
};
