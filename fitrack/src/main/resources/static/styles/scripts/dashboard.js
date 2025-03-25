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
    const placeholder = document.getElementById("workoutPlaceholder");

    // ✅ Ensure modal and button exist before using them
    if (!modal || !markAsDoneButton) {
        console.error("⚠️ Modal or Mark as Done button not found.");
        return;
    }

    // ✅ Attach click event to all workout items dynamically
    document.querySelectorAll(".workout-item").forEach(item => {
        item.addEventListener("click", function () {
            openModal(this);
        });
    });

    // ✅ Mark Workout as Done
    markAsDoneButton.addEventListener("click", function () {
        const workoutId = this.getAttribute("data-id");
        if (!workoutId) {
            console.error("⚠️ Workout ID not found!");
            return;
        }

        // ✅ Immediately close modal (before network request)
        modal.style.display = "none";

        fetch(`/user/workouts/${workoutId}/mark-done`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "X-CSRF-TOKEN": document.querySelector("input[name=_csrf]")?.value || ""
            }
        })
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! Status: ${response.status}`);
            }
            return response.text();  // ✅ Expecting plain text response
        })
        .then(() => {
            console.log("✅ Workout logged successfully!");

            // ✅ Remove workout from dashboard
            const workoutElement = document.querySelector(`.workout-item[data-id="${workoutId}"]`);
            if (workoutElement) {
                workoutElement.remove();
            }

            // ✅ Show placeholder only if no workouts are left
            if (document.querySelectorAll(".workout-item").length === 0 && placeholder) {
                placeholder.style.display = "block";
            }
        })
        .catch(error => console.error("❌ Error:", error));
    });

    // ✅ Close modal when clicking outside
    window.onclick = function(event) {
        if (event.target === modal) {
            modal.style.display = "none";
        }
    };
});

// ✅ Function to open the modal and display workout details
function openModal(workoutElement) {
    const modal = document.getElementById("workoutModal");
    if (!modal) {
        console.error("⚠️ Workout modal not found.");
        return;
    }

    const workoutId = workoutElement.getAttribute("data-id");
    const workoutName = workoutElement.getAttribute("data-name");
    const duration = workoutElement.getAttribute("data-duration");
    const calories = workoutElement.getAttribute("data-calories");

    document.getElementById("modalWorkoutName").innerText = workoutName;
    document.getElementById("modalWorkoutDuration").innerText = duration + " mins";
    document.getElementById("modalWorkoutCalories").innerText = calories + " cal";

    const markAsDoneButton = document.getElementById("markAsDoneButton");
    if (markAsDoneButton) {
        markAsDoneButton.setAttribute("data-id", workoutId);
    }

    modal.style.display = "block";
}
