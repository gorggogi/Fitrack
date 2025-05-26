if (!window.analyticsData) {
    console.error('analyticsData is not defined!');
} else {
// Get data from Thymeleaf
const completedGoalNameFromBackend = window.completedGoalNameFromBackend;
const analyticsData = window.analyticsData;

const workoutTypeData = analyticsData.workoutTypeData || [];
const caloriesData = analyticsData.dailyCaloriesBurned7Days || [];
const chartMeasurementData = analyticsData.chartMeasurementData || [];
const totalProtein = analyticsData.totalProtein7Days || 0;
const totalCarbs = analyticsData.totalCarbs7Days || 0;
const totalFats = analyticsData.totalFats7Days || 0;

// Workout Frequency Chart
const workoutCtx = document.getElementById('workoutFrequencyChart').getContext('2d');

// Define colors for different workout types
const workoutTypeColors = {
    'CARDIO': 'rgba(255, 99, 132, 0.7)',
    'STRENGTH': 'rgba(54, 162, 235, 0.7)',
    'FLEXIBILITY': 'rgba(255, 206, 86, 0.7)',
    'HIIT': 'rgba(75, 192, 192, 0.7)',
    'OTHER': 'rgba(153, 102, 255, 0.7)'
};

// Prepare datasets for the stacked bar chart
const datasets = workoutTypeData.map(typeData => ({
    label: typeData.type,
    data: typeData.counts,
    backgroundColor: workoutTypeColors[typeData.type] || 'rgba(201, 203, 207, 0.7)',
    borderColor: workoutTypeColors[typeData.type]?.replace('0.7', '1') || 'rgba(201, 203, 207, 1)',
    borderWidth: 1
}));

new Chart(workoutCtx, {
    type: 'bar',
    data: {
        labels: ['6 days ago', '5 days ago', '4 days ago', '3 days ago', '2 days ago', 'Yesterday', 'Today'],
        datasets: datasets
    },
    options: {
        responsive: true,
        scales: {
            x: {
                stacked: true
            },
            y: {
                stacked: true,
                beginAtZero: true,
                ticks: {
                    stepSize: 1
                }
            }
        },
        plugins: {
            title: {
                display: true,
                text: 'Workout Frequency by Type'
            },
            tooltip: {
                callbacks: {
                    label: function(context) {
                        let label = context.dataset.label || '';
                        if (label) {
                            label += ': ';
                        }
                        label += context.parsed.y + ' workout(s)';
                        return label;
                    }
                }
            }
        }
    }
});

// Calories Chart
const caloriesCtx = document.getElementById('caloriesChart').getContext('2d');
new Chart(caloriesCtx, {
    type: 'line',
    data: {
        labels: ['6 days ago', '5 days ago', '4 days ago', '3 days ago', '2 days ago', 'Yesterday', 'Today'],
        datasets: [{
            label: 'Calories Burned',
            data: caloriesData,
            fill: false,
            borderColor: 'rgba(255, 99, 132, 1)',
            tension: 0.1
        }]
    },
    options: {
        responsive: true,
        scales: {
            y: {
                beginAtZero: true
            }
        }
    }
});

// Body Composition Chart
const bodyCtx = document.getElementById('bodyCompositionChart').getContext('2d');

// --- Prepare Data for Chart (Sequential Style) --- 
const dateOptions = { month: 'short', day: 'numeric' }; // Format for labels

// Actual measurements data
const actualLabels = chartMeasurementData.map(m => m.dateTime ? new Date(m.dateTime).toLocaleDateString('en-US', dateOptions) : 'N/A');
const actualWeights = chartMeasurementData.map(m => m.weight);
const actualBmi = chartMeasurementData.map(m => m.bmi);

// Filter out null/undefined BMI values before calculating min/max
const validBmi = actualBmi.filter(bmi => bmi != null && !isNaN(bmi));
const minBmi = validBmi.length > 0 ? Math.min(...validBmi) - 1 : 10; // Default min if no valid BMI
const maxBmi = validBmi.length > 0 ? Math.max(...validBmi) + 1 : 40; // Default max

// Filter out null/undefined weight values before calculating min/max
const validWeights = actualWeights.filter(w => w != null && !isNaN(w));
const minWeight = validWeights.length > 0 ? Math.min(...validWeights) - 1 : 30; // Default min weight
const maxWeight = validWeights.length > 0 ? Math.max(...validWeights) + 1 : 150; // Default max weight

// --- Create Chart --- 
const bodyCompChart = new Chart(bodyCtx, {
    type: 'line',
    data: {
        labels: actualLabels,
        datasets: [
            {
                label: 'Weight (kg)',
                data: actualWeights,
                borderColor: 'rgba(75, 192, 192, 1)',
                backgroundColor: 'rgba(75, 192, 192, 0.2)',
                yAxisID: 'y',
                spanGaps: false 
            },
            {
                label: 'BMI',
                data: actualBmi,
                borderColor: 'rgba(255, 159, 64, 1)',
                backgroundColor: 'rgba(255, 159, 64, 0.2)',
                yAxisID: 'y2',
                spanGaps: false
            }
        ]
    },
    options: {
        responsive: true,
        interaction: {
            mode: 'index',
            intersect: false,
        },
        plugins: {
            title: {
                display: true,
                text: 'Body Composition Trends'
            },
            legend: {
                position: 'top',
            },
            tooltip: {
                mode: 'index',
                intersect: false,
            }
        },
        scales: {
            x: {
               title: {
                   display: true,
                   text: 'Measurement Date'
               }
            },
            y: {
                type: 'linear',
                display: true,
                position: 'left',
                title: {
                    display: true,
                    text: 'Weight (kg)'
                },
                min: minWeight,
                max: maxWeight
            },
            y2: {
                type: 'linear',
                display: true,
                position: 'right',
                title: {
                    display: true,
                    text: 'BMI'
                },
                min: minBmi,
                max: maxBmi,
                grid: {
                    drawOnChartArea: false 
                }
            }
        }
    }
});

// --- Add Event Listener for Toggle --- 
const toggleCheckbox = document.getElementById('toggleProjection');
if (toggleCheckbox) {
    toggleCheckbox.addEventListener('change', function() {
        // Dataset index 2 is the projection
        const isVisible = bodyCompChart.isDatasetVisible(2);
        bodyCompChart.setDatasetVisibility(2, !isVisible);
        bodyCompChart.update();
    });
}

// Nutrition Pie Chart
const nutritionCtx = document.getElementById('nutritionPieChart').getContext('2d');
new Chart(nutritionCtx, {
    type: 'pie',
    data: {
        labels: ['Protein', 'Carbohydrates', 'Fats'],
        datasets: [{
            data: [totalProtein || 0, totalCarbs || 0, totalFats || 0],
            backgroundColor: [
                'rgba(255, 99, 132, 0.8)',   // Protein - Red
                'rgba(54, 162, 235, 0.8)',   // Carbs - Blue
                'rgba(255, 206, 86, 0.8)'    // Fats - Yellow
            ],
            borderColor: [
                'rgba(255, 99, 132, 1)',
                'rgba(54, 162, 235, 1)',
                'rgba(255, 206, 86, 1)'
            ],
            borderWidth: 1
        }]
    },
    options: {
        responsive: true,
        plugins: {
            legend: {
                position: 'bottom'
            },
            title: {
                display: true,
                text: 'Macronutrient Distribution (Last 7 Days)'
            }
        }
    }
});

function showLoggedMeals() {
    const modal = document.getElementById('loggedMealsModal');
    const mealsList = document.getElementById('loggedMealsList');
    mealsList.innerHTML = ''; // Clear previous content

    // Get the meals data from the server
    fetch('/api/meals/last-7-days')
        .then(response => response.json())
        .then(meals => {
            meals.forEach(meal => {
                const mealElement = document.createElement('div');
                mealElement.className = 'logged-meal-item';
                mealElement.innerHTML = `
                    <div class="meal-date">${new Date(meal.dateTime).toLocaleDateString()}</div>
                    <div class="meal-name">${meal.mealName}</div>
                    <div class="meal-foods">
                        ${meal.foodItems.map(food => `
                            <div class="food-item">
                                <span class="food-name">${food.foodItem}</span>
                                <span class="food-macros">
                                    P: ${food.protein}g | C: ${food.carbs}g | F: ${food.fat}g
                                </span>
                            </div>
                        `).join('')}
                    </div>
                `;
                mealsList.appendChild(mealElement);
            });
        })
        .catch(error => {
            console.error('Error fetching meals:', error);
            mealsList.innerHTML = '<p class="error-message">Error loading meals. Please try again.</p>';
        });

    modal.classList.add('active');
}

function closeLoggedMeals() {
    const modal = document.getElementById('loggedMealsModal');
    modal.classList.remove('active');
}

function openMeasurementModal() {
    const modal = document.getElementById('measurementModal');
    // Set default date and time to current
    const now = new Date();
    const year = now.getFullYear();
    const month = String(now.getMonth() + 1).padStart(2, '0');
    const day = String(now.getDate()).padStart(2, '0');
    const hours = String(now.getHours()).padStart(2, '0');
    const minutes = String(now.getMinutes()).padStart(2, '0');
    
    const dateTimeInput = document.getElementById('measurementDateTime');
    dateTimeInput.value = `${year}-${month}-${day}T${hours}:${minutes}`;
    
    modal.classList.add('active');
}

function closeMeasurementModal() {
    const modal = document.getElementById('measurementModal');
    modal.classList.remove('active');
}

// Close modals when clicking outside
window.onclick = function(event) {
    if (event.target.classList.contains('modal')) {
        event.target.classList.remove('active');
    }
}

// Handle form submission
document.getElementById('measurementForm').addEventListener('submit', function(e) {
    e.preventDefault();
    const formData = new FormData(this);
    const submitButton = this.querySelector('button[type="submit"]');
    const originalButtonText = submitButton.textContent;
    submitButton.disabled = true;
    submitButton.textContent = 'Saving...';
    
    fetch(this.action, {
        method: 'POST',
        body: formData,
        credentials: 'same-origin',
        headers: {
            'X-Requested-With': 'XMLHttpRequest',
            'Accept': 'application/json' // Important: tell the server we want JSON back
        }
    })
    .then(response => {
        submitButton.disabled = false;
        submitButton.textContent = originalButtonText;
        if (!response.ok) {
            // Try to parse error as JSON, then fallback to text
            return response.json().catch(() => response.text()).then(errorData => {
                let errorMessage = "Failed to save measurement.";
                if (typeof errorData === 'string') {
                    errorMessage = errorData;
                } else if (errorData && errorData.message) {
                    errorMessage = errorData.message;
                }
                throw new Error(errorMessage);
            });
        }
        return response.json(); // Expect JSON response
    })
    .then(data => {
        closeMeasurementModal(); // Close the modal first!
        if (data.success) {
            if (data.completedGoalName) {
                // Ensure showGoalCompletedToast is available (it should be from dashboard.js)
                if (typeof showGoalCompletedToast === 'function') {
                    // Call toast AND tell it to store in session because we reload immediately after
                    showGoalCompletedToast(data.completedGoalName, true);
                } else {
                    console.warn('showGoalCompletedToast function not found, but goal was completed.');
                    // Fallback alert if toast function isn't loaded/working
                    alert('You have completed your goal: "' + data.completedGoalName + '"! Page will now reload.');
                }
                // Reload immediately after showing the toast
                window.location.reload();
            } else {
                // If no goal was completed, just reload immediately to show updated analytics
                window.location.reload();
            }
        } else {
            // Server indicated failure in the JSON response
            alert('Failed to save measurement: ' + (data.message || 'Unknown error'));
        }
    })
    .catch(error => {
        submitButton.disabled = false;
        submitButton.textContent = originalButtonText;
        console.error('Error saving measurement:', error);
        alert('Error saving measurement: ' + error.message);
    });
});

const RECOMMENDATIONS_CACHE_KEY = 'fitrackAnalyticsRecommendationsHTML';
const RECOMMENDATIONS_CACHE_TIMESTAMP_KEY = 'fitrackAnalyticsRecommendationsTimestamp';
const RECOMMENDATIONS_CACHE_BMI_CATEGORY_KEY = 'fitrackAnalyticsRecommendationsBmiCategory';
const CACHE_DURATION_MS = 6 * 60 * 60 * 1000; // 6 hours cache duration

async function estimateCaloriesForWorkout(workoutName, durationMinutes, calorieSpanId) {
    const csrfToken = document.querySelector('input[name="_csrf"]')?.value;
    if (!csrfToken) {
        console.error('CSRF token not found for calorie estimation.');
        if (document.getElementById(calorieSpanId)) {
            document.getElementById(calorieSpanId).textContent = 'Error';
        }
        return Promise.reject('CSRF token not found'); // Return a rejected promise
    }

    try {
        const response = await fetch(contextPath + 'workouts/estimate-calories', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': csrfToken
            },
            body: JSON.stringify({ 
                workoutName: workoutName,
                duration: durationMinutes 
            })
        });

        const data = await response.json();

        if (response.ok && data && typeof data.calories === 'number') {
            if (document.getElementById(calorieSpanId)) {
                document.getElementById(calorieSpanId).textContent = Math.round(data.calories);
            }
            return Promise.resolve(); // Resolve when successful
        } else {
            console.error('Failed to estimate calories:', data.error || response.statusText);
            if (document.getElementById(calorieSpanId)) {
                document.getElementById(calorieSpanId).textContent = 'N/A';
            }
            return Promise.reject(data.error || response.statusText); // Reject with error
        }
    } catch (error) {
        console.error('Error fetching calorie estimation:', error);
        if (document.getElementById(calorieSpanId)) {
            document.getElementById(calorieSpanId).textContent = 'N/A';
        }
        return Promise.reject(error); // Reject with error
    }
}

async function generateDynamicWorkoutRecommendations() {
    const recommendationContainer = document.getElementById('dynamic-workout-recommendations-content');
    if (!recommendationContainer) return;

    const personalizedRecommendationsSection = recommendationContainer.closest('.personalized-recommendations-section');
    if (!personalizedRecommendationsSection) {
        console.error("Could not find parent '.personalized-recommendations-section' to append refresh button.");
        return;
    }

    const refreshBtnId = 'refreshRecommendationsBtn';
    // Button to be full width and display as block
    const refreshBtnContainerId = 'refresh-recs-btn-container';
    const refreshBtnHtml = `<div id="${refreshBtnContainerId}" class="refresh-recs-container-bottom" style="margin-top: 20px;">
                                <button id="${refreshBtnId}" class="btn btn-secondary btn-sm" style="width: 100%; display: block;">Refresh Recommendations</button>
                            </div>`;

    function setupRefreshButtonListener(buttonElement) {
        if (buttonElement) {
            buttonElement.removeEventListener('click', handleRefreshRecommendations);
            buttonElement.addEventListener('click', handleRefreshRecommendations);
            console.log("Refresh button event listener attached (bottom).");
        } else {
            console.error("Refresh button (bottom) element not found or passed incorrectly. Listener not attached.");
        }
    }

    try {
        const cachedHTML = localStorage.getItem(RECOMMENDATIONS_CACHE_KEY);
        const cachedTimestamp = localStorage.getItem(RECOMMENDATIONS_CACHE_TIMESTAMP_KEY);
        const cachedBmiCategory = localStorage.getItem(RECOMMENDATIONS_CACHE_BMI_CATEGORY_KEY);
        const currentBmiCategory = window.analyticsData ? window.analyticsData.currentBmiCategory : null;

        if (cachedHTML && cachedTimestamp && cachedBmiCategory && 
            (Date.now() - parseInt(cachedTimestamp)) < CACHE_DURATION_MS && 
            currentBmiCategory === cachedBmiCategory) {
            console.log("Using cached workout recommendations (category match).");
            recommendationContainer.innerHTML = cachedHTML;

            // Ensure the refresh button is present when loading from cache
            // First, remove any existing one to prevent duplicates if any survived somehow
            const existingBtnContainer = document.getElementById(refreshBtnContainerId);
            if (existingBtnContainer) {
                existingBtnContainer.remove();
            }
            // Append the refresh button HTML again, as it's not part of the cached recommendationContainer.innerHTML
            personalizedRecommendationsSection.insertAdjacentHTML('beforeend', refreshBtnHtml);

            setTimeout(() => { // Keep a short delay for DOM update
                const button = document.getElementById(refreshBtnId);
                if (button) {
                    setupRefreshButtonListener(button);
                } else {
                    // This error should ideally not happen now if IDs are correct and insertion worked
                    console.error("Refresh button element not found after attempting to re-add it on cache load.");
                }
            }, 50);
            return; 
        }
    } catch (e) {
        console.error("Error reading recommendations from localStorage:", e);
    }
    
    console.log("Cache miss or stale. Fetching fresh workout recommendations.");
    recommendationContainer.innerHTML = '<p class="loading-text" style="text-align:center;">Loading recommendations...</p>'; 
    
    const bmiCategory = analyticsData.currentBmiCategory;
    const workoutFreq = analyticsData.workoutTypeFrequency || {}; 
    const avgExerciseCalories = analyticsData.avgDailyExerciseCalories;
    let recommendationsHtmlParts = []; 
    let allEstimationPromises = [];

    const WORKOUT_TYPES = {
        CARDIO: {
            name: 'Cardio',
            key: 'CARDIO',
            examples: [
                { name: 'Brisk Walking', detail: 'Aim for 30+ minutes at a pace where you can talk but feel your heart rate increase.', defaultDurationMinutes: 30 },
                { name: 'Running', detail: 'Builds endurance; start with a comfortable pace and gradually increase distance or speed.', defaultDurationMinutes: 20 },
                { name: 'Cycling', detail: 'Low-impact and great for leg strength; vary resistance or try interval speeds for 20-40 minutes.', defaultDurationMinutes: 30 },
                { name: 'Swimming', detail: 'Full-body workout that\'s gentle on the joints; focus on different strokes.', defaultDurationMinutes: 30 },
                { name: 'Jumping Jacks', detail: 'A simple full-body cardio to get your heart rate up quickly.', defaultDurationMinutes: 10 }
            ]
        },
        STRENGTH: {
            name: 'Strength Training',
            key: 'STRENGTH',
            examples: [
                { name: 'Squats', detail: 'Targets quads, hamstrings, and glutes; focus on depth and keeping your chest up.', defaultDurationMinutes: 20 },
                { name: 'Push-ups', detail: 'Versatile bodyweight exercise for upper body; modify on knees if needed, aiming for 3 sets.', defaultDurationMinutes: 15 },
                { name: 'Lunges', detail: 'Great for single-leg strength and balance; keep your front knee behind your toes.', defaultDurationMinutes: 15 },
                { name: 'Plank', detail: 'Core stability exercise; engage your abs and maintain a straight line from head to heels for 30-60 seconds.', defaultDurationMinutes: 1 },
                { name: 'Deadlifts', detail: 'Compound movement for posterior chain (back, glutes, hamstrings); prioritize form over weight.', defaultDurationMinutes: 20 },
                { name: 'Bench Press', detail: 'Works chest, shoulders, and triceps; ensure a stable setup and controlled movement.', defaultDurationMinutes: 20 }
            ]
        },
        FLEXIBILITY: {
            name: 'Flexibility & Mobility',
            key: 'FLEXIBILITY',
            examples: [
                { name: 'Hamstring Stretch', detail: 'Hold for 30 seconds per leg to improve flexibility in the back of your legs; avoid bouncing.', defaultDurationMinutes: 5 },
                { name: 'Quad Stretch', detail: 'Stand tall and hold for 30 seconds per leg to stretch the front of your thighs.', defaultDurationMinutes: 5 },
                { name: 'Yoga (e.g., Downward Dog)', detail: 'Stretches shoulders, hamstrings, and calves while building strength.', defaultDurationMinutes: 20 },
                { name: 'Pilates (e.g., The Hundred)', detail: 'Core-focused exercises that also emphasize breath control and flexibility.', defaultDurationMinutes: 20 }
            ]
        },
        HIIT: {
            name: 'HIIT',
            key: 'HIIT',
            examples: [
                { name: 'Sprint Intervals', detail: 'Alternate short bursts of all-out sprinting (20-30s) with recovery periods (60-90s).', defaultDurationMinutes: 15 },
                { name: 'Tabata Sprints', detail: '20 seconds of intense effort followed by 10 seconds of rest, repeated for 4 minutes per exercise.', defaultDurationMinutes: 10 },
                { name: 'Burpee Intervals', detail: 'Perform burpees at high intensity for a set time (e.g., 45s), rest briefly (e.g., 15s), and repeat.', defaultDurationMinutes: 10 }
            ]
        },
        BALANCE_STABILITY: {
            name: 'Balance & Stability',
            key: 'BALANCE_STABILITY',
            examples: [
                { name: 'Single-Leg Stands', detail: 'Improve balance by standing on one leg (30s each); try with eyes open, then closed for a challenge.', defaultDurationMinutes: 5 },
                { name: 'Tai Chi movements', detail: 'Slow, flowing movements that enhance balance, coordination, and mindfulness.', defaultDurationMinutes: 20 },
                { name: 'Heel-to-Toe Walk', detail: 'Improves balance and proprioception; walk in a straight line placing heel directly before toe.', defaultDurationMinutes: 5 }
            ]
        },
        FUNCTIONAL_TRAINING: {
            name: 'Functional Training',
            key: 'FUNCTIONAL_TRAINING',
            examples: [
                { name: "Farmer's Walks", detail: 'Builds grip strength and core stability; carry heavy weights for a set distance.', defaultDurationMinutes: 10 },
                { name: 'Kettlebell Swings', detail: 'Develops explosive power in hips and glutes; focus on a hip hinge, not a squat.', defaultDurationMinutes: 15 },
                { name: 'Medicine Ball Slams', detail: 'Full-body power exercise; slam the ball forcefully to the ground from overhead.', defaultDurationMinutes: 10 }
            ]
        },
        SPORTS_RECREATION: {
            name: 'Sports & Recreation',
            key: 'SPORTS_RECREATION',
            examples: [
                { name: 'Basketball', detail: 'Improves agility, coordination, and cardiovascular fitness through dynamic team play.', defaultDurationMinutes: 30 },
                { name: 'Hiking', detail: 'Enjoy nature while getting a great lower body and cardio workout; vary terrain for challenge.', defaultDurationMinutes: 45 },
                { name: 'Dancing', detail: 'A fun way to improve cardio, coordination, and mood; try different styles like Zumba or salsa.', defaultDurationMinutes: 30 }
            ]
        },
        OTHER: { name: 'Other Activities', key: 'OTHER', examples: [] }
    };

    const formatExamples = (examples, workoutTypeKey) => {
        if (!examples || examples.length === 0) return { html: '', promises: [] };
        const localPromises = [];
        const shuffled = [...examples].sort(() => 0.5 - Math.random());
        
        const minToShow = 3;
        const maxToShow = 5;
        let countToShow;

        if (examples.length <= minToShow) {
            countToShow = examples.length;
        } else {
            countToShow = Math.min(examples.length, Math.floor(Math.random() * (maxToShow - minToShow + 1)) + minToShow);
        }
        
        const selectedExamplesWithIds = shuffled.slice(0, countToShow).map(ex => ({
            ...ex,
            calorieSpanId: `calorie-span-${encodeURIComponent(ex.name.replace(/[^a-zA-Z0-9]/g, '-'))}-${Math.random().toString(36).substring(2, 7)}`
        }));

        if (selectedExamplesWithIds.length === 0) return { html: '', promises: [] };

        let exampleHtmlList = '<ul class="example-list workout-recommendation-detailed-list">';
        selectedExamplesWithIds.forEach((ex) => {
            const duration = ex.defaultDurationMinutes || 0;
            const escapedExName = ex.name.replace(/'/g, "\\''"); // Keep original escaping for onclick
            const isScheduled = analyticsData.scheduledWorkoutNames && analyticsData.scheduledWorkoutNames.includes(ex.name);
            const scheduledClass = isScheduled ? 'is-scheduled' : '';
            // Icon is now added/removed by refreshScheduledIndicators or during initial render by it
            const scheduledIndicatorHtml = isScheduled ? ' <i class="fas fa-calendar-check scheduled-indicator" title="Already in your schedule"></i>' : '';


            // Added data-workout-name attribute to the li
            exampleHtmlList += `<li class="workout-rec-item ${scheduledClass}" data-workout-name="${ex.name.replace(/"/g, '&quot;')}"> 
                                   <div onclick="openWorkoutModalWithRecommendation('${escapedExName}', ${duration}, '${workoutTypeKey}', '${ex.calorieSpanId}')" style="cursor: pointer;">
                                       <div class="workout-rec-main">
                                           <strong class="workout-rec-name">${ex.name}${scheduledIndicatorHtml}</strong>
                                           <span class="workout-rec-detail"> – ${ex.detail}</span>
                                       </div>
                                       <div class="workout-rec-stats">
                                           <span class="workout-rec-duration">Recommended: ${duration} mins</span>
                                           <span class="workout-rec-calories">Calories Burned: <span id="${ex.calorieSpanId}" class="estimated-calories-value">Loading...</span> kcal</span>
                                       </div>
                                   </div>
                              </li>`;
            if (duration > 0) {
                localPromises.push(estimateCaloriesForWorkout(ex.name, duration, ex.calorieSpanId));
            }
        });
        exampleHtmlList += '</ul>';
        return { html: exampleHtmlList, promises: localPromises };
    };
    
    const exampleIntroText = '<div class="recommendation-examples-intro">For instance:</div>';
    const allTypeKeys = Object.keys(WORKOUT_TYPES);
    const loggedTypeKeys = Object.keys(workoutFreq);
    const getFrequency = (typeKey) => workoutFreq[typeKey] || 0;

    if (!bmiCategory) {
        recommendationsHtmlParts.push('<div class="workout-recommendation-card"><p>Complete your profile (height and weight) to receive personalized workout recommendations.</p></div>');
    } else {
        if (bmiCategory === 'Underweight') {
            recommendationsHtmlParts.push('<div class="workout-recommendation-card"><h5>Goal Focus: Healthy Weight Gain & Muscle Building</h5></div>');
            if (getFrequency('STRENGTH') < 8) {
                const res = formatExamples(WORKOUT_TYPES.STRENGTH.examples, WORKOUT_TYPES.STRENGTH.key);
                let content = `<p>You should aim for 2-3 sessions weekly, focusing on compound movements and progressive overload.</p>${exampleIntroText}${res.html}`;
                allEstimationPromises.push(...res.promises);
                recommendationsHtmlParts.push(`<div class="workout-recommendation-card"><h5>Prioritize ${WORKOUT_TYPES.STRENGTH.name}</h5>${content}</div>`);
            } else {
                const res = formatExamples(WORKOUT_TYPES.STRENGTH.examples, WORKOUT_TYPES.STRENGTH.key);
                let content = `<p>Ensure progressive overload for continued muscle growth.</p>${exampleIntroText}${res.html}`;
                allEstimationPromises.push(...res.promises);
                recommendationsHtmlParts.push(`<div class="workout-recommendation-card"><h5>Continue Your Great Work with ${WORKOUT_TYPES.STRENGTH.name}</h5>${content}</div>`);
            }
            if (getFrequency('CARDIO') < 4 && avgExerciseCalories < 200) {
                const res = formatExamples(WORKOUT_TYPES.CARDIO.examples, WORKOUT_TYPES.CARDIO.key);
                let content = `<p>Aiming for 2-3 times per week (20-30 mins) for cardiovascular health.</p>${exampleIntroText}${res.html}`;
                allEstimationPromises.push(...res.promises);
                recommendationsHtmlParts.push(`<div class="workout-recommendation-card"><h5>Incorporate Moderate ${WORKOUT_TYPES.CARDIO.name}</h5>${content}</div>`);
            } else {
                const res = formatExamples(WORKOUT_TYPES.CARDIO.examples, WORKOUT_TYPES.CARDIO.key);
                let content = `<p>Balance your cardio with your strength goals. Ensure it's supportive, not excessive, for muscle gain.</p>${exampleIntroText}${res.html}`;
                allEstimationPromises.push(...res.promises);
                recommendationsHtmlParts.push(`<div class="workout-recommendation-card"><h5>Balance Your ${WORKOUT_TYPES.CARDIO.name}</h5>${content}</div>`);
            }
        } else if (bmiCategory === 'Overweight' || bmiCategory.toLowerCase().includes('obese')) {
            recommendationsHtmlParts.push('<div class="workout-recommendation-card"><h5>Goal Focus: Fat Loss & Improved Metabolic Health</h5></div>');
            const cardioRes = formatExamples(WORKOUT_TYPES.CARDIO.examples, WORKOUT_TYPES.CARDIO.key);
            allEstimationPromises.push(...cardioRes.promises);
            if (getFrequency('CARDIO') < 12) {
                let content = `<p>Aiming for 3-5 sessions of moderate-intensity for 30+ minutes.</p>${exampleIntroText}${cardioRes.html}`;
                recommendationsHtmlParts.push(`<div class="workout-recommendation-card"><h5>Increase Your ${WORKOUT_TYPES.CARDIO.name}</h5>${content}</div>`);
            } else {
                let content = `<p>Maintain 150-300 minutes of moderate-intensity cardio weekly. Consider varying type or intensity.</p>${exampleIntroText}${cardioRes.html}`;
                recommendationsHtmlParts.push(`<div class="workout-recommendation-card"><h5>Excellent Consistency with ${WORKOUT_TYPES.CARDIO.name}</h5>${content}</div>`);
            }
            const strengthRes = formatExamples(WORKOUT_TYPES.STRENGTH.examples, WORKOUT_TYPES.STRENGTH.key);
            allEstimationPromises.push(...strengthRes.promises);
            if (getFrequency('STRENGTH') < 8) {
                let content = `<p>Aiming for 2-3 times per week as this builds muscle and boosts metabolism.</p>${exampleIntroText}${strengthRes.html}`;
                recommendationsHtmlParts.push(`<div class="workout-recommendation-card"><h5>Incorporate ${WORKOUT_TYPES.STRENGTH.name}</h5>${content}</div>`);
            } else {
                let content = `<p>It's crucial for preserving muscle mass during fat loss.</p>${exampleIntroText}${strengthRes.html}`;
                recommendationsHtmlParts.push(`<div class="workout-recommendation-card"><h5>Keep Up ${WORKOUT_TYPES.STRENGTH.name}</h5>${content}</div>`);
            }
            if (avgExerciseCalories > 150 && getFrequency('HIIT') < 4) {
                const hiitRes = formatExamples(WORKOUT_TYPES.HIIT.examples, WORKOUT_TYPES.HIIT.key);
                allEstimationPromises.push(...hiitRes.promises);
                let content = `<p>These are an efficient way to boost calorie burn.</p>${exampleIntroText}${hiitRes.html}`;
                recommendationsHtmlParts.push(`<div class="workout-recommendation-card"><h5>Consider Adding ${WORKOUT_TYPES.HIIT.name}</h5>${content}</div>`);
            }
        } else if (bmiCategory === 'Normal') {
            recommendationsHtmlParts.push('<div class="workout-recommendation-card"><h5>Goal Focus: Maintain Health & Optimize Overall Fitness</h5></div>');
            const cardioRes = formatExamples(WORKOUT_TYPES.CARDIO.examples, WORKOUT_TYPES.CARDIO.key);
            allEstimationPromises.push(...cardioRes.promises);
            let cardioContent = `<p>Include regular sessions (3-5 times/week).</p>${exampleIntroText}${cardioRes.html}`;
            recommendationsHtmlParts.push(`<div class="workout-recommendation-card"><h5>Aim for Balanced ${WORKOUT_TYPES.CARDIO.name}</h5>${cardioContent}</div>`);
            
            const strengthRes = formatExamples(WORKOUT_TYPES.STRENGTH.examples, WORKOUT_TYPES.STRENGTH.key);
            allEstimationPromises.push(...strengthRes.promises);
            if (getFrequency('STRENGTH') < 8) {
                let strengthContent = `<p>Incorporate sessions (2-3 times per week) for muscle and bone health.</p>${exampleIntroText}${strengthRes.html}`;
                recommendationsHtmlParts.push(`<div class="workout-recommendation-card"><h5>Incorporate ${WORKOUT_TYPES.STRENGTH.name}</h5>${strengthContent}</div>`);
            } else {
                let strengthContent = `<p>Keep it consistent.</p>${exampleIntroText}${strengthRes.html}`;
                recommendationsHtmlParts.push(`<div class="workout-recommendation-card"><h5>Well Done on ${WORKOUT_TYPES.STRENGTH.name}</h5>${strengthContent}</div>`);
            }
            recommendationsHtmlParts.push('<div class="workout-recommendation-card"><p>Explore a variety of activities to keep your fitness journey engaging and well-rounded!</p></div>');
        }

        // General recommendations for flexibility, functional, balance
        const flexRes = formatExamples(WORKOUT_TYPES.FLEXIBILITY.examples, WORKOUT_TYPES.FLEXIBILITY.key);
        if (getFrequency('FLEXIBILITY') < 8 && flexRes.html) {
            allEstimationPromises.push(...flexRes.promises);
            let content = `<p>This improves range of motion and aids recovery.</p>${exampleIntroText}${flexRes.html}`;
            recommendationsHtmlParts.push(`<div class="workout-recommendation-card"><h5>Don't Forget ${WORKOUT_TYPES.FLEXIBILITY.name}</h5>${content}</div>`);
        }
        const funcRes = formatExamples(WORKOUT_TYPES.FUNCTIONAL_TRAINING.examples, WORKOUT_TYPES.FUNCTIONAL_TRAINING.key);
        if (getFrequency('FUNCTIONAL_TRAINING') < 4 && bmiCategory !== 'Underweight' && funcRes.html) {
             allEstimationPromises.push(...funcRes.promises);
             let content = `<p>It enhances everyday strength and movement.</p>${exampleIntroText}${funcRes.html}`;
             recommendationsHtmlParts.push(`<div class="workout-recommendation-card"><h5>Consider Adding ${WORKOUT_TYPES.FUNCTIONAL_TRAINING.name}</h5>${content}</div>`);
        }
        const balRes = formatExamples(WORKOUT_TYPES.BALANCE_STABILITY.examples, WORKOUT_TYPES.BALANCE_STABILITY.key);
        if (getFrequency('BALANCE_STABILITY') < 4 && balRes.html) {
            allEstimationPromises.push(...balRes.promises);
            let content = `<p>These exercises can improve coordination and reduce injury risk.</p>${exampleIntroText}${balRes.html}`;
            recommendationsHtmlParts.push(`<div class="workout-recommendation-card"><h5>Try Basic ${WORKOUT_TYPES.BALANCE_STABILITY.name}</h5>${content}</div>`);
        }
        
        let suggestedNew = 0;
        const highlyRecommendedForVariety = ['SPORTS_RECREATION', 'FUNCTIONAL_TRAINING', 'HIIT'];
        for (const typeKey of allTypeKeys) {
            if (typeKey === 'OTHER' || WORKOUT_TYPES[typeKey].examples.length === 0) continue;
            if (getFrequency(typeKey) === 0 && suggestedNew < 2) {
                if ( (bmiCategory === 'Normal' && highlyRecommendedForVariety.includes(typeKey)) || 
                     (bmiCategory !== 'Underweight' && typeKey === 'SPORTS_RECREATION') || 
                     (bmiCategory === 'Underweight' && typeKey === 'FLEXIBILITY') 
                   ) {
                    const res = formatExamples(WORKOUT_TYPES[typeKey].examples, WORKOUT_TYPES[typeKey].key);
                    if(res.html){
                        allEstimationPromises.push(...res.promises);
                        let content = `<p>${WORKOUT_TYPES[typeKey].name} could be a great addition to your routine.</p>${exampleIntroText}${res.html}`;
                        recommendationsHtmlParts.push(`<div class="workout-recommendation-card"><h5>Explore Something New: ${WORKOUT_TYPES[typeKey].name}</h5>${content}</div>`);
                        suggestedNew++;
                    }
                }
            }
        }
        if (loggedTypeKeys.length === 0 && recommendationsHtmlParts.length <= 2) {
            recommendationsHtmlParts.push('<div class="workout-recommendation-card"><p>Log your workouts regularly to get even more specific feedback and track your progress effectively.</p></div>');
        }
    }

    if (recommendationsHtmlParts.length === 0) {
        recommendationsHtmlParts.push('<div class="workout-recommendation-card"><p>Keep logging your activities to receive personalized workout recommendations!</p></div>');
    }
    
    const finalHtmlContent = recommendationsHtmlParts.join(''); // Refresh button HTML is NOT added here
    recommendationContainer.innerHTML = finalHtmlContent;
    
    // Remove existing button container if it exists to prevent duplicates on re-generation without full page refresh
    const existingBtnContainer = document.getElementById(refreshBtnContainerId);
    if (existingBtnContainer) {
        existingBtnContainer.remove();
    }
    // Append the refresh button to the parent section, after the workout recommendations content
    personalizedRecommendationsSection.insertAdjacentHTML('beforeend', refreshBtnHtml);

    // Get the button element immediately after inserting it
    const newRefreshButton = document.getElementById(refreshBtnId);
    if (newRefreshButton) {
        setupRefreshButtonListener(newRefreshButton);
    } else {
        // This case should be rare if insertAdjacentHTML worked and IDs are correct
        console.error("CRITICAL: Refresh button element could not be found immediately after insertion.");
    }
    
    try {
        await Promise.all(allEstimationPromises);
        console.log("All calorie estimations completed.");
    } catch (error) {
        console.warn("Some calorie estimations may have failed:", error);
    }

    try {
        localStorage.setItem(RECOMMENDATIONS_CACHE_KEY, recommendationContainer.innerHTML);
        localStorage.setItem(RECOMMENDATIONS_CACHE_TIMESTAMP_KEY, Date.now().toString());
        if (window.analyticsData && window.analyticsData.currentBmiCategory) {
            localStorage.setItem(RECOMMENDATIONS_CACHE_BMI_CATEGORY_KEY, window.analyticsData.currentBmiCategory);
        }
        console.log("Workout recommendations cached (with bottom button and BMI category).");
    } catch (e) {
        console.error("Error saving recommendations to localStorage:", e);
    }
}

function handleRefreshRecommendations() {
    console.log("Refreshing recommendations...");
    try {
        localStorage.removeItem(RECOMMENDATIONS_CACHE_KEY);
        localStorage.removeItem(RECOMMENDATIONS_CACHE_TIMESTAMP_KEY);
        localStorage.removeItem(RECOMMENDATIONS_CACHE_BMI_CATEGORY_KEY);

        // Also remove the button container itself if it was manually added outside the cache
        const btnContainer = document.getElementById('refresh-recs-btn-container');
        if (btnContainer) {
            btnContainer.remove();
        }

    } catch (e) {
        console.error("Error clearing recommendations from localStorage or removing button:", e);
    }
    generateDynamicWorkoutRecommendations(); // Regenerate
}

function updateCalorieTargetRecommendations() {
    if (!analyticsData || analyticsData.tdee == null || analyticsData.tdee <= 0) {
        return;
    }

    const tdee = analyticsData.tdee;
    const bmiCategory = analyticsData.currentBmiCategory ? analyticsData.currentBmiCategory.toLowerCase() : '';

    const weightLossCalories = Math.round(tdee - 500);
    const maintenanceCalories = Math.round(tdee);
    const weightGainCalories = Math.round(tdee + 500);

    document.getElementById('tdeeValue').textContent = `${maintenanceCalories} kcal`;
    document.getElementById('weightLossCalories').textContent = `${weightLossCalories} kcal`;
    document.getElementById('maintenanceCalories').textContent = `${maintenanceCalories} kcal`;
    document.getElementById('weightGainCalories').textContent = `${weightGainCalories} kcal`;

    // Get card elements
    const weightLossCard = document.getElementById('calorieTargetWeightLoss');
    const maintenanceCard = document.getElementById('calorieTargetWeightMaintenance');
    const weightGainCard = document.getElementById('calorieTargetWeightGain');

    // Hide all dietary cards by default
    if(weightLossCard) weightLossCard.style.display = 'none';
    if(maintenanceCard) maintenanceCard.style.display = 'none';
    if(weightGainCard) weightGainCard.style.display = 'none';

    // Dynamically calculate and set the rate text for weight loss
    const dailyDeficitForLoss = tdee - weightLossCalories;
    const weeklyDeficitForLoss = dailyDeficitForLoss * 7;
    const weeklyKgLoss = weeklyDeficitForLoss / 7700;
    if (weeklyKgLoss >= 0.05) {
        document.getElementById('weightLossRate').textContent = `(lose approx. ${weeklyKgLoss.toFixed(1)} kg/week)`;
    } else {
        document.getElementById('weightLossRate').textContent = '';
    }

    document.getElementById('maintenanceRate').textContent = '';

    const dailySurplusForGain = weightGainCalories - tdee;
    const weeklySurplusForGain = dailySurplusForGain * 7;
    const weeklyKgGain = weeklySurplusForGain / 7700;
    if (weeklyKgGain >= 0.05) {
        document.getElementById('weightGainRate').textContent = `(gain approx. ${weeklyKgGain.toFixed(1)} kg/week)`;
    } else {
        document.getElementById('weightGainRate').textContent = '';
    }

    // Clear all recommended texts first
    document.getElementById('weightLossRecommended').textContent = '';
    document.getElementById('weightGainRecommended').textContent = '';
    document.getElementById('maintenanceRecommended').textContent = '';
    
    document.getElementById('recommendedActivityCalories').textContent = '--'; // Default
    document.getElementById('activityBurnRate').textContent = ''; // Initialize to empty
    const activityDescriptionSpan = document.getElementById('activityTargetDescription');
    if(activityDescriptionSpan) activityDescriptionSpan.textContent = 'Activity Level:'; // General Default, refined below

    if (analyticsData.recommendedActivityCalories != null && analyticsData.recommendedActivityCalories > 0) {
        const recommendedActivityKcal = Math.round(analyticsData.recommendedActivityCalories);
        document.getElementById('recommendedActivityCalories').textContent = `${recommendedActivityKcal} kcal`;

        const dailyActivityBurn = analyticsData.recommendedActivityCalories; // Renaming for clarity
        const weeklyActivityBurn = dailyActivityBurn * 7;
        const weeklyKgChangeFromActivity = weeklyActivityBurn / 7700; 

        if (weeklyKgChangeFromActivity >= 0.05) { 
            // For underweight, burning calories reduces potential gain, for others it contributes to loss
            if (bmiCategory.includes('underweight')) {
                if(activityDescriptionSpan) activityDescriptionSpan.textContent = 'Limit calorie burn to around'; // Corrected: calorie burn
                document.getElementById('activityBurnRate').textContent = `(offsets gain by approx. ${weeklyKgChangeFromActivity.toFixed(1)} kg/week)`;
            } else if (bmiCategory.includes('overweight') || bmiCategory.includes('obese')) { // Only show for overweight/obese
                if(activityDescriptionSpan) activityDescriptionSpan.textContent = 'Aim to burn at least'; 
                document.getElementById('activityBurnRate').textContent = `(contributes approx. ${weeklyKgChangeFromActivity.toFixed(1)} kg/week to loss)`;
            } else { // For 'Normal' or any other category not 'underweight', 'overweight', or 'obese'
                document.getElementById('activityBurnRate').textContent = ''; 
                if(activityDescriptionSpan && bmiCategory.includes('normal')) activityDescriptionSpan.textContent = 'Aim to burn around'; // Slightly different phrasing for normal
                else if(activityDescriptionSpan) activityDescriptionSpan.textContent = 'Aim to burn at least'; // Fallback for other cases
            }
        } else {
            document.getElementById('activityBurnRate').textContent = ''; 
            // If no significant weekly kg change, set appropriate description
            if(activityDescriptionSpan && bmiCategory.includes('underweight')) {
                activityDescriptionSpan.textContent = 'Limit calorie burn to around'; // Corrected: calorie burn
            } else if(activityDescriptionSpan && bmiCategory.includes('normal')) {
                activityDescriptionSpan.textContent = 'Aim to burn around';
            } else {
                if(activityDescriptionSpan) activityDescriptionSpan.textContent = 'Aim to burn at least';
            }
        }
    } else {
        // recommendedActivityCalories is already defaulted to '--'
        // activityBurnRate is already initialized to ''
    }

    // This section now only handles showing the relevant dietary card.
    if (bmiCategory.includes('overweight') || bmiCategory.includes('obese')) {
        if(weightLossCard) weightLossCard.style.display = 'block'; 
    } else if (bmiCategory.includes('underweight')) {
        if(weightGainCard) weightGainCard.style.display = 'block'; 
    } else if (bmiCategory.includes('normal')) {
        if(maintenanceCard) maintenanceCard.style.display = 'block'; 
    } else {
        // No specific card shown if no BMI category matches, or handle default display here.
    }

    // --- Combined Effect Summary Calculation ---
    const combinedEffectCard = document.getElementById('combinedEffectCard');
    const combinedDailySummaryEl = document.getElementById('combinedDailySummary'); // Changed ID for generic term
    const combinedWeeklyKgChangeEl = document.getElementById('combinedWeeklyKgChange'); // Changed ID for generic term
    const combinedSummaryTitleEl = document.getElementById('combinedSummaryTitle');
    const combinedSummaryIntroEl = combinedEffectCard.querySelector('p');
    const combinedSummaryListEl = combinedEffectCard.querySelector('ul');


    let dietaryDailyChangeForSummary = 0;
    let weeklyKgChangeFromDietForSummary = 0;
    let showCombinedSummary = false;
    let summaryType = 'loss'; // Default to loss

    if (bmiCategory.includes('overweight') || bmiCategory.includes('obese')) {
        dietaryDailyChangeForSummary = tdee - weightLossCalories; // Deficit
        const rawWeeklyKgLoss = dietaryDailyChangeForSummary * 7 / 7700;
        if (rawWeeklyKgLoss >= 0.05) {
            weeklyKgChangeFromDietForSummary = parseFloat(rawWeeklyKgLoss.toFixed(1));
        }
        showCombinedSummary = true;
        summaryType = 'loss';
    } else if (bmiCategory.includes('underweight')) {
        dietaryDailyChangeForSummary = weightGainCalories - tdee; // Surplus
        const rawWeeklyKgGain = dietaryDailyChangeForSummary * 7 / 7700;
        if (rawWeeklyKgGain >= 0.05) {
            weeklyKgChangeFromDietForSummary = parseFloat(rawWeeklyKgGain.toFixed(1));
        }
        showCombinedSummary = true;
        summaryType = 'gain';
    }
    // No combined summary for 'Normal' weight by default unless specifically designed

    const activityDailyBurnForSummary = (analyticsData.recommendedActivityCalories != null && analyticsData.recommendedActivityCalories > 0) ? analyticsData.recommendedActivityCalories : 0;
    let weeklyKgChangeFromActivityForSummary = 0;
    if (activityDailyBurnForSummary > 0) {
        const rawWeeklyActivityEffect = activityDailyBurnForSummary * 7 / 7700;
        if (rawWeeklyActivityEffect >= 0.05) {
            weeklyKgChangeFromActivityForSummary = parseFloat(rawWeeklyActivityEffect.toFixed(1));
        }
    }

    if (showCombinedSummary && (weeklyKgChangeFromDietForSummary > 0 || (summaryType === 'loss' && weeklyKgChangeFromActivityForSummary > 0) ) ) {
        let totalCombinedDailyEffectDisplay = 0;
        let totalCombinedWeeklyKgChangeDisplay = 0;
        let introText = "";
        let dailySummaryText = "";
        let weeklyChangeText = "";

        if (summaryType === 'loss') {
            if(combinedSummaryTitleEl) combinedSummaryTitleEl.textContent = "Combined Weight Loss Projection";
            totalCombinedDailyEffectDisplay = dietaryDailyChangeForSummary + activityDailyBurnForSummary; // Both contribute to deficit
            totalCombinedWeeklyKgChangeDisplay = weeklyKgChangeFromDietForSummary + weeklyKgChangeFromActivityForSummary;
            
            introText = "If you follow your recommended dietary intake and meet your activity calorie target:";
            dailySummaryText = `Your estimated total daily calorie deficit could be around <strong>${Math.round(totalCombinedDailyEffectDisplay)}</strong> kcal.`;
            weeklyChangeText = `This could lead to an estimated total weight loss of <strong>${totalCombinedWeeklyKgChangeDisplay.toFixed(1)}</strong> kg/week.`;

        } else if (summaryType === 'gain') {
            if(combinedSummaryTitleEl) combinedSummaryTitleEl.textContent = "Combined Weight Gain Projection";
            totalCombinedDailyEffectDisplay = dietaryDailyChangeForSummary - activityDailyBurnForSummary; // Activity offsets surplus
            totalCombinedWeeklyKgChangeDisplay = weeklyKgChangeFromDietForSummary - weeklyKgChangeFromActivityForSummary;

            introText = "If you follow your recommended dietary intake and account for your activity level:";
            if (totalCombinedDailyEffectDisplay > 0) {
                dailySummaryText = `Your estimated total daily calorie surplus could be around <strong>${Math.round(totalCombinedDailyEffectDisplay)}</strong> kcal.`;
            } else {
                dailySummaryText = `Your activity may lead to a net calorie deficit of <strong>${Math.round(Math.abs(totalCombinedDailyEffectDisplay))}</strong> kcal, potentially hindering weight gain.`;
            }
            if (totalCombinedWeeklyKgChangeDisplay > 0.05) {
                weeklyChangeText = `This could lead to an estimated total weight gain of <strong>${totalCombinedWeeklyKgChangeDisplay.toFixed(1)}</strong> kg/week.`;
            } else if (totalCombinedWeeklyKgChangeDisplay < -0.05) {
                 weeklyChangeText = `This could lead to an estimated net weight loss of <strong>${Math.abs(totalCombinedWeeklyKgChangeDisplay).toFixed(1)}</strong> kg/week. Consider adjusting intake or activity.`;
            } else {
                weeklyChangeText = "Your diet and activity may result in minimal net weight change. Adjust as needed for gain.";
            }
        }
        
        if(combinedSummaryIntroEl) combinedSummaryIntroEl.textContent = introText;
        if(combinedDailySummaryEl) combinedDailySummaryEl.innerHTML = Math.round(totalCombinedDailyEffectDisplay); // Just the number for the IDed span
        if(combinedWeeklyKgChangeEl) combinedWeeklyKgChangeEl.innerHTML = totalCombinedWeeklyKgChangeDisplay.toFixed(1); // Just the number for the IDed span

        if(combinedSummaryListEl) combinedSummaryListEl.innerHTML = ''; // Clear existing items

        if (totalCombinedDailyEffectDisplay !== 0 || summaryType === 'gain') { // Show daily summary if there is an effect or if it's for gain (even if zero surplus)
            const liDaily = document.createElement('li');
            liDaily.innerHTML = dailySummaryText;
            if(combinedSummaryListEl) combinedSummaryListEl.appendChild(liDaily);
        }

        // Only show weekly change if it's significant or meaningful for the context
        if ( (summaryType === 'loss' && totalCombinedWeeklyKgChangeDisplay >= 0.05) || 
             (summaryType === 'gain' && Math.abs(totalCombinedWeeklyKgChangeDisplay) >= 0.05) ||
             (summaryType === 'gain' && totalCombinedDailyEffectDisplay <=0) ) { // Also show for gain if daily effect is not positive surplus
            const liWeekly = document.createElement('li');
            liWeekly.innerHTML = weeklyChangeText;
            if(combinedSummaryListEl) combinedSummaryListEl.appendChild(liWeekly);
        }
        
        if (combinedSummaryListEl && combinedSummaryListEl.children.length > 0) { 
            combinedEffectCard.style.display = 'block'; 
        } else {
            combinedEffectCard.style.display = 'none';
        }

    } else {
        combinedEffectCard.style.display = 'none'; 
    }
}

// Call the function to generate recommendations when the script runs and data is available
generateDynamicWorkoutRecommendations();
updateCalorieTargetRecommendations(); // Call the new function

// Function to open workout modal with pre-filled data from recommendations
async function openWorkoutModalWithRecommendation(workoutName, durationMinutes, workoutTypeKey, calorieSpanId) {
    window.analyticsContextSave = true; // Signal that this save originates from analytics context

    // Ensure the global openModal function from dashboard.js is available
    if (typeof openModal !== 'function') {
        console.error('Global openModal function not found. Cannot open workout modal.');
        // Fallback: try to find and display the modal directly if openModal is not global
        const modal = document.getElementById("workoutModal");
        if (modal) {
            modal.style.display = "flex";
        } else {
            alert('Error: Workout modal or function to open it is not available.');
            return;
        }
    }

    const workoutNameInput = document.getElementById('workoutName');
    const durationInput = document.getElementById('duration');
    const workoutTypeSelect = document.getElementById('workoutType');
    const caloriesBurnedInput = document.getElementById('caloriesBurned');
    const workoutIdInput = document.getElementById('workoutId'); // For edit, clear for add
    const workoutForm = document.getElementById('workoutForm'); // Get the form element
    const modalTitle = document.getElementById('modalTitle'); 

    if (workoutNameInput) workoutNameInput.value = workoutName;
    if (durationInput) durationInput.value = durationMinutes;
    if (workoutTypeSelect) {
        // Map specific keys if they differ from modal option values
        let modalWorkoutType = workoutTypeKey;
        if (workoutTypeKey === 'BALANCE_STABILITY') modalWorkoutType = 'BALANCE';
        if (workoutTypeKey === 'FUNCTIONAL_TRAINING') modalWorkoutType = 'FUNCTIONAL';
        if (workoutTypeKey === 'SPORTS_RECREATION') modalWorkoutType = 'SPORTS';
        workoutTypeSelect.value = modalWorkoutType;
    }
    
    const calorieSpan = document.getElementById(calorieSpanId);
    if (caloriesBurnedInput && calorieSpanId && calorieSpan) {
        // If calories are not yet estimated (e.g., show 'Loading...' or empty), estimate them now.
        if (calorieSpan.textContent === 'Loading...' || calorieSpan.textContent.trim() === '' || calorieSpan.textContent.trim() === '--' || !parseFloat(calorieSpan.textContent)) {
            if (durationMinutes > 0) { // Only estimate if duration is sensible
                await estimateCaloriesForWorkout(workoutName, durationMinutes, calorieSpanId);
            }
        }
        // Now that it's (potentially) estimated, populate the input field
        if (calorieSpan.textContent && !isNaN(parseFloat(calorieSpan.textContent))) {
            caloriesBurnedInput.value = Math.round(parseFloat(calorieSpan.textContent));
        } else {
            caloriesBurnedInput.value = ''; // Clear if still not a number (e.g., API error)
            console.warn(`Could not find or parse calories from spanId: ${calorieSpanId} after attempting estimation.`);
        }
    } else if (caloriesBurnedInput) {
        caloriesBurnedInput.value = ''; // Clear if no calorieSpanId provided or span not found
    }

    if (workoutIdInput) workoutIdInput.value = ''; // Ensure it's a new workout

    // Reset form action to default for adding new workout
    if (workoutForm) {
        // Check if this form action is generic enough or needs to be specific to /user/saveworkout
        // For now, assume dashboard.js openModal or scheduledworkouts.js openAddScheduledWorkoutModal handles it.
        // However, to be safe for analytics page context where only adding is logical from here:
        if (workoutForm.action.includes('/update') || workoutForm.action.includes('/edit')) {
            // Try to determine the correct base path or use a sensible default
            // This is a bit of a guess if the structure is inconsistent
            const basePath = window.location.origin; // Or a fixed path if known
            workoutForm.action = `${basePath}/user/saveworkout`; 
            // If using scheduledworkouts.html modal structure which is /user/saveworkout by default for new
            // If using dashboard.html modal structure which is also /user/saveworkout by default
        }
        // If the modal is shared and its action might be for editing, explicitly set to add action.
        // This assumes '/user/saveworkout' is the correct endpoint for adding a new workout.
        // This might conflict if the modal on dashboard/scheduledworkouts has a different default new action.
        // Best would be if openModal() in dashboard.js resets it for new workouts.
        // Let's ensure it's the add new workout action if the context is analytics
        workoutForm.action = '/user/saveworkout'; 
    }
    
    if (modalTitle) modalTitle.textContent = 'Add New Workout';

    // Call the global openModal function if available, otherwise just ensure modal is visible
    if (typeof openModal === 'function') {
        openModal(); 
    } else {
        // This part is already handled by the fallback at the start of this function.
        // const modal = document.getElementById("workoutModal");
        // if (modal) modal.style.display = "flex";
    }

    // Optional: Set a default date (e.g., today) for the workout
    // This depends on your modal having a date input with a known ID, e.g., 'workoutDate'
    // const workoutDateInput = document.getElementById('workoutDate'); // Example ID, adjust if necessary
    // if (workoutDateInput && workoutDateInput.type === 'date') {
    //     const today = new Date();
    //     const year = today.getFullYear();
    //     const month = String(today.getMonth() + 1).padStart(2, '0'); // Months are 0-indexed
    //     const day = String(today.getDate()).padStart(2, '0');
    //     workoutDateInput.value = `${year}-${month}-${day}`;
    // } else if (workoutDateInput && workoutDateInput.type === 'datetime-local') {
    //     const now = new Date();
    //     now.setMinutes(now.getMinutes() - now.getTimezoneOffset()); // Adjust for local timezone for datetime-local
    //     workoutDateInput.value = now.toISOString().slice(0,16);
    // }
}

// Expose functions to be callable from HTML onclick attributes
window.openWorkoutModalWithRecommendation = openWorkoutModalWithRecommendation;
window.showLoggedMeals = showLoggedMeals;
window.closeLoggedMeals = closeLoggedMeals;
window.openMeasurementModal = openMeasurementModal;
window.closeMeasurementModal = closeMeasurementModal;
window.handleRefreshRecommendations = handleRefreshRecommendations;

function refreshScheduledIndicators() {
    if (!window.analyticsData || !window.analyticsData.scheduledWorkoutNames) {
        return;
    }
    const scheduledWorkoutNames = window.analyticsData.scheduledWorkoutNames;
    const recommendationItems = document.querySelectorAll('.workout-rec-item');

    recommendationItems.forEach(item => {
        const workoutName = item.dataset.workoutName;
        if (!workoutName) return;

        const nameStrongTag = item.querySelector('.workout-rec-name');
        let icon = item.querySelector('.scheduled-indicator');

        if (scheduledWorkoutNames.includes(workoutName)) {
            item.classList.add('is-scheduled');
            if (nameStrongTag && !icon) { // Add icon if not present
                const newIcon = document.createElement('i');
                newIcon.className = 'fas fa-calendar-check scheduled-indicator';
                newIcon.title = 'Already in your schedule';
                // Add a space before the icon
                nameStrongTag.appendChild(document.createTextNode(' ')); 
                nameStrongTag.appendChild(newIcon);
            }
        } else {
            item.classList.remove('is-scheduled');
            if (icon) { // Remove icon if present
                // Also remove preceding space if it was added for the icon
                if (icon.previousSibling && icon.previousSibling.nodeType === Node.TEXT_NODE && icon.previousSibling.textContent === ' ') {
                    icon.previousSibling.remove();
                }
                icon.remove();
            }
        }
    });
}

// Expose functions to be callable from HTML onclick attributes
window.openWorkoutModalWithRecommendation = openWorkoutModalWithRecommendation;
window.showLoggedMeals = showLoggedMeals;
window.closeLoggedMeals = closeLoggedMeals;
window.openMeasurementModal = openMeasurementModal;
window.closeMeasurementModal = closeMeasurementModal;
window.handleRefreshRecommendations = handleRefreshRecommendations;
window.refreshScheduledIndicators = refreshScheduledIndicators; // Expose the new function
} 