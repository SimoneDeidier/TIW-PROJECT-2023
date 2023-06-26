(function(){
	let pageOrchestrator = new PageOrchestrator();
	let title, categoriesContainer, createCategoryForm, logoutManager;
	
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
			createCategoryForm.setEvent();
			logoutManager = new LogoutManager(document.getElementById("logout"));
			logoutManager.setEvent();
		}
		
		this.refresh = function() {
			categoriesContainer.update(); //remember that it's asynchronous
			logoutManager.show();
		}
	}
	
	function CategoriesContainer(_categoriesContainerDiv, _noCategoriesYetMessage) {
		this.categoriesContainerDiv = _categoriesContainerDiv;
		this.noCategoriesYetMessage = _noCategoriesYetMessage;
		this.categoriesList=[];
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
							if(temp === null){
								self.noCategoriesYetMessage.textContent = "There are no categories yet! Please insert a new one with the form!";
								createCategoryForm.refresh(self.categoriesList);//it also manages the refresh of createCategoryForm 
								return;
							}
							self.categoriesList=[]; //emptying the list
							temp.forEach(function(category){
								self.categoriesList.push(new Category(parseInt(category.categoryID),category.name,parseInt(category.parentID)));
							});
							self.noCategoriesYetMessage.textContent = "";
							self.createCategoriesHTML();
							createCategoryForm.refresh(self.categoriesList);//it also manages the refresh of createCategoryForm 
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
			var self=this;
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
				
				span.addEventListener('dragstart', (e)=>{ 
					let categoryIDBeingDragged = parseInt(e.target.id);
					
					//check
					let checkSelectedNotModified = false;
					let checkModifications = true;
					let lastCheckedID = -1;
					self.categoriesList.forEach(function(category){
						if(category.categoryID === categoryIDBeingDragged){ 
							checkSelectedNotModified = true;
						}
						if(document.getElementById(category.categoryID) === null){
							checkModifications = false;
						}
						if(lastCheckedID !== -1 && checkModifications){ //all elements but the first
							if(parseInt(document.getElementById(lastCheckedID).nextElementSibling.nextElementSibling.id) !== category.categoryID){
								checkModifications=false;
							}
						}					
						lastCheckedID=category.categoryID;
					})
					if(!checkSelectedNotModified || !checkModifications){
						e.preventDefault();
						alert("There was a problem with a category during the drag&drop operation, try again!");
						self.createCategoriesHTML();
						return;
					}
					
					//actual execution
					let categoriesIDBeingDragged = self.selectCategoriesIDsBeingDragged(categoryIDBeingDragged);
					self.categoriesBeingDragged = []; //emptying the previous values 
					
					//Adding copy at level zero option
					let categoriesListWithCopyHere = []
					categoriesListWithCopyHere.push.apply(categoriesListWithCopyHere,self.categoriesList);
					categoriesListWithCopyHere.push(new Category(0,"copy_here",-1));
					let span = document.createElement("span");
					let br = document.createElement("br");
					span.textContent = "Copy to level zero";
					span.setAttribute('id', 0);
					self.categoriesContainerDiv.appendChild(span);
					self.categoriesContainerDiv.appendChild(br);
					
					
					categoriesListWithCopyHere.forEach(function(category){
						//addition of red text for categories being dragged
						if(categoriesIDBeingDragged.includes(category.categoryID)){
							setTimeout(() => {
								let span= document.getElementById(category.categoryID);
   								span.classList.add('redtext');
							}, 0);
							self.categoriesBeingDragged.push(new Category(category.categoryID,category.name,category.parentID));
							
						}
						//event listeners for the others
						else{
							let span= document.getElementById(category.categoryID);
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
									if(categoriesIDBeingDragged.includes(category.categoryID)){		
										let span= document.getElementById(category.categoryID);
		   								span.classList.remove('redtext');
									};
								});
								
								//if user doesn't confirm drop the changes
								if(!confirm("Are you sure you want to copy into the category with the ID " + e.target.id + "?")){ 
									self.createCategoriesHTML(); //resets the Event Listeners
									return;
								}
								
								let categoryIDOfDrop = parseInt(e.target.id);
								
								//other check, always important to preserve database integrity
								lastCheckedID = -1;
								self.categoriesList.forEach(function(category){
									if(category.categoryID === categoryIDOfDrop){ //list contains the id selected
										checkSelectedNotModified = true;
									}
									if(document.getElementById(category.categoryID)===null){ //something was modified
										checkModifications = false;
									}
									if(lastCheckedID !== -1 && checkModifications){ //all elements but the first
										if(parseInt(document.getElementById(lastCheckedID).nextElementSibling.nextElementSibling.id) !== category.categoryID){
											checkModifications=false;
										}
									}					
									lastCheckedID=category.categoryID;
								})
								if(!checkSelectedNotModified || !checkModifications){
									alert("There was a problem during the drag & drop, try again!");
									self.createCategoriesHTML();
									return;
								}
								
								//actual execution
								try{
									let categoriesToAddList = self.getListOfNewCategories(categoryIDOfDrop);
									self.categoriesList.push.apply(self.categoriesList,categoriesToAddList); //adding the categories to add in the categoriesList
									let unorderedArray=Array.from(self.categoriesList).sort((x,y) => x.categoryID - y.categoryID); //sorts in ascending order based on category IDs
									self.categoriesList=self.orderList(unorderedArray,0);
									self.dropHappened=true;
									self.showTemporaryList(categoriesToAddList,categoryIDBeingDragged,categoryIDOfDrop);
								} catch (error){
									alert(error);
									self.createCategoriesHTML(); //resets the Event Listeners
								}
									
							});
						}
					});
				});
				
				span.addEventListener('dragend', (e)=>{
					if(!self.dropHappened){
						self.createCategoriesHTML(); //resets the Event Listeners in the case drag started but drop didn't happen
					}
					self.dropHappened=false;
				});
				
				span.addEventListener('click', (e) => {
					let clickedCategoryID = parseInt(e.target.id);
					let check = false;
					let checkModifications = true;
					let lastCheckedID = -1;
					self.categoriesList.forEach(function(category){
						if(category.categoryID === clickedCategoryID) {
							check = true;
						}
						if(document.getElementById(category.categoryID)===null){ //something was modified
							checkModifications = false;
						}
						if(lastCheckedID !== -1 && checkModifications){ //all elements but the first
							if(parseInt(document.getElementById(lastCheckedID).nextElementSibling.nextElementSibling.id) !== category.categoryID){
								checkModifications = false;
							}
						}					
						lastCheckedID = category.categoryID;
					});
					if(!check || !checkModifications) {
						alert("There was a problem during the name changing operation, please try again!");
						self.createCategoriesHTML();
						return;
					}
					let toChangeNameSpan = document.getElementById(clickedCategoryID);
					toChangeNameSpan.setAttribute("draggable", false);
					toChangeNameSpan.innerText = clickedCategoryID.toString() + " - ";
					let inputArea = document.createElement('input');
					inputArea.setAttribute('type', 'text');
					let oldName = null;
					self.categoriesList.forEach(function(category) {
						if(category.categoryID === clickedCategoryID) {
							oldName = category.name;
							return;
						}
					});
					inputArea.setAttribute('value', oldName);
					toChangeNameSpan.appendChild(inputArea);
					inputArea.addEventListener('click', (e) => {
						e.stopPropagation();
					}, true);
					inputArea.focus();
					inputArea.addEventListener('focusout', (e) => {
						let newName = inputArea.value;
						if(newName === null || newName === oldName) {
							self.createCategoriesHTML();
						}
						else {
							let data = new FormData();
							data.append("changedID", clickedCategoryID);
							data.append("newName", newName);
							makeCallReady('POST', 'ChangeCategoryName', data, function(x) {
								if(x.readyState === XMLHttpRequest.DONE) {
									let response = x.responseText;
									
									switch(x.status) {
										case 200: {
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
										default: {
											 alert("Unknown error occurred.");
											 break;
										 }
									}
									pageOrchestrator.refresh();
								}
							});
						}
					});
				});
				
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
			//hide form
			document.getElementById("createCategoryForm").classList.replace("box", "hide");
			
			//hide logout
			document.getElementById("logoutMessage").classList.add('hide');
			//create box
			let div = document.createElement("div");
			div.classList.add("box");
			div.classList.add("marginRight");
			//Create text
			let par = document.createElement("p");
			par.textContent = "If you are pleased with the result, click the button to submit the changes to the database!";
			div.appendChild(par);
			//create button to save in database
			let button = document.createElement("button");
			button.textContent = "Confirm";
			button.addEventListener('click',(e)=>{
				let data = new FormData();
				data.append("jsonData",JSON.stringify(categoriesToAddList));
				makeCallReady("POST", 'InsertCopiedCategory', data, function(x){
						if(x.readyState === XMLHttpRequest.DONE) {
							let response = x.responseText;
							
							switch(x.status) {
								case 200: {
									div.remove();
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
								default: {
									 alert("Unknown error occurred.");
									 break;
								 }
							}
							pageOrchestrator.refresh();
						}
					});
			});
			div.appendChild(button);
			document.getElementById("rightSection").appendChild(div);
		}
		
		this.getListOfNewCategories = function(parentID){
			var self=this;
			let list = [];
			let newCategoryID,lastChildrenOfParent;
			lastChildrenOfParent = self.findLastChildrenID(parentID);
			if(lastChildrenOfParent % 10 == 9){
				throw new Error("The drop position selected already has nine children!")
			}
			if(lastChildrenOfParent!=0){
				newCategoryID= lastChildrenOfParent+1;
			}
			else{
				newCategoryID = (parentID*10) + 1;
			}
			try{
				self.categoriesBeingDragged.forEach(function(category){
					//calculating new IDs for the categories and adding them to the list
					let idString = category.categoryID.toString();	
					let categoryIDString = self.categoriesBeingDragged[0].categoryID.toString();
					let newCategoryIDString = newCategoryID.toString();
					let temp= idString.substring(categoryIDString.length);
					let newIDString = newCategoryIDString + temp;
					if(newIDString.length >= 18){
						throw new Error("temp")
					}
					if(newIDString.length !== 1)
						list.push(new Category(parseInt(newIDString),category.name,parseInt(newIDString.slice(0,-1))));
					else
						list.push(new Category(parseInt(newIDString),category.name,0));
					
				});
				return list;
			}catch(error){
				throw new Error("This is due to the fact that the website support category IDs only up to 18 digits. Select another destination.")
			}
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
		
		this.selectCategoriesIDsBeingDragged = function (categoryID){
			var self=this;
			let temp = [];
			temp.push(categoryID);
			self.categoriesList.forEach(function(categoryInList){
				if(categoryInList.parentID === categoryID){
					Array.prototype.push.apply(temp,self.selectCategoriesIDsBeingDragged(categoryInList.categoryID) );
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
								default: {
									 alert("Unknown error occurred.");
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
			if(list !== null){
				list.forEach(function(category) {
					option = document.createElement("option");
					option.text = category.categoryID;
					option.value = category.categoryID;
					self.parentIDCreation.appendChild(option);
				});
			}
			if(self.createCategoryForm.classList.contains('hide')){
				self.createCategoryForm.classList.replace('hide', 'box');
			}
		}
	}
	
	
	function LogoutManager(_logoutButton){
		this.logoutButton = _logoutButton;
		
		this.setEvent = function() {
			let self = this;
			
			self.logoutButton.addEventListener('click', (e) => {
				makeCall('GET', 'Logout', null, function(x){
					if(x.readyState === XMLHttpRequest.DONE) {
					 let response = x.responseText;
					 
					 switch(x.status) {
						 case 200: {
							 sessionStorage.removeItem('username');
							 window.location.href = "index.html";
							 break;
						 }
						 default: {
							 alert("Unknown error occurred.");
							 break;
						 }
					 }
				 }
				})
			});
		}
		
		this.show = function() {
			let message = document.getElementById("logoutMessage");
			if(message.classList.contains('hide')) {
				message.classList.remove('hide');
			}
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