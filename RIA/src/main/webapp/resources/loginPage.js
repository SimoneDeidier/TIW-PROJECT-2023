/**
 * JavaScript source for login management
 */

 (function() {
	 
	 let registrationButton= document.getElementById("registrationButton");
	 
	 loginButton.addEventListener('click', (e) => {
		 let form = e.target.closest("form");
		 
		 if(form.checkValidity()) {
			 makeCall("POST", 'CheckLogin', form, function(x) {
				 if(x.readyState === XMLHttpRequest.DONE) {
					 let response = x.responseText;
					 
					 switch(x.status) {
						 case 200: {
							 sessionStorage.setItem('username', response);
							 window.location.href = "Home.html";
							 break;
						 }
						 case 400: {	// bad request
							 alert(response);
							 break;
						 }
						 case 401: {	// unauthorized
							 alert(response);
							 break;
						 }
						 case 500: {	// server error
							 alert(response);
							 break;
						 }
						 default: {
							 alert("Unknown error occurred.");
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
			var newUsername = document.getElementById("newUsername").value;
			 makeCall("POST", 'RegisterNewUser', form, function(x) {
				 if(x.readyState === XMLHttpRequest.DONE) {
					 let response = x.responseText;
					 
					 switch(x.status) {
						 case 200: {
							 alert(response);
							 document.getElementById("username").value = newUsername;
							 document.getElementById("password").focus();
							 break;
						 }
						 case 400: {	// bad request
							 alert(response);
							 break;
						 }
						 case 401: {	// unauthorized
							 alert(response);
							 break;
						 }
						 case 500: {	// server error
							 alert(response);
							 break;
						 }
						 default: {
							 alert("Unknown error occurred.");
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