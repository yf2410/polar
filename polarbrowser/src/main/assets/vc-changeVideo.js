(function(win) {
    (function(i, s, o, g, r, a, m) {
        i['GoogleAnalyticsObject'] = r;
        i[r] = i[r] || function() {
            (i[r].q = i[r].q || []).push(arguments)
        }, i[r].l = 1 * new Date();
        a = s.createElement(o),
            m = s.getElementsByTagName(o)[0];
        a.async = 1;
        a.src = g;
        m.parentNode.insertBefore(a, m)
    })(win, document, 'script', 'https://www.google-analytics.com/analytics.js', 'ga');
    ga('create', 'UA-72572504-4', 'auto');
    ga('send', 'pageview');
    if (win.jsFlage) {
        return;
    }
    var oBody = document.getElementsByTagName("body")[0];
    win.jsFlage = true;

    var clearTimes = {
        playerCtrl: "",
        judgeScreenTime: "",
        solveBlackScreen: "",
        displayTime: "",
    }
    var videoObj = {
        parentEle: "",
        playingVideo: "",
        controls: "",
        controlProg: "",
        progress: "",
        ball: "",
        playBtn: "",
        pauseBtn: "",
        currentTime: "",
        duration: "",
        src: "",
        fullScreen: "",
        bigPlayBtn: "",
        bigPauseBtn: "",
        bigReplayBtn: "",
        loadAni: "",
        systemVer: SystemAndVer(),
        firstPlay: true,
        loadStartTime: 0,
        loadTimeFlag: 0,
        locationOfBar: 0,
        isEnd: false,
        exitFullScreenStation: false,
        exitFullScreenDisplay: 1,
    };
    var disctance = {};
    //将样式作为一个字符串添加
    var loadAniStyle = ".loadAni{position:absolute;top:50%;left:50%;margin:-15px 0 0 -15px;height:30px;width:30px;background:url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAHgAAAB5CAMAAADF5hVFAAACzVBMVEUAAAAAAAAAAAAAAAADAwMBAQEBAQECAgIAAAAAAAABAQEBAQECAgIAAAAAAAABAQEAAAD////////////////7+/v////////////////////////////////////+/v7////////////////////4+Pj////////////////////////////////////+/v7+/v7////////////////////////////////////////////////////////////+/v7///////9ERET+/v7////////////////////////////////////////////////////////////////////////////9/f3///////9tbW3////////////////////////////////////////////+/v7////+/v7///////////////////////////////////9cXFz///8hISFfX187Ozv8/Pz////////////+/v739/f////////////////////////V1dUlJSWwsLBra2v///8eHh5ra2v////8/Pz+/v75+fny8vL////////c3NzKysqwsLDNzc2QkJD////////7+/v////////////b29ve3t7p6env7+/n5+eNjY26uro4ODje3t5ISEiAgIClpaWMjIyfn5/////////5+fn29vb29vb////19fXn5+f////z8/Pj4+P////l5eWcnJxzc3PBwcGcnJzKysp3d3e1tbWampr4+Pjo6Oj5+fnf39/Ly8vy8vLJycnJycm/v7/Ly8unp6e7u7vd3d2Hh4fGxsZXV1c/Pz9LS0v09PTv7+/y8vLs7Ozm5ub7+/vp6enz8/Px8fHw8PC5ubnY2Nju7u7e3t7W1tadnZ3n5+fw8PDo6Oj29vb09PTPz8/d3d3x8fGysrKWlpbq6ur4+Pjn5+fZiN9BAAAA73RSTlMAAQQGAgkDCxIVDhAdDBcbGQXw6xEK1vr1zcvCsJQ1D+nYfmtVDOPa0ci+qZ0yMCoa8u3h396zeRUTmXZlXlo9LiwmJiTQxLyijFhRRh4b5dOkj4mFfG9KSUA4LSAH/Pi6t7WrloJORUE6+fSfc3JdUjsoJyIcFRAD9+etnJuRh4Fj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) no-repeat top center;background-size:100% 100%;z-index:15;animation:loadingRota 1s linear both infinite;-webkit-animation:loadingRota 1s linear both infinite;transform-origin:center center}@-webkit-keyframes loadingRota{0%{transform:rotate(0);-webkit-transform:rotate(0)}25%{transform:rotate(90deg);-webkit-transform:rotate(90deg)}50%{transform:rotate(180deg);-webkit-transform:rotate(180deg)}75%{transform:rotate(270deg);-webkit-transform:rotate(270deg)}100%{transform:rotate(360deg);-webkit-transform:rotate(360deg)}}@keyframes loadingRota{0%{transform:rotate(0);-webkit-transform:rotate(0)}25%{transform:rotate(90deg);-webkit-transform:rotate(90deg)}50%{transform:rotate(180deg);-webkit-transform:rotate(180deg)}75%{transform:rotate(270deg);-webkit-transform:rotate(270deg)}100%{transform:rotate(360deg);-webkit-transform:rotate(360deg)}}";
    loadAniStyle += ".controllers{position:absolute;left:0;right:0;bottom:0;height:25%;max-height:50px;background:url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA8AAAB3CAMAAADreerUAAAARVBMVEUAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAADc6ur3AAAAF3RSTlNZUwYCTwwVEEtARCUcIDssNDAISDcpGSX24U0AAADESURBVDjL7YrbEoIwDEQjYOWipVTk/z/VJe2MrnUGoi8+eNrs5nQqNfO1V3UFsKUWhG654YTdYxXjk0dm04+ZmApOmP3EfOjOYdYWR9h9WRa3OIfUghOFnxmzX5j9PiQfGLPfmDc+4mKQKBmZTZ8yYyo4UXpYTwgTAkggSp/nvM1hxg4nCvepvdfx4ok93qEQnSIdYfeeMftV6fuccKLwtkW2AAHgxC5vNLSkYex+0EbpIocHeIIzZn89rHCgSeR/f36NOxVJNRaa9LCzAAAAAElFTkSuQmCC) no-repeat top center;background-size:100% 100%;z-index:16}.controlProg{height:4px;background:rgba(255,255,255,0.3);margin:0 0 4px;position:relative}.buffered{height:100%;width:100%}.progress{display:block;background:#fff;width:0;height:100%}.ball{height:28px;width:28px;position:absolute;top:50%;left:1%;margin:-14px 0 0 -14px;background:url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAEAAAABACAMAAACdt4HsAAAAilBMVEUAAAAAAAAAAAAAAAACAgIAAAAAAAAAAAAEBAQBAQEAAAADAwMAAAD///8EBAT8/PwAAAD9/f34+Pj6+vrm5ubFxcWtra2BgYG4uLgAAAAAAAD9/f3y8vL09PTv7+/19fX19fX6+vrY2Njg4ODq6uqZmZmvr6/Y2NjGxsbU1NTZ2dnz8/Pz8/P////mOikiAAAALXRSTlMAAwcLDhcRHRQmGysh+jTsJPXiz5iAa08yLy7d19XSy8rIpHBrZ1xTUUxJkZAS8PcdAAABrUlEQVRYw+2W2W7CMBBF2xInNs4GIQtladmX1v//ex07UwRNPDFIrVQp9w2Jc3LHsRw/9enT5x/k+ZLH6Ns8gL9c5U4F0oNL0HHX0wE7f66SKEpWH2f44d5C48B725m6ZLb1Bq4l8PFVpm6SVVjCjfdOifqR5OQ5GWr+EKlGokNtcOGrkWrJqOo2QEPgeaZak3EwwF+6CvgbZcnG76pgBuBjm2DMzRBUAeD9o7Lm6JshaAGf2AUTTgvMCrA3u+CdmVWgC7DELkgYVqAEw8guiIadAp8FlCBgfreAGsFJQC2ik4B6jU6CvV2wdxII+1YWIOh+jWluE+Tp0GEfsFAu2vmFDBkKyK0ciLL9QClF8L2V6RlCUbQdaYUIcQJaABVSWTQ6jAqZQgFaADNgBVnOb/l5KbGAnoCsAKugDXE+vnp/eax5WAEsQB+qtWEa79ZL/WlbrnfxtObxUKUroCHVCgzgKfKmgJMhCIWQ8hUipRBh0M2jAA1cK1Jhkmqca97942gU4NAB2uBNnjaAgjMTDriFpy8Y4DAB2uCQP7jioKF5yYL88jWPvmj26UPnC1hIdsbwHtvkAAAAAElFTkSuQmCC) no-repeat center center;background-size:100%}.controlPlay,.controlPause{float:left;height:80%;width:10%;max-width:40px;max-height:40px}.controlPlay{background:url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAGAAAABgBAMAAAAQtmoLAAAAElBMVEUAAAD///////////////////8+Uq06AAAABXRSTlMA7pEGicokfn8AAABISURBVFjD7dahDQAgAANBVkAwAWEBNiGh+6+Cr6jBkPDnKt63AMDjZu0+s6HtM2taPjNJNgkICAgICAj+CcJ1uDkn+f4AwNsODahbaaeZKyYAAAAASUVORK5CYII=) no-repeat center center;background-size:100%}.controlPause{background:url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAGAAAABgCAMAAADVRocKAAAAaVBMVEUAAAD////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////8G612AAAAInRSTlMAYvr21eaEDPPOxa6lmkpAMyUeGQTs6+HXt4x7alk4KhAJn0Q7UQAAAMdJREFUaN7t10kOwjAQRNHOwJSBjJAECIG6/yGRAGHYwCa1MKp3gS9ZctttIiIiIiJ+KOMoq42oBxAtV0aDu0VgLHjaFMaBl7QyBjjh4WLzw7v9qeUEnHhLCjhJSQo4eUMOYH28cgJON5ICTnImBZyspgZmm4H4ZhG0hMDMFw+/DBM5gDAgB7DzPcA+orQyZmBTkC+a16Miq30e193o85MZkh/9YfL54/X4Onr7+f2Yat4tIMQVqgewZi6BZRzljYmIiIiI/IMbEg1q9v3DbloAAAAASUVORK5CYII=) no-repeat center center;background-size:100%;display:none}.currentTime,.duration{float:left;height:80%;max-height:40px;color:#fff}.fullScreen{float:right;height:80%;width:10%;max-width:30px;max-height:30px;margin:3px 10px 0 0;background:url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAFoAAABaBAMAAADKhlwxAAAAG1BMVEUAAAD////////////////////////////////rTT7CAAAACHRSTlMA+VPmSU3k6GESMAsAAADiSURBVFjD7dMxCsJAFEXRj5oNpLLUHaQQSRnIBrIHswC3IAH5y7aSO917STXFvPpwCWR+tLVVsjnZNik9ZrFB6Tysv5Oh+2Baf2479JW4oe/EDX0ibugg7mjijibuaOKOJu5p4loT9zRxrYm7mrjWxLUm7mriWhPXmvhiaOKD0uMWxN9Kz2v8172WaKt73XPh1z2UPmdfPAuteeeZSne8c6U5Iq05IrQfR+s4WsfRdhyt42gdR5txtI6jzThax9FWHO3E0VYc7cTRThztxNFOXOvLmMX0zR/W264vWaOtrY79AMIgkfDQAmzcAAAAAElFTkSuQmCC) no-repeat center;background-size:100%}.clear{height:0;width:0;clear:both}";
    loadAniStyle += ".shareBtn{float:right;margin:3px 10px 0 0;;height:80%;width:10%;max-width:30px;max-height:30px;background:url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAFoAAABaCAMAAAAPdrEwAAAAeFBMVEUAAAD////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////GqOSsAAAAJ3RSTlMA+dUe8wK5bwbkpyhsRHhYIXL6dytpC/bLTt6RgjYXr1ChnFwQXk9i4Jb6AAABrElEQVRYw+2Y2XKDMAxFC2Ex+xJCKFvWVv//h0VjZwolfamlKZnxebMebkBgn4g3g8Fg+B92qRdYju2lOXVwWMKDcEeZ/O7ADCejS/bhBwVVcgaIXURCRIUNSEbUZ+zG8eDKlRtb2BOafodTlJV8rxPMDkkuGibieSXGCsVl77HP7rziYr/3BNHelOOv3xiPIDqYcqJlKZpKAUE0PjSxLAl8sDzRNUbzNKTChmz7MaZsL1/erLbMASvax7YojgAsG71qAKE/nkQv1SLzbT8SdeWTHKqDTHFu1CqoO2XC8YnAtGydtIAE54d2YaZdHVvfr3J5vS9+viwD/HkdW58DQNqE2tajuvmuJre1I0sDg62RshcMtkaaisvWvTC2NrZ+YVszvnw5i62fb/Qek0OWYQKQVFCNQEtbI+3NZRjcFPbnL/7UEdhJLZtEd9pd23rslA+8QXfaXds6v6iMU0U/7VYnlXHJ6afdwZPZVjfST7tJozprM/wJPsubZ5l23Y8WU3iOMBEHwHbw1o7xp/HnZvwpN/rWP8Y982e2/Q+fXJ9rNabdP/jTYDAYyPkCOTd5r1LG4kcAAAAASUVORK5CYII=) no-repeat center;background-size:100%}";
    loadAniStyle += ".loadBtn{display:none;float:right;margin:3px 10px 0 0;;height:80%;width:10%;max-width:30px;max-height:30px;background:url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAFoAAABaBAMAAADKhlwxAAAAKlBMVEUAAAD///////////////////////////////////////////////////+Gu8ovAAAADXRSTlMA/OdeVmRfTkn0voQ7zfljRAAAAPFJREFUWMPt1bEJwkAUxvGgwahgJdgFwk0QN4gbuICgG2QER7C2snUIRxAMkkrfLj654vNy3AvXqbyvS+7HvwlcEp3uDzY2+wg9oCJCz+imWrVq1V+rM1M6OjW1eP+1jt5RIWpaf+hRLuo0pxaa002VCFtx3GqbXoQp4lYvqdkGJeJWD5EW41ZzuhT+NIizfqfnOPP/NIizRhpnzrdGnJ+QxpmjEecnmxY04hu6Iy1rjhM1nJY14jykBY142aMRR1rQiCMtaMSR7teZqSI0T3WkntAzqK906LyZkrRTR2ei9u7Oi4Af/mV8DuJjneh0P7kXPRoqmr2EKCgAAAAASUVORK5CYII=) no-repeat center;background-size:100%;}"
    loadAniStyle += ".bigPlayBtn,.bigPauseBtn,.bigReplayBtn{display:none;position:absolute;top:50%;left:50%;z-index:10;margin:-30px 0 0 -30px;height:60px;width:60px;background-repeat:no-repeat;background-position:center center;background-size:100%;}";
    loadAniStyle += ".bigPlayBtn{background-image:url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAALQAAAC0CAMAAAAKE/YAAAAAb1BMVEUAAAAAAAC8vLywsLAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAD6+vqgoKD09PTc3Nyurq6BgYF2dnYzMzMxMTEAAAD////n0XiKAAAAJHRSTlNmALeuJmRgWVRQQSAaDklFMgsEPV0UNixLBvij79Stko50czXo56WqAAAD3UlEQVR42uzW6W7rIBCG4a89YDCLjZdsTXf5/q/xuFmapUnqAHEYqe9vS/MIYTR4iFllmGvUXIo2y4Asa4Wcq8YxU0UdEwu90K6UHGfjUjmzCBgQG22ZEhiUUMwGjwtH17oRuCrR6Np/Xjh6wQoOj3jB/G5KOFqXHN7xUnuMDETbaYvA2qnH/Q5AmwJRKozHcD80k4iWZB6A69FMIGpiADsQrSWiJ/XDrvjoSY6blE92MyKjK4WbpXbbSVT0LMMNy2bB6PCbEX5HwtGO4+ZxFxVtc4xSbuOhWYaRylgkdN1gxJo6BrrKMWp5FY42LUauNaFoxjF6nIWhHe6SC0E3uFONP7rE3So90XWBO1bUF9CJmoF57YEucOeK8+gE7/O28lq0QgKp69BTJNH0GjRDIrHhaMORSNwMRdsWydTaYehaIqFkfQKd0MIxeA1Buj/hNvY72mZIrMz+is6RXPkPdBpb/+XcZfQkmRd6Pz45Qid/Ob7KL6FnSLTZeXSV3MuxLasO0Mnto6dT59ATJNxkH53+X7guP43WSDp9Ep3UcvczuYdOd1E6jp1ACySe2KHJHPTeUSP46Xh/XX5gcB/L13d4lh+jDTx76bru6Q0De3vqP3+BZ+YIXcCvx+6rp08M6rM39z3Cr+IQbeHZc7dqiUEtu1XP8MweoKfwrOuuObt/3Tp4Nj1AtzTQ7T5agwYaeg9dUkGXO/SCU0HzxTeagQoa7Btd0EEXW3TN6aB5vUFr0EFDb9ANJXSzQQtKaLFGW1BCw67QM1ro2QqtaKHVCi1oocUXugItNKoebaihdY921NCuRytq6LJHS2po2aM5NTR/QAVqaFQw9NAGjB6awdFDOzT00A1KeugSc3roOSQ9tISghxbI6KFbiugM+EOPgsYf+n95d47DIBAEUdQBYpkE2XJAzv3v6MBCJARIbP165gQIzdpdVf+u6WEuRPCjv+bhQh7j5IWJvJqSjwDyuUU+bMkSAlmsIctiZgGSLPWSRXWyfUE2isiWnNn8JNvMZEOflE6YIhVSDkQKr0iJmykmJGWbpEDWlCKTom9TXk8aGUjLiGnOIW1QpuGMtPbF/9VNFruqaQwmLdim2Z2MFTADHMiojLBrccgX/0IG7YScIEPO8Kh4F6eNmK6gSYrrKHmj58iQPzNOMdBibLJHhAbZQkoNsbcRAoY/lUQ5k6HZZjz5k3tIORK5P78eGXN9cAMTI2ECO0w0igmhQXE/JlgJRViZsDAUy2YC8FDUIAp1RPGZKKgURcKi8F0Vc4wCpVV09zLeOyDpp/HdL8DRj1PftX8cfdv103g+jv4HzyXJ2qUtmxsAAAAASUVORK5CYII=)}"
    loadAniStyle += ".bigPauseBtn{background-image:url(data:image/png;base64,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)}"
    loadAniStyle += ".bigReplayBtn{display:none;background-image:url(data:image/png;base64,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)}";
    if(win.JSInterfaceManager.getAppVersionCode()>57){
        loadAniStyle +=".loadBtn{display:block}";
    }
    /*全屏后的显示逻辑*/
    // loadAniStyle += "video::-webkit-media-controls-enclosure {display: none !important;}";
    // loadAniStyle += ".fullScreenCtrl{height:25%;min-height:28px;max-height:50px;position:fixed;left:0;right:0;bottom:0;z-index: 2147483700;border:1px solid red;}"
        //
        // loadAniStyle += "video::-webkit-media-controls{display:-webkit-box;display: -webkit-flex;display: flex;-webkit-box-orient:vertical;-webkit-box-direction:normal;flex-direction:column;-webkit-flex-direction:column;-webkit-justify-content:flex-end;justify-content: flex-end;align-items: center;-webkit-align-items:center;box-align:center;}"
        // loadAniStyle += "video::-webkit-media-controls-overlay-enclosure {display: flex;position: relative;flex-direction: column;justify-content: flex-end;align-items: center;min-height: 0px;width: 100% ;margin-bottom: 10px;text-indent: 0px;box-sizing:border-box;flex: 110% ;overflow: hidden;}";
        // loadAniStyle += "video::-webkit-media-controls-enclosure {width: 100%; height: 32px;flex-shrink: 0;bottom: 0px;text-indent: 0px;box-sizing: border-box;padding: 0px;margin: 0px;}"
        // loadAniStyle += "video::-webkit-media-controls-panel{height:48px;min-width:48px;line-height:48px;font-size:14px} div{display:block;}";
        // loadAniStyle += "video::-webkit-media-controls-panel{display:flex;flex-direction:row;align-items:center;justify-content:flex-start;user-select:none;position:relative;width:100%;z-index:0;text-align:right;bottom:auto;height:32px;min-width:48px;line-height:32px;background-color:#fafafa;font-size:12px;font-weight:400;font-style:normal;overflow:hidden;transition:opacity .3s}"

    var styleEle = document.createElement("style");
    styleEle.setAttribute("type", "text/css");
    styleEle.innerText = loadAniStyle;
    document.getElementsByTagName("head")[0].appendChild(styleEle);
    oBody.addEventListener("click", function(e) {
        var e = e || win.event;
        var target = e.target || e.srcElement;
        var parentEle = target.parentNode;
        // var oVideo = siblings(target, "video")
        // if (oVideo != "") {
        //     videoObj.playingVideo = oVideo;
        // }
        // console.log(target);
        if (target.ClassName != "") {
            var targetClassName = target.className;
        }
        switch (targetClassName) {
            case "controlPlay":
                videoObj.playingVideo.pause();
                return;
            case "controlPause":
                videoObj.playingVideo.play();
                return;
            case "fullScreen":
                H5RequestFullScreen(videoObj.playingVideo);
                ga('send', 'event', '视频播放模块', "页面内", "全屏按钮");
                return;
            case "shareBtn":
                ga('send', 'event', '视频播放模块', "页面内", "分享按钮");
                win.JSInterfaceManager.shareVideo(videoObj.src);
                return;
            case "loadBtn":
                ga('send', 'event', '视频播放模块', "页面内", "下载按钮");
                win.JSInterfaceManager.downloadVideo(videoObj.src,"vidoe/mp4");
                // console.log(videoObj.src);
                return;

            case "bigPlayBtn":
                videoObj.playingVideo.pause();
                return;
            case "bigPauseBtn":
                videoObj.playingVideo.play();
                return;
            case "bigReplayBtn":
                videoObj.isEnd = false;
                videoObj.progress.style.width = "0";
                videoObj.ball.style.left = "1%";
                ga('send', 'event', '视频播放模块', "页面内", "重播按钮");
                videoObj.playingVideo.play();
                return;
        }
        if (target.nodeName.toLowerCase() == "video") {
            if (win.getComputedStyle(videoObj.controls, "").display == "none") {
                if (target.paused) {
                    videoObj.bigPlayBtn.style.display = "none";
                    videoObj.bigPauseBtn.style.display = "block";
                } else {
                    videoObj.bigPauseBtn.style.display = "none";
                    videoObj.bigPlayBtn.style.display = "block";
                }
                videoObj.controls.style.display = "block";
            } else {
                videoObj.controls.style.display = "none";
                videoObj.bigPlayBtn.style.display = "none";
                videoObj.bigPauseBtn.style.display = "none";
            }
            clearTimeout(clearTimes.displayTime);
            clearTimes.displayTime = setTimeout(function() {
                videoObj.controls.style.display = "none";
                videoObj.bigPlayBtn.style.display = "none";
                videoObj.bigPauseBtn.style.display = "none";
            }, 2000)
            e.preventDefault();
            e.stopPropagation();
            return;
        }

        if ((parentEle.className.indexOf("_53mw") > -1) && (target.className != "controllers") && (target.className != "loadAni")) {
            ga('send', 'event', '视频播放模块', "页面内", "播放按钮");
            videoObj.loadStartTime = new Date().getTime();
            removeAddEle();
            setTimeout(function() {
                if (siblings(target, "video") != "") {
                    videoObj.playingVideo = siblings(target, "video");
                }
                videoObj.firstPlay = true;
                videoObj.parentEle = parentEle
                var loadAni = document.createElement("div");
                loadAni.className = "loadAni";
                loadAni.style.display = "block";
                videoObj.parentEle.appendChild(loadAni);
                videoObj.loadAni = loadAni;
                var bigPlayBtn = document.createElement("div");
                bigPlayBtn.className = "bigPlayBtn";
                // loadAni.style.display = "none";
                var bigPauseBtn = document.createElement("div");
                bigPauseBtn.className = "bigPauseBtn";
                bigPauseBtn.style.display = "none";
                var bigReplayBtn = document.createElement("div");
                bigReplayBtn.className = "bigReplayBtn";
                bigReplayBtn.style.display = "none";
                videoObj.parentEle.appendChild(bigPlayBtn);
                videoObj.parentEle.appendChild(bigPauseBtn);
                videoObj.parentEle.appendChild(bigReplayBtn);
                // 生成控制条
                var controls = document.createElement("div");
                controls.className = "controllers";
                controls.innerHTML = '<div class="controlProg"><span class="progress"></span><span class="ball"></span></div><p class="controlPlay"></p><p class="controlPause"></p><p class="currentTime">00:00</p><p class="duration">/00:00</p><p class="fullScreen"></p><p class="shareBtn"></p><p class="loadBtn"></p><div class="clear"></div>';
                parentEle.appendChild(controls);
                videoObj.controls = controls;
                videoObj.controlProg = children(controls, ".controlProg");
                videoObj.ball = children(videoObj.controlProg, ".ball");
                videoObj.progress = children(videoObj.controlProg, ".progress");
                videoObj.playBtn = children(controls, ".controlPlay");
                videoObj.pauseBtn = children(controls, ".controlPause");
                videoObj.currentTimeEle = children(controls, ".currentTime");
                videoObj.durationEle = children(controls, ".duration");
                videoObj.fullScreen = children(controls, ".fullScreen");
                if (videoObj.systemVer.version < "4.5") {
                    videoObj.fullScreen.style.display = "none";
                }
                videoObj.bigPauseBtn = bigPauseBtn;
                videoObj.bigPlayBtn = bigPlayBtn;
                videoObj.bigReplayBtn = bigReplayBtn;
                var lineHeiVal = videoObj.currentTimeEle.clientHeight;
                videoObj.currentTimeEle.style.lineHeight = lineHeiVal + "px";
                videoObj.durationEle.style.lineHeight = lineHeiVal + "px";
                // videoObj.controls.style.display="none";
                videoObj.playingVideo.removeAttribute("controls");

                videoObj.playingVideo.setAttribute("poster", "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAIAAAACCAQAAADYv8WvAAAABGdBTUEAALGPC/xhBQAAACBjSFJNAAB6JgAAgIQAAPoAAACA6AAAdTAAAOpgAAA6mAAAF3CculE8AAAAAmJLR0QA/4ePzL8AAAAHdElNRQfgDBsONSOSsBJzAAAAEklEQVQI12P8z8zAwMTAwMAAAAktAQY3lak9AAAAJXRFWHRkYXRlOmNyZWF0ZQAyMDE2LTEyLTI3VDE0OjUzOjAzKzA4OjAwAxT+mQAAACV0RVh0ZGF0ZTptb2RpZnkAMjAxNi0xMi0yN1QxNDo1MzozNSswODowMJ8WdPwAAAAASUVORK5CYII=");
                // 添加video的监听事件
                videoObj.playingVideo.addEventListener("play", function(e) {
                    videoObj.src = this.src;

                    if ((videoObj.exitFullScreenDisplay == 0) || videoObj.isEnd) {
                        return;
                    }
                    videoObj.pauseBtn.style.display = "none";
                    videoObj.bigReplayBtn.style.display = "none";
                    videoObj.playBtn.style.display = "block";
                    videoObj.bigPauseBtn.style.display = "none";
                    videoObj.bigPlayBtn.style.display = "block";


                    clearTimeout(clearTimes.playerCtrl);
                    if (videoObj.firstPlay) {
                        controls.style.display = "none";
                        videoObj.bigPlayBtn.style.display = "none";
                    } else {
                        clearTimes.playerCtrl = setTimeout(function() {
                            controls.style.display = "none";
                            videoObj.bigPlayBtn.style.display = "none";
                        }, 2000);
                    }

                    videoObj.firstPlay = false;
                    videoObj.loadTimeFlag = 0;
                    videoObj.isEnd = false;
                    e.preventDefault();
                    e.stopPropagation();
                }, false);
                videoObj.playingVideo.addEventListener("pause", function(e) {
                    if ((videoObj.exitFullScreenDisplay == 0) || videoObj.isEnd) {
                        return;
                    }
                    videoObj.playBtn.style.display = "none";
                    videoObj.pauseBtn.style.display = "block";
                    videoObj.bigPlayBtn.style.display = "none";
                    videoObj.bigPauseBtn.style.display = "block";
                    e.preventDefault();
                    e.stopPropagation();
                }, false);

                videoObj.playingVideo.addEventListener("timeupdate", function(e) {
                    if (videoObj.loadTimeFlag == 0) {
                        var canplayTime = new Date().getTime();
                        var loadTimeDuration = parseInt((canplayTime - videoObj.loadStartTime) / 1000);
                        ga('send', 'event', '视频播放模块', "视频加载时长", statisticLoadTime(loadTimeDuration));
                        videoObj.loadTimeFlag++
                    }

                    if (this.buffered.end(this.buffered.length - 1) - this.currentTime > 2) {
                        loadAni.style.display = "none";
                    } else if (this.duration.toFixed(2) != this.buffered.end(this.buffered.length - 1).toFixed(2)) {
                        loadAni.style.display = "block";
                    }

                    videoObj.duration = parseInt(this.duration);
                    videoObj.durationEle.innerHTML = "";
                    videoObj.durationEle.innerHTML = "/" + timeStyle(videoObj.duration);
                    var currentTime = parseInt(this.currentTime);
                    var percentVal = (currentTime / videoObj.duration) * 100;
                    videoObj.progress.style.width = percentVal + "%";
                    if ((currentTime / videoObj.duration) * 100 > 1) {
                        if ((currentTime / videoObj.duration) * 100 > 99) {
                            percentVal = 99;
                        }
                        videoObj.ball.style.left = percentVal + "%";
                    }

                    videoObj.currentTimeEle.innerHTML = "";
                    videoObj.currentTimeEle.innerHTML = timeStyle(currentTime);
                    e.preventDefault();
                    e.stopPropagation();
                }, false);
                videoObj.playingVideo.addEventListener("ended", function(e) {
                    videoObj.loadAni.style.display = "none";
                    videoObj.bigPauseBtn.style.display = "none";
                    videoObj.bigReplayBtn.style.display = "block";
                    videoObj.isEnd = true;
                    e.preventDefault();
                    e.stopPropagation();
                }, false);
                clickOrDrag(controls, videoObj.playingVideo);
            }, 50);
        }
        // e.preventDefault();
        // e.stopPropagation();
    }, false);
    //选取兄弟元素
    function siblings(elm, select) {
        if (arguments.length == 0) {
            return;
        }
        if ((typeof elm) != "object") {
            return;
        }
        if (elm != null && elm != undefined) {
            var a = [];
            var sele = "";
            var p = elm.parentNode.children;
            for (var i = 0, pl = p.length; i < pl; i++) {
                if (p[i] !== elm) a.push(p[i]);
            }
        }
        if ((arguments.length == 2) && (select != null && select != undefined)) {
            var selEle = "";
            if (select.indexOf(".") > -1) {
                var clsssName = select.slice(1)
                selEle = elm.parentNode.getElementsByClassName(clsssName)[0];
            } else if (select.indexOf("#") > -1) {
                var idName = select.slice(1);
                selEle = elm.parentNode.getElementById(idName);
            } else {
                selEle = elm.parentNode.getElementsByTagName(select)[0];
            }
            for (var aLen = 0; aLen < a.length; aLen++) {
                if (a[aLen] == selEle) {
                    sele = a[aLen];
                }
            }
            if (sele != "") {
                return sele;
            }

        }
        return a;
    }

    // 选取子元素
    function children(par, select) {
        if (arguments.length == 0) {
            return;
        }
        if ((typeof par) != "object") {
            return;
        }
        if (par != null && par != undefined) {
            var a = par.children;
        }
        if ((arguments.length == 2) && (select != null && select != undefined)) {
            var selEle = "";
            if (select.indexOf(".") > -1) {
                var clsssName = select.slice(1)
                selEle = par.getElementsByClassName(clsssName)[0];
            } else if (select.indexOf("#") > -1) {
                var idName = select.slice(1);
                selEle = par.getElementById(idName);
            } else {
                selEle = par.getElementsByTagName(select)[0];
            }
            for (var aLen = 0; aLen < a.length; aLen++) {
                if (a[aLen] == selEle) {
                    sele = a[aLen];
                }
            }
            if (sele != "") {
                return sele;
            }
        }
        return a;
    }

    function removeAddEle() {
        var oLoadAni = document.querySelectorAll(".loadAni");
        var ocontrol = document.querySelectorAll(".controllers");
        var bigPlay = document.querySelectorAll(".bigPlayBtn");
        var bigPause = document.querySelectorAll(".bigPauseBtn");
        var bigRplay = document.querySelectorAll(".bigReplayBtn");
        if (oLoadAni.length == 0) {
            return
        }
        var elePar = oLoadAni[0].parentNode;
        for (var eleLen = 0; eleLen < ocontrol.length; eleLen++) {
            if (ocontrol[eleLen] != null && ocontrol[eleLen] != undefined) {
                elePar.removeChild(ocontrol[eleLen]);
            }
            if (oLoadAni[eleLen] != null && oLoadAni[eleLen] != undefined) {
                elePar.removeChild(oLoadAni[eleLen]);
            }
            if (bigPlay[eleLen] != null && bigPlay[eleLen] != undefined) {
                elePar.removeChild(bigPlay[eleLen]);
            }
            if (bigPause[eleLen] != null && bigPause[eleLen] != undefined) {
                elePar.removeChild(bigPause[eleLen]);
            }
            if (bigRplay[eleLen] != null && bigRplay[eleLen] != undefined) {
                elePar.removeChild(bigRplay[eleLen]);
            }
        }
    }

    function clickOrDrag(controls, videoEle) {
        var whiteProg = videoObj.progress;
        var whiteBall = videoObj.ball;
        videoObj.controlProg.addEventListener("touchstart", function(e) {
            // console.log("touchstart");
            var touch = e.touches[0];
            disctance.oldAddrX = touch.clientX;
            disctance.oldAddrY = touch.clientY;
            disctance.newAddrX = touch.clientX;
            disctance.newAddrY = touch.clientY;
            clearTimeout(clearTimes.displayTime);
            e.stopPropagation();
            e.preventDefault();
        }, false);

        videoObj.controlProg.addEventListener("touchmove", function(e) {
            // console.log("touchmove");
            var touch = e.touches[0];
            disctance.newAddrX = touch.clientX;
            disctance.newAddrY = touch.clientY;
            
            e.stopPropagation();
            e.preventDefault();
        }, false)

        videoObj.controlProg.addEventListener("touchend", function(e) {
            // console.log("touchend");
            var disY = Math.abs(disctance.newAddrY - disctance.oldAddrY);
            var disX = disctance.newAddrX - disctance.oldAddrX;
            var angle = parseInt(Math.atan(disY / Math.abs(disX)) * 360) / (2 * Math.PI);
            if (angle > 10) {
                return false;
            }
            var progLeft = videoObj.controlProg.offsetLeft;
            var progWidth = videoObj.controlProg.clientWidth;
            var progEnd = progLeft + progWidth;

            if (disX > 0) {
                if (disctance.newAddrX > progEnd) {
                    videoEle.currentTime = videoObj.duration;
                    whiteProg.style.width = "100%";
                    whiteBall.style.left = "99%";
                } else {
                    var newAddr = ((disctance.newAddrX - progLeft) / progWidth) * 100;
                    videoEle.currentTime = parseInt((videoObj.duration * newAddr) / 100);
                    // whiteProg.css("width", newAddr + "%");
                    // whiteBall.css("left", newAddr + "%");
                    whiteProg.style.width = newAddr + "%";
                    whiteBall.style.left = newAddr + "%";
                }
            } else if (disX == 0 && disY < 10) {
                var newAddr = ((disctance.newAddrX - progLeft) / progWidth) * 100;
                videoEle.currentTime = parseInt((videoObj.duration * newAddr) / 100);
                whiteProg.style.width = newAddr + "%";
                whiteBall.style.left = newAddr + "%";
            } else {
                if (disctance.newAddrX < progLeft) {
                    videoEle.currentTime = 0;
                    whiteProg.style.width = "1%";
                    whiteBall.style.left = "1%";
                } else {
                    var backAddr = ((disctance.newAddrX - progLeft) / progWidth) * 100;
                    videoEle.currentTime = parseInt((videoObj.duration * backAddr) / 100);
                    // whiteProg.css("width", backAddr + "%");
                    whiteProg.style.width = backAddr + "%";
                    whiteBall.style.left = backAddr + "%";
                    // whiteBall.css("left", backAddr + "%");
                }
            }
            clearTimeout(clearTimes.displayTime);
            clearTimes.displayTime = setTimeout(function() {
                videoObj.controls.style.display = "none";
                videoObj.bigPlayBtn.style.display = "none";
                videoObj.bigPauseBtn.style.display = "none";
            }, 2000)
            e.stopPropagation();
            e.preventDefault();
        }, false)
    }
    /*
    H5RequestFullScreen(thisVideo)全屏函数，全屏时取消对手机自带控制条的限制
    */
    function H5RequestFullScreen(thisVideo) {
        videoObj.locationOfBar = win.scrollY;
        // videoObj.controls.className+=" fullScreenCtrl";
        if (thisVideo.requestFullscreen) {
            thisVideo.requestFullscreen();
        } else if (thisVideo.mozRequestFullScreen) {
            thisVideo.mozRequestFullScreen();
        } else if (thisVideo.webkitRequestFullScreen) {
            thisVideo.webkitRequestFullScreen();
        } else if (thisVideo.msRequestFullscreen) {
            thisVideo.msRequestFullscreen();
        } else if (thisVideo.oRequestFullscreen) {
            thisVideo.oRequestFullscreen();
        }
        fullOrNot(thisVideo);
    }

    function timeStyle(getDuration) {
        if (getDuration < 10) {
            getDuration = "00:0" + getDuration;
            return getDuration
        } else if (getDuration < 60) {
            getDuration = "00:" + getDuration;
            return getDuration
        } else {
            var getMin = parseInt(getDuration / 60);
            var second = parseInt(getDuration % 60);
            if (getMin < 10) {
                getMin = "0" + getMin;
            }
            if (second < 10) {
                second = "0" + second;
            }
            getDuration = getMin + ":" + second;
            return getDuration;
        }
    }
    // 全屏

    win.fullScreenShare = function() {
            win.JSInterfaceManager.fullscreenShareVideo(videoObj.src);
        }
     win.fullScreenDownload=function(){
           win.JSInterfaceManager.downloadVideo(videoObj.src,"vidoe/mp4");    
    }
    function SystemAndVer() {
        var u = win.navigator.userAgent;
        var brower = {
            versions: function() {
                var num;
                var core = "";
                if (u.indexOf('Trident') > -1) {
                    //CuPlayer.com提示：IE
                    core = "ie";
                } else if (u.indexOf('Presto') > -1) {
                    //CuPlayer.com提示：opera
                    core = "opera";
                } else if (u.indexOf('Gecko') > -1 && u.indexOf('KHTML') == -1) {
                    //firefox
                    core = "firefox";
                } else if (u.indexOf('AppleWebKit') > -1 && u.indexOf('Safari') > -1) {
                    //CuPlayer.com提示：苹果、谷歌内核
                    if (u.indexOf('Chrome') > -1) {
                        core = "chrome";
                    } else if (u.indexOf('safari') > -1) {
                        //Safari
                        core = "safari";
                    } else {
                        core = "webkit"
                    }
                }
                if (u.indexOf('Mobile') > -1) {
                    //CuPlayer.com提示：移动端 
                    if (!!u.match(/\(i[^;]+;( U;)? CPU.+Mac OS X/)) {
                        //ios 
                        if (u.indexOf('iPhone') > -1) {
                            //iphone 
                            var str = u.toLowerCase();
                            var ver = str.match(/cpu iphone os (.*?) like mac os/);
                            num = ver[1].replace(/_/g, ".")
                            return { "type": "iphone", "version": num, "core": core };
                        } else if (u.indexOf('iPod') > -1) {
                            //ipod 
                            return "iPod"
                        } else if (u.indexOf('iPad') > -1) {
                            //ipad 
                            return "iPad"
                        }
                    } else if (u.indexOf('Android') > -1 || u.indexOf('Linux') > -1) {
                        //android 
                        num = u.substr(u.indexOf('Android') + 8, 3);
                        return { "type": "android", "version": num, "core": core };
                    } else if (u.indexOf('BB10') > -1) {
                        //CuPlayer.com提示：黑莓bb10系统 
                        return "BB10";
                    } else if (u.indexOf('IEMobile')) {
                        //windows phone 
                        return "Windows Phone"
                    }
                }
            }
        }
        return brower.versions();
    }
    // window.addEventListener("orientationchange", function(e) {
    //     // 用手机自带的返回会触发横屏事件
    //     // $("body").scrollTop(scrolladdr);
    //     e.preventDefault();
    //     e.stopPropagation();
    //     return false;
    // }, false);
    // 时间样式，单位s
    function statisticLoadTime(timeDiff) {
        if (timeDiff < 5) {
            return "小于5s";
        } else if (timeDiff < 10) {
            return "5s到10s";
        } else if (timeDiff < 30) {
            return "10s到30s";
        } else if (timeDiff < 60) {
            return "30s到60s";
        } else {
            return "大于1分钟";
        }
    }

    // 检测是否处于全屏
    function fullOrNot(thisVideo) {
        if (clearTimes.judgeScreenTime != "") {
            clearInterval(clearTimes.judgeScreenTime);
        }
        clearTimes.judgeScreenTime = setInterval(function(e) {
            if (!(invokeFieldOrMethod(document, 'FullScreen') || invokeFieldOrMethod(document, 'IsFullScreen') || document.IsFullScreen)) {
                H5ExitFullscreen(thisVideo);
                clearInterval(clearTimes.judgeScreenTime);
            }
        }, 1000)
    }

    function H5ExitFullscreen(thisVideo) {
        videoObj.exitFullScreenStation = thisVideo.paused;
        // videoObj.controls.className="controllers";
        if (thisVideo.exitFullscreen) {
            thisVideo.exitFullscreen();
        } else if (thisVideo.msExitFullscreen) {
            thisVideo.msExitFullscreen();
        } else if (thisVideo.mozCancelFullScreen) {
            thisVideo.mozCancelFullScreen();
        } else if (thisVideo.oExitFullscreen) {
            thisVideo.oCancelFullScreen();
        } else if (document.webkitExitFullscreen) {
            thisVideo.webkitExitFullscreen();
        }
        if (videoObj.exitFullScreenStation == true) {
            videoObj.exitFullScreenDisplay = 0;
        }
        thisVideo.pause();
        win.scrollTo(0, videoObj.locationOfBar);
        thisVideo.play();
        if (videoObj.exitFullScreenStation == true) {
            thisVideo.currentTime = parseInt(thisVideo.currentTime) - 1;
            clearTimeout(clearTimes.solveBlackScreen);
            clearTimes.solveBlackScreen = setTimeout(function() {
                videoObj.exitFullScreenDisplay = 1;
                thisVideo.pause();
            }, 400);
        }
    }
    //是否全屏
    //反射調用
    var invokeFieldOrMethod = function(element, method) {
        var usablePrefixMethod;
        ["webkit", "moz", "ms", "o", ""].forEach(function(prefix) {
            if (usablePrefixMethod) return;
            if (prefix === "") {
                // 无前缀，方法首字母小写
                method = method.slice(0, 1).toLowerCase() + method.slice(1);
            }
            var typePrefixMethod = typeof element[prefix + method];
            if (typePrefixMethod + "" !== "undefined") {
                if (typePrefixMethod === "function") {
                    usablePrefixMethod = element[prefix + method]();
                } else {
                    usablePrefixMethod = element[prefix + method];
                }
            }
        });
        return usablePrefixMethod;
    };
    //檢查瀏覽器是否處於全屏
})(window)
