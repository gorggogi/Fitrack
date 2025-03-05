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
                    <input type="text" name="foodItems[${itemCount}].foodItem" placeholder="Enter food item" class="form-control" required>
                </div>
                <div class="form-group">
                    <label>Calories</label>
                    <input type="number" name="foodItems[${itemCount}].calories" placeholder="Calories" class="form-control" required>
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


