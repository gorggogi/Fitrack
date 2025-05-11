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
} 