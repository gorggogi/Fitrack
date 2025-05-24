const markAsDoneButton = document.getElementById("markAsDoneButton");

if (markAsDoneButton) {
    markAsDoneButton.addEventListener("click", function () {
        const workoutId = this.getAttribute("data-id");
        if (!workoutId) return console.error("⚠️ Workout ID not found!");

        const csrfTokenElement = document.querySelector('input[name="_csrf"]');
        if (!csrfTokenElement) {
            console.error("CSRF token input not found.");
            alert("An error occurred. Please try refreshing the page.");
            return;
        }
        const csrfToken = csrfTokenElement.value;

        const originalButtonText = this.textContent;
        this.disabled = true;
        this.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Logging...';

        fetch(`/user/workouts/${workoutId}/mark-done`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "X-CSRF-TOKEN": csrfToken,
            },
        })
        .then(response => {
            if (!response.ok) {
                return response.json().then(err => { throw new Error(err.message || 'Failed to mark workout as done.') });
            }
            return response.json();
        })
        .then(data => {
            console.log("Workout marked as done:", data);
            // The button will reset on page reload, so no need to restore text here if reload happens.
            closeWorkoutModal();
            window.location.reload(); 
        })
        .catch(error => {
            console.error("Error:", error);
            alert("Failed to mark workout as done: " + error.message);
            // Restore button on error
            this.disabled = false;
            this.innerHTML = originalButtonText;
        });
    });
}

const workoutForm = document.getElementById('workoutForm');
if (workoutForm) {
    const saveWorkoutButton = workoutForm.querySelector('button[type="submit"]');
    let originalSaveWorkoutButtonText = '';
    if (saveWorkoutButton) {
        originalSaveWorkoutButtonText = saveWorkoutButton.textContent;
    }

    workoutForm.addEventListener('submit', function(e) {
        e.preventDefault();
        const formData = new FormData(this);
        const csrfTokenElement = document.querySelector('input[name="_csrf"]');
        if (!csrfTokenElement) {
            console.error("CSRF token input not found for workout form.");
            alert("An error occurred. Please try refreshing the page.");
            return;
        }
        const csrfToken = csrfTokenElement.value;

        if (saveWorkoutButton) {
            saveWorkoutButton.disabled = true;
            saveWorkoutButton.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Saving...';
        }

        fetch('/user/saveworkout', {
            method: 'POST',
            body: formData,
            headers: {
                "X-CSRF-TOKEN": csrfToken,
            }
        })
        .then(response => {
            if (response.ok) {
                // Button will reset on page reload
                closeWorkoutModal();
                this.reset();
                window.location.reload();
            } else {
                response.text().then(text => {
                    console.error('Error saving workout:', text);
                    alert('Error saving workout: ' + text);
                    if (saveWorkoutButton) {
                        saveWorkoutButton.disabled = false;
                        saveWorkoutButton.innerHTML = originalSaveWorkoutButtonText;
                    }
                });
            }
        })
        .catch(error => {
            console.error('Network error saving workout:', error);
            alert('Network error saving workout. Please try again.');
            if (saveWorkoutButton) {
                saveWorkoutButton.disabled = false;
                saveWorkoutButton.innerHTML = originalSaveWorkoutButtonText;
            }
        });
    });
}

const mealForm = document.getElementById('mealForm');
if (mealForm) {
    const saveMealButton = mealForm.querySelector('button[type="submit"]');
    let originalSaveMealButtonText = '';
    if (saveMealButton) {
        originalSaveMealButtonText = saveMealButton.textContent;
    }

    mealForm.addEventListener('submit', function(e) {
        // Prevent default only if it's the initial save, not for add food item
        if (this.getAttribute('action') === '/user/meals/save') {
            e.preventDefault(); 
            const formData = new FormData(this);
            const csrfTokenElement = document.querySelector('input[name="_csrf"]');

            if (!csrfTokenElement) {
                console.error("CSRF token input not found for meal form.");
                alert("An error occurred. Please try refreshing the page.");
                return;
            }
            const csrfToken = csrfTokenElement.value;

            if (saveMealButton) {
                saveMealButton.disabled = true;
                saveMealButton.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Saving...';
            }
            
            fetch('/user/meals/save', {
                method: 'POST',
                body: formData,
                headers: {
                    "X-CSRF-TOKEN": csrfToken,
                }
            })
            .then(response => {
                if (!response.ok) {
                    return response.text().then(text => { throw new Error(text || 'Failed to save meal.') });
                }
                return response.json();
            })
            .then(todayMealDTOs => {
                console.log("Received updated meals:", todayMealDTOs);
                updateDashboardMeals(todayMealDTOs); 
                closeMealModal(); 
                this.reset(); // Reset form after successful save
                if (saveMealButton) {
                    saveMealButton.disabled = false;
                    saveMealButton.innerHTML = originalSaveMealButtonText;
                }
            })
            .catch(error => {
                console.error('Error saving meal:', error);
                alert('Error saving meal: ' + error.message); 
                if (saveMealButton) {
                    saveMealButton.disabled = false;
                    saveMealButton.innerHTML = originalSaveMealButtonText;
                }
            });
        }
        // If action is not '/user/meals/save', it's likely 'addFoodItemToMeal', let it submit normally or handle separately if AJAX.
    });
} 