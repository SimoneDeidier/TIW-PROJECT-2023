(function(){
	let pageOrchestrator = new PageOrchestrator();
	let title, categoriesContainer, createCategoryForm;
	//let categoriesBeingCopied; Useless?
	
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
		this.elementsBeingDraggedID;
		this.categoriesBeingDragged = [];
		
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
		
		this.offlineUpdate = function(unorderedList,parentID){
			self=this;
			//
		}
		
		this.createCategoriesHTML = function(list) {
			var self = this;
			
			self.categoriesContainerDiv.innerHTML = "";
			list.forEach(function(category) {
				let span = document.createElement("span");
				let br = document.createElement("br");
				span.textContent = category.categoryID + " - " + category.name;
				span.setAttribute('id', category.categoryID);
				
				if(category.categoryID >= 10){
					span.classList.add("moveright");
					}
				
				//starting drag and drop feature	
				span.setAttribute('draggable', true);
				span.addEventListener('dragstart', (e)=>{
					let elementSelectedForDrag = parseInt(e.target.id);
					//TODO check here on the categoryID
					self.elementsBeingDraggedID = self.selectElementsBeingDragged(elementSelectedForDrag);
					self.categoriesList.forEach(function(category){
						if(self.elementsBeingDraggedID.includes(category.categoryID)){
							setTimeout(() => {
								let span= document.getElementById(category.categoryID);
   								span.classList.add('redtext');
							}, 0);
							self.categoriesBeingDragged.push(new Category(category.categoryID,category.name,category.parentID));
						}
						else{
							let span= document.getElementById(category.categoryID);
							
							//TODO REMEMBER TO REMOVE THEM AFTER DROP OR ANYWAY AFTER DRAG ENDS; HOW
								
							span.addEventListener('dragenter', (e)=>{
								e.preventDefault();
								e.target.classList.add('drag-over');
							});
							
							span.addEventListener('dragover', (e)=>{
								e.preventDefault();
								e.target.classList.add('drag-over');
								
							});
							
							span.addEventListener('dragleave', (e)=>{
								e.target.classList.remove('drag-over');
							});
							
							span.addEventListener('drop', (e)=>{
								//remove drag-over from target
								e.target.classList.remove('drag-over');
								//remove redtext from categories that were being dragged
								self.categoriesList.forEach(function(category){
									if(self.elementsBeingDraggedID.includes(category.categoryID)){		
										let span= document.getElementById(category.categoryID);
		   								span.classList.remove('redtext');
									};
								});
								//TODO CHECK ON e.target.id
								let categoriesToAddList = self.getListOfNewCategories(e.target.id);
								newList=self.offlineUpdate(Array.prototype.push.apply(self.categoriesList,categoriesToAddList),0);
							});
						}
							//CREDO useless perchè uso un array
						//e.dataTransfer.setData('text/plain', JSON.stringify(self.elementsBeingDraggedID));
					});
				});
				
				// todo vanno inseriti gli altri event listener!
				
				self.categoriesContainerDiv.appendChild(span);
				self.categoriesContainerDiv.appendChild(br);
			});
		}
		
		this.getListOfNewCategories = function(parentID){
			self=this;
			//console.log(self.categoriesList);
			//console.log(self.elementsBeingDraggedID);
			//console.log(self.categoriesBeingDragged)
			let list = [];
			let newCategoryID,lastChildrenOfParent;
			//todo check on lastChildrenOfParent
			lastChildrenOfParent = self.findLastChildrenID(parentID);
			if(lastChildrenOfParent!=0){
				newCategoryID= lastChildrenOfParent+1;
			}
			else{
				newCategoryID = (parentID*10) + 1;
			}
			self.categoriesBeingDragged.forEach(function(category){
				//calculating new IDs for the categories and adding them to the list
				let idString = category.categoryID.toString();
				let categoryIDString = self.categoriesBeingDragged[0].categoryID;
				let newCategoryIDString = newCategoryID.toString();
				let temp= idString.substring(categoryIDString.lenght);
				let newIDString = newCategoryIDString + temp;
				//todo check su max lenght (???)
				list.push(new Category(parseInt(newIDString),category.name,parseInt(newIDString)/10));
				
			});
			return list;
			
		}
		
		this.findLastChildrenID = function(parentID){
			self=this;
			let maxIndex=0;
			self.categoriesList.forEach(function(category){
				if(category.parentID === parentID && category.parentID>maxIndex){
					maxIndex=category.parentID;
				}
			});
			return maxIndex;
		}
		
		this.selectElementsBeingDragged = function (categoryID){
			var self=this;
			let temp = [];
			temp.push(categoryID);
			self.categoriesList.forEach(function(categoryInList){
				if(categoryInList.parentID === categoryID){
					Array.prototype.push.apply(temp,self.selectElementsBeingDragged(categoryInList.categoryID) );
				}	
			});
			return temp;
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
	
	class Category {
        constructor(_categoryID, _name, _parentID) {
            this.categoryID = _categoryID;
            this.name = _name;
            this.parentID = _parentID;
        }
    }
	
	
	
})();