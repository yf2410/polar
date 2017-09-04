(function(){
		function isWhiteList(url) {
		return /(youku)\.com/i.test(url) || 
			   /(iqiyi)\.com/i.test(url) ;
		}

 function isHasVideoElem()
 {

 var doc = document;
 
 if (!isWhiteList(doc.URL)) {return; };

 var videoArray = doc.getElementsByTagName('video');
 var vlen = videoArray.length;
 if(vlen > 0)
 {
 window.video.isHasVideo('YES');
 return;
 }
 else
 {
 var pageUrl = doc.URL;
 if (/(pptv)\.com/i.test(pageUrl))
 {
 var frames = doc.getElementsByTagName('iframe');
 var flen = frames.length;
 for (var j = 0; j < flen; j++)
 {
 var m = frames[j];
 if (typeof(m.src) != 'undefined' && /(pptv)\.com/i.test(m.src))
 {
 var conDoc = m.contentDocument;
 var g = conDoc.getElementsByTagName('video');
 var glen = g.length;
 if(glen > 0)
 {
 window.video.isHasVideo('YES');
 return;
 }
 }
 }
 }
 window.video.isHasVideo('NO');
 return;
 }
 };
 return isHasVideoElem();
})();