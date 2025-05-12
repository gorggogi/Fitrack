const csrfToken = document.querySelector('input[name="_csrf"]').value;

function openMealModal() {
    document.getElementById('mealModal').style.display = 'flex';
    document.getElementById('mealForm').reset();
    document.getElementById('mealId').value = '';
    document.getElementById('modalTitle').textContent = 'Log New Meal';
    document.getElementById('mealForm').action = '/user/savemeal';
    
    const container = document.getElementById('foodItemsContainer');
    container.innerHTML = '';
    addFoodItem();
}

function closeMealModal() {
    document.getElementById('mealModal').style.display = 'none';
}

function addFoodItem() {
    const container = document.getElementById('foodItemsContainer');
    const index = container.children.length;
    const foodItemDiv = document.createElement('div');
    foodItemDiv.className = 'food-item-input';
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
            <div class="form-group">
                <label>Unit</label>
                <select name="foodItems[${index}].unit" class="form-control">
                    <option value="">None</option>
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
                </select>
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

function removeFoodItem(button) {
    const foodItemRow = button.closest('.food-item-input');
    const container = foodItemRow.parentElement;
    foodItemRow.remove();
    const items = container.children;
    for (let i = 0; i < items.length; i++) {
        const inputs = items[i].querySelectorAll('input, select');
        inputs.forEach(input => {
            const name = input.name;
            if (name) {
                input.name = name.replace(/foodItems\[\d+\]/, `foodItems[${i}]`);
            }
        });
    }
}

function editMeal(id) {
    const editIconContainer = document.querySelector(`a[onclick="editMeal(${id})"]`);
    const originalIconHTML = editIconContainer.innerHTML;
    editIconContainer.innerHTML = '<i class="fas fa-spinner fa-spin"></i>';

    fetch(`/user/meals/${id}`, {
        headers: {
            'X-CSRF-TOKEN': csrfToken,
            'Accept': 'application/json'
        }
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Failed to load meal details for editing.');
        }
        return response.json();
    })
    .then(meal => {
        document.getElementById('modalTitle').textContent = 'Edit Meal';
        document.getElementById('mealForm').action = `/user/meals/${id}/update`;
        document.getElementById('mealId').value = meal.id;
        document.getElementById('mealName').value = meal.mealName;
        const localDateTime = meal.dateTime ? meal.dateTime.substring(0, 16) : '';
        document.getElementById('dateTime').value = localDateTime;

        const container = document.getElementById('foodItemsContainer');
        container.innerHTML = '';

        if (meal.foodItems && meal.foodItems.length > 0) {
            meal.foodItems.forEach((foodItem, index) => {
                const foodItemDiv = document.createElement('div');
                foodItemDiv.className = 'food-item-input';
                foodItemDiv.innerHTML = `
                    <div class="form-row">
                        <div class="form-group">
                            <label>Food Item</label>
                            <input type="text" name="foodItems[${index}].foodItem" value="${foodItem.foodItem || ''}" placeholder="Enter food" class="form-control" required>
                        </div>
                        <div class="form-group">
                            <label>Quantity</label>
                            <input type="number" name="foodItems[${index}].quantity" value="${foodItem.quantity || ''}" placeholder="Quantity" class="form-control" step="any">
                        </div>
                        <div class="form-group">
                            <label>Unit</label>
                            <select name="foodItems[${index}].unit" class="form-control">
                                <option value="">None</option>
                                <option value="g" ${foodItem.unit === 'g' ? 'selected' : ''}>Grams</option>
                                <option value="kg" ${foodItem.unit === 'kg' ? 'selected' : ''}>Kilograms</option>
                                <option value="ml" ${foodItem.unit === 'ml' ? 'selected' : ''}>Milliliters</option>
                                <option value="l" ${foodItem.unit === 'l' ? 'selected' : ''}>Liters</option>
                                <option value="cup" ${foodItem.unit === 'cup' ? 'selected' : ''}>Cups</option>
                                <option value="tbsp" ${foodItem.unit === 'tbsp' ? 'selected' : ''}>Tablespoons</option>
                                <option value="tsp" ${foodItem.unit === 'tsp' ? 'selected' : ''}>Teaspoons</option>
                                <option value="oz" ${foodItem.unit === 'oz' ? 'selected' : ''}>Ounce (oz)</option>
                                <option value="serving" ${foodItem.unit === 'serving' ? 'selected' : ''}>Serving</option>
                                <option value="piece" ${foodItem.unit === 'piece' ? 'selected' : ''}>Piece</option>
                            </select>
                        </div>
                        <div class="form-group">
                            <label>Calories</label>
                            <input type="number" name="foodItems[${index}].calories" value="${foodItem.calories || 0}" placeholder="Calories" class="form-control" required>
                        </div>
                        <button type="button" class="remove-item" onclick="removeFoodItem(this)" style="align-self: flex-end; margin-bottom: 1rem;">×</button>
                    </div>
                `;
                container.appendChild(foodItemDiv);
            });
        } else {
             addFoodItem();
        }
        document.getElementById('mealModal').style.display = 'flex';
    })
    .catch(error => {
        console.error('Error loading meal for edit:', error);
        alert('Error loading meal details. Please try again.');
    })
    .finally(() => {
        editIconContainer.innerHTML = originalIconHTML;
    });
}

function deleteMeal(id) {
    if (confirm('Are you sure you want to delete this meal?')) {
        const deleteIconContainer = document.querySelector(`a[onclick="deleteMeal(${id})"]`);
        const originalIconHTML = deleteIconContainer.innerHTML;
        deleteIconContainer.innerHTML = '<i class="fas fa-spinner fa-spin"></i>';
        
        fetch(`/user/meals/${id}/delete`, {
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
                 response.text().then(text => { throw new Error(text || 'Failed to delete meal'); });
            }
        })
        .catch(error => {
            console.error('Error deleting meal:', error);
            alert('Error deleting meal. Please try again.');
        })
        .finally(() => {
             deleteIconContainer.innerHTML = originalIconHTML;
        });
    }
}

function estimateAllCaloriesInModal() {
    const foodItemsInputs = document.querySelectorAll('#foodItemsContainer .food-item-input');
    const foodData = [];
    let allFieldsValid = true;

    foodItemsInputs.forEach((itemRow, index) => {
        const foodNameInput = itemRow.querySelector(`input[name="foodItems[${index}].foodItem"]`);
        const quantityInput = itemRow.querySelector(`input[name="foodItems[${index}].quantity"]`);
        const unitInput = itemRow.querySelector(`select[name="foodItems[${index}].unit"]`);

        if (!foodNameInput.value || !quantityInput.value || !unitInput.value) {
            if (!foodNameInput.value && !quantityInput.value && !unitInput.value && foodItemsInputs.length > 1 && index === foodItemsInputs.length -1) {
            } else {
                 allFieldsValid = false;
            }
        }
        if (foodNameInput.value && quantityInput.value && unitInput.value) {
            foodData.push({
                foodName: foodNameInput.value,
                quantity: parseFloat(quantityInput.value),
                unit: unitInput.value,
                originalIndex: index
            });
        }
    });

    if (!allFieldsValid) {
         alert("Please ensure all food items have a name, quantity, and unit before estimating calories.");
         return;
    }
    if (foodData.length === 0) {
        alert("No food items to estimate calories for. Please add food items with name, quantity, and unit.");
        return;
    }

    const estimateButton = document.querySelector('.btn-warning[onclick="estimateAllCaloriesInModal()"]');
    const originalButtonText = estimateButton.textContent;
    estimateButton.disabled = true;
    estimateButton.textContent = 'Estimating...';

    fetch('/meals/estimate-calories', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'X-CSRF-TOKEN': csrfToken
        },
        body: JSON.stringify(foodData)
    })
    .then(response => {
        if (!response.ok) {
            return response.text().then(text => { throw new Error(text || 'Failed to estimate calories'); });
        }
        return response.json();
    })
    .then(estimatedResults => {
        estimatedResults.forEach((calories, resultIndex) => {
            const foodItemData = foodData[resultIndex];
            if (foodItemData) {
                 const caloriesInput = document.querySelector(`#foodItemsContainer .food-item-input:nth-child(${foodItemData.originalIndex + 1}) input[name="foodItems[${foodItemData.originalIndex}].calories"]`);
                if (caloriesInput) {
                    caloriesInput.value = Math.round(calories);
                }
            }
        });
    })
    .catch(error => {
        console.error('Error estimating calories:', error);
        alert('Error estimating calories: ' + error.message);
    })
    .finally(() => {
        estimateButton.disabled = false;
        estimateButton.textContent = originalButtonText;
    });
}

document.addEventListener('DOMContentLoaded', () => {
    const addMealButton = document.querySelector('.page-header .add-button');
    if (addMealButton) {
        addMealButton.onclick = function() {
            openMealModal();
        };
    }
}); 