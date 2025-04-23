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
                // Remove the workout from the list
                document.querySelector(`.workout-item[data-id="${workoutId}"]`)?.remove();

                // If no more workouts, reload the page
                if (document.querySelectorAll(".workout-item").length === 0) {
                    location.reload();
                }

                closeWorkoutModal();
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
        
        // Add click event listener for the Mark as Done button
        markAsDoneButton.onclick = function() {
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
                // Remove the workout from the list
                document.querySelector(`.workout-item[data-id="${workoutId}"]`)?.remove();

                // If no more workouts, reload the page
                if (document.querySelectorAll(".workout-item").length === 0) {
                    location.reload();
                }

                closeWorkoutModal();
            })
            .catch(error => {
                console.error("Error:", error);
                alert("Failed to mark workout as done. Please try again.");
            });
        };
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
});

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

