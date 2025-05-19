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

function generateDynamicWorkoutRecommendations() {
    const recommendationContainer = document.getElementById('dynamic-workout-recommendations-content');
    if (!recommendationContainer) return;

    const bmiCategory = analyticsData.currentBmiCategory;
    const workoutFreq = analyticsData.workoutTypeFrequency || {}; // Counts over last 90 days
    const avgExerciseCalories = analyticsData.avgDailyExerciseCalories;

    let recommendationsHtml = []; // Changed variable name for clarity

    const WORKOUT_TYPES = {
        CARDIO: {
            name: 'Cardio',
            examples: [
                { name: 'Brisk Walking', detail: 'Aim for 30+ minutes at a pace where you can talk but feel your heart rate increase.' },
                { name: 'Running', detail: 'Builds endurance; start with a comfortable pace and gradually increase distance or speed.' },
                { name: 'Cycling', detail: 'Low-impact and great for leg strength; vary resistance or try interval speeds for 20-40 minutes.' },
                { name: 'Swimming', detail: 'Full-body workout that\'s gentle on the joints; focus on different strokes.' },
                { name: 'Jumping Jacks', detail: 'A simple full-body cardio to get your heart rate up quickly.' }
            ]
        },
        STRENGTH: {
            name: 'Strength Training',
            examples: [
                { name: 'Squats', detail: 'Targets quads, hamstrings, and glutes; focus on depth and keeping your chest up.' },
                { name: 'Push-ups', detail: 'Versatile bodyweight exercise for upper body; modify on knees if needed, aiming for 3 sets.' },
                { name: 'Lunges', detail: 'Great for single-leg strength and balance; keep your front knee behind your toes.' },
                { name: 'Plank', detail: 'Core stability exercise; engage your abs and maintain a straight line from head to heels for 30-60 seconds.' },
                { name: 'Deadlifts', detail: 'Compound movement for posterior chain (back, glutes, hamstrings); prioritize form over weight.' },
                { name: 'Bench Press', detail: 'Works chest, shoulders, and triceps; ensure a stable setup and controlled movement.' }
            ]
        },
        FLEXIBILITY: {
            name: 'Flexibility & Mobility',
            examples: [
                { name: 'Hamstring Stretch', detail: 'Hold for 30 seconds per leg to improve flexibility in the back of your legs; avoid bouncing.' },
                { name: 'Quad Stretch', detail: 'Stand tall and hold for 30 seconds per leg to stretch the front of your thighs.' },
                { name: 'Yoga (e.g., Downward Dog)', detail: 'Stretches shoulders, hamstrings, and calves while building strength.' },
                { name: 'Pilates (e.g., The Hundred)', detail: 'Core-focused exercises that also emphasize breath control and flexibility.' }
            ]
        },
        HIIT: {
            name: 'HIIT',
            examples: [
                { name: 'Sprint Intervals', detail: 'Alternate short bursts of all-out sprinting (20-30s) with recovery periods (60-90s).' },
                { name: 'Tabata Sprints', detail: '20 seconds of intense effort followed by 10 seconds of rest, repeated for 4 minutes per exercise.' },
                { name: 'Burpee Intervals', detail: 'Perform burpees at high intensity for a set time (e.g., 45s), rest briefly (e.g., 15s), and repeat.' }
            ]
        },
        BALANCE_STABILITY: {
            name: 'Balance & Stability',
            examples: [
                { name: 'Single-Leg Stands', detail: 'Improve balance by standing on one leg (30s each); try with eyes open, then closed for a challenge.' },
                { name: 'Tai Chi movements', detail: 'Slow, flowing movements that enhance balance, coordination, and mindfulness.' },
                { name: 'Heel-to-Toe Walk', detail: 'Improves balance and proprioception; walk in a straight line placing heel directly before toe.' }
            ]
        },
        FUNCTIONAL_TRAINING: {
            name: 'Functional Training',
            examples: [
                { name: "Farmer\'s Walks", detail: 'Builds grip strength and core stability; carry heavy weights for a set distance.' },
                { name: 'Kettlebell Swings', detail: 'Develops explosive power in hips and glutes; focus on a hip hinge, not a squat.' },
                { name: 'Medicine Ball Slams', detail: 'Full-body power exercise; slam the ball forcefully to the ground from overhead.' }
            ]
        },
        SPORTS_RECREATION: {
            name: 'Sports & Recreation',
            examples: [
                { name: 'Basketball', detail: 'Improves agility, coordination, and cardiovascular fitness through dynamic team play.' },
                { name: 'Hiking', detail: 'Enjoy nature while getting a great lower body and cardio workout; vary terrain for challenge.' },
                { name: 'Dancing', detail: 'A fun way to improve cardio, coordination, and mood; try different styles like Zumba or salsa.' }
            ]
        },
        OTHER: { name: 'Other Activities', examples: [] }
    };

    const formatExamples = (examples) => {
        if (!examples || examples.length === 0) return '';
        const shuffled = [...examples].sort(() => 0.5 - Math.random());
        
        const minToShow = 3;
        const maxToShow = 5;
        let countToShow;

        if (examples.length <= minToShow) {
            countToShow = examples.length;
        } else {
            countToShow = Math.min(examples.length, Math.floor(Math.random() * (maxToShow - minToShow + 1)) + minToShow);
        }
        
        const selected = shuffled.slice(0, countToShow);

        if (selected.length === 0) return '';

        let exampleHtmlList = '<ul class="example-list">';
        selected.forEach((ex) => {
            exampleHtmlList += `<li><strong>${ex.name}</strong> – ${ex.detail}</li>`;
        });
        exampleHtmlList += '</ul>';
        return exampleHtmlList;
    };
    
    const exampleIntroText = '<div class="recommendation-examples-intro">For instance:</div>';

    const allTypeKeys = Object.keys(WORKOUT_TYPES);
    const loggedTypeKeys = Object.keys(workoutFreq);

    const getFrequency = (typeKey) => workoutFreq[typeKey] || 0;

    if (!bmiCategory) {
        recommendationsHtml.push('<div class="workout-recommendation-card"><p>Complete your profile (height and weight) to receive personalized workout recommendations.</p></div>');
    } else {
        // This is the line to be removed/commented out
        // recommendationsHtml.push(`<div class="workout-recommendation-card"><h5>Your BMI Category: ${bmiCategory}</h5><p>Here are some tailored suggestions:</p></div>`);

        if (bmiCategory === 'Underweight') {
            recommendationsHtml.push('<div class="workout-recommendation-card"><h5>Goal Focus: Healthy Weight Gain & Muscle Building</h5></div>');
            if (getFrequency('STRENGTH') < 8) {
                let content = `<p>You should aim for 2-3 sessions weekly, focusing on compound movements and progressive overload.</p>${exampleIntroText}${formatExamples(WORKOUT_TYPES.STRENGTH.examples)}`;
                recommendationsHtml.push(`<div class="workout-recommendation-card"><h5>Prioritize ${WORKOUT_TYPES.STRENGTH.name}</h5>${content}</div>`);
            } else {
                let content = `<p>Ensure progressive overload for continued muscle growth.</p>${exampleIntroText}${formatExamples(WORKOUT_TYPES.STRENGTH.examples)}`;
                recommendationsHtml.push(`<div class="workout-recommendation-card"><h5>Continue Your Great Work with ${WORKOUT_TYPES.STRENGTH.name}</h5>${content}</div>`);
            }
            if (getFrequency('CARDIO') < 4 && avgExerciseCalories < 200) {
                let content = `<p>Aiming for 2-3 times per week (20-30 mins) for cardiovascular health.</p>${exampleIntroText}${formatExamples(WORKOUT_TYPES.CARDIO.examples)}`;
                recommendationsHtml.push(`<div class="workout-recommendation-card"><h5>Incorporate Moderate ${WORKOUT_TYPES.CARDIO.name}</h5>${content}</div>`);
            } else {
                let content = `<p>Balance your cardio with your strength goals. Ensure it\'s supportive, not excessive, for muscle gain.</p>${exampleIntroText}${formatExamples(WORKOUT_TYPES.CARDIO.examples)}`;
                recommendationsHtml.push(`<div class="workout-recommendation-card"><h5>Balance Your ${WORKOUT_TYPES.CARDIO.name}</h5>${content}</div>`);
            }
        } else if (bmiCategory === 'Overweight' || bmiCategory.toLowerCase().includes('obese')) {
            recommendationsHtml.push('<div class="workout-recommendation-card"><h5>Goal Focus: Fat Loss & Improved Metabolic Health</h5></div>');
            if (getFrequency('CARDIO') < 12) {
                let content = `<p>Aiming for 3-5 sessions of moderate-intensity for 30+ minutes.</p>${exampleIntroText}${formatExamples(WORKOUT_TYPES.CARDIO.examples)}`;
                recommendationsHtml.push(`<div class="workout-recommendation-card"><h5>Increase Your ${WORKOUT_TYPES.CARDIO.name}</h5>${content}</div>`);
            } else {
                let content = `<p>Maintain 150-300 minutes of moderate-intensity cardio weekly. Consider varying type or intensity.</p>${exampleIntroText}${formatExamples(WORKOUT_TYPES.CARDIO.examples)}`;
                recommendationsHtml.push(`<div class="workout-recommendation-card"><h5>Excellent Consistency with ${WORKOUT_TYPES.CARDIO.name}</h5>${content}</div>`);
            }
            if (getFrequency('STRENGTH') < 8) {
                let content = `<p>Aiming for 2-3 times per week as this builds muscle and boosts metabolism.</p>${exampleIntroText}${formatExamples(WORKOUT_TYPES.STRENGTH.examples)}`;
                recommendationsHtml.push(`<div class="workout-recommendation-card"><h5>Incorporate ${WORKOUT_TYPES.STRENGTH.name}</h5>${content}</div>`);
            } else {
                let content = `<p>It\'s crucial for preserving muscle mass during fat loss.</p>${exampleIntroText}${formatExamples(WORKOUT_TYPES.STRENGTH.examples)}`;
                recommendationsHtml.push(`<div class="workout-recommendation-card"><h5>Keep Up ${WORKOUT_TYPES.STRENGTH.name}</h5>${content}</div>`);
            }
            if (avgExerciseCalories > 150 && getFrequency('HIIT') < 4) {
                let content = `<p>These are an efficient way to boost calorie burn.</p>${exampleIntroText}${formatExamples(WORKOUT_TYPES.HIIT.examples)}`;
                recommendationsHtml.push(`<div class="workout-recommendation-card"><h5>Consider Adding ${WORKOUT_TYPES.HIIT.name}</h5>${content}</div>`);
            }
        } else if (bmiCategory === 'Normal') {
            recommendationsHtml.push('<div class="workout-recommendation-card"><h5>Goal Focus: Maintain Health & Optimize Overall Fitness</h5></div>');
            let cardioContent = `<p>Include regular sessions (3-5 times/week).</p>${exampleIntroText}${formatExamples(WORKOUT_TYPES.CARDIO.examples)}`;
            recommendationsHtml.push(`<div class="workout-recommendation-card"><h5>Aim for Balanced ${WORKOUT_TYPES.CARDIO.name}</h5>${cardioContent}</div>`);
            if (getFrequency('STRENGTH') < 8) {
                let strengthContent = `<p>Incorporate sessions (2-3 times per week) for muscle and bone health.</p>${exampleIntroText}${formatExamples(WORKOUT_TYPES.STRENGTH.examples)}`;
                recommendationsHtml.push(`<div class="workout-recommendation-card"><h5>Incorporate ${WORKOUT_TYPES.STRENGTH.name}</h5>${strengthContent}</div>`);
            } else {
                let strengthContent = `<p>Keep it consistent.</p>${exampleIntroText}${formatExamples(WORKOUT_TYPES.STRENGTH.examples)}`;
                recommendationsHtml.push(`<div class="workout-recommendation-card"><h5>Well Done on ${WORKOUT_TYPES.STRENGTH.name}</h5>${strengthContent}</div>`);
            }
            recommendationsHtml.push('<div class="workout-recommendation-card"><p>Explore a variety of activities to keep your fitness journey engaging and well-rounded!</p></div>');
        }

        if (getFrequency('FLEXIBILITY') < 8) {
            let content = `<p>This improves range of motion and aids recovery.</p>${exampleIntroText}${formatExamples(WORKOUT_TYPES.FLEXIBILITY.examples)}`;
            recommendationsHtml.push(`<div class="workout-recommendation-card"><h5>Don\'t Forget ${WORKOUT_TYPES.FLEXIBILITY.name}</h5>${content}</div>`);
        }
        if (getFrequency('FUNCTIONAL_TRAINING') < 4 && bmiCategory !== 'Underweight') {
             let content = `<p>It enhances everyday strength and movement.</p>${exampleIntroText}${formatExamples(WORKOUT_TYPES.FUNCTIONAL_TRAINING.examples)}`;
             recommendationsHtml.push(`<div class="workout-recommendation-card"><h5>Consider Adding ${WORKOUT_TYPES.FUNCTIONAL_TRAINING.name}</h5>${content}</div>`);
        }
        if (getFrequency('BALANCE_STABILITY') < 4) {
            let content = `<p>These exercises can improve coordination and reduce injury risk.</p>${exampleIntroText}${formatExamples(WORKOUT_TYPES.BALANCE_STABILITY.examples)}`;
            recommendationsHtml.push(`<div class="workout-recommendation-card"><h5>Try Basic ${WORKOUT_TYPES.BALANCE_STABILITY.name}</h5>${content}</div>`);
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
                    let content = `<p>${WORKOUT_TYPES[typeKey].name} could be a great addition to your routine.</p>${exampleIntroText}${formatExamples(WORKOUT_TYPES[typeKey].examples)}`;
                    recommendationsHtml.push(`<div class="workout-recommendation-card"><h5>Explore Something New: ${WORKOUT_TYPES[typeKey].name}</h5>${content}</div>`);
                    suggestedNew++;
                }
            }
        }
         if (loggedTypeKeys.length === 0 && recommendationsHtml.length <= 2) { // Check new array name
            recommendationsHtml.push('<div class="workout-recommendation-card"><p>Log your workouts regularly to get even more specific feedback and track your progress effectively.</p></div>');
        }
    }

    if (recommendationsHtml.length === 0) {
        recommendationsHtml.push('<div class="workout-recommendation-card"><p>Keep logging your activities to receive personalized workout recommendations!</p></div>');
    }

    recommendationContainer.innerHTML = recommendationsHtml.join(''); // Use the new array and join directly
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

    const approxRateText = "(approx. 0.5 kg/week)";

    document.getElementById('tdeeValue').textContent = `${maintenanceCalories} kcal`;
    document.getElementById('weightLossCalories').textContent = `${weightLossCalories} kcal`;
    document.getElementById('maintenanceCalories').textContent = `${maintenanceCalories} kcal`;
    document.getElementById('weightGainCalories').textContent = `${weightGainCalories} kcal`;

    document.getElementById('weightLossRecommended').textContent = '';
    document.getElementById('weightGainRecommended').textContent = '';
    document.getElementById('maintenanceRecommended').textContent = '';
    document.getElementById('weightLossRate').textContent = '';
    document.getElementById('maintenanceRate').textContent = '';
    document.getElementById('weightGainRate').textContent = '';

    if (bmiCategory.includes('overweight') || bmiCategory.includes('obese')) {
        document.getElementById('weightLossRecommended').textContent = "(Recommended)";
        document.getElementById('weightLossRate').textContent = approxRateText;
        document.getElementById('weightGainRate').textContent = approxRateText;
    } else if (bmiCategory.includes('underweight')) {
        document.getElementById('weightGainRecommended').textContent = "(Recommended)";
        document.getElementById('weightGainRate').textContent = approxRateText;
        document.getElementById('weightLossRate').textContent = "approx. 0.5 kg/week";
    } else if (bmiCategory.includes('normal')) {
        document.getElementById('maintenanceRecommended').textContent = "(Recommended)";
        document.getElementById('weightLossRate').textContent = approxRateText;
        document.getElementById('weightGainRate').textContent = approxRateText;
    } else {
        document.getElementById('weightLossRate').textContent = approxRateText;
        document.getElementById('weightGainRate').textContent = approxRateText;
    }
}

// Call the function to generate recommendations when the script runs and data is available
generateDynamicWorkoutRecommendations();
updateCalorieTargetRecommendations(); // Call the new function
} 