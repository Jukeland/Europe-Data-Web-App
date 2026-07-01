let popChartInstance = null;
let infChartInstance = null;
let migChartInstance = null;

let currentYearData = null;

/**
 * function to populate the dropdown lists on document load
 */
document.addEventListener('DOMContentLoaded', async () => {
    
    await populateYearsDropdown();
    const yearSelect = document.getElementById('yearSelect');
    yearSelect.addEventListener('change', (e) => {
        const selectedYear = e.target.value;
        if(selectedYear) 
            getCompareData(selectedYear);
        
    });
    
    document.getElementById('sortSelect').addEventListener('change', () => {
        if(currentYearData)
            renderAllCharts();
    });
    
});

/**
 * populates the dropdown list with the available years
 */
async function populateYearsDropdown(){
    
    const yearSelect = document.getElementById('yearSelect');
    
    try{
        
        const response = await fetch('GetAvailableYears');
        const years = await response.json();
        
        yearSelect.innerHTML = '';
        
        if(years.length === 0){
            yearSelect.innerHTML = '<option value="">No data available</option>';
            return;
        }

        years.forEach(year => {
            const option = document.createElement('option');
            option.value = year;
            option.textContent = year;
            yearSelect.appendChild(option);
        });

        getCompareData(years[0]);

    }catch (error){
        console.error("Failed to load years:", error);
        yearSelect.innerHTML = '<option value="">Error loading years</option>';
    }
    
}

/**
 * sends a GET request to the server for all the data for a specific year
 * @param {int} year
 */
async function getCompareData(year){
    
    try{
        
        const response = await fetch(`GetCompareData?year=${year}`);
        if(!response.ok)
            throw new Error("Network response was not ok");
        
        const data = await response.json();
        
        //console.log("Data received from server:", data); 

        if(data.error)
            throw new Error(data.error);
        
        currentYearData = data;
        renderAllCharts();

    }catch(error){
        console.error("Error loading charts:", error);
        alert("Could not load data for " + year + ". Check console for details.");
    }
    
}

/**
 * renders all charts for comparing the data
 */
function renderAllCharts(){
    
    const sortMethod = document.getElementById('sortSelect').value;
    
    popChartInstance = drawChart('populationChart', 'Population', currentYearData.population, '#4f46e5', popChartInstance, sortMethod);
    infChartInstance = drawChart('inflationChart', 'Inflation (%)', currentYearData.inflation, '#dc3545', infChartInstance, sortMethod);
    migChartInstance = drawChart('migrationChart', 'Migration', currentYearData.migration, '#28a745', migChartInstance, sortMethod);
    
}

/**
 * draws the a chart with the provided data
 * @param {type} canvasId the html canvas tag id
 * @param {type} labelText the text shown as the tilte for the chart
 * @param {type} dataObject the data to be displayed to the chart
 * @param {type} color the color code of the bars on the graph
 * @param {type} oldChartInstance the chart instance that existed before, used to destroy it before drawing the new chart
 * @param {type} sortMethod the sort method that will be used to display the data
 * @returns {Chart} the final chart 
 */
function drawChart(canvasId, labelText, dataObject, color, oldChartInstance, sortMethod){
    
    const canvas = document.getElementById(canvasId);
    
    if(!canvas){
        console.error(`Canvas ${canvasId} not found!`);
        return oldChartInstance;
    }
    
    if(oldChartInstance){
        oldChartInstance.destroy();
    }

    if(!dataObject || typeof dataObject !== 'object'){
        console.warn(`No valid data provided for ${labelText}`);
        return null;
    }

    let entries = Object.entries(dataObject);

    if(sortMethod === 'value_desc')
        entries.sort((a, b) => b[1] - a[1]); // Highest Number First
    else if(sortMethod === 'value_asc')
        entries.sort((a, b) => a[1] - b[1]); // Lowest Number First
    else if(sortMethod === 'alpha_asc')
        entries.sort((a, b) => a[0].localeCompare(b[0])); // A to Z
    else if(sortMethod === 'alpha_desc')
        entries.sort((a, b) => b[0].localeCompare(a[0])); // Z to A

    const sortedCountries = entries.map(entry => entry[0]);
    const sortedValues = entries.map(entry => entry[1]);

    const ctx = canvas.getContext('2d');
    const newChart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: sortedCountries,
            datasets: [{
                label: labelText,
                data: sortedValues,
                backgroundColor: color,
                borderRadius: 4
            }]
        },
        options: {
            indexAxis: 'y', 
            responsive: true,
            maintainAspectRatio: false, 
            plugins: {
                title: { display: true, text: `${labelText} Comparison` }      
            }
        }
    });

    return newChart; 
    
}