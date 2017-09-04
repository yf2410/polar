 (function(){
  function getVideoUrl() {
    var index = 0;
    if(typeof(window.magicPlayingIndex) != 'undefined' && window.magicPlayingIndex >= 0)
    {
      index = window.magicPlayingIndex;

    }
    var videoUrl = null;
    var vs = document.getElementsByTagName('video');
    var vsLen = vs.length;
    if(vsLen > 0 && index < vsLen)
    {
      var tempVideo = vs[index];
      videoUrl = tempVideo.currentSrc;
      if(videoUrl == null || videoUrl.length <= 0)
      {
        videoUrl = tempVideo.getAttribute('src');
      }
      
      if(videoUrl == null || videoUrl.length <= 0)
      {
        var sources = tempVideo.getElementsByTagName('source');
        if(sources.length > 0)
        {
          videoUrl = sources[0].getAttribute('src');
        }

      }
    
      if(videoUrl == null || videoUrl.length <= 0)
      {
        var innerHtmlContent = tempVideo.innerHTML;
        var patt1=new RegExp("\bhttps?://[a-zA-Z0-9\-.]+(?::(\d+))?(?:(?:/[a-zA-Z0-9\-._?,'+\&%$=~*!():@\\]*)+)?");
        videoUrl = patt1.exec(innerHtmlContent);
      }
    }

    if(videoUrl != null && videoUrl.length > 0)
    {
      window.video.getVideoUrl(videoUrl);
    }

    var pageUrl = document.URL;
    
    if (/(pptv)\.com/i.test(pageUrl))
    {
      var frames = document.getElementsByTagName('iframe');
      var flen = frames.length;
      for (var j = 0; j < flen; j++)
      {
        var m = frames[j];
        if (typeof(m.src) != 'undefined' && /(pptv)\.com/i.test(m.src))
        {

          var conDoc = m.contentDocument;
          var g = conDoc.getElementsByTagName('video');
          var glen = g.length;
          if (glen > 0 && index < glen)
          {
            var video = g[index];
            videoUrl = video.currentSrc;
            if (videoUrl != null && videoUrl.length > 0)
            {
              break;
            }
            else
            {
              videoUrl = video.getAttribute('src');
              if (videoUrl != null && videoUrl.length > 0)
              {
                break;
              }
            }
          }
        }
      }
    }
    
    window.video.getVideoUrl(videoUrl);
}
return getVideoUrl();

})();