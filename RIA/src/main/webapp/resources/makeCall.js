/**
 * Asynchronous JavaScript And XML call
 */

 function makeCall(method, url, formElement, callback, reset = true) {
	 
	var req = new XMLHttpRequest(); // visible by closure
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
 
 function makeCallReady(method, url, jsonData, cback) {
	    var req = new XMLHttpRequest(); 
	    req.onreadystatechange = function() {
	      cback(req)
	    }; // closure
	    req.open(method, url);
	    req.send(formData);
}