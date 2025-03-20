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

window.onload = function() {
    const now = new Date();
    const year = now.getFullYear();
    const month = String(now.getMonth() + 1).padStart(2, '0'); 
    const day = String(now.getDate()).padStart(2, '0');
    const hours = String(now.getHours()).padStart(2, '0');
    const minutes = String(now.getMinutes()).padStart(2, '0');
    
    const formattedDateTime = `${year}-${month}-${day}T${hours}:${minutes}`;
    document.querySelector('input[type="datetime-local"]').value = formattedDateTime;
};

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
                    <option value=" ">N/A</option>
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

    document.querySelectorAll("#food-items-container .food-item").forEach(item => {
        let foodName = item.querySelector('input[name$=".foodItem"]').value;
        let quantity = item.querySelector('input[name$=".quantity"]').value;
        let unit = item.querySelector('select[name$=".unit"]').value;

        if (foodName && quantity && unit) {
            foodItems.push({ foodName, quantity, unit });
        }
    });

    if (foodItems.length === 0) {
        alert("Please enter at least one food item.");
        return;
    }

    console.log("Sending calorie estimation request:", JSON.stringify(foodItems));

    fetch("http://localhost:8080/meals/estimate-calories", { 
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(foodItems)
    })
    .then(response => response.json())
    .then(data => {
        console.log("Received calorie estimation:", data);

        document.querySelectorAll("#food-items-container .food-item").forEach((item, index) => {
            let calorieField = item.querySelector('input[name$=".calories"]');
            if (calorieField && data[index] !== undefined) {
                calorieField.value = data[index];
            }
        });
    })
    .catch(error => {
        console.error("Error estimating calories:", error);
        alert("Failed to estimate calories. Please try again.");
    });
}


