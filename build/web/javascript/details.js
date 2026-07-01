// extract the country name from the URL
const urlParams = new URLSearchParams(window.location.search);
const countryName = urlParams.get('country') || "Unknown Country";

// dynamically edit the heading of the page with the country's name
document.getElementById('country_name').innerText = `${countryName} Analytics`;

/* global variables */
let cleanPopulationData = null; 
let popChartInstance = null;
let genderChartInstance = null;

/**
 * requests the country's annual data from the server, sorts it 
 * @param {string} country the country's name
 */
async function getAnnualData(country){
    
    try{
        
        // request the country's annual data from the server
        const response = await fetch(`GetAnnualData?country=${encodeURIComponent(country)}`);

        if(response.ok){
            
            // parse the response as json and process it
            const data = await response.json(); 
            processAnnualData(data);
            
        }else{
            console.error("Server error:", response.status);
        }
        
    }catch(error){
        console.error("getAnnualData error:", error);
    }
    
}

/**
 * processes the country's annual data and feeds it to the chart drawing functions
 * @param data the country's annual data
 */
function processAnnualData(data){

    // process the population data and save it globally
    cleanPopulationData = data
        .filter(item => item.total_population !== null && item.total_population !== 0)
        .sort((a, b) => parseInt(a.year) - parseInt(b.year));
        
    const allYears = cleanPopulationData.map(item => item.year);
    populateRangeDropdowns(allYears);

    // render the default chart with the decades
    renderFilteredPopulation('decades');
    
    // filter out null years for the inflation data and feed it to the designated chart drawing function
    const infYears = [];
    const infRates = [];
    data.forEach(item => {
        if(item.inflation !== null) {
            infYears.push(item.year);
            infRates.push(item.inflation);
        }
    });
    showInflationChart({ years: infYears, rates: infRates });
    
    // filter out null years for the migration data and feed it to the designated chart drawing function
    const migYears = [];
    const migValues = [];
    data.forEach(item => {
        if(item.migration !== null) {
            migYears.push(item.year);
            migValues.push(item.migration);
        }
    });
    showMigrationChart({ years: migYears, values: migValues });
    
}

// ==========================================
// Range UI & Filtering Logic
// ==========================================

/**
 * populates the dropdown lists for the year selection with all available years
 * @param {type} yearsArray
 */
function populateRangeDropdowns(yearsArray){
    
    // get the html tags to populate
    const startSelect = document.getElementById('startYear');
    const endSelect = document.getElementById('endYear');
    
    // if somehow any of them does not exist, do nothing
    if(!startSelect || !endSelect)
        return;

    startSelect.innerHTML = '';
    endSelect.innerHTML = '';

    // for each available year, add it to the select tags dropdown list
    yearsArray.forEach(year => {
        startSelect.add(new Option(year, year));
        endSelect.add(new Option(year, year));
    });

    // put default values for the start and end years, the first and last available years
    if(yearsArray.length > 0){
        startSelect.value = yearsArray[0];
        endSelect.value = yearsArray[yearsArray.length - 1];
    }
    
}

/**
 * filters the population data according to the user's needs
 * @param {string} mode either 'decades' or 'range'
 */
function renderFilteredPopulation(mode){
    
    // if somehow the global variable cleanPopulationData is not initialized, do nothing
    if(!cleanPopulationData)
        return;

    let filteredObj = [];
    const startYear = parseInt(document.getElementById('startYear').value);
    const endYear = parseInt(document.getElementById('endYear').value);

    // filter the data through the years according to the mode 
    for(let i = 0; i < cleanPopulationData.length; i++){
        
        let year = parseInt(cleanPopulationData[i].year);
        
        if(mode === 'decades'){
            if(year % 10 === 0 || i === cleanPopulationData.length - 1)
                filteredObj.push(cleanPopulationData[i]);
        }else if(mode === 'range'){
            if(year >= startYear && year <= endYear) 
                filteredObj.push(cleanPopulationData[i]);            
        }
        
    }
    
    let decades = [];
    let population = [];
    let male_percentage = [];
    let female_percentage = [];
    
    // extract the data required for the population charts and feed it to the designated chart drawing function
    for(let i = 0; i < filteredObj.length; i++){
        decades.push(filteredObj[i].year);
        population.push(filteredObj[i].total_population);
        male_percentage.push(filteredObj[i].male_percentage);
        female_percentage.push(filteredObj[i].female_percentage);
    }
    showPopulationCharts({ decades, population, male_percentage, female_percentage });
    
}

/**
 *  event listener for the filter buttons:
 *  "Apply Filter" button clicked means mode="range"
 *  "Show Decaded" button clicked means mode="decades"
 */
document.addEventListener('DOMContentLoaded', () => {
    
    const applyButton = document.getElementById('applyRangeBtn');
    const resetButton = document.getElementById('resetDecadesBtn');

    if(applyButton){
        applyButton.addEventListener('click', () => {
            const start = parseInt(document.getElementById('startYear').value);
            const end = parseInt(document.getElementById('endYear').value);
            
            if(start > end){
                alert("Start year cannot be greater than end year!");
                return;
            }
            renderFilteredPopulation('range');
        });
    }

    if(resetButton){
        resetButton.addEventListener('click', () => {
            renderFilteredPopulation('decades');
        });
    }
    
});

// ==========================================
// Chart Drawing Functions
// ==========================================

/**
 * draws the population and gender distribution charts with the country's population data
 * @param data the country's population data
 */
function showPopulationCharts(data){
    
    if(popChartInstance) popChartInstance.destroy();
    if(genderChartInstance) genderChartInstance.destroy();

    const popCtx = document.getElementById('population_chart').getContext('2d');
    popChartInstance = new Chart(popCtx, {
        type: 'line',
        data: {
            labels: data.decades,
            datasets: [{
                label: 'Total Population',
                data: data.population,
                borderColor: '#6366f1',
                backgroundColor: 'rgba(99, 102, 241, 0.1)',
                borderWidth: 3,
                fill: true,
                tension: 0.4 
            }]
        },
        options: {
            responsive: true,
            plugins: { legend: { display: false } },
            scales: { y: { beginAtZero: false } }
        }
    });
    
    const genderCtx = document.getElementById('gender_chart').getContext('2d');
    genderChartInstance = new Chart(genderCtx, {
        type: 'bar',
        data: {
            labels: data.decades,
            datasets: [
                {
                    label: 'Female %',
                    data: data.female_percentage,
                    backgroundColor: '#ec4899',
                    borderRadius: 4
                },
                {
                    label: 'Male %',
                    data: data.male_percentage,
                    backgroundColor: '#3b82f6',
                    borderRadius: 4
                }
            ]
        },
        options: {
            responsive: true,
            scales: {
                x: { stacked: true }, 
                y: { 
                    stacked: true, 
                    min: 40, 
                    max: 60,
                    ticks: { callback: function(value) { return value + '%'; } }
                }
            },
            plugins: {
                tooltip: {
                    callbacks: { label: function(context) { return context.raw + '%'; } }
                }
            }
        }
    });
    
}

/**
 * draws the inflation chart with the country's inflation data. If no such data is available leave blank
 * @param data the country's inflation data
 */
function showInflationChart(data){
    
    const infChart = document.getElementById('inflation_chart').getContext('2d');
    new Chart(infChart, {
        type: 'line',
        data: {
            labels: data.years,
            datasets: [{
                label: 'Inflation',
                data: data.rates,
                borderColor: '#6366f1',
                backgroundColor: 'rgba(99, 102, 241, 0.1)',
                borderWidth: 3,
                fill: true,
                tension: 0.4
            }]
        },
        options: {
            responsive: true,
            plugins: { legend: { display: false } },
            scales: { y: { beginAtZero: false } }
        }
    });
    
}

/**
 * draws the migration chart with the country's migration data. If no such data is available leave blank
 * @param data the country's migration data
 */
function showMigrationChart(data){
    
    const migChart = document.getElementById('migration_chart').getContext('2d');
    new Chart(migChart, {
        type: 'line',
        data: {
            labels: data.years,
            datasets: [{
                label: 'Migration',
                data: data.values,
                borderColor: '#6366f1',
                backgroundColor: 'rgba(99, 102, 241, 0.1)',
                borderWidth: 3,
                fill: true,
                tension: 0.4
            }]  
        },
        options: {
            responsive: true,
            plugins: { legend: { display: false } },
            scales: { y: { beginAtZero: false } }
        }
    });
    
}

// on page load request for the country's data
getAnnualData(countryName);