// // === WORLD CLOCK — app.js ===

   // Live UTC Clock
   function updateUTCClock() {
       const now = new Date();
       const h = String(now.getUTCHours()).padStart(2, '0');
       const m = String(now.getUTCMinutes()).padStart(2, '0');
       const s = String(now.getUTCSeconds()).padStart(2, '0');
       const el = document.getElementById('utc-clock');
       if (el) el.textContent = `${h}:${m}:${s}`;
   }

   // Live city clocks — update every second using browser Intl API
   function updateCityClocks() {
       const timeEls = document.querySelectorAll('.city-time[data-timezone]');
       timeEls.forEach(el => {
           const tz = el.getAttribute('data-timezone');
           try {
               const now = new Date();
               const timeStr = now.toLocaleTimeString('el-GR', {
                   timeZone: tz,
                   hour: '2-digit',
                   minute: '2-digit',
                   second: '2-digit',
                   hour12: false
               });
               el.textContent = timeStr;
           } catch (e) {
               // Keep server-rendered value
           }
       });
   }

   // Stagger card animations on load
   function initCardAnimations() {
       const cards = document.querySelectorAll('.city-card');
       cards.forEach((card, i) => {
           card.style.animationDelay = `${i * 0.05}s`;
       });
   }

   // Toast notification
   function showToast(msg, isError = false) {
       const toast = document.getElementById('toast');
       toast.textContent = msg;
       toast.style.borderColor = isError ? 'var(--danger)' : 'var(--accent)';
       toast.classList.add('show');
       setTimeout(() => toast.classList.remove('show'), 3000);
   }

   // Build a city card HTML from data
   function buildCityCard(city) {
       const temp = isNaN(city.temperature) ? 'N/A' :
           `${Math.round(city.temperature)}°C`;
       const feelsLike = isNaN(city.feelslike) ? '--' :
           `Αίσθηση ${Math.round(city.feelslike)}°`;

       return `
       <div class="city-card" data-timezone="${city.timezone}" data-weather="${city.weathermain}" style="animation-delay:0s">
           <div class="card-top">
               <div class="city-header">
                   <div>
                       <h2 class="city-name">${city.name}</h2>
                       <p class="city-country">${city.country}</p>
                   </div>
                   <div class="weather-icon">${city.weathericon || '❓'}</div>
               </div>
               <div class="time-display">
                   <span class="city-time" data-timezone="${city.timezone}">${city.localtime}</span>
               </div>
               <div class="date-row">
                   <span class="city-day">${city.dayofweek}</span>
                   <span class="city-date">${city.date}</span>
               </div>
           </div>
           <div class="card-weather">
               <div class="temp-main">
                   <span class="temp-value">${temp}</span>
                   <span class="weather-desc">${city.weatherdescription}</span>
               </div>
               <div class="weather-details">
                   <div class="weather-detail">
                       <span class="detail-icon">🌡️</span>
                       <span>${feelsLike}</span>
                   </div>
                   <div class="weather-detail">
                       <span class="detail-icon">💧</span>
                       <span>${city.humidity}%</span>
                   </div>
                   <div class="weather-detail">
                       <span class="detail-icon">💨</span>
                       <span>${(city.windspeed || 0).toFixed(1)} m/s</span>
                   </div>
               </div>
           </div>
           <div class="card-footer">
               <span class="timezone-badge">${city.timezone}</span>
               <button class="delete-btn" onclick="deleteCity(${city.id})" style="background: none; border: none; cursor: pointer; color: #ff4d4d; font-size: 1.2rem;" title="Διαγραφή Πόλης">❌</button>
           </div>
       </div>`;
   }

   // Add city via API
   async function addCity() {
       const name = document.getElementById('new-city-name').value.trim();
       const country = document.getElementById('new-city-country').value.trim();
       const timezone = document.getElementById('new-city-timezone').value;
       const lat = parseFloat(document.getElementById('new-city-lat').value);
       const lon = parseFloat(document.getElementById('new-city-lon').value);

       if (!name || !country || !timezone || isNaN(lat) || isNaN(lon)) {
           showToast('⚠️ Συμπληρώστε όλα τα πεδία!', true);
           return;
       }

       const btn = document.querySelector('.add-btn');
       btn.textContent = 'Φόρτωση...';
       btn.disabled = true;

       try {
           const params = new URLSearchParams({ name, country, timezone, latitude: lat, longtitude: lon });
           const res = await fetch(`/api/add-city?${params.toString()}`);

           if (!res.ok) throw new Error('Server error');
           const city = await res.json();

           const grid = document.getElementById('city-grid');
           const cardHtml = buildCityCard(city);
           const temp = document.createElement('div');
           temp.innerHTML = cardHtml;
           grid.appendChild(temp.firstElementChild);

           // Clear form
           ['new-city-name','new-city-country','new-city-lat','new-city-lon'].forEach(id => {
               document.getElementById(id).value = '';
           });
           document.getElementById('new-city-timezone').value = '';

           showToast(`✅ Η ${name} προστέθηκε!`);

           // Αυξάνουμε το μετρητή των πόλεων (για να ενημερώνεται πάνω αριστερά)
           const countEl = document.querySelector('.stat-num');
           if (countEl) countEl.textContent = parseInt(countEl.textContent) + 1;

       } catch (err) {
           showToast('❌ Σφάλμα κατά την προσθήκη πόλης.', true);
       } finally {
           btn.textContent = 'Προσθήκη';
           btn.disabled = false;
       }
   }

   // Refresh all weather data every 10 minutes
   async function refreshWeather() {
       try {
           const res = await fetch('/api/cities');
           const cities = await res.json();
           const cards = document.querySelectorAll('.city-card');
           cities.forEach((city, i) => {
               if (cards[i]) {
                   const tempEl = cards[i].querySelector('.temp-value');
                   const descEl = cards[i].querySelector('.weather-desc');
                   const iconEl = cards[i].querySelector('.weather-icon');
                   if (tempEl && !isNaN(city.temperature)) {
                       tempEl.textContent = `${Math.round(city.temperature)}°C`;
                   }
                   if (descEl) descEl.textContent = city.weatherdescription;
                   if (iconEl) iconEl.textContent = city.weathericon;
               }
           });
       } catch(e) { /* silent fail */ }
   }

   // === INIT ===
   document.addEventListener('DOMContentLoaded', () => {
       initCardAnimations();
       updateUTCClock();
       updateCityClocks();

       // Tick every second
       setInterval(() => {
           updateUTCClock();
           updateCityClocks();
       }, 1000);

       // Refresh weather every 10 minutes
       setInterval(refreshWeather, 10 * 60 * 1000);
   });
function deleteCity(cityId) {
    if (confirm("Είσαι σίγουρος ότι θέλεις να σβήσεις αυτή την πόλη;")) {
        fetch('/api/delete-city/' + cityId, {
            method: 'DELETE'
        })
        .then(response => {
            if (response.ok) {
                window.location.reload();
            } else {
                alert("Υπήρξε πρόβλημα κατά τη διαγραφή.");
            }
        })
        .catch(error => console.error('Σφάλμα:', error));


        function sortCities(criteria) {
            // 1. Βρίσκουμε το πλαίσιο που έχει μέσα όλες τις κάρτες
            const grid = document.getElementById('city-grid');

            // 2. Παίρνουμε όλες τις κάρτες και τις κάνουμε Λίστα (Array)
            const cards = Array.from(grid.getElementsByClassName('city-card'));

            // 3. Βάζουμε τους κανόνες ταξινόμησης
            cards.sort((a, b) => {
                if (criteria === 'name') {
                    const nameA = a.getAttribute('data-name');
                    const nameB = b.getAttribute('data-name');
                    // Το localeCompare ταξινομεί τέλεια και τα Ελληνικά!
                    return nameA.localeCompare(nameB, 'el');

                } else if (criteria === 'temp-desc') {
                    const tempA = parseFloat(a.getAttribute('data-temp')) || -999;
                    const tempB = parseFloat(b.getAttribute('data-temp')) || -999;
                    return tempB - tempA; // Από την πιο ζεστή στην πιο κρύα

                } else if (criteria === 'temp-asc') {
                    const tempA = parseFloat(a.getAttribute('data-temp')) || 999;
                    const tempB = parseFloat(b.getAttribute('data-temp')) || 999;
                    return tempA - tempB; // Από την πιο κρύα στην πιο ζεστή
                }
            });

            // 4. Αδειάζουμε το παλιό πλέγμα και βάζουμε τις κάρτες με τη νέα σειρά!
            grid.innerHTML = '';
            cards.forEach(card => grid.appendChild(card));
        }
    }
}