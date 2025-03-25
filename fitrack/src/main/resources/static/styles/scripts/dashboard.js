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

function openModal(element) {
    document.getElementById("modalWorkoutName").innerText = element.dataset.name;
    document.getElementById("modalDuration").innerText = element.dataset.duration;
    document.getElementById("modalCalories").innerText = element.dataset.calories;
    document.getElementById("workoutModal").style.display = "block";

    // Save the workout ID for marking as done
    document.getElementById("workoutModal").dataset.id = element.dataset.id;
}

function closeModal() {
    document.getElementById("workoutModal").style.display = "none";
}

function markAsDone() {
    let workoutId = document.getElementById("workoutModal").getAttribute("data-id");
    
    let csrfToken = document.querySelector('input[name="_csrf"]').value;

    fetch(`/user/workouts/${workoutId}/mark-done`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "X-CSRF-TOKEN": csrfToken  
        }
    })
    .then(response => {
        if (!response.ok) {
            throw new Error("Failed to mark workout as done");
        }
        return response.text();
    })
    .then(data => {
        console.log(data);
        alert("Workout marked as done!");
        closeModal();
    })
    .catch(error => console.error("Error:", error));
}

