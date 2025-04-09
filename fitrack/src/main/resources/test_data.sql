-- Clear existing data
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE body_measurement;
TRUNCATE TABLE workout;
TRUNCATE TABLE workout_repeat_days;
TRUNCATE TABLE workout_log;
TRUNCATE TABLE meal;
TRUNCATE TABLE meal_food_item;
SET FOREIGN_KEY_CHECKS = 1;

-- Insert initial body measurement for new user
INSERT INTO body_measurement (id, user_id, date_time, weight, notes)
VALUES 
(1, 1, '2024-04-02 08:00:00', 70.0, 'Initial measurement - Starting fitness journey
Current daily intake (~1,500 kcal) will result in:
- Expected weight loss: ~0.7-0.8 kg per week
- Monthly projection: ~2.9 kg loss
- Safe and sustainable rate for beginners

To adjust weight goals:
1. For faster weight loss: Keep current intake
2. For maintenance: Increase to ~2,300 kcal/day
3. For weight gain: Increase to ~2,500-2,700 kcal/day

Note: Weight loss may slow down as body adapts. 
Ensure adequate protein intake (1.6-2.2g per kg) to maintain muscle mass.');

-- Insert workouts for first week (April 2-9)
INSERT INTO workout (id, user_id, date_time, workout_name, duration, burned_calories)
VALUES
-- April 2 (First workout)
(1, 1, '2024-04-02 18:00:00', 'Beginner Full Body', 30, 150),
-- April 4
(2, 1, '2024-04-04 18:00:00', 'Beginner Cardio', 20, 100),
-- April 6
(3, 1, '2024-04-06 18:00:00', 'Beginner Full Body', 30, 150),
-- April 8
(4, 1, '2024-04-08 18:00:00', 'Beginner Cardio', 20, 100);

-- Insert workout logs
INSERT INTO workout_log (id, user_id, workout_name, duration, burned_calories, completed_at)
VALUES
(1, 1, 'Beginner Full Body', 30, 150, '2024-04-02 18:00:00'),
(2, 1, 'Beginner Cardio', 20, 100, '2024-04-04 18:00:00'),
(3, 1, 'Beginner Full Body', 30, 150, '2024-04-06 18:00:00'),
(4, 1, 'Beginner Cardio', 20, 100, '2024-04-08 18:00:00');

-- Insert meals for April 9 (today)
INSERT INTO meal (id, user_id, date_time, meal_name)
VALUES
(1, 1, '2024-04-09 08:00:00', 'Breakfast'),
(2, 1, '2024-04-09 12:00:00', 'Lunch'),
(3, 1, '2024-04-09 18:00:00', 'Dinner'),
(4, 1, '2024-04-09 21:00:00', 'Snack');

-- Insert meal food items for April 9 (today)
INSERT INTO meal_food_item (id, meal_id, food_item, calories, quantity, unit)
VALUES
-- Breakfast
(1, 1, 'Oatmeal', 150, 1, 'cup'),
(2, 1, 'Banana', 105, 1, 'medium'),
(3, 1, 'Almond Milk', 30, 0.5, 'cup'),
-- Lunch
(4, 2, 'Chicken Breast', 165, 100, 'g'),
(5, 2, 'Brown Rice', 220, 1, 'cup'),
(6, 2, 'Mixed Vegetables', 50, 1, 'cup'),
-- Dinner
(7, 3, 'Salmon', 280, 150, 'g'),
(8, 3, 'Quinoa', 220, 1, 'cup'),
(9, 3, 'Steamed Broccoli', 55, 1, 'cup'),
-- Snack
(10, 4, 'Greek Yogurt', 100, 1, 'cup'),
(11, 4, 'Mixed Berries', 50, 0.5, 'cup');