const FILE_CONFIG = [
    { id: "male_population_csv", key: "male_population", label: "Male Population" },
    { id: "female_population_csv", key: "female_population", label: "Female Population" },
    { id: "inflation_csv", key: "inflation", label: "Inflation" },
    { id: "migration_csv", key: "migration", label: "Migration" }
];

/**
 * shows the appropriate message for the uploads 
 * @param {type} titleText
 * @param {type} type
 * @param {type} errorLines
 * @returns {undefined}
 */
function showStatus(titleText, type, errorLines = []){
    
    const statusDiv = document.getElementById("status_message");
    const titleEl = document.getElementById("status_title");
    const reportContainer = document.getElementById("report_container");

    statusDiv.className = `status-message ${type}`;
    reportContainer.innerHTML = "";
    titleEl.textContent = titleText;

    errorLines.forEach(error => {
        const row = document.createElement("div");
        row.textContent = `${error.label}: `; 

        const statusSpan = document.createElement("span");
        statusSpan.textContent = error.statusText;
        statusSpan.className = "text-error"; 

        row.appendChild(statusSpan);
        reportContainer.appendChild(row);
    });

    statusDiv.style.display = "block";
    
}

/**
 * 
 * @returns {undefined}
 */
function resetInputVisuals(){
    
    FILE_CONFIG.forEach(file => {
        const el = document.getElementById(file.id);
        if(el)
            el.classList.remove("input-success", "input-error");
    });
    
}

/**
 * 
 * @param {type} inputId
 * @param {type} isSuccess
 * @returns {undefined}
 */
function updateInputState(inputId, isSuccess){
    
    const el = document.getElementById(inputId);
    if(!el)
        return;
    
    if(isSuccess)
        el.classList.add("input-success");
    else
        el.classList.add("input-error");
    
}

/**
 * submits
 * @param {type} event
 * @returns {undefined}
 */
async function submitForm(event){
    
    event.preventDefault();

    const formData = new FormData();
    const activeFiles = [];

    FILE_CONFIG.forEach(fileConfig => {
        const inputElement = document.getElementById(fileConfig.id);
        const file = inputElement ? inputElement.files[0] : null;
        
        if (file) {
            formData.append(fileConfig.key, file);
            activeFiles.push(fileConfig);
        }
    });
    
    if(activeFiles.length === 0){
        showStatus("Please select at least one CSV file to upload.", "error");
        return;
    }
    
    showStatus("Uploading files and validating...", "loading");
    resetInputVisuals(); 
    
    try{
       
        const response = await fetch('UploadCSV', {
            method: 'POST',
            body: formData
        });

        if(!response.ok)
            throw new Error(`Server Error: ${response.status}`);

        const data = await response.json(); 

        if(data.error){
            showStatus(`Fatal Server Error: ${data.error}`, "error");
            return;
        }

        const errorData = [];
        let allSuccess = true;

        activeFiles.forEach(fileConfig => {
            const serverMessage = data[fileConfig.key];
            
            let isSuccess;
            if(serverMessage === 'Success')
                isSuccess = true;
            else
                isSuccess = false;
            
            updateInputState(fileConfig.id, isSuccess);
            
            if(!isSuccess){
                errorData.push({ label: fileConfig.label, statusText: serverMessage });
                allSuccess = false;
            }
        });

        if(allSuccess){
            showStatus("The database was updated successfully.", "success");
        }else{
            showStatus("Upload completed with errors:", "error", errorData);
        }

    }catch (error){
        console.error('Upload Error:', error);
        showStatus("Network Error: Could not reach the server.", "error");
    }
    
}

/**
 * sends a POST request to the server about the logout
 */
async function logout(){
    
    const response = await fetch('Logout', {
            method: 'POST'
        });

        const data = await response.json();

        if(data.status === 'ok'){
            window.location.href = 'index.html';
        }else{
            console.error("Logout failed on the server.");
            alert("Could not log out. Please try again.");
        }
    
}