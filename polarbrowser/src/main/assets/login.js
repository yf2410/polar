(function (){

	function getPasswordElement(){
		window.video.jsOutput('getPasswordElement--start');
		var inputs = window.document.getElementsByTagName('input');
		if (inputs.length != 0) {
			for (var i = inputs.length - 1; i >= 0; i--) {
				if (inputs[i].type == 'password') {
					window.video.jsOutput('getPasswordElement--find password element');
					return inputs[i];
				};
			};
		};
		window.video.jsOutput('getPasswordElement--not find password element');
		window.video.jsOutput('getPasswordElement--no login');
		return null;
	};

	function getFormFromElement(elePassWord){
		window.video.jsOutput('getFormFromElement--start');
		var temp = elePassWord.parentNode;
		if (temp != null) {
			if (temp.tagName == 'form' || temp.tagName == 'FORM') {
				window.video.jsOutput('getFormFromElement--find password element');
				return temp;
			} else {
				return getFormFromElement(temp);
			};
		};
		
		return null;
	};

	function getUserNameElement(form, psdEle){
		window.video.jsOutput('getUserNameElement--start');
		if (/(live)\.com/i.test(window.location.href)) {
			window.video.jsOutput('getUserNameElement--test regexp true');
			var username = document.getElementById('i0116');
			if (username != null) {
				window.video.jsOutput('getUserNameElement--for www.live.com');
				return username;
			};

		};

		var inputs = form.getElementsByTagName('input');
		for (var i = inputs.length - 1; i >= 0; i--) {
			var ele = inputs[i];
			if (ele == psdEle) {
				var n = i-1;
				if (n >= 0) {
					window.video.jsOutput('getUserNameElement--find username element');
					return inputs[n];
				};
			};
		};
		window.video.jsOutput('getUserNameElement--not find username element');
		return null;
	};

	var psd = getPasswordElement();
	if (psd == null) { return ;};

	window.video.jsOutput('0-getPasswordElement');

	var form = getFormFromElement(psd);
	if (form == null) {return;};

	window.video.jsOutput('1-getFormFromElement');

	var username = getUserNameElement(form, psd);
	if (username != null &&
		psd != null &&
		form != null) {
		window.video.jsOutput('2-getUserNameElement');
		
		if (window.vcInstance == null) {
			var vcInstance = {};
			window.vcInstance = vcInstance;	
		};	
		window.vcInstance.username = username;
		window.vcInstance.password = psd;
		window.vcInstance.href = window.location.href;
		window.vcInstance.fillForm = function (urlParam, usernameParam, passwordParam){
			window.video.jsOutput('3-pre-fillForm');
			window.video.jsOutput(urlParam);
			window.video.jsOutput(usernameParam);
			window.video.jsOutput(passwordParam);
			
			if (urlParam != window.location.host){
				window.video.jsOutput('url changed, return!');
				return;
			}
			if (usernameParam.length &&
				passwordParam.length) {
				window.video.jsOutput('4-fillForm');
				window.vcInstance.username.value = usernameParam;
				window.vcInstance.password.value = passwordParam;
				if (/(live)\.com/i.test(window.location.href)) {
					window.video.jsOutput('match www.live.com');
					var phholder = window.vcInstance.username.parentElement.getElementsByClassName('phholder')[0];
					phholder.style.display = 'none';
				}else{
					window.video.jsOutput('not match www.live.com');
				}
			};

		};
		window.video.fillForm(window.vcInstance.href);
		attachEvent(form, 'submit', cbSubmit);
		attachBtnEvent(form);
	};	
	
	function attachBtnEvent(form) {
		
		var btns = form.getElementsByTagName('button');
		for (var i = btns.length - 1; i >= 0; i--) {
			var btn = btns[i];
			attachEvent(btn, 'click', cbSubmit);
		};
	};

	function attachEvent(ele, event, callback) {
	window.video.jsOutput('5-attachBtnEvent');
		if (window.document.addEventListener) {
			ele.addEventListener(event, callback);
		};
	};

	function cbSubmit(){
		window.video.jsOutput('5-cbSubmit');
		var inc = window.vcInstance;
		if (inc != null) {
			var un = inc.username;
			var pd = inc.password;
			var hf = inc.href;
			if (un != null && 
				pd != null &&
				hf != null) {
				var username = un.value;
				var password = pd.value;
				if (username.length &&
					password.length &&
					hf.length) {
					window.video.saveUserNamePassWord(hf,username,password);
				};

			};
		};
	};
})();
