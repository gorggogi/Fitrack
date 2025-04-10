-- Clear existing data
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE body_measurement;
TRUNCATE TABLE workout;
TRUNCATE TABLE workout_repeat_days;
TRUNCATE TABLE workout_log;
TRUNCATE TABLE meal;
TRUNCATE TABLE meal_food_item;
SET FOREIGN_KEY_CHECKS = 1;

-- Insert body measurements for user 1 (weekly measurements showing weight gain)
INSERT INTO body_measurement (id, user_id, date_time, weight, notes)
VALUES 
(1, 1, '2025-03-10 08:00:00', 65.0, 'Starting weight - Beginning bulking phase'),
(2, 1, '2025-03-17 08:00:00', 65.3, 'Week 1 progress - Good protein intake'),
(3, 1, '2025-03-24 08:00:00', 65.7, 'Week 2 progress - Increased appetite'),
(4, 1, '2025-03-31 08:00:00', 66.0, 'Week 3 progress - Strength improving'),
(5, 1, '2025-04-07 08:00:00', 66.4, 'Week 4 progress - Consistent gains');

-- Insert workouts for user 1 (varied frequency and intensity over 30 days)
INSERT INTO workout (id, user_id, date_time, workout_name, duration, burned_calories)
VALUES
-- Week 1 (March 10-16)
(1, 1, '2025-03-10 07:00:00', 'Morning Cardio', 30, 200),
(2, 1, '2025-03-10 10:00:00', 'Upper Body Workout', 60, 350),
(3, 1, '2025-03-10 18:00:00', 'Yoga', 30, 150),
(4, 1, '2025-03-11 10:00:00', 'Lower Body Workout', 60, 400),
(5, 1, '2025-03-11 18:00:00', 'Core Workout', 30, 200),
(6, 1, '2025-03-12 10:00:00', 'HIIT', 45, 400),
(7, 1, '2025-03-13 07:00:00', 'Morning Run', 30, 250),
(9, 1, '2025-03-14 10:00:00', 'Upper Body Workout', 60, 350),
(10, 1, '2025-03-15 10:00:00', 'Lower Body Workout', 60, 400),
(11, 1, '2025-03-15 18:00:00', 'Yoga', 30, 150),
(12, 1, '2025-03-16 10:00:00', 'Rest Day', 0, 0),

-- Week 2 (March 17-23)
(13, 1, '2025-03-17 07:00:00', 'Morning Cardio', 30, 200),
(14, 1, '2025-03-17 10:00:00', 'Lower Body Workout', 60, 400),
(15, 1, '2025-03-17 18:00:00', 'Core Workout', 30, 200),
(16, 1, '2025-03-18 10:00:00', 'HIIT', 45, 400),
(17, 1, '2025-03-19 07:00:00', 'Morning Run', 30, 250),
(18, 1, '2025-03-19 10:00:00', 'Upper Body Workout', 60, 350),
(19, 1, '2025-03-20 10:00:00', 'Cardio', 45, 300),
(20, 1, '2025-03-21 07:00:00', 'Morning Cardio', 30, 200),
(21, 1, '2025-03-21 10:00:00', 'Full Body Workout', 60, 450),
(22, 1, '2025-03-22 10:00:00', 'Yoga', 30, 150),
(23, 1, '2025-03-23 10:00:00', 'Rest Day', 0, 0),

-- Week 3 (March 24-30)
(24, 1, '2025-03-24 10:00:00', 'Upper Body Workout', 60, 350),
(25, 1, '2025-03-24 18:00:00', 'Yoga', 30, 150),
(26, 1, '2025-03-25 10:00:00', 'Lower Body Workout', 60, 400),
(27, 1, '2025-03-25 18:00:00', 'Core Workout', 30, 200),
(28, 1, '2025-03-26 10:00:00', 'Cardio', 45, 300),
(29, 1, '2025-03-27 07:00:00', 'Morning Run', 30, 250),
(30, 1, '2025-03-27 10:00:00', 'Full Body Workout', 60, 450),
(31, 1, '2025-03-28 10:00:00', 'Yoga', 30, 150),
(32, 1, '2025-03-29 10:00:00', 'Upper Body Workout', 60, 350),
(33, 1, '2025-03-30 10:00:00', 'Rest Day', 0, 0),

-- Week 4 (March 31-April 6)
(34, 1, '2025-03-31 07:00:00', 'Morning Cardio', 30, 200),
(35, 1, '2025-03-31 10:00:00', 'Lower Body Workout', 60, 400),
(36, 1, '2025-03-31 18:00:00', 'Core Workout', 30, 200),
(37, 1, '2025-04-01 10:00:00', 'HIIT', 45, 400),
(38, 1, '2025-04-02 07:00:00', 'Morning Run', 30, 250),
(39, 1, '2025-04-02 10:00:00', 'Upper Body Workout', 60, 350),
(40, 1, '2025-04-03 10:00:00', 'Cardio', 45, 300),
(41, 1, '2025-04-04 07:00:00', 'Morning Cardio', 30, 200),
(42, 1, '2025-04-04 10:00:00', 'Full Body Workout', 60, 450),
(43, 1, '2025-04-05 10:00:00', 'Yoga', 30, 150),
(44, 1, '2025-04-06 10:00:00', 'Rest Day', 0, 0),

-- Week 5 (April 7-9)
(45, 1, '2025-04-07 07:00:00', 'Morning Cardio', 30, 200),
(46, 1, '2025-04-07 10:00:00', 'Upper Body Workout', 60, 350),
(47, 1, '2025-04-07 18:00:00', 'Yoga', 30, 150),
(48, 1, '2025-04-08 10:00:00', 'Lower Body Workout', 60, 400),
(49, 1, '2025-04-08 18:00:00', 'Core Workout', 30, 200),
(50, 1, '2025-04-09 10:00:00', 'HIIT', 45, 400);

-- Insert workout logs (matching the workouts)
INSERT INTO workout_log (id, user_id, workout_name, duration, burned_calories, completed_at)
VALUES
-- Week 1
(1, 1, 'Morning Cardio', 30, 200, '2025-03-10 07:00:00'),
(2, 1, 'Upper Body Workout', 60, 350, '2025-03-10 10:00:00'),
(3, 1, 'Yoga', 30, 150, '2025-03-10 18:00:00'),
(4, 1, 'Lower Body Workout', 60, 400, '2025-03-11 10:00:00'),
(5, 1, 'Core Workout', 30, 200, '2025-03-11 18:00:00'),
(6, 1, 'HIIT', 45, 400, '2025-03-12 10:00:00'),
(7, 1, 'Morning Run', 30, 250, '2025-03-13 07:00:00'),
(8, 1, 'Full Body Workout', 60, 450, '2025-03-13 10:00:00'),
(9, 1, 'Upper Body Workout', 60, 350, '2025-03-14 10:00:00'),
(10, 1, 'Lower Body Workout', 60, 400, '2025-03-15 10:00:00'),
(11, 1, 'Yoga', 30, 150, '2025-03-15 18:00:00'),

-- Week 2
(12, 1, 'Morning Cardio', 30, 200, '2025-03-17 07:00:00'),
(13, 1, 'Lower Body Workout', 60, 400, '2025-03-17 10:00:00'),
(14, 1, 'Core Workout', 30, 200, '2025-03-17 18:00:00'),
(15, 1, 'HIIT', 45, 400, '2025-03-18 10:00:00'),
(16, 1, 'Morning Run', 30, 250, '2025-03-19 07:00:00'),
(17, 1, 'Upper Body Workout', 60, 350, '2025-03-19 10:00:00'),
(18, 1, 'Cardio', 45, 300, '2025-03-20 10:00:00'),
(19, 1, 'Morning Cardio', 30, 200, '2025-03-21 07:00:00'),
(20, 1, 'Full Body Workout', 60, 450, '2025-03-21 10:00:00'),
(21, 1, 'Yoga', 30, 150, '2025-03-22 10:00:00'),

-- Week 3
(22, 1, 'Upper Body Workout', 60, 350, '2025-03-24 10:00:00'),
(23, 1, 'Yoga', 30, 150, '2025-03-24 18:00:00'),
(24, 1, 'Lower Body Workout', 60, 400, '2025-03-25 10:00:00'),
(25, 1, 'Core Workout', 30, 200, '2025-03-25 18:00:00'),
(26, 1, 'Cardio', 45, 300, '2025-03-26 10:00:00'),
(27, 1, 'Morning Run', 30, 250, '2025-03-27 07:00:00'),
(28, 1, 'Full Body Workout', 60, 450, '2025-03-27 10:00:00'),
(29, 1, 'Yoga', 30, 150, '2025-03-28 10:00:00'),
(30, 1, 'Upper Body Workout', 60, 350, '2025-03-29 10:00:00'),

-- Week 4
(31, 1, 'Morning Cardio', 30, 200, '2025-03-31 07:00:00'),
(32, 1, 'Lower Body Workout', 60, 400, '2025-03-31 10:00:00'),
(33, 1, 'Core Workout', 30, 200, '2025-03-31 18:00:00'),
(34, 1, 'HIIT', 45, 400, '2025-04-01 10:00:00'),
(35, 1, 'Morning Run', 30, 250, '2025-04-02 07:00:00'),
(36, 1, 'Upper Body Workout', 60, 350, '2025-04-02 10:00:00'),
(37, 1, 'Cardio', 45, 300, '2025-04-03 10:00:00'),
(38, 1, 'Morning Cardio', 30, 200, '2025-04-04 07:00:00'),
(39, 1, 'Full Body Workout', 60, 450, '2025-04-04 10:00:00'),
(40, 1, 'Yoga', 30, 150, '2025-04-05 10:00:00'),

-- Week 5
(41, 1, 'Morning Cardio', 30, 200, '2025-04-07 07:00:00'),
(42, 1, 'Upper Body Workout', 60, 350, '2025-04-07 10:00:00'),
(43, 1, 'Yoga', 30, 150, '2025-04-07 18:00:00'),
(44, 1, 'Lower Body Workout', 60, 400, '2025-04-08 10:00:00'),
(45, 1, 'Core Workout', 30, 200, '2025-04-08 18:00:00'),
(46, 1, 'HIIT', 45, 400, '2025-04-09 10:00:00');

-- Insert meals for the last 30 days (sample days)
INSERT INTO meal (id, user_id, date_time, meal_name)
VALUES
-- Sample day 1 (April 9)
(1, 1, '2025-04-09 08:00:00', 'Breakfast'),
(2, 1, '2025-04-09 12:00:00', 'Lunch'),
(3, 1, '2025-04-09 18:00:00', 'Dinner'),
(4, 1, '2025-04-09 21:00:00', 'Snack'),

-- Sample day 2 (April 8)
(5, 1, '2025-04-08 08:00:00', 'Breakfast'),
(6, 1, '2025-04-08 12:00:00', 'Lunch'),
(7, 1, '2025-04-08 18:00:00', 'Dinner'),
(8, 1, '2025-04-08 21:00:00', 'Snack'),

-- Sample day 3 (April 7)
(9, 1, '2025-04-07 08:00:00', 'Breakfast'),
(10, 1, '2025-04-07 12:00:00', 'Lunch'),
(11, 1, '2025-04-07 18:00:00', 'Dinner'),
(12, 1, '2025-04-07 21:00:00', 'Snack');

-- Insert meal food items with realistic calorie values for healthy weight gain
INSERT INTO meal_food_item (id, meal_id, food_item, calories, quantity, unit)
VALUES
-- April 9
(1, 1, 'Protein Oatmeal', 400, 1.5, 'cup'),
(2, 1, 'Banana', 105, 2, 'medium'),
(3, 1, 'Peanut Butter', 190, 2, 'tbsp'),
(4, 1, 'Protein Shake', 300, 1, 'serving'),
(5, 2, 'Chicken Breast', 330, 250, 'g'),
(6, 2, 'Brown Rice', 440, 2, 'cup'),
(7, 2, 'Mixed Vegetables', 100, 2, 'cup'),
(8, 2, 'Avocado', 240, 1, 'medium'),
(9, 3, 'Salmon', 420, 225, 'g'),
(10, 3, 'Quinoa', 440, 2, 'cup'),
(11, 3, 'Sweet Potato', 180, 2, 'medium'),
(12, 4, 'Greek Yogurt', 200, 2, 'cup'),
(13, 4, 'Mixed Nuts', 350, 100, 'g'),
(14, 4, 'Honey', 60, 1, 'tbsp'),

-- April 8
(15, 5, 'Scrambled Eggs', 280, 4, 'large'),
(16, 5, 'Whole Wheat Toast', 160, 4, 'slices'),
(17, 5, 'Avocado', 240, 1, 'medium'),
(18, 6, 'Beef Stir Fry', 600, 1, 'serving'),
(19, 6, 'White Rice', 440, 2, 'cup'),
(20, 7, 'Grilled Chicken', 400, 250, 'g'),
(21, 7, 'Mashed Potatoes', 400, 2, 'cup'),
(22, 8, 'Cottage Cheese', 200, 1, 'cup'),
(23, 8, 'Mixed Berries', 100, 1, 'cup'),

-- April 7
(24, 9, 'Pancakes', 400, 4, 'medium'),
(25, 9, 'Maple Syrup', 100, 2, 'tbsp'),
(26, 9, 'Bacon', 200, 4, 'slices'),
(27, 10, 'Burger', 600, 1, 'serving'),
(28, 10, 'Fries', 400, 1, 'serving'),
(29, 11, 'Steak', 500, 200, 'g'),
(30, 11, 'Baked Potato', 300, 2, 'medium'),
(31, 12, 'Protein Bar', 300, 1, 'bar'),
(32, 12, 'Almond Milk', 100, 1, 'cup');

