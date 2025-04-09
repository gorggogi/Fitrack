-- Clear existing data
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE body_measurement;
TRUNCATE TABLE workout;
TRUNCATE TABLE workout_repeat_days;
TRUNCATE TABLE workout_log;
TRUNCATE TABLE meal;
TRUNCATE TABLE meal_food_item;
SET FOREIGN_KEY_CHECKS = 1;

-- Insert body measurements for user 1 (weekly measurements)
INSERT INTO body_measurement (id, user_id, date_time, weight, notes)
VALUES 
(1, 1, '2024-02-26 08:00:00', 70.5, 'Starting weight'),
(2, 1, '2024-03-04 08:00:00', 70.3, 'Week 1 progress'),
(3, 1, '2024-03-11 08:00:00', 70.1, 'Week 2 progress'),
(4, 1, '2024-03-18 08:00:00', 69.9, 'Week 3 progress'),
(5, 1, '2024-03-25 08:00:00', 69.8, 'Week 4 progress'),
(6, 1, '2024-04-01 08:00:00', 69.6, 'Week 5 progress'),
(7, 1, '2024-04-08 08:00:00', 69.4, 'Week 6 progress');

-- Insert workouts for user 1 (multiple workouts per day)
INSERT INTO workout (id, user_id, date_time, workout_name, duration, burned_calories)
VALUES
-- Day 1 (3 workouts)
(1, 1, '2024-04-08 06:00:00', 'Morning Run', 45, 400),
(2, 1, '2024-04-08 12:00:00', 'Upper Body Workout', 60, 350),
(3, 1, '2024-04-08 18:00:00', 'HIIT Session', 30, 300),
-- Day 2 (2 workouts)
(4, 1, '2024-04-07 07:00:00', 'Yoga Flow', 45, 200),
(5, 1, '2024-04-07 17:00:00', 'Lower Body Focus', 50, 320),
-- Day 3 (3 workouts)
(6, 1, '2024-04-06 06:00:00', 'Swimming', 40, 400),
(7, 1, '2024-04-06 12:00:00', 'Full Body Workout', 60, 450),
(8, 1, '2024-04-06 19:00:00', 'Core Workout', 30, 250),
-- Day 4 (2 workouts)
(9, 1, '2024-04-05 07:00:00', 'Morning Run', 45, 400),
(10, 1, '2024-04-05 18:00:00', 'Upper Body Workout', 60, 350),
-- Day 5 (3 workouts)
(11, 1, '2024-04-04 06:00:00', 'HIIT Session', 30, 300),
(12, 1, '2024-04-04 12:00:00', 'Yoga Flow', 45, 200),
(13, 1, '2024-04-04 19:00:00', 'Lower Body Focus', 50, 320),
-- Day 6 (2 workouts)
(14, 1, '2024-04-03 08:00:00', 'Swimming', 40, 400),
(15, 1, '2024-04-03 17:00:00', 'Full Body Workout', 60, 450),
-- Day 7 (3 workouts)
(16, 1, '2024-04-02 06:00:00', 'Core Workout', 30, 250),
(17, 1, '2024-04-02 12:00:00', 'Morning Run', 45, 400),
(18, 1, '2024-04-02 18:00:00', 'Upper Body Workout', 60, 350);

-- Insert workout repeat days for user 1
INSERT INTO workout_repeat_days (workout_id, repeat_day)
VALUES
(1, 'Monday'),
(1, 'Wednesday'),
(1, 'Friday'),
(2, 'Tuesday'),
(2, 'Thursday'),
(3, 'Daily'),
(4, 'Monday'),
(4, 'Wednesday'),
(4, 'Friday'),
(5, 'Tuesday'),
(5, 'Thursday'),
(6, 'Wednesday'),
(6, 'Saturday'),
(7, 'Monday'),
(7, 'Thursday');

-- Insert workout logs for user 1 (matching the workouts)
INSERT INTO workout_log (id, user_id, workout_name, duration, burned_calories, completed_at)
VALUES
-- Day 1 (3 workouts)
(1, 1, 'Morning Run', 45, 400, '2024-04-08 06:00:00'),
(2, 1, 'Upper Body Workout', 60, 350, '2024-04-08 12:00:00'),
(3, 1, 'HIIT Session', 30, 300, '2024-04-08 18:00:00'),
-- Day 2 (2 workouts)
(4, 1, 'Yoga Flow', 45, 200, '2024-04-07 07:00:00'),
(5, 1, 'Lower Body Focus', 50, 320, '2024-04-07 17:00:00'),
-- Day 3 (3 workouts)
(6, 1, 'Swimming', 40, 400, '2024-04-06 06:00:00'),
(7, 1, 'Full Body Workout', 60, 450, '2024-04-06 12:00:00'),
(8, 1, 'Core Workout', 30, 250, '2024-04-06 19:00:00'),
-- Day 4 (2 workouts)
(9, 1, 'Morning Run', 45, 400, '2024-04-05 07:00:00'),
(10, 1, 'Upper Body Workout', 60, 350, '2024-04-05 18:00:00'),
-- Day 5 (3 workouts)
(11, 1, 'HIIT Session', 30, 300, '2024-04-04 06:00:00'),
(12, 1, 'Yoga Flow', 45, 200, '2024-04-04 12:00:00'),
(13, 1, 'Lower Body Focus', 50, 320, '2024-04-04 19:00:00'),
-- Day 6 (2 workouts)
(14, 1, 'Swimming', 40, 400, '2024-04-03 08:00:00'),
(15, 1, 'Full Body Workout', 60, 450, '2024-04-03 17:00:00'),
-- Day 7 (3 workouts)
(16, 1, 'Core Workout', 30, 250, '2024-04-02 06:00:00'),
(17, 1, 'Morning Run', 45, 400, '2024-04-02 12:00:00'),
(18, 1, 'Upper Body Workout', 60, 350, '2024-04-02 18:00:00');

-- Insert meals for user 1 (3 meals per day for a week)
INSERT INTO meal (id, user_id, date_time, meal_name)
VALUES
-- Day 1
(1, 1, '2024-04-08 08:00:00', 'Breakfast'),
(2, 1, '2024-04-08 12:00:00', 'Lunch'),
(3, 1, '2024-04-08 18:00:00', 'Dinner'),
-- Day 2
(4, 1, '2024-04-07 08:00:00', 'Breakfast'),
(5, 1, '2024-04-07 12:00:00', 'Lunch'),
(6, 1, '2024-04-07 18:00:00', 'Dinner'),
-- Day 3
(7, 1, '2024-04-06 08:00:00', 'Breakfast'),
(8, 1, '2024-04-06 12:00:00', 'Lunch'),
(9, 1, '2024-04-06 18:00:00', 'Dinner'),
-- Day 4
(10, 1, '2024-04-05 08:00:00', 'Breakfast'),
(11, 1, '2024-04-05 12:00:00', 'Lunch'),
(12, 1, '2024-04-05 18:00:00', 'Dinner'),
-- Day 5
(13, 1, '2024-04-04 08:00:00', 'Breakfast'),
(14, 1, '2024-04-04 12:00:00', 'Lunch'),
(15, 1, '2024-04-04 18:00:00', 'Dinner'),
-- Day 6
(16, 1, '2024-04-03 08:00:00', 'Breakfast'),
(17, 1, '2024-04-03 12:00:00', 'Lunch'),
(18, 1, '2024-04-03 18:00:00', 'Dinner'),
-- Day 7
(19, 1, '2024-04-02 08:00:00', 'Breakfast'),
(20, 1, '2024-04-02 12:00:00', 'Lunch'),
(21, 1, '2024-04-02 18:00:00', 'Dinner');

-- Insert meal food items for user 1 (varied meals throughout the week)
INSERT INTO meal_food_item (id, meal_id, food_item, calories, quantity, unit)
VALUES
-- Day 1
(1, 1, 'Oatmeal', 150, 1, 'cup'),
(2, 1, 'Banana', 105, 1, 'medium'),
(3, 1, 'Honey', 60, 1, 'tbsp'),
(4, 2, 'Chicken Breast', 165, 100, 'g'),
(5, 2, 'Brown Rice', 220, 1, 'cup'),
(6, 2, 'Mixed Vegetables', 50, 1, 'cup'),
(7, 3, 'Salmon', 280, 150, 'g'),
(8, 3, 'Quinoa', 220, 1, 'cup'),
-- Day 2
(9, 4, 'Greek Yogurt', 100, 1, 'cup'),
(10, 4, 'Mixed Berries', 50, 0.5, 'cup'),
(11, 5, 'Turkey Sandwich', 350, 1, 'serving'),
(12, 5, 'Apple', 95, 1, 'medium'),
(13, 6, 'Grilled Chicken', 200, 150, 'g'),
(14, 6, 'Sweet Potato', 180, 1, 'medium'),
-- Day 3
(15, 7, 'Protein Shake', 200, 1, 'serving'),
(16, 7, 'Toast', 80, 2, 'slices'),
(17, 8, 'Tuna Salad', 300, 1, 'serving'),
(18, 8, 'Whole Wheat Bread', 160, 2, 'slices'),
(19, 9, 'Beef Stir Fry', 400, 1, 'serving'),
-- Day 4
(20, 10, 'Scrambled Eggs', 140, 2, 'large'),
(21, 10, 'Whole Wheat Toast', 80, 2, 'slices'),
(22, 11, 'Quinoa Bowl', 350, 1, 'serving'),
(23, 12, 'Grilled Fish', 250, 150, 'g'),
(24, 12, 'Steamed Vegetables', 100, 1, 'cup'),
-- Day 5
(25, 13, 'Smoothie Bowl', 250, 1, 'serving'),
(26, 14, 'Chicken Wrap', 350, 1, 'serving'),
(27, 15, 'Pasta with Meat Sauce', 450, 1, 'serving'),
-- Day 6
(28, 16, 'Pancakes', 200, 2, 'medium'),
(29, 16, 'Maple Syrup', 50, 1, 'tbsp'),
(30, 17, 'Burger', 500, 1, 'serving'),
(31, 18, 'Grilled Salmon', 280, 150, 'g'),
-- Day 7
(32, 19, 'Omelette', 300, 1, 'serving'),
(33, 20, 'Chicken Caesar Salad', 400, 1, 'serving'),
(34, 21, 'Steak', 350, 150, 'g'),
(35, 21, 'Mashed Potatoes', 200, 1, 'cup');