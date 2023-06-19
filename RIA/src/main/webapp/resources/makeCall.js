/**
 * Asynchronous JavaScript And XML call
 */

 function makeCall(method, url, formElement, callback, reset = true) {
	 
	var req = new XMLHttpRequest(); // visible by closure
	 
	console.log("MAKE CALL")
	console.log(formElement)
	req.onreadystatechange = function() {
		callback(req)
	}; // closure
	
	req.open(method, url);
	
	if (formElement == null) {
		req.send();
	}
	else {
		console.log(new FormData(formElement))
		req.send(new FormData(formElement));
	}
	if (formElement !== null && reset === true) {
		formElement.reset();
	}
    
 }