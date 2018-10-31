/*global cordova, module*/

module.exports = {
    download: function (url, path, fileName, title, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "Download", "download", [url, path, fileName,title]);
    },
	downloadWithADM: function (url, path, fileName, title, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "Download", "downloadWithADM", [url, path, fileName,title]);
    },
	downloadWithADMPro: function (url, path, fileName, title, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "Download", "downloadWithADMPro", [url, path, fileName,title]);
    }
};
