function toggleDropdown() {
    const dropdown = document.getElementById("dropdownMenu");
    dropdown.style.display = dropdown.style.display === "block" ? "none" : "block";
}

document.addEventListener("click", function(event) {
    const dropdown = document.getElementById("dropdownMenu");
    const userInfo = document.querySelector(".user-info");

    if (!userInfo.contains(event.target)) {
        dropdown.style.display = "none";
    }
});

document.addEventListener("DOMContentLoaded", function () {
    const modal = document.getElementById("workoutModal");
    const markAsDoneButton = document.getElementById("markAsDoneButton");

    if (!modal) {
        console.error("⚠️ Workout modal not found.");
        return;
    }

    document.querySelectorAll(".workout-item").forEach(item => {
        item.addEventListener("click", () => openModal(item));
    });

    if (markAsDoneButton) {
        markAsDoneButton.addEventListener("click", function () {
            const workoutId = this.getAttribute("data-id");
            if (!workoutId) return console.error("⚠️ Workout ID not found!");

            const csrfToken = document.querySelector('input[name="_csrf"]').value;
            if (!csrfToken) {
                console.error("⚠️ CSRF token not found!");
                return;
            }

            fetch(`/user/workouts/${workoutId}/mark-done`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "X-CSRF-TOKEN": csrfToken
                },
                credentials: 'same-origin'
            })
            .then(response => {
                if (!response.ok) {
                    return response.text().then(text => {
                        throw new Error(text || `HTTP error: ${response.status}`);
                    });
                }
                return response.json();
            })
            .then(data => {
                // Close the modal
                closeWorkoutModal();
                // Reload the page to update the summary board
                window.location.reload();
            })
            .catch(error => {
                console.error("Error:", error);
                alert("Failed to mark workout as done. Please try again.");
            });
        });
    }
    
    window.addEventListener("click", event => {
        if (event.target === modal) modal.style.display = "none";
    });
});


function openModal(workoutElement) {
    const modal = document.getElementById("workoutModal");
    const workoutForm = document.getElementById("workoutForm");
    const workoutDetails = document.getElementById("workoutDetails");
    const modalTitle = document.getElementById("modalTitle");
    
    if (!modal) return console.error("⚠️ Workout modal not found.");

    if (workoutElement) {
        // Viewing existing workout
        modalTitle.textContent = "Workout Details";
        workoutForm.style.display = "none";
        workoutDetails.style.display = "block";
        
        document.getElementById("modalWorkoutName").textContent = workoutElement.getAttribute("data-name");
        document.getElementById("modalWorkoutDuration").textContent = workoutElement.getAttribute("data-duration") + " mins";
        document.getElementById("modalWorkoutCalories").textContent = workoutElement.getAttribute("data-calories") + " cal";

        const markAsDoneButton = document.getElementById("markAsDoneButton");
        markAsDoneButton.setAttribute("data-id", workoutElement.getAttribute("data-id"));
    } else {
        // Adding new workout
        modalTitle.textContent = "Add New Workout";
        workoutForm.style.display = "block";
        workoutDetails.style.display = "none";
    }

    modal.style.display = "flex";
}

function closeWorkoutModal() {
    const modal = document.getElementById("workoutModal");
    const workoutForm = document.getElementById("workoutForm");
    const workoutDetails = document.getElementById("workoutDetails");
    
    if (modal) {
        modal.style.display = "none";
        // Reset form if it exists
        if (workoutForm) workoutForm.reset();
        // Hide workout details if they exist
        if (workoutDetails) workoutDetails.style.display = "none";
    }
}

// Modal functions
function openGoalModal() {
    document.getElementById('goalModal').style.display = 'flex';
}

function closeGoalModal() {
    document.getElementById('goalModal').style.display = 'none';
}

function openWorkoutModal() {
    document.getElementById('workoutModal').style.display = 'flex';
}

function openMealModal() {
    document.getElementById('mealModal').style.display = 'flex';
    
    // Set current date and time
    const now = new Date();
    const year = now.getFullYear();
    const month = String(now.getMonth() + 1).padStart(2, '0');
    const day = String(now.getDate()).padStart(2, '0');
    const hours = String(now.getHours()).padStart(2, '0');
    const minutes = String(now.getMinutes()).padStart(2, '0');
    
    const formattedDateTime = `${year}-${month}-${day}T${hours}:${minutes}`;
    document.querySelector('#mealModal input[name="dateTime"]').value = formattedDateTime;
}

function closeMealModal() {
    document.getElementById('mealModal').style.display = 'none';
}

// Initialize modals when the page loads
document.addEventListener('DOMContentLoaded', function() {
    // Close modals when clicking outside
    window.onclick = function(event) {
        const goalModal = document.getElementById('goalModal');
        const workoutModal = document.getElementById('workoutModal');
        const mealModal = document.getElementById('mealModal');
        
        if (event.target == goalModal) {
            closeGoalModal();
        }
        if (event.target == workoutModal) {
            closeWorkoutModal();
        }
        if (event.target == mealModal) {
            closeMealModal();
        }
    }

    // Handle workout form submission
    const workoutForm = document.getElementById('workoutForm');
    if (workoutForm) {
        workoutForm.addEventListener('submit', function(e) {
            e.preventDefault();
            
            const formData = new FormData(this);
            
            fetch('/user/saveworkout', {
                method: 'POST',
                body: formData
            })
            .then(response => {
                if (response.ok) {
                    closeWorkoutModal();
                    // Clear the form
                    this.reset();
                    // Reload the page to show the new workout
                    window.location.reload();
                } else {
                    response.text().then(text => {
                        console.error('Error response:', text);
                        alert('Error adding workout. Please try again.');
                    });
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert('Error adding workout. Please try again.');
            });
        });
    }

    // Handle goal form submission
    const goalForm = document.getElementById('goalForm');
    if (goalForm) {
        goalForm.addEventListener('submit', function(e) {
            e.preventDefault();
            
            const formData = new FormData(this);
            
            fetch('/user/goals/add', {
                method: 'POST',
                body: formData
            })
            .then(response => {
                if (response.ok) {
                    closeGoalModal();
                    // Clear the form
                    this.reset();
                    // Reload the page to show the new goal
                    window.location.reload();
                } else {
                    response.text().then(text => {
                        console.error('Error response:', text);
                        alert('Error adding goal. Please try again.');
                    });
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert('Error adding goal. Please try again.');
            });
        });
    }

    // Handle meal form submission (NEW)
    const mealForm = document.getElementById('mealForm');
    if (mealForm) {
        mealForm.addEventListener('submit', function(e) {
            e.preventDefault(); // Prevent traditional form submission

            const formData = new FormData(this);
            const csrfToken = document.querySelector('input[name="_csrf"]').value;

            // Show loading state maybe?
            // const saveButton = mealForm.querySelector('button[type="submit"]');
            // saveButton.disabled = true;
            // saveButton.textContent = 'Saving...';

            fetch('/user/meals/save', { 
                method: 'POST',
                body: formData,
                headers: {
                    'X-CSRF-TOKEN': csrfToken,
                    'Accept': 'application/json' 
                },
                credentials: 'same-origin'
            })
            .then(response => {
                // Restore button state
                // saveButton.disabled = false;
                // saveButton.textContent = 'Save Meal';
                
                if (response.ok) {
                    return response.json(); // Parse the JSON body which contains List<MealDTO>
                } else {
                    // Handle errors
                    return response.text().then(text => {
                        throw new Error(text || 'Failed to save meal'); // Throw error to be caught below
                    });
                }
            })
            .then(todayMealDTOs => {
                // --- Success: Update UI dynamically --- 
                console.log("Received updated meals:", todayMealDTOs);
                updateDashboardMeals(todayMealDTOs); // Call function to update UI
                closeMealModal(); 
                // alert('Meal saved!'); // Optional: Replace with a less intrusive notification
            })
            .catch(error => {
                // Restore button state on error too
                // saveButton.disabled = false;
                // saveButton.textContent = 'Save Meal';

                console.error('Error saving meal:', error);
                alert('Error saving meal: ' + error.message); // Show error to user
            });
        });
    }
});

// --- NEW Function to update dashboard UI --- 
function updateDashboardMeals(mealDTOs) {
    const mealsSection = document.querySelector('.meals-section'); // Container for meal items + placeholder
    const placeholder = mealsSection ? mealsSection.querySelector('.placeholder-message') : null;
    const mealItemsContainer = mealsSection; // Assuming meals are direct children or find specific inner div if needed
    
    // Find summary cards (adjust selectors if needed)
    const summaryCards = document.querySelectorAll('.summary-card');
    let calorieSummaryEl = null;
    let mealCountSummaryEl = null;
    summaryCards.forEach(card => {
        const titleEl = card.querySelector('p');
        if (titleEl && titleEl.textContent.includes('Total Calorie Intake')) {
            calorieSummaryEl = card.querySelector('h2');
        }
        if (titleEl && titleEl.textContent.includes('Workouts Completed')) {
             // Need the meals count summary. Let's assume it's the 3rd card or needs a specific ID/class.
             // For now, let's target based on text if possible, or add IDs later.
             // This selector needs verification based on actual HTML structure for meal count.
             // Let's assume the 3rd card is 'Meals Logged' or similar, or find by text 'Meals'
             // Placeholder: Targeting the *Workouts Completed* one for now, needs correction
            mealCountSummaryEl = card.querySelector('h2'); 
            // TODO: Fix selector for meal count summary element
        }
    });

    if (!mealItemsContainer) {
        console.error("Could not find meal items container in dashboard.");
        return;
    }

    // Clear existing meal items (excluding the header and add button)
    mealItemsContainer.querySelectorAll('.meal-item').forEach(item => item.remove());

    let totalCaloriesToday = 0;
    let mealCountToday = 0;

    if (mealDTOs && mealDTOs.length > 0) {
        mealCountToday = mealDTOs.length;
        mealDTOs.forEach(meal => {
            totalCaloriesToday += meal.totalCalories;
            const mealElement = document.createElement('div');
            mealElement.className = 'meal-item';

            // Format time (handle potential errors)
            let formattedTime = 'N/A';
            try {
                formattedTime = new Date(meal.dateTime).toLocaleTimeString('en-US', { hour: 'numeric', minute: '2-digit', hour12: true });
            } catch (e) { console.error("Error formatting date:", e); }

            // Create food item list string
            const foodListHtml = meal.foodItems
                                    .map(fi => `<span class="food-item-name">${escapeHtml(fi.foodItem)}</span>`)
                                    .join(', '); // Join with comma and space

            mealElement.innerHTML =
                `<div>
                    <div class="meal-header">
                        <span>${escapeHtml(meal.mealName)}</span>
                    </div>
                    <div class="meal-content">
                        <span>${foodListHtml}</span>
                        <span class="meal-calories">(${meal.totalCalories} cal)</span>
                    </div>
                </div>
                <span class="meal-time">${formattedTime}</span>`;
            
            mealItemsContainer.appendChild(mealElement);
        });

        // Hide placeholder if it exists
        if (placeholder) {
            placeholder.style.display = 'none';
        }
    } else {
        // Show placeholder if it exists and there are no meals
        if (placeholder) {
            placeholder.style.display = 'block';
        }
    }

    // Update summary cards
    if (calorieSummaryEl) {
        calorieSummaryEl.textContent = totalCaloriesToday + ' cal'; // Assuming format is just value + ' cal'
    }
    if (mealCountSummaryEl) {
        // TODO: Update this once the correct element selector is found
        // mealCountSummaryEl.textContent = mealCountToday;
        console.warn("Selector for meal count summary needs verification. Update skipped.");
    } else {
        console.warn("Could not find summary element for meal count.");
    }
}

// Helper function to escape HTML characters (basic)
function escapeHtml(unsafe) {
    if (typeof unsafe !== 'string') return unsafe;
    return unsafe
         .replace(/&/g, "&amp;")
         .replace(/</g, "&lt;")
         .replace(/>/g, "&gt;")
         .replace(/"/g, "&quot;")
         .replace(/'/g, "&#039;");
 }

// Meal-related functions
let itemCount = 1;

function addFoodItem() {
    const container = document.getElementById('food-items-container');
    const newItem = document.createElement('div');
    newItem.className = 'food-item';
    newItem.innerHTML = `
        <div class="form-row">
            <div class="form-group">
                <label>Food Item</label>
                <input type="text" name="foodItems[${itemCount}].foodItem" placeholder="Enter food" class="form-control" required>
            </div>
            <div class="form-group">
                <label>Quantity</label>
                <input type="number" name="foodItems[${itemCount}].quantity" placeholder="Quantity" class="form-control" required>
            </div>
            <div class="form-group">
                <label>Unit</label>
                <select name="foodItems[${itemCount}].unit" class="form-control" required>
                    <option value="">Select unit</option>
                    <option value="g">Grams (g)</option>
                    <option value="cup">Cup</option>
                    <option value="oz">Ounce (oz)</option>
                    <option value="serving">Serving</option>
                    <option value="tbsp">Tablespoon</option>
                    <option value="tsp">Teaspoon</option>
                    <option value="piece">Piece</option>
                </select>
            </div>
            <div class="form-group">
                <label>Calories</label>
                <input type="number" name="foodItems[${itemCount}].calories" placeholder="Estimate" class="form-control" required>
            </div>
            <button type="button" class="remove-item" onclick="removeFoodItem(this)">×</button>
        </div>
    `;
    container.appendChild(newItem);
    itemCount++;
}

function removeFoodItem(button) {
    const item = button.closest('.food-item');
    item.remove();
    itemCount--;
}

function estimateCalories() {
    let foodItems = [];
    let hasEmptyFields = false;

    document.querySelectorAll("#food-items-container .food-item").forEach(item => {
        let foodName = item.querySelector('input[name$=".foodItem"]').value.trim();
        let quantity = item.querySelector('input[name$=".quantity"]').value.trim();
        let unit = item.querySelector('select[name$=".unit"]').value.trim();

        if (!foodName || !quantity || !unit) {
            hasEmptyFields = true;
            return;
        }

        foodItems.push({
            foodName: foodName,
            quantity: parseFloat(quantity),
            unit: unit
        });
    });

    if (hasEmptyFields) {
        alert("Please fill in all fields (Food Item, Quantity, and Unit) before estimating calories.");
        return;
    }

    if (foodItems.length === 0) {
        alert("Please add at least one food item.");
        return;
    }

    // Get CSRF token
    const csrfToken = document.querySelector('input[name="_csrf"]').value;
    if (!csrfToken) {
        console.error("CSRF token not found");
        alert("Security token missing. Please refresh the page and try again.");
        return;
    }

    console.log("Sending calorie estimation request:", JSON.stringify(foodItems));

    fetch("/meals/estimate-calories", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "X-CSRF-TOKEN": csrfToken
        },
        body: JSON.stringify(foodItems),
        credentials: 'same-origin'
    })
    .then(response => {
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        return response.json();
    })
    .then(data => {
        console.log("Received calorie estimation:", data);
        if (Array.isArray(data)) {
            document.querySelectorAll("#food-items-container .food-item").forEach((item, index) => {
                if (data[index] !== undefined) {
                    const calorieField = item.querySelector('input[name$=".calories"]');
                    if (calorieField) {
                        calorieField.value = Math.round(data[index]);
                    }
                }
            });
        } else {
            throw new Error("Invalid response format");
        }
    })
    .catch(error => {
        console.error("Error estimating calories:", error);
        alert("Failed to estimate calories. Please try again or enter calories manually.");
    });
} 

function removeFoodItem(button) {
    const foodItem = button.closest('.food-item');
    foodItem.remove();
    
    // Update the indices of remaining food items
    const container = document.getElementById('food-items-container');
    const foodItems = container.querySelectorAll('.food-item');
    foodItems.forEach((item, index) => {
        const inputs = item.querySelectorAll('input, select');
        inputs.forEach(input => {
            const name = input.getAttribute('name');
            if (name) {
                input.setAttribute('name', name.replace(/\[\d+\]/, `[${index}]`));
            }
        });
    });
}

// Workout-related functions
function estimateBurnedCalories() {
    const duration = parseFloat(document.getElementById('duration').value);
    const workoutName = document.getElementById('workoutName').value;
    
    if (!duration || !workoutName || isNaN(duration)) {
        alert('Please enter both workout name and a valid duration');
        return;
    }

    fetch('/workouts/estimate-calories', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'X-CSRF-TOKEN': document.querySelector('input[name="_csrf"]').value
        },
        body: JSON.stringify({
            workoutName: workoutName,
            duration: duration
        })
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }
        return response.json();
    })
    .then(data => {
        if (data && typeof data.calories === 'number') {
            document.getElementById('caloriesBurned').value = data.calories;
        } else {
            throw new Error('Invalid response format');
        }
    })
    .catch(error => {
        console.error('Error:', error);
        alert('Failed to estimate calories. Please try again.');
    });
}

