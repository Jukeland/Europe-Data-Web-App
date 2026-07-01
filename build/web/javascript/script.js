/* global L */

/**
 * sends a GET request to the server about creating the database
 * @returns {undefined}
 */
function createDB(){

    const xhr = new XMLHttpRequest();
    xhr.open('GET', 'InitDB');
    xhr.send();

    xhr.onload = function () {
        if(xhr.readyState === 4 && xhr.status === 200)
              console.log("The database has been created successfully");
        else if (xhr.status !== 200)
             console.log("The database has already been created");
    };
    
}

/**
 * sends a POST request to the server about getting a random fun fact
 * @param {String} country the name of the country you want to get the fun fact about
 * @returns {String}
 */
async function getFunFact(country){

    try{
        
        const response = await fetch(`GetFunFact?country=${encodeURIComponent(country)}`);

        if(response.ok){

            const res = await response.text(); 
            return res; 
            
        }else{
            console.error("Server error:", response.status);
            return "Fun fact unavailable.";
        }
        
    }catch (error){
        console.error("Network error:", error);
        return "Could not connect to server.";
    }
    
}

/*
 * replaces the country name for special cases
 * @param {string} country_name the country's name
 * @returns {String} the new country's name
 */
function getDifferentName(country_name){
    
    if(country_name === "Republic of Serbia")
        return "Serbia";
    if(country_name === "Macedonia")
        return "North Macedonia";
    if(country_name === "United Kingdom")
        return "United Kingdom of Great Britain and Northern Ireland";
    if(country_name === "United Kingdom of Great Britain and Northern Ireland")
        return "United Kingdom";
    if(country_name === "Russia")
        return "Russian Federation";
    if(country_name === "Russian Federation")
        return "Russia";
    if(country_name === "Moldova")
        return "Moldova (Republic of)"; 
    if(country_name === "Moldova (Republic of)")
        return "Moldova"; 
    if(country_name === "Kosovo")
        return "Republic of Kosovo";
    if(country_name === "Republic of Kosovo")
        return "Kosovo";

    return country_name;
    
}

const map = L.map('map').setView([51.505, 15.0], 4);

L.tileLayer('https://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}{r}.png', {
    attribution: '&copy; OpenStreetMap contributors, &copy; CARTO'
}).addTo(map);

const formatNumber = (num) => num.toLocaleString('en-US');

let highlightedLayer = null;

/**
 * gets the pop-up card template html file
 * @returns the html code as text
 */
async function getPopupTemplate(){

    const response = await fetch('popup_template.html');
    return await response.text();

}

/**
 * calls the RestCountries API through the back-end for safety reasons
 * @returns a dictionary that maps each country's name to its data
 */
/*async function getEuropeanCountries(){
    
    try{
       
        const response = await fetch('CallRestCountriesAPI');

        if(!response.ok){
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const jsonResponse = await response.json();
        const countriesArray = jsonResponse.data.objects;

        const dictionary = {};
        countriesArray.forEach(country => { 
            dictionary[country.names.common] = country; 
        });
        //console.log("dictionary: ", dictionary);
        return dictionary;

    }catch(error){
        console.error("Failed to fetch European countries:", error);
    }
    
}*/

/**
 * fetches the European countries directly from the free ApiCountries API
 * @returns a dictionary that maps each country's name to its data
 */
async function getEuropeanCountries(){
    
    try{
        
        const response = await fetch('CallApiCountries');

        if(!response.ok)
            throw new Error(`HTTP error! status: ${response.status}`);
        
        const countriesArray = await response.json();

        const dictionary = {};
        countriesArray.forEach(country => { 
            dictionary[country.name] = country; 
        });
        
        return dictionary;

    }catch(error){
        console.error("Failed to fetch European countries:", error);
    }
    
}

/**
 * gets the exchange rates for each currency from the API
 * @returns the exchange rates
 */
/*async function getExchangeRates() {
    try{
        const response = await fetch('https://open.er-api.com/v6/latest/EUR');
        const data = await response.json();
        return data.rates;
    }catch(e){
        console.warn("Rates failed to load.");
        return {};
    }
}*/

/**
 * gets the exchange rates for each currency from the API
 * @returns the exchange rates
 */
async function getExchangeRates(){
    
    try{

        const response = await fetch('https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api@latest/v1/currencies/eur.json');
        
        if(!response.ok){
            
            console.warn("Primary CDN failed, trying fallback...");
            const fallbackResponse = await fetch('https://latest.currency-api.pages.dev/v1/currencies/eur.json');
            if(!fallbackResponse.ok)
                throw new Error("Both exchange rate endpoints failed.");
            
            const fallbackData = await fallbackResponse.json();
            return processRates(fallbackData);
            
        }
        
        const data = await response.json();
        return processRates(data);

    }catch(e){
        console.warn("Exchange rates failed to load:", e);
        return {};
    }
    
}

/**
 * helper function to map the lowercase API keys to uppercase
 * @param {string} data the lowercase currency code
 * @returns {string} the uppercase currency code
 */
function processRates(data){
    
    const rawRates = data.eur;
    const uppercaseRates = {};
    
    // loop through all lowercase currencies and force them to uppercase
    for(const [currencyCode, rateValue] of Object.entries(rawRates)){
        uppercaseRates[currencyCode.toUpperCase()] = rateValue;
    }
    
    return uppercaseRates;
    
}

/**
 * function to get the map borders from the API to use at the map
 * @returns the json response from the API
 */
async function getMapBoundaries() {
    const response = await fetch('https://raw.githubusercontent.com/johan/world.geo.json/master/countries.geo.json');
    return await response.json();
}

/**
 * the main function that builds the app page
 */
async function startApp(){
    
    try{
        
        // fetch the heavy map boundaries, utilizing sessionStorage caching so "Back" navigation is instant
        let mapBoundaries;
        const cachedMap = sessionStorage.getItem('mapBoundaries');
        if(cachedMap)
            mapBoundaries = JSON.parse(cachedMap);
        else{
            mapBoundaries = await getMapBoundaries();
            sessionStorage.setItem('mapBoundaries', JSON.stringify(mapBoundaries));
        }

        // fetch the other 3 data sources at the same time
        const [htmlTemplate, euroData, exchangeRates] = await Promise.all([
            getPopupTemplate(),
            getEuropeanCountries(),
            getExchangeRates()
        ]);
        
        // preload the flags silently in the background
        Object.values(euroData).forEach(country => {
            const preloadImg = new Image();
            preloadImg.src = country.flags.png; 
        });

        // render the map borders
        const geoLayer = L.geoJSON(mapBoundaries, {
            filter: (feature) => {
                const mappedName = getDifferentName(feature.properties.name);
                return euroData.hasOwnProperty(mappedName);
            },
            style: () => { return { color: "#ffffff", weight: 1.2, fillColor: "#3498db", fillOpacity: 0.65 }; },
            
            onEachFeature: (feature, layer) => {
                let geoName = getDifferentName(feature.properties.name);
                const countryInfo = euroData[geoName];
                geoName = getDifferentName(geoName);
                if(geoName === "Czech Republic")
                    geoName = "Czechia";
                
                // when the user hovers a country border make it a different color
                layer.on('mouseover', (e) => {
                    if(highlightedLayer) geoLayer.resetStyle(highlightedLayer);
                    highlightedLayer = e.target;
                    e.target.setStyle({ fillOpacity: 0.9, fillColor: "#6366f1" });
                    e.target.bringToFront();
                });

                // when the user's cursor leaves the country border go back to the original style
                layer.on('mouseout', (e) => {
                    geoLayer.resetStyle(e.target);
                    if(highlightedLayer === e.target)
                        highlightedLayer = null;
                });

                 // when the user clicks on a country
                layer.on('click', async (e) => {
                    
                    // get the currency code and name from the API
                    let currCode = countryInfo.currencies[0].code;
                    let currName = countryInfo.currencies[0].name;
                    
                    // special cases for the currency names that don't fit in the display box
                    if(geoName === "Iceland" || geoName === "Norway" || geoName === "Belarus" || geoName === "Ukraine" || geoName === "Bosnia and Herzegovina" || geoName === "Hungary"){
                        let words = currName.split(' ');
                        //currName = words[1].charAt(0).toUpperCase() + words[1].slice(1);
                        let lastWord = words.pop();
                        currName = lastWord.charAt(0).toUpperCase() + lastWord.slice(1);
                    }else{
                        currName = currName.charAt(0).toUpperCase() + currName.slice(1);
                    }
                    
                    // bypass the API with the new Bulgarian currency
                    if(geoName === "Bulgaria"){
                        currName = "Euro";
                        currCode = "EUR"; 
                    }
                    
                    // get the exchange rate from the API
                    let rateText = "1.00 EUR";
                    if(currCode !== 'EUR' && exchangeRates[currCode])
                        rateText = `${exchangeRates[currCode]} ${currCode}`;
                    else if(currCode !== 'EUR')
                        rateText = "Rate unavailable";
                    
                    // get a random fun fact from the database
                    const funFact = await getFunFact(geoName);
                   
                   // replace all placeholders with the actual data
                    const finalHTML = htmlTemplate
                        .replaceAll('{{COUNTRY_NAME}}', geoName)
                        .replaceAll('{{COUNTRY_URL}}', encodeURIComponent(geoName))
                        .replace('{{FLAG_URL}}', countryInfo.flags.png)
                        .replace('{{POPULATION}}', formatNumber(countryInfo.population))
                        .replace('{{CURRENCY}}', currName)
                        .replace('{{EXCHANGE_RATE}}', rateText)
                        .replace('{{FUN_FACT}}', funFact);

                    L.popup({ className: 'popup' })
                        .setLatLng(e.latlng)
                        .setContent(finalHTML)
                        .openOn(map);
                });
            }
        }).addTo(map);

    } catch(error) {
        console.error("Failed to start the map app:", error);
    }
}


/**
 * function to set-up the login form when the login button is pressed
 */
document.addEventListener('DOMContentLoaded', async () => {
    
    try{
        const response = await fetch('login_popup.html');
        if(!response.ok)
            throw new Error("Could not load login popup HTML");
        
        const popupHtml = await response.text();
        document.body.insertAdjacentHTML('beforeend', popupHtml);
    }catch(error){
        console.error("Failed to inject login component:", error);
        return;
    }

    const loginButton = document.getElementById('login_button');
    const loginOverlay = document.getElementById('login_overlay');
    const closeButton = document.getElementById('close_button');
    const loginForm = document.getElementById('login_form');
    const loginMessage = document.getElementById('login_message');
    
    loginButton.addEventListener('click', () => {
        loginOverlay.style.display = 'flex';
        loginMessage.style.display = 'none'; 
        loginForm.reset(); 
    });
    
    closeButton.addEventListener('click', () => {
        loginOverlay.style.display = 'none';
    });

    loginOverlay.addEventListener('click', (e) => {
        if (e.target === loginOverlay) {
            loginOverlay.style.display = 'none';
        }
    });

});

/**
 * sends a POST request to the server about the login
 */
function loginPOST(){
    
    let myForm = document.getElementById("login_form");
    let formData = new FormData(myForm);
    const data = {};
    formData.forEach((value, key) => data[key] = value);
    let jsonData = JSON.stringify(data);
    console.log("logged_in_username: " + data["username"]);
    console.log("logged_in_password: " + data["password"]);
    
    const xhr = new XMLHttpRequest();
    xhr.open('POST', 'Login');
    xhr.send(jsonData);

    xhr.onload = function () {
        if(xhr.readyState === 4 && xhr.status === 200){
            const res = JSON.parse(xhr.responseText);
            window.location.href = 'LoggedInChecker';
        }else if(xhr.status === 401){
            $("#login_message").html("Provide a correct username and password");
            $("#login_message").show();
        }
    };
    
}