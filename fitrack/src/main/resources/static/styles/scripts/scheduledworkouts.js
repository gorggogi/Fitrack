let currentEditId = null;

function editWorkout(id) {
    currentEditId = id;
    const csrfToken = document.querySelector('input[name="_csrf"]').value;
    
    // Show loading state
    const editIcon = document.querySelector(`a[onclick="editWorkout(${id})"]`);
    const originalIcon = editIcon.innerHTML;
    editIcon.innerHTML = '<i class="fas fa-spinner fa-spin"></i>';
    
    // Fetch workout details
    fetch(`/user/workouts/${id}`, {
        headers: {
            'X-CSRF-TOKEN': csrfToken,
            'Accept': 'application/json'
        }
    })
    .then(response => {
        if (!response.ok) {
            if (response.status === 404) {
                throw new Error('Workout not found');
            }
            throw new Error('Failed to load workout details');
        }
        return response.text().then(text => {
            try {
                return JSON.parse(text);
            } catch (e) {
                console.error('Error parsing JSON:', text);
                throw new Error('Invalid response format');
            }
        });
    })
    .then(workout => {
        console.log('Received workout data:', workout);
        
        // Populate the form
        document.getElementById('workoutId').value = workout.id;
        document.getElementById('workoutName').value = workout.workoutName || '';
        document.getElementById('duration').value = workout.duration || '';
        document.getElementById('caloriesBurned').value = workout.caloriesBurned || '';
        
        // Reset all checkboxes
        document.querySelectorAll('input[name="repeatDays"]').forEach(checkbox => {
            checkbox.checked = false;
        });
        
        // Check the appropriate repeat days
        if (workout.repeatDays && Array.isArray(workout.repeatDays)) {
            workout.repeatDays.forEach(day => {
                const checkbox = document.querySelector(`input[name="repeatDays"][value="${day}"]`);
                if (checkbox) {
                    checkbox.checked = true;
                }
            });
        }
        
        // Update modal title and form action
        document.getElementById('modalTitle').textContent = 'Edit Workout';
        document.getElementById('workoutForm').action = `/user/workouts/${id}/update`;
        
        // Show the modal
        openWorkoutModal();
    })
    .catch(error => {
        console.error('Error:', error);
        alert(error.message || 'Error loading workout details. Please try again.');
    })
    .finally(() => {
        // Restore original icon
        editIcon.innerHTML = originalIcon;
    });
}

function deleteWorkout(id) {
    if (confirm('Are you sure you want to delete this workout?')) {
        const csrfToken = document.querySelector('input[name="_csrf"]').value;
        
        fetch(`/user/workouts/${id}/delete`, {
            method: 'POST',
            headers: {
                'X-CSRF-TOKEN': csrfToken,
                'Content-Type': 'application/json'
            }
        })
        .then(response => {
            if (response.ok) {
                window.location.reload();
            } else {
                throw new Error('Failed to delete workout');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('Error deleting workout. Please try again.');
        });
    }
}

// Override the openWorkoutModal function to reset the form when adding new workout
const originalOpenWorkoutModal = window.openWorkoutModal;
window.openWorkoutModal = function() {
    if (!currentEditId) {
        // Reset form for new workout
        document.getElementById('workoutForm').reset();
        document.getElementById('workoutId').value = '';
        document.getElementById('modalTitle').textContent = 'Add New Workout';
        document.getElementById('workoutForm').action = '/user/saveworkout';
        document.getElementById('daily').checked = true;
    }
    originalOpenWorkoutModal();
};

// Override the closeWorkoutModal function to reset the edit state
const originalCloseWorkoutModal = window.closeWorkoutModal;
window.closeWorkoutModal = function() {
    currentEditId = null;
    originalCloseWorkoutModal();
}; 