-- Owners
INSERT INTO owners (first_name, last_name, email, phone, address) VALUES
('Alice', 'Johnson', 'alice@example.com', '555-0101', '123 Maple Street'),
('Bob', 'Smith', 'bob@example.com', '555-0102', '456 Oak Avenue'),
('Carol', 'Williams', 'carol@example.com', '555-0103', '789 Pine Road');

-- Animals
INSERT INTO animals (name, species, breed, date_of_birth, color, gender, owner_id) VALUES
('Buddy',   'DOG',    'Golden Retriever', '2020-03-15', 'Golden',  'MALE',   1),
('Whiskers','CAT',    'Siamese',          '2019-07-22', 'Cream',   'FEMALE', 1),
('Max',     'DOG',    'German Shepherd',  '2021-01-10', 'Black',   'MALE',   2),
('Tweety',  'BIRD',   'Canary',           '2022-05-05', 'Yellow',  'UNKNOWN',2),
('Fluffy',  'RABBIT', 'Holland Lop',      '2021-11-30', 'White',   'FEMALE', 3);

-- Visits
INSERT INTO visits (animal_id, visit_date, reason, height, weight, age, vet_name, diagnosis, treatment) VALUES
(1, '2024-01-10', 'Annual check-up',      65.0, 30.2, 3.8, 'Dr. Evans',  'Healthy',              'Vaccines updated'),
(1, '2024-06-20', 'Limping on left paw',  65.5, 30.8, 4.3, 'Dr. Evans',  'Mild sprain',          'Rest and anti-inflammatories'),
(2, '2024-02-14', 'Annual check-up',      25.0,  4.1, 4.6, 'Dr. Chen',   'Healthy',              'Flea treatment applied'),
(3, '2024-03-05', 'Vaccination',          60.0, 28.5, 3.1, 'Dr. Evans',  'Healthy',              'DHPP booster'),
(5, '2024-04-18', 'Not eating properly',  28.0,  1.9, 2.4, 'Dr. Martin', 'Mild GI upset',        'Dietary change recommended');

-- Notes
INSERT INTO notes (animal_id, content, created_at) VALUES
(1, 'Buddy loves fetch. Very energetic at visits.', '2024-01-10 10:30:00'),
(1, 'Owner reports occasional limping after long walks.', '2024-05-01 09:00:00'),
(2, 'Whiskers is shy but calm during examinations.', '2024-02-14 14:00:00'),
(3, 'Max is well-trained. No issues noted.', '2024-03-05 11:00:00'),
(5, 'Fluffy has been less active than usual.', '2024-04-18 16:00:00');
