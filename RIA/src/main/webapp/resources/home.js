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
		this.categoriesList=[];
		this.elementsBeingDraggedID;
		this.categoriesBeingDragged = [];
		this.dropHappened = false;
		
	
		this.update = function() {
			var self = this;
			makeCall("GET", "GetCategories", null, function(x){
				let response = x.responseText;
				
				if(x.readyState === XMLHttpRequest.DONE) {
					switch(x.status) {
						case 200: {
							let temp = JSON.parse(response);
							self.categoriesList=[]; //emptying the list
							temp.forEach(function(category){
								self.categoriesList.push(new Category(parseInt(category.categoryID),category.name,parseInt(category.parentID)));
							});
							if(self.categoriesList.length === 0) {
								self.noCategoriesYetMessage.textContent = "There are no categories yet! Please insert a new one with the form below!";
								return
							}
							self.noCategoriesYetMessage.textContent = "";
							self.createCategoriesHTML();
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
		
		this.orderList = function(unorderedList,parentID){
			self=this;
			let list=[];
			
			unorderedList.forEach(function(category){
				if(category.parentID === parentID){
					list.push(category);
					list.push.apply(list,self.orderList(unorderedList,category.categoryID) );
				}
			});
			return list;
		}
		
		this.createCategoriesHTML = function() {
			var self = this;
			
			self.categoriesContainerDiv.innerHTML = "";
			self.categoriesList.forEach(function(category) {
				let span = document.createElement("span");
				let br = document.createElement("br");
				span.textContent = category.categoryID + " - " + category.name;
				span.setAttribute('id', category.categoryID);
				
				if(category.categoryID >= 10){
					span.classList.add("moveright");
					}
				
				//starting drag and drop feature	
				span.setAttribute('draggable', true);
				
				span.addEventListener('dragend', (e)=>{
					if(!self.dropHappened)
						self.createCategoriesHTML(); //resets the Event Listeners
				});
				
				span.addEventListener('dragstart', (e)=>{ 
					
					//handling the creation of element to copy at level zero
					/*let span = document.createElement("span");
					let br = document.createElement("br");
					span.textContent = "copy at level zero";
					span.setAttribute('id', "level_zero");
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
							if(confirm("Are you sure you want to copy into the category with the ID " + e.target.id + "?")){
								let categoriesToAddList = self.getListOfNewCategories(0);
								self.categoriesList.push.apply(self.categoriesList,categoriesToAddList); //adding the categories to add in the categoriesList
								let unorderedArray=Array.from(self.categoriesList).sort((x,y) => x.categoryID - y.categoryID); //sorts in ascending order based on category IDs
								self.categoriesList=self.orderList(unorderedArray,0);
								self.dropHappened=true;
								//I could use other functions, without all eventHandler and even hiding the form, to be decided
								//self.createCategoriesHTML(); 
							}
							else{
								self.createCategoriesHTML(); //resets the Event Listeners
							}
						});
					self.categoriesContainerDiv.appendChild(span);
					self.categoriesContainerDiv.appendChild(br);
					*/
					
					//normal categories
					let elementSelectedForDrag = parseInt(e.target.id);
					//TODO check here on the categoryID
					self.elementsBeingDraggedID = self.selectElementsBeingDragged(elementSelectedForDrag);
					self.categoriesBeingDragged = [];
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
							//TODO REMEMBER TO REMOVE THEM AFTER DROP
								
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
								if(confirm("Are you sure you want to copy into the category with the ID " + e.target.id + "?")){
									//TODO CHECK ON e.target.id
									let categoriesToAddList = self.getListOfNewCategories(parseInt(e.target.id));
									self.categoriesList.push.apply(self.categoriesList,categoriesToAddList); //adding the categories to add in the categoriesList
									let unorderedArray=Array.from(self.categoriesList).sort((x,y) => x.categoryID - y.categoryID); //sorts in ascending order based on category IDs
									self.categoriesList=self.orderList(unorderedArray,0);
									self.dropHappened=true; //TODO -> quando rimetterlo a false?
									//nel senso, caso che non clicchi il bottone ma refresha?
									self.showTemporaryList(categoriesToAddList);
								}
								else{
									self.createCategoriesHTML(); //resets the Event Listeners
								}
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
		
		this.showTemporaryList = function(categoriesToAddList){
			var self=this;
			self.categoriesContainerDiv.innerHTML = "";
			self.categoriesList.forEach(function(category) {
				let span = document.createElement("span");
				let br = document.createElement("br");
				span.textContent = category.categoryID + " - " + category.name;
				span.setAttribute('id', category.categoryID);
				
				if(category.categoryID >= 10){
					span.classList.add("moveright");
					}
				self.categoriesContainerDiv.appendChild(span);
				self.categoriesContainerDiv.appendChild(br);
				
			})
			//hide button
			document.getElementById("createCategoryForm").classList.add('hide');
			//Create text
			let span = document.createElement("span");
			span.textContent = "If you are pleased with the result, click the button to submit the changes to the database!";
			span.classList.add('center');
			self.categoriesContainerDiv.appendChild(span);
			let br = document.createElement("br");
			self.categoriesContainerDiv.appendChild(br);
			//create button to save in database
			let button = document.createElement("button");
			button.textContent = "Click here";
			button.classList.add('center')
			button.addEventListener('click',(e)=>{
				makeCallReady("POST", 'InsertCopiedCategory', JSON.stringify(categoriesToAddList), function(x){
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
			});
			self.categoriesContainerDiv.appendChild(button);
			
			
		}
		
		this.getListOfNewCategories = function(parentID){
			var self=this;
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
				let categoryIDString = self.categoriesBeingDragged[0].categoryID.toString();
				let newCategoryIDString = newCategoryID.toString();
				let temp= idString.substring(categoryIDString.length);
				let newIDString = newCategoryIDString + temp;
				//todo check su max lenght
				if(newIDString.length !== 1)
					list.push(new Category(parseInt(newIDString),category.name,parseInt(newIDString.slice(0,-1))));
				else
					list.push(new Category(parseInt(newIDString),category.name,0));
				
			});
			return list;
			
		}
		
		this.findLastChildrenID = function(parentID){
			var self=this;
			let maxIndex=0;
			self.categoriesList.forEach(function(category){
				if(category.parentID == parentID && category.categoryID > maxIndex){
					maxIndex=category.categoryID;
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