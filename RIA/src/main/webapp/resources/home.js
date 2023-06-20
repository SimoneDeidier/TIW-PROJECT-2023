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
			createCategoryForm.setEvent(); //si dovrebbe fare dopo?
		}
		
		this.refresh = function() {
			categoriesContainer.update();
			//createCategoryForm.refresh(categoriesContainer.categoriesList);
		}
	}
	
	function CategoriesContainer(_categoriesContainer, _noCategoriesYetMessage) {
		this.categoriesContainer = _categoriesContainer;
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
							createCategoryForm.refresh(self.categoriesList);
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
			var span, br; //forse potrei metterle dentro funzione?
			
			self.categoriesContainer.innerHTML = "";		
			list.forEach(function(category) {
				span = document.createElement("span");
				br = document.createElement("br");
				span.textContent = category.categoryID + " - " + category.name;
				span.setAttribute('categoryID', category.categoryID);
				// todo vanno inseriti tutti gli event listener!
				self.categoriesContainer.appendChild(span);
				self.categoriesContainer.appendChild(br);
			});
		}
		
		this.getCategoriesList = function() {
			return this.categoriesList;
		}
		
	}
	
	function CreateCategoryForm(_createCategoryForm, _submitButton, _parentIDCreation) {
		this.createCategoryForm = _createCategoryForm;
		this.submitButton = _submitButton;
		this.parentIDCreation = _parentIDCreation;
		
		this.setEvent = function() {
			var self = this;
			
			self.submitButton.addEventListener('click', (e) => {
				e.preventDefault();
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
			
			self.parentIDCreation.innerHTML = "";	
			list.forEach(function(category) {
				let option = document.createElement("option");
				option.text = category.categoryID;
				option.value = category.categoryID;
				self.parentIDCreation.appendChild(option);
			});
		}
	}
	
})();