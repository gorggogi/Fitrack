-- Clear existing data
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE body_measurement;
TRUNCATE TABLE workout;
TRUNCATE TABLE workout_repeat_days;
TRUNCATE TABLE workout_log;
TRUNCATE TABLE meal;
TRUNCATE TABLE meal_food_item;
SET FOREIGN_KEY_CHECKS = 1;

-- Update user's current weight to match latest measurement
UPDATE users SET weight = 70.9 WHERE id = 1;

-- Insert body measurements for user 1 (weekly measurements showing weight loss)
INSERT INTO body_measurement (id, user_id, date_time, weight, notes)
VALUES 
(1, 1, '2025-03-10 08:00:00', 75.0, 'Starting weight - Beginning weight loss journey'),
(2, 1, '2025-03-17 08:00:00', 74.2, 'Week 1 progress - Good start with diet changes'),
(3, 1, '2025-03-24 08:00:00', 73.5, 'Week 2 progress - Increased water intake'),
(4, 1, '2025-03-31 08:00:00', 72.8, 'Week 3 progress - Consistent with workouts'),
(5, 1, '2025-04-07 08:00:00', 72.1, 'Week 4 progress - Feeling more energetic'),
(6, 1, '2025-04-14 08:00:00', 71.5, 'Week 5 progress - Clothes fitting better'),
(7, 1, '2025-04-21 08:00:00', 70.9, 'Week 6 progress - Maintaining good habits');

-- Insert workouts for user 1 (extending to April 23)
INSERT INTO workout (id, user_id, date_time, workout_name, duration, burned_calories)
VALUES
-- Week 1 (March 10-16)
(1, 1, '2025-03-10 07:00:00', 'Morning Walk', 30, 150),
(2, 1, '2025-03-10 10:00:00', 'Full Body Workout', 45, 250),
(3, 1, '2025-03-10 18:00:00', 'Yoga', 30, 120),
(4, 1, '2025-03-11 10:00:00', 'Cardio', 30, 200),
(5, 1, '2025-03-11 18:00:00', 'Core Workout', 30, 150),
(6, 1, '2025-03-12 10:00:00', 'HIIT', 30, 300),
(7, 1, '2025-03-13 07:00:00', 'Morning Walk', 30, 150),
(9, 1, '2025-03-14 10:00:00', 'Full Body Workout', 45, 250),
(10, 1, '2025-03-15 10:00:00', 'Cardio', 30, 200),
(11, 1, '2025-03-15 18:00:00', 'Yoga', 30, 120),
(12, 1, '2025-03-16 10:00:00', 'Rest Day', 0, 0),

-- Week 2 (March 17-23)
(13, 1, '2025-03-17 07:00:00', 'Morning Walk', 30, 150),
(14, 1, '2025-03-17 10:00:00', 'Full Body Workout', 45, 250),
(15, 1, '2025-03-17 18:00:00', 'Core Workout', 30, 150),
(16, 1, '2025-03-18 10:00:00', 'HIIT', 30, 300),
(17, 1, '2025-03-19 07:00:00', 'Morning Walk', 30, 150),
(18, 1, '2025-03-19 10:00:00', 'Full Body Workout', 45, 250),
(19, 1, '2025-03-20 10:00:00', 'Cardio', 30, 200),
(20, 1, '2025-03-21 07:00:00', 'Morning Walk', 30, 150),
(21, 1, '2025-03-21 10:00:00', 'Full Body Workout', 45, 250),
(22, 1, '2025-03-22 10:00:00', 'Yoga', 30, 120),
(23, 1, '2025-03-23 10:00:00', 'Rest Day', 0, 0),

-- Week 3 (March 24-30)
(24, 1, '2025-03-24 10:00:00', 'Full Body Workout', 45, 250),
(25, 1, '2025-03-24 18:00:00', 'Yoga', 30, 120),
(26, 1, '2025-03-25 10:00:00', 'Cardio', 30, 200),
(27, 1, '2025-03-25 18:00:00', 'Core Workout', 30, 150),
(28, 1, '2025-03-26 10:00:00', 'HIIT', 30, 300),
(29, 1, '2025-03-27 07:00:00', 'Morning Walk', 30, 150),
(30, 1, '2025-03-27 10:00:00', 'Full Body Workout', 45, 250),
(31, 1, '2025-03-28 10:00:00', 'Yoga', 30, 120),
(32, 1, '2025-03-29 10:00:00', 'Cardio', 30, 200),
(33, 1, '2025-03-30 10:00:00', 'Rest Day', 0, 0),

-- Week 4 (March 31-April 6)
(34, 1, '2025-03-31 07:00:00', 'Morning Walk', 30, 150),
(35, 1, '2025-03-31 10:00:00', 'Full Body Workout', 45, 250),
(36, 1, '2025-03-31 18:00:00', 'Core Workout', 30, 150),
(37, 1, '2025-04-01 10:00:00', 'HIIT', 30, 300),
(38, 1, '2025-04-02 07:00:00', 'Morning Walk', 30, 150),
(39, 1, '2025-04-02 10:00:00', 'Full Body Workout', 45, 250),
(40, 1, '2025-04-03 10:00:00', 'Cardio', 30, 200),
(41, 1, '2025-04-04 07:00:00', 'Morning Walk', 30, 150),
(42, 1, '2025-04-04 10:00:00', 'Full Body Workout', 45, 250),
(43, 1, '2025-04-05 10:00:00', 'Yoga', 30, 120),
(44, 1, '2025-04-06 10:00:00', 'Rest Day', 0, 0),

-- Week 5 (April 7-13)
(45, 1, '2025-04-07 07:00:00', 'Morning Walk', 30, 150),
(46, 1, '2025-04-07 10:00:00', 'Full Body Workout', 45, 250),
(47, 1, '2025-04-07 18:00:00', 'Yoga', 30, 120),
(48, 1, '2025-04-08 10:00:00', 'Cardio', 30, 200),
(49, 1, '2025-04-08 18:00:00', 'Core Workout', 30, 150),
(50, 1, '2025-04-09 10:00:00', 'HIIT', 30, 300),
(51, 1, '2025-04-10 10:00:00', 'Full Body Workout', 45, 250),
(52, 1, '2025-04-11 07:00:00', 'Morning Walk', 30, 150),
(53, 1, '2025-04-11 10:00:00', 'Cardio', 30, 200),
(54, 1, '2025-04-12 10:00:00', 'Yoga', 30, 120),
(55, 1, '2025-04-13 10:00:00', 'Rest Day', 0, 0),

-- Week 6 (April 14-20)
(56, 1, '2025-04-14 07:00:00', 'Morning Walk', 30, 150),
(57, 1, '2025-04-14 10:00:00', 'Full Body Workout', 45, 250),
(58, 1, '2025-04-15 10:00:00', 'HIIT', 30, 300),
(59, 1, '2025-04-16 07:00:00', 'Morning Walk', 30, 150),
(60, 1, '2025-04-16 10:00:00', 'Cardio', 30, 200),
(61, 1, '2025-04-17 10:00:00', 'Full Body Workout', 45, 250),
(62, 1, '2025-04-18 10:00:00', 'Yoga', 30, 120),
(63, 1, '2025-04-19 07:00:00', 'Morning Walk', 30, 150),
(64, 1, '2025-04-19 10:00:00', 'Full Body Workout', 45, 250),
(65, 1, '2025-04-20 10:00:00', 'Rest Day', 0, 0),

-- Week 7 (April 21-23)
(66, 1, '2025-04-21 07:00:00', 'Morning Walk', 30, 150),
(67, 1, '2025-04-21 10:00:00', 'Full Body Workout', 45, 250),
(68, 1, '2025-04-22 10:00:00', 'HIIT', 30, 300),
(69, 1, '2025-04-23 10:00:00', 'Cardio', 30, 200);

-- Insert workout logs (matching the extended workouts)
INSERT INTO workout_log (id, user_id, workout_name, duration, burned_calories, completed_at)
VALUES
-- Week 1
(1, 1, 'Morning Walk', 30, 150, '2025-03-10 07:00:00'),
(2, 1, 'Full Body Workout', 45, 250, '2025-03-10 10:00:00'),
(3, 1, 'Yoga', 30, 120, '2025-03-10 18:00:00'),
(4, 1, 'Cardio', 30, 200, '2025-03-11 10:00:00'),
(5, 1, 'Core Workout', 30, 150, '2025-03-11 18:00:00'),
(6, 1, 'HIIT', 30, 300, '2025-03-12 10:00:00'),
(7, 1, 'Morning Walk', 30, 150, '2025-03-13 07:00:00'),
(8, 1, 'Full Body Workout', 45, 250, '2025-03-13 10:00:00'),
(9, 1, 'Full Body Workout', 45, 250, '2025-03-14 10:00:00'),
(10, 1, 'Cardio', 30, 200, '2025-03-15 10:00:00'),
(11, 1, 'Yoga', 30, 120, '2025-03-15 18:00:00'),

-- Week 2
(12, 1, 'Morning Walk', 30, 150, '2025-03-17 07:00:00'),
(13, 1, 'Full Body Workout', 45, 250, '2025-03-17 10:00:00'),
(14, 1, 'Core Workout', 30, 150, '2025-03-17 18:00:00'),
(15, 1, 'HIIT', 30, 300, '2025-03-18 10:00:00'),
(16, 1, 'Morning Walk', 30, 150, '2025-03-19 07:00:00'),
(17, 1, 'Full Body Workout', 45, 250, '2025-03-19 10:00:00'),
(18, 1, 'Cardio', 30, 200, '2025-03-20 10:00:00'),
(19, 1, 'Morning Walk', 30, 150, '2025-03-21 07:00:00'),
(20, 1, 'Full Body Workout', 45, 250, '2025-03-21 10:00:00'),
(21, 1, 'Yoga', 30, 120, '2025-03-22 10:00:00'),

-- Week 3
(22, 1, 'Full Body Workout', 45, 250, '2025-03-24 10:00:00'),
(23, 1, 'Yoga', 30, 120, '2025-03-24 18:00:00'),
(24, 1, 'Cardio', 30, 200, '2025-03-25 10:00:00'),
(25, 1, 'Core Workout', 30, 150, '2025-03-25 18:00:00'),
(26, 1, 'HIIT', 30, 300, '2025-03-26 10:00:00'),
(27, 1, 'Morning Walk', 30, 150, '2025-03-27 07:00:00'),
(28, 1, 'Full Body Workout', 45, 250, '2025-03-27 10:00:00'),
(29, 1, 'Yoga', 30, 120, '2025-03-28 10:00:00'),
(30, 1, 'Cardio', 30, 200, '2025-03-29 10:00:00'),

-- Week 4
(31, 1, 'Morning Walk', 30, 150, '2025-03-31 07:00:00'),
(32, 1, 'Full Body Workout', 45, 250, '2025-03-31 10:00:00'),
(33, 1, 'Core Workout', 30, 150, '2025-03-31 18:00:00'),
(34, 1, 'HIIT', 30, 300, '2025-04-01 10:00:00'),
(35, 1, 'Morning Walk', 30, 150, '2025-04-02 07:00:00'),
(36, 1, 'Full Body Workout', 45, 250, '2025-04-02 10:00:00'),
(37, 1, 'Cardio', 30, 200, '2025-04-03 10:00:00'),
(38, 1, 'Morning Walk', 30, 150, '2025-04-04 07:00:00'),
(39, 1, 'Full Body Workout', 45, 250, '2025-04-04 10:00:00'),
(40, 1, 'Yoga', 30, 120, '2025-04-05 10:00:00'),

-- Week 5
(41, 1, 'Morning Walk', 30, 150, '2025-04-07 07:00:00'),
(42, 1, 'Full Body Workout', 45, 250, '2025-04-07 10:00:00'),
(43, 1, 'Yoga', 30, 120, '2025-04-07 18:00:00'),
(44, 1, 'Cardio', 30, 200, '2025-04-08 10:00:00'),
(45, 1, 'Core Workout', 30, 150, '2025-04-08 18:00:00'),
(46, 1, 'HIIT', 30, 300, '2025-04-09 10:00:00'),
(47, 1, 'Full Body Workout', 45, 250, '2025-04-10 10:00:00'),
(48, 1, 'Morning Walk', 30, 150, '2025-04-11 07:00:00'),
(49, 1, 'Cardio', 30, 200, '2025-04-11 10:00:00'),
(50, 1, 'Yoga', 30, 120, '2025-04-12 10:00:00'),

-- Week 6
(51, 1, 'Morning Walk', 30, 150, '2025-04-14 07:00:00'),
(52, 1, 'Full Body Workout', 45, 250, '2025-04-14 10:00:00'),
(53, 1, 'HIIT', 30, 300, '2025-04-15 10:00:00'),
(54, 1, 'Morning Walk', 30, 150, '2025-04-16 07:00:00'),
(55, 1, 'Cardio', 30, 200, '2025-04-16 10:00:00'),
(56, 1, 'Full Body Workout', 45, 250, '2025-04-17 10:00:00'),
(57, 1, 'Yoga', 30, 120, '2025-04-18 10:00:00'),
(58, 1, 'Morning Walk', 30, 150, '2025-04-19 07:00:00'),
(59, 1, 'Full Body Workout', 45, 250, '2025-04-19 10:00:00'),

-- Week 7
(60, 1, 'Morning Walk', 30, 150, '2025-04-21 07:00:00'),
(61, 1, 'Full Body Workout', 45, 250, '2025-04-21 10:00:00'),
(62, 1, 'HIIT', 30, 300, '2025-04-22 10:00:00'),
(63, 1, 'Cardio', 30, 200, '2025-04-23 10:00:00');

-- Insert meals for the extended period (sample days)
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
(12, 1, '2025-04-07 21:00:00', 'Snack'),

-- Sample day 4 (April 23)
(13, 1, '2025-04-23 08:00:00', 'Breakfast'),
(14, 1, '2025-04-23 12:00:00', 'Lunch'),
(15, 1, '2025-04-23 18:00:00', 'Dinner'),
(16, 1, '2025-04-23 21:00:00', 'Snack'),

-- Sample day 5 (April 22)
(17, 1, '2025-04-22 08:00:00', 'Breakfast'),
(18, 1, '2025-04-22 12:00:00', 'Lunch'),
(19, 1, '2025-04-22 18:00:00', 'Dinner'),
(20, 1, '2025-04-22 21:00:00', 'Snack'),

-- Sample day 6 (April 21)
(21, 1, '2025-04-21 08:00:00', 'Breakfast'),
(22, 1, '2025-04-21 12:00:00', 'Lunch'),
(23, 1, '2025-04-21 18:00:00', 'Dinner'),
(24, 1, '2025-04-21 21:00:00', 'Snack');

-- Insert meal food items for the extended period
INSERT INTO meal_food_item (id, meal_id, food_item, calories, quantity, unit)
VALUES
-- April 9
(1, 1, 'Oatmeal with Berries', 300, 1, 'cup'),
(2, 1, 'Greek Yogurt', 150, 1, 'cup'),
(3, 1, 'Banana', 105, 1, 'medium'),
(4, 2, 'Grilled Chicken Salad', 400, 1, 'serving'),
(5, 2, 'Whole Grain Bread', 160, 2, 'slices'),
(6, 3, 'Baked Salmon', 350, 150, 'g'),
(7, 3, 'Quinoa', 220, 1, 'cup'),
(8, 3, 'Steamed Vegetables', 100, 2, 'cup'),
(9, 4, 'Apple', 95, 1, 'medium'),
(10, 4, 'Almonds', 160, 30, 'g'),

-- April 8
(11, 5, 'Scrambled Eggs', 280, 2, 'large'),
(12, 5, 'Whole Wheat Toast', 160, 2, 'slices'),
(13, 5, 'Avocado', 120, 0.5, 'medium'),
(14, 6, 'Turkey Wrap', 400, 1, 'serving'),
(15, 6, 'Mixed Vegetables', 100, 1, 'cup'),
(16, 7, 'Grilled Chicken', 300, 150, 'g'),
(17, 7, 'Brown Rice', 220, 1, 'cup'),
(18, 8, 'Greek Yogurt', 150, 1, 'cup'),
(19, 8, 'Mixed Berries', 100, 1, 'cup'),

-- April 7
(20, 9, 'Protein Pancakes', 350, 2, 'medium'),
(21, 9, 'Maple Syrup', 50, 1, 'tbsp'),
(22, 9, 'Banana', 105, 1, 'medium'),
(23, 10, 'Chicken Caesar Salad', 450, 1, 'serving'),
(24, 10, 'Whole Grain Bread', 160, 2, 'slices'),
(25, 11, 'Baked Fish', 300, 150, 'g'),
(26, 11, 'Sweet Potato', 180, 1, 'medium'),
(27, 12, 'Protein Bar', 200, 1, 'bar'),
(28, 12, 'Almond Milk', 100, 1, 'cup'),

-- April 23
(29, 13, 'Oatmeal with Protein', 350, 1, 'cup'),
(30, 13, 'Greek Yogurt', 150, 1, 'cup'),
(31, 13, 'Mixed Berries', 100, 1, 'cup'),
(32, 14, 'Grilled Chicken Salad', 400, 1, 'serving'),
(33, 14, 'Quinoa', 220, 1, 'cup'),
(34, 15, 'Baked Salmon', 350, 150, 'g'),
(35, 15, 'Brown Rice', 220, 1, 'cup'),
(36, 15, 'Steamed Vegetables', 100, 2, 'cup'),
(37, 16, 'Apple', 95, 1, 'medium'),
(38, 16, 'Almonds', 160, 30, 'g'),

-- April 22
(39, 17, 'Scrambled Eggs', 280, 2, 'large'),
(40, 17, 'Whole Wheat Toast', 160, 2, 'slices'),
(41, 17, 'Avocado', 120, 0.5, 'medium'),
(42, 18, 'Turkey Wrap', 400, 1, 'serving'),
(43, 18, 'Mixed Vegetables', 100, 1, 'cup'),
(44, 19, 'Grilled Chicken', 300, 150, 'g'),
(45, 19, 'Brown Rice', 220, 1, 'cup'),
(46, 20, 'Greek Yogurt', 150, 1, 'cup'),
(47, 20, 'Mixed Berries', 100, 1, 'cup'),

-- April 21
(48, 21, 'Protein Pancakes', 350, 2, 'medium'),
(49, 21, 'Maple Syrup', 50, 1, 'tbsp'),
(50, 21, 'Banana', 105, 1, 'medium'),
(51, 22, 'Chicken Caesar Salad', 450, 1, 'serving'),
(52, 22, 'Whole Grain Bread', 160, 2, 'slices'),
(53, 23, 'Baked Fish', 300, 150, 'g'),
(54, 23, 'Sweet Potato', 180, 1, 'medium'),
(55, 24, 'Protein Bar', 200, 1, 'bar'),
(56, 24, 'Almond Milk', 100, 1, 'cup');

