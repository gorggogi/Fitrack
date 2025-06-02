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
    const workoutModal = document.getElementById("workoutModal");
    const markAsDoneButton = document.getElementById("markAsDoneButton");

    if (!workoutModal) {
        console.error("⚠️ Workout modal not found.");
    }

    document.querySelectorAll(".workout-item").forEach(item => {
        console.log("Attaching listener to:", item); // Debug log
        item.addEventListener("click", (event) => { // Pass event
            event.stopPropagation(); // Keep stopPropagation if needed
            openModal(item);
        });
    });

    if (markAsDoneButton) {
        markAsDoneButton.addEventListener("click", function () {
            const workoutId = this.getAttribute("data-id");
            if (!workoutId) return console.error("⚠️ Workout ID not found!");

            const csrfTokenElement = document.querySelector('input[name="_csrf"]');
            if (!csrfTokenElement || !csrfTokenElement.value) {
                console.error("⚠️ CSRF token not found or empty!");
                return;
            }
            const csrfToken = csrfTokenElement.value;

            fetch(contextPath + `user/workouts/${workoutId}/mark-done`, {
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
                closeWorkoutModal();
                window.location.reload();
            })
            .catch(error => {
                console.error("Error:", error);
                alert("Failed to mark workout as done. Please try again.");
            });
        });
    }
    
    window.addEventListener("click", event => {
        if (workoutModal && event.target === workoutModal) {
            closeWorkoutModal();
        }
        const mealModal = document.getElementById('mealModal');
        if (mealModal && event.target === mealModal) {
            closeMealModal();
        }
        const goalModal = document.getElementById('goalModal');
        if (goalModal && event.target === goalModal) {
            closeGoalModal();
        }
        const completedGoalModal = document.getElementById('completedGoalDetailModal');
        if (completedGoalModal && event.target == completedGoalModal) {
            closeCompletedGoalDetailModal();
        }
    });

    let goalNameFromSession = null;
    try {
        goalNameFromSession = sessionStorage.getItem('fitrackShowToastGoal');
        if (goalNameFromSession) {
            sessionStorage.removeItem('fitrackShowToastGoal');
        }
    } catch (e) {
        console.error("Failed to read from sessionStorage", e);
    }

    if (goalNameFromSession) {
        showGoalCompletedToast(goalNameFromSession, false);
    } else if (typeof completedGoalNameFromBackend !== 'undefined' && completedGoalNameFromBackend) {
        showGoalCompletedToast(completedGoalNameFromBackend, false);
    }

    const completedGoalCards = document.querySelectorAll('.goal-card.completed-goal');
    completedGoalCards.forEach(card => {
        card.addEventListener('click', function() {
            openCompletedGoalDetailModal(this);
        });
    });
    
    const workoutForm = document.getElementById('workoutForm');
    if (workoutForm) {
        workoutForm.addEventListener('submit', function(e) {
            e.preventDefault();
            const formData = new FormData(this);
            const csrfTokenElement = document.querySelector('input[name="_csrf"]');
            if (!csrfTokenElement || !csrfTokenElement.value) {
                console.error("⚠️ CSRF token not found for workout form!");
                alert('Security token missing. Please refresh and try again.');
                return;
            }

            let targetUrl = contextPath + 'user/saveworkout';
            if (this.action.includes('/update') || this.action.match(/\/\d+$/)) {
                const workoutIdInput = document.getElementById('workoutId');
                const modalTitleElement = document.getElementById('modalTitle');
                let isLikelyAddNewContext = (!workoutIdInput || !workoutIdInput.value);
                if(modalTitleElement && modalTitleElement.textContent === 'Add New Workout'){
                    isLikelyAddNewContext = true;
                }
                if(isLikelyAddNewContext){
                    console.log("Workout form action is for update, but context is 'add new'. Overriding to save new workout.");
                } else {
                    targetUrl = this.action; 
                }
            }
            
            const workoutName = formData.get('workoutName'); // Get workout name for potential client-side update

            fetch(targetUrl, {
                method: 'POST',
                body: formData
            })
            .then(response => {
                if (response.ok) {
                    if (window.analyticsContextSave === true) {
                        // Analytics context: Update UI client-side, do not reload page
                        console.log("Analytics context save: Updating UI client-side.");
                        if (workoutName && window.analyticsData && window.analyticsData.scheduledWorkoutNames && !window.analyticsData.scheduledWorkoutNames.includes(workoutName)) {
                            window.analyticsData.scheduledWorkoutNames.push(workoutName);
                        }
                        if (typeof window.refreshScheduledIndicators === 'function') {
                            window.refreshScheduledIndicators();
                        }
                        window.analyticsContextSave = false; // Reset flag
                        closeWorkoutModal();
                        this.reset();
                        // DO NOT RELOAD
                    } else {
                        // Default context: Close modal, reset form, and reload page
                        closeWorkoutModal();
                        this.reset();
                        window.location.reload();
                    }
                } else {
                    response.text().then(text => {
                        console.error('Error response:', text);
                        alert('Error adding workout. Please try again.');
                    });
                     if (window.analyticsContextSave === true) window.analyticsContextSave = false; // Reset flag on error too
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert('Error adding workout. Please try again.');
                if (window.analyticsContextSave === true) window.analyticsContextSave = false; // Reset flag on error too
            });
        });
    }

    const goalForm = document.getElementById('goalForm');
    if (goalForm) {
        goalForm.addEventListener('submit', function(e) {
            e.preventDefault();
            const formData = new FormData(this);
            fetch(contextPath + 'user/goals/add', {
                method: 'POST',
                body: formData
            })
            .then(response => {
                if (response.ok) {
                    closeGoalModal();
                    this.reset();
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

    const mealForm = document.getElementById('mealForm');
    if (mealForm) {
        mealForm.addEventListener('submit', function(e) {
            // Only handle via AJAX if the action is for the dashboard's default save action
            // Otherwise, allow default submission (e.g., for loggedmeals.html edit)
            if (this.getAttribute('action') !== '/user/meals/save') {
                return; // Allow default browser submission
            }

            e.preventDefault(); 
            const formData = new FormData(this);
            const csrfTokenElement = document.querySelector('input[name="_csrf"]');
            if (!csrfTokenElement || !csrfTokenElement.value) {
                console.error("⚠️ CSRF token not found for meal form!");
                alert('Security token missing. Please refresh and try again.');
                return;
            }
            const csrfToken = csrfTokenElement.value;
            
            fetch(contextPath + 'user/meals/save', { 
                method: 'POST',
                body: formData,
                headers: {
                    'X-CSRF-TOKEN': csrfToken,
                    'Accept': 'application/json' 
                }
            })
            .then(response => {
                if (response.ok) {
                    return response.json(); 
                } else {
                    return response.text().then(text => {
                        throw new Error(text || 'Failed to save meal'); 
                    });
                }
            })
            .then(todayMealDTOs => {
                console.log("Received updated meals:", todayMealDTOs);
                updateDashboardMeals(todayMealDTOs); 
                closeMealModal(); 
            })
            .catch(error => {
                console.error('Error saving meal:', error);
                alert('Error saving meal: ' + error.message); 
            });
        });
    }

    const mealsSection = document.querySelector('.meals-section');
    if (mealsSection) {
        const addMealButton = mealsSection.querySelector('.add-button');
        if (addMealButton) {
            addMealButton.addEventListener('click', function(event) {
                event.stopPropagation(); 
                openMealModal(); 
            });
        }
    }

    // Update to attach the new modal opening function to ALL goal cards
    const allGoalCards = document.querySelectorAll('.goal-card');
    allGoalCards.forEach(card => {
        card.addEventListener('click', function() {
            openGoalDetailModal(this); // Use the new generic function
        });
    });
});

function openModal(workoutElement) {
    console.log("openModal called. workoutElement:", workoutElement);
    const modal = document.getElementById("workoutModal");
    const workoutForm = document.getElementById("workoutForm");
    const workoutDetails = document.getElementById("workoutDetails");
    const modalTitle = document.getElementById("modalTitle");
    
    if (!modal) return console.error("⚠️ Workout modal not found globally.");
    if (!modalTitle) console.warn("⚠️ modalTitle element not found in current modal.");

    if (workoutElement) { // For viewing details of a workout item from the dashboard
        if (modalTitle) modalTitle.textContent = "Workout Details";
        if (workoutForm) workoutForm.style.display = "none"; else console.warn("⚠️ workoutForm not found in modal for workoutElement branch.");
        if (workoutDetails) workoutDetails.style.display = "block"; else console.warn("⚠️ workoutDetails not found in modal for workoutElement branch.");
        
        if (workoutDetails) { // Only populate if workoutDetails exists
            const modalWorkoutName = document.getElementById("modalWorkoutName");
            const modalWorkoutDuration = document.getElementById("modalWorkoutDuration");
            const modalWorkoutCalories = document.getElementById("modalWorkoutCalories");
            const markAsDoneButton = document.getElementById("markAsDoneButton");

            if (modalWorkoutName) modalWorkoutName.textContent = workoutElement.getAttribute("data-name");
            if (modalWorkoutDuration) modalWorkoutDuration.textContent = workoutElement.getAttribute("data-duration") + " mins";
            if (modalWorkoutCalories) modalWorkoutCalories.textContent = workoutElement.getAttribute("data-calories") + " cal";
            if (markAsDoneButton) markAsDoneButton.setAttribute("data-id", workoutElement.getAttribute("data-id"));
        } else {
            console.warn("Cannot populate workout details because #workoutDetails element is missing.");
        }
    } else { // For adding a new workout (or when called by scheduledworkouts.js)
        if (modalTitle) modalTitle.textContent = "Add New Workout";
        if (workoutForm) workoutForm.style.display = "block"; else console.warn("⚠️ workoutForm not found in modal for new/scheduled branch.");
        if (workoutDetails) workoutDetails.style.display = "none"; // Still hide if it exists, but don't error if not
    }

    modal.style.display = "flex";
}

function closeWorkoutModal() {
    const modal = document.getElementById("workoutModal");
    const workoutForm = document.getElementById("workoutForm");
    const workoutDetails = document.getElementById("workoutDetails"); // This ID is specific to dashboard.html modal
    
    if (modal) {
        modal.style.display = "none";
        // Reset form only if it exists
        if (workoutForm) workoutForm.reset(); 
        // Hide details section only if it exists
        if (workoutDetails) workoutDetails.style.display = "none"; 
    }
}

function openGoalModal() {
    document.getElementById('goalModal').style.display = 'flex';
}

function closeGoalModal() {
    document.getElementById('goalModal').style.display = 'none';
}

function openMealModal() {
    const mealModal = document.getElementById('mealModal');
    if (!mealModal) {
        console.error("Meal modal not found!");
        return;
    }
    mealModal.style.display = 'flex';

    const mealForm = document.getElementById('mealForm');
    if (mealForm) mealForm.reset(); 

    const mealIdInput = document.getElementById('mealId');
    if (mealIdInput) mealIdInput.value = ''; 

    const mealModalTitle = mealModal.querySelector('#modalTitle') || mealModal.querySelector('h2');
    if (mealModalTitle) mealModalTitle.textContent = 'Add New Meal';
    
    if (mealForm) mealForm.action = '/user/meals/save'; 

    const dateTimeInput = document.getElementById('dateTime');
    if (dateTimeInput) {
        const now = new Date();
        const year = now.getFullYear();
        const month = (now.getMonth() + 1).toString().padStart(2, '0');
        const day = now.getDate().toString().padStart(2, '0');
        const hours = now.getHours().toString().padStart(2, '0');
        const minutes = now.getMinutes().toString().padStart(2, '0');
        dateTimeInput.value = `${year}-${month}-${day}T${hours}:${minutes}`;
    }

    const container = document.getElementById('foodItemsContainer');
    if (container) {
        container.innerHTML = ''; 
        addFoodItem();
    } else {
        console.error("foodItemsContainer not found in meal modal!");
    }
}

function closeMealModal() {
    const modal = document.getElementById('mealModal');
    if (modal) {
        modal.style.display = 'none';
    }
}

function openGoalDetailModal(goalCardElement) {
    const modal = document.getElementById('completedGoalDetailModal');
    if (!modal) return;

    console.log('[Debug] Opening goal detail modal. Goal card classes:', goalCardElement.className);

    const goalId = goalCardElement.getAttribute('data-goal-id');
    const goalName = goalCardElement.getAttribute('data-goal-name');
    const goalStartDate = goalCardElement.getAttribute('data-goal-start-date');
    const goalType = goalCardElement.getAttribute('data-goal-type');
    const goalInitialValue = goalCardElement.getAttribute('data-goal-initial-value');
    const goalCurrentValue = goalCardElement.getAttribute('data-goal-achieved-value'); // Current value is stored in 'data-goal-achieved-value'
    const goalTargetValue = goalCardElement.getAttribute('data-goal-target-value');
    const goalCompletionDate = goalCardElement.getAttribute('data-goal-completion-date');

    let unitSuffix = '';
    let startingLabel = 'Starting:';

    if (goalType) {
        const upperGoalType = goalType.toUpperCase();
        if (upperGoalType === 'WEIGHT_LOSS' || upperGoalType === 'WEIGHT_GAIN') {
            unitSuffix = ' kg';
            startingLabel = 'Initial Value:';
        }
    }

    const formattedGoalType = goalType ? goalType.replace('_', ' ').toLowerCase().replace(/\b\w/g, l => l.toUpperCase()) : 'N/A';

    document.getElementById('completedGoalNameModalText').textContent = goalName;
    document.getElementById('completedGoalTypeModalText').textContent = `Type: ${formattedGoalType}`;
    document.getElementById('completedGoalTargetModalText').textContent = `${startingLabel} ${goalInitialValue}${unitSuffix}`;
    document.getElementById('completedGoalAchievedModalText').textContent = `Current: ${goalCurrentValue}${unitSuffix} / Target: ${goalTargetValue}${unitSuffix}`;
    document.getElementById('completedGoalSetDateText').textContent = `Goal set on: ${goalStartDate}.`;
    document.getElementById('completedGoalAchievedDateText').textContent = goalCompletionDate && goalCompletionDate !== 'N/A' ? `Goal achieved on: ${goalCompletionDate}.` : '';

    const trophyIcon = modal.querySelector('.modal-trophy-icon');
    const isCompleted = goalCardElement.classList.contains('completed-goal');
    console.log('[Debug] isCompleted:', isCompleted);

    if (trophyIcon) {
        trophyIcon.classList.remove('modal-trophy-icon-incomplete');
        trophyIcon.classList.add('fas', 'fa-trophy');

        if (isCompleted) {
            console.log('[Debug] Trophy: Goal is completed.');
        } else {
            console.log('[Debug] Trophy: Goal is NOT completed, making icon gray.');
            trophyIcon.classList.add('modal-trophy-icon-incomplete');
        }
    }

    const actionButton = document.getElementById('archiveGoalButton');
    console.log('[Debug] actionButton element:', actionButton);
    const csrfTokenEl = document.querySelector('input[name="_csrf"]');
    let csrfToken = '';
    if (!csrfTokenEl || !csrfTokenEl.value) {
        console.error('Error: Security token not found. Button actions will fail.');
        // Optionally disable the button or alert the user more directly
        actionButton.disabled = true;
        actionButton.textContent = 'Error';
    } else {
        csrfToken = csrfTokenEl.value;
        actionButton.disabled = false; // Ensure button is enabled if CSRF token is found
    }

    if (isCompleted) {
        console.log('[Debug] Button: Goal is completed. Setting text to "archive goal".');
        actionButton.textContent = 'archive goal';
        actionButton.onclick = function() {
            if (!goalId) {
                alert('Error: Goal ID not found.');
                return;
            }
            if (!confirm(`Are you sure you want to archive the goal "${goalName}"?`)) {
                return;
            }
            if (!csrfToken) {
                 alert('Error: Security token not found. Please refresh the page.');
                 return;
            }

            actionButton.disabled = true;
            actionButton.textContent = 'archiving...';

            fetch(contextPath + `user/goals/archive/${goalId}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-CSRF-TOKEN': csrfToken
                },
                credentials: 'same-origin'
            })
            .then(response => {
                if (!response.ok) {
                    return response.text().then(text => { 
                        throw new Error(text || `Failed to archive goal. Status: ${response.status}`); 
                    });
                }
                return response.text();
            })
            .then(message => {
                console.log(message);
                closeCompletedGoalDetailModal();
                window.location.reload();
            })
            .catch(error => {
                console.error('Error archiving goal:', error);
                alert('Error archiving goal: ' + error.message);
                actionButton.disabled = false;
                actionButton.textContent = 'archive goal';
            });
        };
    } else { // Goal is not completed
        console.log('[Debug] Button: Goal is NOT completed. Setting text to "remove goal".');
        actionButton.textContent = 'remove goal';
        actionButton.onclick = function() {
            if (!goalId) {
                alert('Error: Goal ID not found.');
                return;
            }
            if (!confirm(`Are you sure you want to remove the goal "${goalName}"? This action cannot be undone.`)) {
                return;
            }
            if (!csrfToken) {
                 alert('Error: Security token not found. Please refresh the page.');
                 return;
            }

            actionButton.disabled = true;
            actionButton.textContent = 'removing...';

            fetch(contextPath + `user/goals/delete/${goalId}`, {
                method: 'POST', // Assuming delete endpoint is POST as per typical Spring Boot setups with CSRF
                headers: {
                    'Content-Type': 'application/json', // Or 'application/x-www-form-urlencoded' if backend expects that
                    'X-CSRF-TOKEN': csrfToken
                },
                credentials: 'same-origin'
            })
            .then(response => {
                if (!response.ok) {
                    return response.text().then(text => { 
                        throw new Error(text || `Failed to remove goal. Status: ${response.status}`); 
                    });
                }
                // Check if response has content before parsing as text/json
                // For a delete operation, often a 200 OK or 204 No Content is returned.
                // If 204, response.text() might be empty or cause issues.
                if (response.status === 204) {
                    return "Goal removed successfully (No Content)";
                }
                return response.text(); 
            })
            .then(message => {
                console.log(message);
                closeCompletedGoalDetailModal();
                window.location.reload();
            })
            .catch(error => {
                console.error('Error removing goal:', error);
                alert('Error removing goal: ' + error.message);
                actionButton.disabled = false;
                actionButton.textContent = 'remove goal';
            });
        };
    }

    modal.style.display = 'flex';
}

function closeCompletedGoalDetailModal() {
    const modal = document.getElementById('completedGoalDetailModal');
    if (modal) {
        modal.style.display = 'none';
    }
}

let toastTimeout;

function showGoalCompletedToast(goalName, storeInSession = false) {
    const toast = document.getElementById('goalCompletedToast');
    const toastMessageSpan = document.getElementById('toastMessage');

    if (toast && toastMessageSpan) {
        toastMessageSpan.textContent = 'You have completed your goal! "' + goalName + '"';
        toast.style.display = 'flex';

        if (toastTimeout) {
            clearThoimeout(toastTimeout);
        }

        toastTimeout = setTimeout(function() {
            toast.style.display = 'none';
        }, 15000);

        if (storeInSession) {
            try {
                sessionStorage.setItem('fitrackShowToastGoal', goalName);
            } catch (e) {
                console.error("Failed to write to sessionStorage", e);
            }
        }
    }
}

function updateDashboardMeals(mealDTOs) {
    const mealsSection = document.querySelector('.meals-section');
    const placeholder = mealsSection ? mealsSection.querySelector('.placeholder-message') : null;
    const mealItemsContainer = mealsSection;
    
    const summaryCards = document.querySelectorAll('.summary-card');
    let calorieSummaryEl = null;
    let mealCountSummaryEl = null;
    summaryCards.forEach(card => {
        const titleEl = card.querySelector('p');
        if (titleEl && titleEl.textContent.includes('Total Calorie Intake')) {
            calorieSummaryEl = card.querySelector('h2');
        }
        if (titleEl && titleEl.textContent.includes('Workouts Completed')) {
            mealCountSummaryEl = card.querySelector('h2'); 
        }
    });

    if (!mealItemsContainer) {
        console.error("Could not find meal items container in dashboard.");
        return;
    }

    mealItemsContainer.querySelectorAll('.meal-item').forEach(item => item.remove());

    let totalCaloriesToday = 0;
    let mealCountToday = 0;

    if (mealDTOs && mealDTOs.length > 0) {
        mealCountToday = mealDTOs.length;
        mealDTOs.forEach(meal => {
            totalCaloriesToday += meal.totalCalories;
            const mealElement = document.createElement('div');
            mealElement.className = 'meal-item';

            let formattedTime = 'N/A';
            try {
                formattedTime = new Date(meal.dateTime).toLocaleTimeString('en-US', { hour: 'numeric', minute: '2-digit', hour12: true });
            } catch (e) { console.error("Error formatting date:", e); }

            const foodListHtml = meal.foodItems
                                    .map(fi => `<span class="food-item-name">${escapeHtml(fi.foodItem)}</span>`)
                                    .join(', ');

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

        if (placeholder) {
            placeholder.style.display = 'none';
        }
    } else {
        if (placeholder) {
            placeholder.style.display = 'block';
        }
    }

    if (calorieSummaryEl) {
        calorieSummaryEl.textContent = totalCaloriesToday + ' cal';
    }
    if (mealCountSummaryEl) {
        console.warn("Selector for meal count summary needs verification. Update skipped.");
    } else {
        console.warn("Could not find summary element for meal count.");
    }
}

function escapeHtml(unsafe) {
    if (typeof unsafe !== 'string') return unsafe;
    return unsafe
         .replace(/&/g, "&amp;")
         .replace(/</g, "&lt;")
         .replace(/>/g, "&gt;")
         .replace(/"/g, "&quot;")
         .replace(/'/g, "&#039;");
 }

function addFoodItem() {
    const container = document.getElementById('foodItemsContainer');
    if (!container) {
        console.error("foodItemsContainer for addFoodItem not found!");
        return;
    }
    const index = container.children.length;
    const foodItemDiv = document.createElement('div');
    foodItemDiv.className = 'food-item-row';
    foodItemDiv.innerHTML = `
        <div class="form-row">
            <div class="form-group">
                <label>Food Item</label>
                <input type="text" name="foodItems[${index}].foodItem" placeholder="Enter food" class="form-control" required>
            </div>
            <div class="form-group">
                <label>Quantity</label>
                <input type="number" name="foodItems[${index}].quantity" placeholder="Quantity" class="form-control" step="any">
            </div>
            <div class="form-group unit-group">
                <label>Unit</label>
                <select name="foodItems[${index}].unit" class="form-control unit-select" data-index="${index}" onchange="handleUnitChange(this, ${index})">
                    <option value="g">Grams</option>
                    <option value="kg">Kilograms</option>
                    <option value="ml">Milliliters</option>
                    <option value="l">Liters</option>
                    <option value="cup">Cups</option>
                    <option value="tbsp">Tablespoons</option>
                    <option value="tsp">Teaspoons</option>
                    <option value="oz">Ounce (oz)</option>
                    <option value="serving">Serving</option>
                    <option value="piece">Piece</option>
                    <option value="other">Other</option> 
                </select>
                <input type="text" name="foodItems[${index}].otherUnit" class="form-control other-unit-input" placeholder="Specify unit" style="display: none; margin-top: 5px;">
            </div>
            <div class="form-group">
                <label>Calories</label>
                <input type="number" name="foodItems[${index}].calories" placeholder="Calories" class="form-control" required>
            </div>
            <button type="button" class="remove-item" onclick="removeFoodItem(this)" style="align-self: flex-end; margin-bottom: 1rem;">×</button>
        </div>
    `;
    container.appendChild(foodItemDiv);
}

function handleUnitChange(selectElement, index) {
    const otherUnitInput = selectElement.closest('.unit-group').querySelector('.other-unit-input');
    if (selectElement.value === 'other') {
        otherUnitInput.style.display = 'block';
        otherUnitInput.name = `foodItems[${index}].otherUnit`; // Ensure name is set for submission
        // Optionally, if you want the select to have no value when "Other" is chosen
        // and the text input takes precedence, you might clear the select's name or value.
        // However, for simplicity in backend, it might be better to send both 
        // and let backend decide based on "other" selection.
        // Or, ensure the select's name is `foodItems[${index}].unit` and backend checks if it's "other".
    } else {
        otherUnitInput.style.display = 'none';
        otherUnitInput.name = `foodItems[${index}].otherUnit_disabled`; // Change name so it's not submitted
        otherUnitInput.value = ''; // Clear value
    }
}

function removeFoodItem(button) {
    const foodItemRow = button.closest('.food-item-row');
    if (foodItemRow) {
        const container = foodItemRow.parentElement;
        foodItemRow.remove();
        const items = container.querySelectorAll('.food-item-row');
        items.forEach((item, newIndex) => {
            // Update index for all inputs and selects within the row
            const inputs = item.querySelectorAll('input, select');
            inputs.forEach(input => {
                const name = input.getAttribute('name');
                if (name && name.startsWith('foodItems[')) {
                    const newName = name.replace(/foodItems\[\d+\]/, `foodItems[${newIndex}]`);
                    input.setAttribute('name', newName);
                }
            });
            // Specifically update the unit select's onchange handler if it exists
            const unitSelect = item.querySelector('.unit-select');
            if (unitSelect) {
                unitSelect.setAttribute('onchange', `handleUnitChange(this, ${newIndex})`);
                // Also update the data-index if you use it elsewhere, though onchange is key here
                unitSelect.setAttribute('data-index', newIndex);

                // Ensure the associated otherUnit input also gets its name updated if visible
                const otherUnitInput = item.querySelector('.other-unit-input');
                if (otherUnitInput && otherUnitInput.style.display !== 'none') {
                    otherUnitInput.setAttribute('name', `foodItems[${newIndex}].otherUnit`);
                } else if (otherUnitInput) {
                     otherUnitInput.setAttribute('name', `foodItems[${newIndex}].otherUnit_disabled`);
                }
            }
        });
    }
}

async function estimateCalories() {
    const foodItemsContainer = document.getElementById('foodItemsContainer');
    const foodItemRows = foodItemsContainer.querySelectorAll('.food-item-row');
    const estimateButton = document.getElementById('estimateCaloriesButton');
    const csrfTokenElement = document.querySelector('input[name="_csrf"]');

    if (!csrfTokenElement || !csrfTokenElement.value) {
        console.error("⚠️ CSRF token not found for calorie estimation!");
        alert('Security token missing. Please refresh and try again.');
        return;
    }
    const csrfToken = csrfTokenElement.value;

    if (estimateButton) {
        estimateButton.disabled = true;
        estimateButton.textContent = 'Estimating...';
    }

    const foodDataToSend = [];
    const calorieInputFields = []; // To map responses back to inputs

    for (let i = 0; i < foodItemRows.length; i++) {
        const itemRow = foodItemRows[i];
        const foodNameInput = itemRow.querySelector('input[name^="foodItems"][name$=".foodItem"]');
        const quantityInput = itemRow.querySelector('input[name^="foodItems"][name$=".quantity"]');
        const unitInput = itemRow.querySelector('select[name^="foodItems"][name$=".unit"]');
        const caloriesInput = itemRow.querySelector('input[name^="foodItems"][name$=".calories"]');

        const foodName = foodNameInput ? foodNameInput.value : null;
        const quantity = quantityInput ? parseFloat(quantityInput.value) : null;
        const unit = unitInput ? unitInput.value : null;

        if (foodName && quantity && unit && quantity > 0) {
            foodDataToSend.push({
                foodName: foodName,
                quantity: quantity,
                unit: unit
            });
            calorieInputFields.push(caloriesInput); // Store the input field to update later
        } else {
            // If an item is invalid for estimation, push a null placeholder for caloriesInput
            // to maintain array alignment, or handle it by skipping and alerting the user.
            // For simplicity now, we'll only send valid items.
            // If an item row is partially filled, it won't be sent.
            console.warn("Skipping partially filled or invalid food item row for estimation:", itemRow);
        }
    }

    if (foodDataToSend.length === 0) {
        alert("No valid food items to estimate. Please ensure food name, quantity (greater than 0), and unit are filled for at least one item.");
        if (estimateButton) {
            estimateButton.disabled = false;
            estimateButton.textContent = 'Estimate Calories';
        }
        return;
    }
    
    console.log(`Sending calorie estimation request for multiple items:`, foodDataToSend);

    try {
        const response = await fetch(contextPath + 'meals/estimate-calories', {
            method: 'POST',
        headers: {
            "Content-Type": "application/json",
            "X-CSRF-TOKEN": csrfToken
        },
            body: JSON.stringify(foodDataToSend), // Send the array
        credentials: 'same-origin'
        });

        if (!response.ok) {
            // Attempt to get more detailed error from response body
            const errorText = await response.text();
            throw new Error(`HTTP error! status: ${response.status}, message: ${errorText || 'No error message'}`);
        }

        const estimatedCaloriesArray = await response.json(); // Expecting an array of numbers

        if (Array.isArray(estimatedCaloriesArray) && estimatedCaloriesArray.length === calorieInputFields.length) {
            let totalEstimatedCalories = 0;
            estimatedCaloriesArray.forEach((calories, index) => {
                if (calorieInputFields[index]) {
                    const roundedCalories = Math.round(calories);
                    calorieInputFields[index].value = roundedCalories;
                    totalEstimatedCalories += roundedCalories;
                }
            });
            // Optionally, display total estimated for the batch
            // alert(`Estimated total calories for submitted items: ${totalEstimatedCalories.toFixed(0)} cal`);
        } else {
            console.error("Mismatch in response array length or format.", estimatedCaloriesArray);
            throw new Error("Invalid response format from calorie estimation service. Expected an array of calorie values matching the number of valid items sent.");
        }

    } catch (error) {
        console.error("Error estimating calories:", error);
        alert("Failed to estimate calories. " + error.message);
    } finally {
        if (estimateButton) {
            estimateButton.disabled = false;
            estimateButton.textContent = 'Estimate Calories';
        }
    }
}

async function estimateBurnedCalories() {
    console.log("estimateBurnedCalories called");
    const exerciseNameInput = document.getElementById('workoutName');
    const durationInput = document.getElementById('duration');
    const caloriesBurnedInput = document.getElementById('caloriesBurned');
    const estimateButton = document.getElementById('estimateBurnedCaloriesButton'); // Get the estimate button

    const csrfTokenElement = document.querySelector('input[name="_csrf"]');
    if (!csrfTokenElement || !csrfTokenElement.value) {
        console.error("⚠️ CSRF token not found for burned calorie estimation!");
        alert('Security token missing. Please refresh and try again.');
        if (estimateButton) estimateButton.disabled = false; // Re-enable if CSRF is missing
        return;
    }
    const csrfToken = csrfTokenElement.value;

    const exerciseName = exerciseNameInput.value;
    const duration = parseFloat(durationInput.value);
    
    if (!exerciseName || !duration || duration <= 0) {
        alert('Please enter both exercise name and a valid duration.');
        if (estimateButton) estimateButton.disabled = false; // Re-enable if input is invalid
        return;
    }

    if (estimateButton) {
        estimateButton.disabled = true; // Disable the button
        estimateButton.textContent = 'Estimating...'; // Change text
    }

    console.log(`Sending burned calorie estimation request:`, { exerciseName, duration });

    try {
        const response = await fetch(contextPath + 'workouts/estimate-calories', { // Corrected URL: removed -burned
        method: 'POST',
        headers: {
                "Content-Type": "application/json",
                "X-CSRF-TOKEN": csrfToken
        },
        body: JSON.stringify({
                workoutName: exerciseName, // Corrected JSON key to workoutName
            duration: duration
        })
        });

        if (!response.ok) {
            throw new Error(`Network response was not ok. Status: ${response.status}`);
        }

        const data = await response.json();
        if (data && typeof data.calories === 'number') {
            caloriesBurnedInput.value = data.calories.toFixed(0); // Display as whole number
        } else {
            throw new Error('Invalid response format from calorie estimation service.');
        }
    } catch (error) {
        console.error('Error estimating burned calories:', error);
        alert('Failed to estimate calories: ' + error.message);
    } finally {
        if (estimateButton) {
            estimateButton.disabled = false;
            estimateButton.textContent = 'Estimate Calories';
        }
    }
}


