
INSERT INTO app_user (email, name, password_hash, created_at, updated_at)
VALUES
    ('alice@example.com', 'Alice Johnson', 'hashed_pw_1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('bob@example.com', 'Bob Smith', 'hashed_pw_2', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('carol@example.com', 'Carol Davis', 'hashed_pw_3', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);


INSERT INTO list (owner_id, name, description, created_at, updated_at)
VALUES
    (1, 'Personal Tasks', 'Alice’s personal to-do list', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (1, 'Work Projects', 'Tasks related to Alice’s work', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 'Shopping List', 'Bob’s grocery and shopping items', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3, 'Travel Plans', 'Carol’s upcoming trips', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);



INSERT INTO task (list_id, creator_id, title, description, status, due_date, priority, created_at, updated_at, completed_at)
VALUES
-- ===== Alice's Personal Tasks (list_id = 1, creator_id = 1) =====
(1, 1, 'Buy groceries', 'Milk, eggs, bread, veggies', 'todo', DATEADD('DAY', 1, CURRENT_DATE), 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
(1, 1, 'Call plumber', 'Fix slow drain in kitchen sink', 'in_progress', DATEADD('DAY', 2, CURRENT_DATE), 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
(1, 1, 'Schedule dentist appointment', 'Annual check-up', 'todo', DATEADD('DAY', 14, CURRENT_DATE), 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
(1, 1, 'Renew gym membership', 'Check discounts for annual plan', 'todo', DATEADD('DAY', 7, CURRENT_DATE), 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
(1, 1, 'Clean balcony', 'Declutter and wipe railing', 'archived', DATEADD('DAY', -10, CURRENT_DATE), 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
(1, 1, 'Organize wardrobe', 'Sort summer/winter clothes', 'in_progress', DATEADD('DAY', 5, CURRENT_DATE), 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
(1, 1, 'Meditation session', 'Try 10-minute guided session', 'done', DATEADD('DAY', -1, CURRENT_DATE), 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, 1, 'Plan weekly meals', 'Create a meal plan and shopping list', 'todo', DATEADD('DAY', 3, CURRENT_DATE), 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
(1, 1, 'Backup phone photos', 'Move to cloud storage', 'todo', DATEADD('DAY', 4, CURRENT_DATE), 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
(1, 1, 'Fix bike tire', 'Patch or replace front tire', 'todo', DATEADD('DAY', 2, CURRENT_DATE), 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
(1, 1, 'Read book chapter', 'Finish Chapter 12 of current book', 'done', DATEADD('DAY', -2, CURRENT_DATE), 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, 1, 'Practice guitar', 'Learn new chord progression', 'todo', DATEADD('DAY', 6, CURRENT_DATE), 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
(1, 1, 'Update budget spreadsheet', 'Record monthly expenses', 'in_progress', DATEADD('DAY', 1, CURRENT_DATE), 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
(1, 1, 'Deep clean kitchen', 'Stove, oven, and cabinets', 'todo', DATEADD('DAY', 8, CURRENT_DATE), 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
(1, 1, 'Buy birthday gift', 'For friend next weekend', 'todo', DATEADD('DAY', 5, CURRENT_DATE), 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),

-- ===== Alice's Work Projects (list_id = 2, creator_id = 1) =====
(2, 1, 'Prepare quarterly presentation', 'Slides for Monday leadership meeting', 'in_progress', DATEADD('DAY', 3, CURRENT_DATE), 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
(2, 1, 'Review pull requests', 'Check PRs #142, #143, #147', 'done', DATEADD('DAY', -1, CURRENT_DATE), 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 1, 'Update Jira tickets', 'Refine story points and priorities', 'todo', DATEADD('DAY', 2, CURRENT_DATE), 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
(2, 1, 'Refactor service module', 'Extract shared logic into utility class', 'in_progress', DATEADD('DAY', 10, CURRENT_DATE), 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
(2, 1, 'Write integration tests', 'Cover payment flow with mocks', 'todo', DATEADD('DAY', 6, CURRENT_DATE), 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
(2, 1, 'On-call runbook updates', 'Add new alert resolution steps', 'todo', DATEADD('DAY', 9, CURRENT_DATE), 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
(2, 1, 'Data migration verification', 'Validate migrated rows count & checksums', 'todo', DATEADD('DAY', 4, CURRENT_DATE), 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
(2, 1, 'Security review checklist', 'Address findings from last audit', 'in_progress', DATEADD('DAY', 12, CURRENT_DATE), 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
(2, 1, 'Docker image cleanup', 'Remove old images and pin versions', 'done', DATEADD('DAY', -2, CURRENT_DATE), 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 1, 'Update API documentation', 'Add new endpoints and examples', 'todo', DATEADD('DAY', 7, CURRENT_DATE), 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
(2, 1, 'Sprint retrospective notes', 'Summarize key learnings', 'archived', DATEADD('DAY', -5, CURRENT_DATE), 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),

-- ===== Bob's Shopping List (list_id = 3, creator_id = 2) =====
(3, 2, 'Order laptop', '16GB RAM, 512GB SSD, lightweight', 'todo', DATEADD('DAY', 5, CURRENT_DATE), 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
(3, 2, 'Buy coffee beans', 'Medium roast, 1kg bag', 'done', DATEADD('DAY', -1, CURRENT_DATE), 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 2, 'Pick up dry cleaning', 'Suits and shirts from Main St.', 'archived', DATEADD('DAY', -2, CURRENT_DATE), 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
(3, 2, 'Order running shoes', 'Size 43, neutral support', 'todo', DATEADD('DAY', 6, CURRENT_DATE), 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
(3, 2, 'Buy gift wrap', 'Eco-friendly, blue/white patterns', 'todo', DATEADD('DAY', 2, CURRENT_DATE), 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
(3, 2, 'Purchase power bank', '20,000 mAh, USB-C PD', 'todo', DATEADD('DAY', 8, CURRENT_DATE), 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
(3, 2, 'Kitchen towels', 'Pack of 6, cotton', 'done', DATEADD('DAY', -3, CURRENT_DATE), 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 2, 'Refill printer ink', 'Black XL cartridge', 'todo', DATEADD('DAY', 4, CURRENT_DATE), 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
(3, 2, 'Buy HDMI cable', '2 meters, 2.1 spec', 'todo', DATEADD('DAY', 3, CURRENT_DATE), 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
(3, 2, 'Order wireless mouse', 'Ergonomic, Bluetooth', 'in_progress', DATEADD('DAY', 3, CURRENT_DATE), 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
(3, 2, 'Replace shower curtain', 'Mold-resistant, white', 'todo', DATEADD('DAY', 10, CURRENT_DATE), 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),

-- ===== Carol's Travel Plans (list_id = 4, creator_id = 3) =====
(4, 3, 'Book flight tickets', 'Round-trip to Paris, flexible dates', 'in_progress', DATEADD('DAY', 10, CURRENT_DATE), 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
(4, 3, 'Reserve hotel', '4 nights near city center', 'todo', DATEADD('DAY', 12, CURRENT_DATE), 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
(4, 3, 'Renew passport', 'Expires soon—bring photos', 'todo', DATEADD('DAY', 15, CURRENT_DATE), 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
(4, 3, 'Buy travel insurance', 'Medical and cancellation coverage', 'todo', DATEADD('DAY', 9, CURRENT_DATE), 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
(4, 3, 'Create itinerary', 'Museums, cafes, walking tours', 'in_progress', DATEADD('DAY', 13, CURRENT_DATE), 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
(4, 3, 'Pack essentials', 'Adapters, medication, toiletries', 'todo', DATEADD('DAY', 14, CURRENT_DATE), 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
(4, 3, 'Exchange currency', 'Get some EUR cash', 'todo', DATEADD('DAY', 7, CURRENT_DATE), 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
(4, 3, 'Check visa requirements', 'Confirm Schengen needs', 'done', DATEADD('DAY', -4, CURRENT_DATE), 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 3, 'Download offline maps', 'Paris and Versailles area', 'todo', DATEADD('DAY', 11, CURRENT_DATE), 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
(4, 3, 'Set up roaming plan', 'Contact mobile provider', 'archived', DATEADD('DAY', -6, CURRENT_DATE), 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
(4, 3, 'Notify bank of travel', 'Avoid card blocks', 'todo', DATEADD('DAY', 8, CURRENT_DATE), 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),

-- ===== Cross-list Work & Personal Mix (extra realistic variety) =====
(2, 1, 'Conduct performance review', 'Prepare notes for team members', 'todo', DATEADD('DAY', 3, CURRENT_DATE), 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
(2, 1, 'Incident postmortem', 'Write RCA and action items', 'done', DATEADD('DAY', -2, CURRENT_DATE), 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 1, 'Optimize SQL queries', 'Add indexes to slow endpoints', 'in_progress', DATEADD('DAY', 6, CURRENT_DATE), 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
(1, 1, 'Plant herbs', 'Basil, mint, rosemary', 'todo', DATEADD('DAY', 5, CURRENT_DATE), 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
(3, 2, 'Buy reusable water bottle', 'Leak-proof, 1L', 'todo', DATEADD('DAY', 2, CURRENT_DATE), 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
(4, 3, 'Book train to Versailles', 'Day trip planning', 'todo', DATEADD('DAY', 16, CURRENT_DATE), 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
(2, 1, 'Set up CI pipeline cache', 'Improve build speed', 'todo', DATEADD('DAY', 9, CURRENT_DATE), 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
(2, 1, 'Accessibility audit', 'Keyboard navigation & contrast', 'in_progress', DATEADD('DAY', 12, CURRENT_DATE), 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
(1, 1, 'Replace lightbulbs', 'Warm white, energy-saving', 'done', DATEADD('DAY', -1, CURRENT_DATE), 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 2, 'Buy USB-C hub', 'HDMI, USB-A, SD card slots', 'todo', DATEADD('DAY', 7, CURRENT_DATE), 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
(4, 3, 'Pre-book museum tickets', 'Avoid queues at Louvre', 'todo', DATEADD('DAY', 9, CURRENT_DATE), 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
(2, 1, 'Log aggregation setup', 'Centralize logs with structured fields', 'todo', DATEADD('DAY', 11, CURRENT_DATE), 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
(2, 1, 'Patch dependencies', 'Update vulnerable libs', 'done', DATEADD('DAY', -3, CURRENT_DATE), 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, 1, 'Test recipe', 'New pasta sauce', 'todo', DATEADD('DAY', 4, CURRENT_DATE), 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL);
