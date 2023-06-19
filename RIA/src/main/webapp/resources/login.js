/**
 * JavaScript source for login management
 */

 (function() {
	 
	 let loginButton = document.getElementById("loginButton");
	 let loginError = document.getElementById("loginError");
	 
	 loginButton.addEventListener('click', (e) => {
		 let form = e.target.closest("form");
		 
		 if(form.checkValidity()) {
			 console.log("CHECKVAL OK")
			 makeCall("POST", 'CheckLogin', e.target.closest("form"), function(x) {
				 if(x.readyState == XMLHttpRequest.DONE) {
					 let response = x.responseText;
					 
					 switch(x.status) {
						 case 200: {
							 console.log(response)
							 sessionStorage.setItem('username', response);
							 window.location.href = "Home.html";
							 break;
						 }
						 case 400: {	// bad request
							 loginError.textContent = response;
							 break;
						 }
						 case 401: {	// unauthorized
							 loginError.textContent = response;
							 break;
						 }
						 case 500: {	// server error
							 loginError.textContent = response;
							 break;
						 }
						 default: {
							 loginError.textContent = "Unknown error occurred.";
							 break;
						 }
					 }
				 }
				 else {
					 form.reportValidity();
				 }
			 });
		 }
	 });
	 
 })();