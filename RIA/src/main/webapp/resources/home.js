(function(){
	let pageOrchestrator = new PageOrchestrator();
	let title, categoriesContainer, createCategoryForm;
	
	window.addEventListener("load", () => {
		if(sessionStorage.getItem("username") === null) {
			window.location.href = "index.html";
		}
		else {
			pageOrchestrator.start();
			title.innerHTML = "Welcome to your homepage, " + sessionStorage.getItem("username") + "!";
			pageOrchestrator.refresh();
		}
	}, false);
	
	function PageOrchestrator() {
		this.start = function() {
			title = document.getElementById("welcomeTitle");
			categoriesContainer = new CategoriesContainer(document.getElementById("categoriesContainer"),
													document.getElementById("noCategories"));
			
			
			createCategoryForm = new CreateCategoryForm(document.getElementById("createCategoryForm"),
													document.getElementById("createCategory"),
													document.getElementById("parentIDCreation"));
			createCategoryForm.setEvent(); //si dovrebbe fare dopo? Nel senso, non è una cosa da refresh?
		}
		
		this.refresh = function() {
			categoriesContainer.update();
			//remember that it's asynchronous
		}
	}
	
	function CategoriesContainer(_categoriesContainerDiv, _noCategoriesYetMessage) {
		this.categoriesContainerDiv = _categoriesContainerDiv;
		this.noCategoriesYetMessage = _noCategoriesYetMessage;
		this.categoriesList;
		
		this.update = function() {
			var self = this;
			makeCall("GET", "GetCategories", null, function(x){
				let response = x.responseText;
				
				if(x.readyState === XMLHttpRequest.DONE) {
					switch(x.status) {
						case 200: {
							self.categoriesList = JSON.parse(response); //ci possiamo fidare di """liste""" di js?
							if(self.categoriesList.length === 0) {
								self.noCategoriesYetMessage.textContent = "There are no categories yet! Please insert a new one with the form below!";
								return
							}
							self.noCategoriesYetMessage.textContent = "";
							self.createCategoriesHTML(self.categoriesList);
							createCategoryForm.refresh(self.categoriesList);//here it also manages the refresh of createCategoryForm 
							break;
						}
						case 500: {
							alert(response);
							break;
						}
						default: {
							alert("Unknown error occurred.");
							break;
						}
					}
				}
			});
			
		};
		
		this.createCategoriesHTML = function(list) {
			var self = this;
			
			self.categoriesContainerDiv.innerHTML = "";
			list.forEach(function(category) {
				let span = document.createElement("span");
				let br = document.createElement("br");
				span.textContent = category.categoryID + " - " + category.name;
				span.setAttribute('categoryID', category.categoryID);
				
				if(category.categoryID >= 10)
					span.classList.add("moveright");
				
				// todo vanno inseriti tutti gli event listener!
				
				self.categoriesContainerDiv.appendChild(span);
				self.categoriesContainerDiv.appendChild(br);
			});
		}
		
	}
	
	function CreateCategoryForm(_createCategoryForm, _submitButton, _parentIDCreation) {
		this.createCategoryForm = _createCategoryForm;
		this.submitButton = _submitButton;
		this.parentIDCreation = _parentIDCreation;
		
	 	this.createCategoryForm.addEventListener("submit", (e)=>{
 	 		e.preventDefault();
	 	});
		
		this.setEvent = function() {
			var self = this;
			self.submitButton.addEventListener('click', (e) => {
				let form = self.createCategoryForm;
				if(form.checkValidity()) {
					makeCall("POST", 'CreateCategory', form, function(x){
						if(x.readyState === XMLHttpRequest.DONE) {
							let response = x.responseText;
							
							switch(x.status) {
								case 200: {
									pageOrchestrator.refresh();
									break;
								}
								case 400: {
									alert(response);
									break;
								}
								case 500: {
									alert(response);
									break;
								}
							}
						}
					});
				}
			});
		}
		
		this.refresh = function(list) {
			var self = this;
			let option;
			
			self.parentIDCreation.innerHTML = "";	
			
			option = document.createElement("option"); //creating option for root
			option.text = "root";
			option.value = "root";
			self.parentIDCreation.appendChild(option);
			
			list.forEach(function(category) {
				option = document.createElement("option");
				option.text = category.categoryID;
				option.value = category.categoryID;
				self.parentIDCreation.appendChild(option);
			});
		}
	}
	
})();