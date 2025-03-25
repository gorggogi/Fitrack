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

    if (!modal || !markAsDoneButton) {
        console.error("⚠️ Modal or Mark as Done button not found.");
        return;
    }


    document.querySelectorAll(".workout-item").forEach(item => {
        item.addEventListener("click", () => openModal(item));
    });


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
            }, 500); 

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
    const modal = document.getElementById("workoutModal");
    if (!modal) return console.error("⚠️ Workout modal not found.");

    document.getElementById("modalWorkoutName").innerText = workoutElement.getAttribute("data-name");
    document.getElementById("modalWorkoutDuration").innerText = workoutElement.getAttribute("data-duration") + " mins";
    document.getElementById("modalWorkoutCalories").innerText = workoutElement.getAttribute("data-calories") + " cal";

    document.getElementById("markAsDoneButton").setAttribute("data-id", workoutElement.getAttribute("data-id"));
    modal.style.display = "block";
}

