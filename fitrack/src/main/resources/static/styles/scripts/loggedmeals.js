const csrfToken = document.querySelector('input[name="_csrf"]').value;

function openMealModal() {
    document.getElementById('mealModal').style.display = 'flex';
    document.getElementById('mealForm').reset();
    document.getElementById('mealId').value = '';
    document.getElementById('modalTitle').textContent = 'Log New Meal';
    document.getElementById('mealForm').action = contextPath + 'user/savemeal';
    
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

function removeFoodItem(button) {
    const foodItemRow = button.closest('.food-item-row');
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
    console.log('Context Path in editMeal:', contextPath);
    console.log('Fetching meal with ID:', id, 'CSRF Token:', csrfToken);
    const editIconContainer = document.querySelector(`a[onclick="editMeal(${id})"]`);
    const originalIconHTML = editIconContainer.innerHTML;
    editIconContainer.innerHTML = '<i class="fas fa-spinner fa-spin"></i>';

    fetch(contextPath + `user/meals/${id}`, {
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
        document.getElementById('mealForm').action = contextPath + `user/meals/${id}/update`;
        document.getElementById('mealId').value = meal.id;
        document.getElementById('mealName').value = meal.mealName;
        const localDateTime = meal.dateTime ? meal.dateTime.substring(0, 16) : '';
        document.getElementById('dateTime').value = localDateTime;

        const container = document.getElementById('foodItemsContainer');
        container.innerHTML = '';

        if (meal.foodItems && meal.foodItems.length > 0) {
            meal.foodItems.forEach((foodItemData, index) => {
                addFoodItem();
                
                const currentFoodItemRow = container.children[index];
                
                currentFoodItemRow.querySelector(`input[name="foodItems[${index}].foodItem"]`).value = foodItemData.foodItem || '';
                currentFoodItemRow.querySelector(`input[name="foodItems[${index}].quantity"]`).value = foodItemData.quantity || '';
                currentFoodItemRow.querySelector(`input[name="foodItems[${index}].calories"]`).value = foodItemData.calories || 0;

                const unitSelect = currentFoodItemRow.querySelector(`select[name="foodItems[${index}].unit"]`);
                const otherUnitInput = currentFoodItemRow.querySelector(`input[name="foodItems[${index}].otherUnit"]`);

                if (unitSelect && otherUnitInput) {
                    const predefinedUnits = Array.from(unitSelect.options)
                                               .map(opt => opt.value)
                                               .filter(val => val !== 'other' && val !== '');

                    if (foodItemData.unit && predefinedUnits.includes(foodItemData.unit)) {
                        unitSelect.value = foodItemData.unit;
                    } else if (foodItemData.unit) {
                        unitSelect.value = 'other';
                        otherUnitInput.value = foodItemData.unit;
                    } else {
                        unitSelect.value = 'other';
                        otherUnitInput.value = '';
                    }
                    handleUnitChange(unitSelect, index);
                }
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
        
        fetch(contextPath + `user/meals/${id}/delete`, {
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
    const foodItemsInputs = document.querySelectorAll('#foodItemsContainer .food-item-row');
    const foodData = [];
    let allFieldsValid = true;

    console.log('Number of food item rows found by selector:', foodItemsInputs.length);
    // Log the actual elements found to be sure
    console.log('Food item row elements:', foodItemsInputs);

    foodItemsInputs.forEach((itemRow, index) => {
        console.log(`--- Estimating Calories: Row ${index} ---`);
        const foodNameInput = itemRow.querySelector(`input[name="foodItems[${index}].foodItem"]`);
        const quantityInput = itemRow.querySelector(`input[name="foodItems[${index}].quantity"]`);
        const unitSelect = itemRow.querySelector(`select[name="foodItems[${index}].unit"]`);
        const otherUnitTextInput = itemRow.querySelector(`input.other-unit-input`);

        console.log('Food Name Input Element:', foodNameInput, 'Value:', foodNameInput ? foodNameInput.value : 'N/A');
        console.log('Quantity Input Element:', quantityInput, 'Value:', quantityInput ? quantityInput.value : 'N/A');
        console.log('Unit Select Element:', unitSelect, 'Value:', unitSelect ? unitSelect.value : 'N/A');
        console.log('Other Unit Text Input Element:', otherUnitTextInput);
        
        let rawOtherUnitTextValue = 'N/A';
        if (otherUnitTextInput && typeof otherUnitTextInput.value === 'string') {
            rawOtherUnitTextValue = otherUnitTextInput.value;
        }
        console.log('Raw Other Unit Text Input Value:', rawOtherUnitTextValue);

        let unitValue = unitSelect ? unitSelect.value : '';
        if (unitValue === 'other') {
            if (typeof rawOtherUnitTextValue === 'string') {
                unitValue = rawOtherUnitTextValue.trim();
            } else {
                unitValue = ''; // Fallback if rawOtherUnitTextValue isn't a string
            }
        }
        console.log('Derived unitValue for estimation:', unitValue);

        if (!foodNameInput || !foodNameInput.value || !quantityInput || !quantityInput.value || !unitValue) { 
            if (!(index === foodItemsInputs.length - 1 && foodItemsInputs.length > 1 && (!foodNameInput || !foodNameInput.value) && (!quantityInput || !quantityInput.value) && !unitValue)) {
                 allFieldsValid = false;
                 console.log(`Row ${index} marked allFieldsValid = false. Field values: foodName='${foodNameInput ? foodNameInput.value : ''}', quantity='${quantityInput ? quantityInput.value : ''}', unit='${unitValue}'`);
            }
        }

        const canPushToFoodData = foodNameInput && foodNameInput.value && quantityInput && quantityInput.value && unitValue;
        console.log(`Row ${index} - Condition to push to foodData (foodName && quantity && unitValue):`, canPushToFoodData);

        if (canPushToFoodData) {
            foodData.push({
                foodName: foodNameInput.value,
                quantity: parseFloat(quantityInput.value),
                unit: unitValue, 
                originalIndex: index
            });
            console.log(`Row ${index} - Pushed item to foodData. Current foodData length:`, foodData.length);
        } else {
            console.log(`Row ${index} - Did NOT push item to foodData.`);
        }
    });

    console.log('Final allFieldsValid status:', allFieldsValid);
    console.log('Final foodData content:', foodData);
    console.log('Final foodData length:', foodData.length);

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

    fetch(contextPath + 'meals/estimate-calories', {
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
                 const caloriesInput = document.querySelector(`#foodItemsContainer .food-item-row:nth-child(${foodItemData.originalIndex + 1}) input[name="foodItems[${foodItemData.originalIndex}].calories"]`);
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