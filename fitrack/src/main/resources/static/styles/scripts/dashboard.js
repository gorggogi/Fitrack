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
    const modal = document.getElementById("workoutDoneModal");
    const markAsDoneButton = document.getElementById("markAsDoneButton");

    if (!modal || !markAsDoneButton) {
        console.error("⚠️ Modal or Mark as Done button not found.");
        return;
    }

    // Attach click listener to each workout item
    document.querySelectorAll(".workout-item").forEach(item => {
        item.addEventListener("click", () => openModal(item));
    });

    // Mark as Done logic
    markAsDoneButton.addEventListener("click", function () {
        const workoutId = this.getAttribute("data-id");
        if (!workoutId) return console.error("⚠️ Workout ID not found!");

        fetch(`/user/workouts/${workoutId}/mark-done`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "X-CSRF-TOKEN": document.querySelector("input[name=_csrf]")?.value || ""
            }
        })
        .then(response => response.ok ? response.text() : Promise.reject(`HTTP error: ${response.status}`))
        .then(() => {
            document.querySelector(`.workout-item[data-id="${workoutId}"]`)?.remove();

            setTimeout(() => {
                if (document.querySelectorAll(".workout-item").length === 0) {
                    console.log("🚀 Reloading page...");
                    location.reload();
                }
            });

            modal.style.display = "none";
        })
        .catch(error => {
            console.error("Error:", error);
            alert("Something went wrong. Please try again.");
        });
    });

    window.addEventListener("click", event => {
        if (event.target === modal) modal.style.display = "none";
    });
});


function openModal(workoutElement) {
    const modal = document.getElementById("workoutDoneModal");
    if (!modal) return console.error("⚠️ Workout modal not found.");

    document.getElementById("modalWorkoutName").innerText = workoutElement.getAttribute("data-name");
    document.getElementById("modalWorkoutDuration").innerText = workoutElement.getAttribute("data-duration") + " mins";
    document.getElementById("modalWorkoutCalories").innerText = workoutElement.getAttribute("data-calories") + " cal";

    document.getElementById("markAsDoneButton").setAttribute("data-id", workoutElement.getAttribute("data-id"));
    modal.style.display = "block";
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

function closeWorkoutModal() {
    document.getElementById('workoutModal').style.display = 'none';
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
function estimateCalories() {
    const foodItems = document.querySelectorAll('.food-item');
    foodItems.forEach((item, index) => {
        const foodName = item.querySelector('input[name="foodItems[' + index + '].foodItem"]').value;
        const quantity = item.querySelector('input[name="foodItems[' + index + '].quantity"]').value;
        const unit = item.querySelector('select[name="foodItems[' + index + '].unit"]').value;
        
        if (foodName && quantity) {
            // This is a simple estimation - in a real app, you'd want to use a food database API
            const estimatedCalories = Math.round(quantity * 2); // Simple estimation: 2 calories per unit
            item.querySelector('input[name="foodItems[' + index + '].calories"]').value = estimatedCalories;
        }
    });
}

function addFoodItem() {
    const container = document.getElementById('food-items-container');
    const foodItemCount = container.children.length;
    
    const newFoodItem = document.createElement('div');
    newFoodItem.className = 'food-item';
    newFoodItem.innerHTML = `
        <div class="form-row">
            <div class="form-group">
                <label>Food Item</label>
                <input type="text" name="foodItems[${foodItemCount}].foodItem" placeholder="Enter food" class="form-control" required>
            </div>
            <div class="form-group">
                <label>Quantity</label>
                <input type="number" name="foodItems[${foodItemCount}].quantity" placeholder="Quantity" class="form-control" required>
            </div>
            <div class="form-group">
                <label>Unit</label>
                <select name="foodItems[${foodItemCount}].unit" class="form-control" required>
                    <option value=" ">None</option>
                    <option value="g">Grams</option>
                    <option value="kg">Kilograms</option>
                    <option value="ml">Milliliters</option>
                    <option value="l">Liters</option>
                    <option value="cup">Cups</option>
                    <option value="tbsp">Tablespoons</option>
                    <option value="tsp">Teaspoons</option>
                </select>
            </div>
            <div class="form-group">
                <label>Calories</label>
                <input type="number" name="foodItems[${foodItemCount}].calories" placeholder="Estimate" class="form-control" required>
            </div>
            <button type="button" class="remove-item" onclick="removeFoodItem(this)">×</button>
        </div>
    `;
    
    container.appendChild(newFoodItem);
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
    const duration = document.getElementById('duration').value;
    const workoutName = document.getElementById('workoutName').value.toLowerCase();
    
    if (!duration) {
        alert('Please enter the duration first');
        return;
    }

    // Base calorie burn rate (calories per minute)
    let baseRate = 5; // Default moderate intensity
    
    // Adjust base rate based on workout type
    if (workoutName.includes('run') || workoutName.includes('sprint')) {
        baseRate = 10; // High intensity
    } else if (workoutName.includes('walk') || workoutName.includes('yoga')) {
        baseRate = 3; // Low intensity
    } else if (workoutName.includes('swim') || workoutName.includes('cycle')) {
        baseRate = 8; // Medium-high intensity
    } else if (workoutName.includes('weight') || workoutName.includes('strength')) {
        baseRate = 7; // Medium intensity
    }

    // Calculate estimated calories
    const estimatedCalories = Math.round(duration * baseRate);
    document.getElementById('burnedCalories').value = estimatedCalories;
}

