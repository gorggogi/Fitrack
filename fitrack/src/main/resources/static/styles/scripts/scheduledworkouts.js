let currentEditId = null;

function editWorkout(id) {
    currentEditId = id;
    const csrfToken = document.querySelector('input[name="_csrf"]').value;
    const editIconContainer = document.querySelector(`a[onclick="editWorkout(${id})"]`);
    let originalIconHTML = '';
    if (editIconContainer) {
        originalIconHTML = editIconContainer.innerHTML;
        editIconContainer.innerHTML = '<i class="fas fa-spinner fa-spin"></i>';
    }

    fetch(contextPath + `user/workouts/${id}`, {
        headers: { 'X-CSRF-TOKEN': csrfToken, 'Accept': 'application/json' }
    })
    .then(response => {
        if (!response.ok) {
            if (response.status === 404) throw new Error('Workout not found');
            throw new Error('Failed to load workout details');
        }
        return response.text().then(text => {
            try { return JSON.parse(text); }
            catch (e) { console.error('Error parsing JSON:', text); throw new Error('Invalid response format'); }
        });
    })
    .then(workout => {
        document.getElementById('workoutId').value = workout.id;
        document.getElementById('workoutName').value = workout.workoutName || '';
        document.getElementById('duration').value = workout.duration || '';
        document.getElementById('caloriesBurned').value = workout.caloriesBurned || '';
        document.getElementById('workoutType').value = workout.workoutType || '';
        
        document.querySelectorAll('input[name="repeatDays"]').forEach(checkbox => checkbox.checked = false);
        if (workout.repeatDays && Array.isArray(workout.repeatDays)) {
            workout.repeatDays.forEach(day => {
                const checkbox = document.querySelector(`input[name="repeatDays"][value="${day}"]`);
                if (checkbox) checkbox.checked = true;
            });
        }
        
        const form = document.getElementById('workoutForm');
        if (form) form.action = contextPath + `user/workouts/${id}/update`;
        
        const title = document.getElementById('modalTitle');
        if (title) title.textContent = 'Edit Workout';
        
        if (typeof openModal === 'function') {
            openModal(); // Calls dashboard.js's openModal
            // Ensure this page's form is visible and title is re-asserted
            if (form) form.style.display = "block";
            if (title) title.textContent = 'Edit Workout'; 
        } else {
            console.error("Global openModal (from dashboard.js) not found!");
            if (form) form.style.display = "block";
            const modal = document.getElementById("workoutModal");
            if(modal) modal.style.display = "flex";
        }
    })
    .catch(error => {
        console.error('Error in editWorkout:', error);
        alert(error.message || 'Error loading workout details. Please try again.');
    })
    .finally(() => {
        if (editIconContainer && originalIconHTML) {
            editIconContainer.innerHTML = originalIconHTML;
        }
    });
}

function deleteWorkout(id) {
    if (confirm('Are you sure you want to delete this workout?')) {
        const csrfToken = document.querySelector('input[name="_csrf"]').value;
        fetch(contextPath + `user/workouts/${id}/delete`, {
            method: 'POST',
            headers: { 'X-CSRF-TOKEN': csrfToken, 'Content-Type': 'application/json' }
        })
        .then(response => {
            if (response.ok) {
                window.location.reload();
            } else {
                response.text().then(text => { throw new Error(text || 'Failed to delete workout'); });
            }
        })
        .catch(error => {
            console.error('Error deleting workout:', error);
            alert('Error deleting workout: ' + (error.message || 'Please try again.'));
        });
    }
}

window.openAddScheduledWorkoutModal = function() {
    currentEditId = null;
    const form = document.getElementById('workoutForm');
    if (form) {
        form.reset();
        form.action = contextPath + 'user/saveworkout';
    }
    const workoutIdInput = document.getElementById('workoutId');
    if (workoutIdInput) workoutIdInput.value = '';
    const modalTitle = document.getElementById('modalTitle');
    if (modalTitle) modalTitle.textContent = 'Add New Workout';
    
    document.querySelectorAll('input[name="repeatDays"]').forEach(checkbox => checkbox.checked = false);
    const dailyCheckbox = document.getElementById('daily');
    if (dailyCheckbox) dailyCheckbox.checked = true;

    if (typeof openModal === 'function') {
        openModal(); // Calls dashboard.js's openModal
        // Ensure this page's form is visible
        if (form) form.style.display = "block";
    } else {
        console.error("Global openModal (from dashboard.js) not found!");
        if (form) form.style.display = "block";
        const modal = document.getElementById("workoutModal");
        if(modal) modal.style.display = "flex";
    }
};

window.closeScheduledWorkoutModal = function() {
    currentEditId = null;
    if (typeof closeWorkoutModal === 'function') {
        closeWorkoutModal(); 
    } else {
        console.error("Global closeWorkoutModal (from dashboard.js) not found!");
        const modal = document.getElementById("workoutModal");
        if(modal) modal.style.display = "none";
    }
};

// IIFE previously here is now removed.
// estimateBurnedCalories function is intentionally omitted. It's used from dashboard.js. 