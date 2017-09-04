var videoPlay;
if (!__qbGlobalVars) {
    var __qbGlobalVars = {
        iframerx: /(pptv|mp\.weixin\.qq)\.com/i,
        srcrx: /(pptv|v\.qq)\.com/i,
    };
}

function magicbridge() {
    this.sendMsg = function(msg) {
        this.send('magicvideo://msg/' + msg);
    };
    
    this.send = function(dst) {
        var bd = document.body;
        var bridge = document.createElement('iframe');
        bridge.style.cssText = "display:none;height:0px;width:0px;";
        bridge.setAttribute('frameloader', '0');
        bridge.src = dst;
        bd.appendChild(bridge);
        bd.removeChild(bridge);
    };
}

function qbLog(msg)
{
    return;
}

function magicvideo() {
    this.addOverlay = function(v, index) {
        if (v.style.display == "none") {
            return;
        }
        if (!v.magicVideoInstance.mttOverlay) {
            var parent = v.parentNode;
            if (parent && parent.style.display != "none") {
                parent.style.position = "relative";
                var overlay = document.createElement("div");
                overlay.id = "magicvideo-overlay-" + index;
                overlay.style.cssText = "width:100%;height:100%;position:absolute;top:0;background-color:rgba(0,0,0,0) !important;";
                parent.appendChild(overlay);
                overlay.addEventListener("click", function() {
                    v.play();
                }, false);
            }
            v.magicVideoInstance.mttOverlay = true;
        }
        
    };

    this.hook = function(v) {
        window.video.jsOutput('hook');
        if (v.magicVideoInstance == undefined) {
            videoPlay = v;
            var magicv = this;
            v.magicVideoInstance = magicv;
            v.orgLoad = v.load;
            v.orgPlay = v.play;
            v.is_Loaded = 0;
            v.is_Playing = 0;
            v.is_loadstart = false;
            v.addEventListener("play", function() {window.video.jsOutput('play');}, false);
            v.addEventListener("loadstart", function() {window.video.jsOutput('loadstart'); v.is_loadstart = true; v.play();}, false);
            v.addEventListener("loadeddata", function() {window.video.jsOutput('loaddata'); v.play();}, false);
            v.addEventListener("click", function() {window.video.jsOutput('click'); v.play();}, false);
            
            v.play = function() {
                if (v.style.display == "none") {
                    return;
                }
                v.isPlaying++;
                if (!v.is_loadstart && v.isLoaded > 0) {
                    v.orgPlay();
                }
                if (v.is_Loaded > 0 && v.is_Playing != v.is_Loaded) {
                    return;
                }
                if (!this.ended) {
                    this.magicVideoInstance.workaroundBefore(v);
                    this.magicVideoInstance.newPlay(v);
                    this.magicVideoInstance.workaroundAfter(v);
                }
            };
            
            v.load = function() {
                if (v.style.display == "none") {
                    return;
                }
                v.isLoaded++;
                if (this.autoplay) {
                    this.magicVideoInstance.workaroundBefore(v);
                    this.magicVideoInstance.newLoad(v);
                    this.magicVideoInstance.workaroundAfter(v);
                }
            };
            v.magicVideoInstance.workaroundInit(v);
        }
    };

    this.indexOf = function(v) {
        var vs = document.getElementsByTagName('video');
        var len = vs.length;
        for (var i = 0; i < len; ++i) {
            if (vs[i] == v) {
                return i;
            }
        }
        
        var pageUrl = document.URL;
        if (/(pptv)\.com/i.test(pageUrl)) {
            var frames = doc.getElementsByTagName("iframe");
            var flen = frames.length;
            for (var j = 0; j < flen; j++) {
                var m = frames[j];
                if (typeof(m.src) != "undefined" && /(pptv)\.com/i.test(m.src)) {
                    var conDoc = m.contentDocument;
                    var g = conDoc.getElementsByTagName("video");
                    var glen = g.length;
                    for (var index = 0; index < glen; index++) {
                        if (g[index] == v) {
                            return index;
                        }
                    }
                }
            }
        }
        return -1;
    };

    this.newPlay = function(v) {
        if (event != null && event.type != null) {
            new magicbridge().send('magicvideo://play?index=' + this.indexOf(v) + '#' + event.type);
        }

        else {
            new magicbridge().send('magicvideo://play?index=' + this.indexOf(v) + '#' + 'newPlay');
        }
    };
    
    this.newLoad = function(v) {
        if (event != null && event.type != null) {
            new magicbridge().send('magicvideo://load?index=' + this.indexOf(v) + '#' + event.type);
        } 

        else {
            new magicbridge().send('magicvideo://load?index=' + this.indexOf(v) + '#' + 'newLoad');
        }
    };

    this.workaroundInit = function(v) {
        if (location.host.indexOf('youku.com') >= 0) {
            YKU.Player.prototype.switchFullScreen = function() {};
            playerWidth = function() {
                var a = $(window).width(),
                    b = $(window).height();
                $(".yk-player .yk-player-inner").css({
                    width: a + "px",
                    height: 9 * a / 16 + "px"
                });
            };
            autoFullscreen = function() {};
            onSwitchFullScreen = function() {
                clearInterval(window.timers), setTimeout("playerWidth()", 500), setTimeout("playerWidth()", 1E3), $("body").removeClass("fullscreen"), $("body").removeAttr("style"), $(".yk-m").removeAttr("style"), playerWidth(), tabFixed.unfixed();
            };
            onPlayerCompleteH5 = function(a) {};
            onPlayerReadyH5 = function() {};
            window.youkuCheckTimer = setInterval(function() {
                clearInterval(window.timers);
            }, 500);
        }
    };

    this.workaroundBefore = function(v) {
    };
    
    this.workaroundAfter = function(v) {
        v.readyState = 4;
    };

    this.sendEvent = function(obj, type) {
        var eventObj = document.createEvent('HTMLEvents');
        eventObj.initEvent(type, false, true);
        obj.dispatchEvent(eventObj);
    };
}

function overlayCheck(v, i) {
    var magicv = v.magicVideoInstance;
    if (magicv) {
        magicv.addOverlay(v, i);
        var pageUrl = document.URL;
        if (magicv.addOverlay && (/(m\.v\.qq\.com\/live\.html)/i.test(pageUrl) || /(tv\.cntv)\.(cn|com)/i.test(pageUrl))) {
            var o = document.getElementById("magicvideo-overlay-" + i);
            o.style.zIndex = 1000;
        }
    }
}
window.video.jsOutput('0');
window.video.jsOutput('1');

function getVideoElements(force)
{
    if (force == "true" || !__qbGlobalVars.videos) {
        __qbGlobalVars.videos = document.getElementsByTagName("video");
    }
    return __qbGlobalVars.videos;
}

window.video.jsOutput('2');

function hookCheck() {
    var vs = getVideoElements(true);
    var len = vs.length;
    if (len <= 0 && __qbGlobalVars.iframerx.test(document.URL)) {
        return false;
    }

    for (var i = 0; i < len; ++i) {
        if (!vs[i].magicVideoInstance) {
            return false;
        }
    }
    return true;
}

window.video.jsOutput('3');

function hookVideo() {
    if (hookCheck()) {
        return;
    }

    var vs = getVideoElements();
    var len = vs.length;
    for (var i = 0; i < len; ++i) {
        new magicvideo().hook(vs[i]);
        overlayCheck(vs[i], i);
    }
    var pageUrl = document.URL;
    var iframerx = __qbGlobalVars.iframerx;
    if (iframerx.test(pageUrl)) 
    {
        var srcrx = __qbGlobalVars.srcrx;
        var frames = document.getElementsByTagName("iframe");
        var flen = frames.length;
        for (var j = 0; j < flen; j++) {
            var m = frames[j];
            if (typeof(m.src) != "undefined" && srcrx.test(m.src)) {
                var conDoc = m.contentDocument;
                var g = conDoc.getElementsByTagName("video");
                var glen = g.length;
                for (var index = 0; index < glen; index++) {
                    new magicvideo().hook(g[index]);
                    overlayCheck(g[index], index);
                }
            }
        }
    }
}

window.video.jsOutput('4');

function clearHookVideo() {
    window.video.jsOutput('clearhook start');
    if (typeof(__qbGlobalVars.hookId) != "undefined") {
        clearInterval(__qbGlobalVars.hookId);
        __qbGlobalVars.hookId = null;
    }
    window.video.jsOutput('clearhook finished');
}

window.video.jsOutput('5');

function tryHook() {
    window.video.jsOutput('tryhook start');
    hookVideo();
    if (!__qbGlobalVars.hookId) {
        __qbGlobalVars.hookId = setInterval("hookVideo()", 250);
    }
    
    if (!__qbGlobalVars.clearHookId) {
        __qbGlobalVars.clearHookId = setTimeout("clearHookVideo()", 3000);
    }
    window.video.jsOutput('tryhook end');
}

window.video.jsOutput('6');

function observeDOMMutation() {
    if (!__qbGlobalVars.observer) {
    	var MutationObserver = window.MutationObserver || window.WebKitMutationObserver || window.MozMutationObserver;
        var target = document.body;
        var observer = new MutationObserver(function(mutations) {
            mutations.forEach(function(mutation) {
                switch (mutation.type) {
                    case "attributes":
                    	if (mutation.target.tagName == "VIDEO") {
                        	tryHook();
                        }
                        break;
                    case "childList":
                        if (mutation.addedNodes) {
                            var nodes = mutation.addedNodes;
                            var nlen = nodes.length;
                            if (nlen <= 0) {
                                return;
                            }
                            var vs = getVideoElements(true);
                            for (var i = 0; i < nlen; i++) {
                                var node = nodes[i];
                                if (node.tagName == "VIDEO") {
                                    node.pause();
                                    new qqvideo().hook(node);
                                    node.play();
                                    var len = vs.length;
                                    for (var i = 0; i < len; i++) {
                                        if (vs[i] == node) {
                                            overlayCheck(node, i);
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                        break;
                    default:
                        break;
                }
            });
        });
        var config = {
            attributes: true,
            attributeFilter: ["src"],
            childList: true,
            subtree: true
        };
    }
}

window.video.jsOutput('7');

function MolemonplayVideoEnded() {
    if(videoPlay.dispatchEvent)
    {
        window.video.jsOutput('MolemonplayVideoEnded');
        var e = document.createEvent('HTMLEvents');
        e.initEvent("ended",true,true);
        videoPlay.dispatchEvent(e);
    }
}

clearHookVideo();

window.video.jsOutput('8');

if (typeof(__qbGlobalVars.clearHookId) != "undefined") {
    qbLog("--- clearTimeout()");
    clearTimeout(__qbGlobalVars.clearHookId);
    __qbGlobalVars.clearHookId = null;
};

window.video.jsOutput('9');

tryHook();

window.video.jsOutput('10');

if (!__qbGlobalVars.dcloaded) {
    document.addEventListener("DOMContentLoaded", observeDOMMutation, true);
    __qbGlobalVars.dcloaded = true;
};

window.video.jsOutput('11');

