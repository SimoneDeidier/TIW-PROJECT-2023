/**
 * JavaScript source for login management
 */

 (function() {
	 
	 let loginButton = document.getElementById("loginButton");
	 let loginError = document.getElementById("loginError");
	 let registrationError= document.getElementById("registrationError");
	 let registrationSuccess= document.getElementById("registrationSuccess");
	 let registrationButton= document.getElementById("registrationButton");
	 
	 loginButton.addEventListener('click', (e) => {
		 let form = e.target.closest("form");
		 
		 if(form.checkValidity()) {
			 makeCall("POST", 'CheckLogin', e.target.closest("form"), function(x) {
				 if(x.readyState == XMLHttpRequest.DONE) {
					 let response = x.responseText;
					 
					 switch(x.status) {
						 case 200: {
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
	 
	 registrationButton.addEventListener('click', (e) => {
		 let form = e.target.closest("form");
		 
		 if(form.checkValidity()) {
			 makeCall("POST", 'RegisterNewUser', e.target.closest("form"), function(x) {
				 if(x.readyState == XMLHttpRequest.DONE) {
					 let response = x.responseText;
					 
					 switch(x.status) {
						 case 200: {
							 registrationSuccess.textContent = response;
							 break;
						 }
						 case 400: {	// bad request
							 registrationError.textContent = response;
							 break;
						 }
						 case 401: {	// unauthorized
							 registrationError.textContent = response;
							 break;
						 }
						 case 500: {	// server error
							 registrationError.textContent = response;
							 break;
						 }
						 default: {
							 registrationError.textContent = "Unknown error occurred.";
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