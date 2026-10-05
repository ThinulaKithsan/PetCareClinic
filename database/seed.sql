USE petcare;
-- Synthetic demonstration data only. Run once after schema.sql.
INSERT INTO customers(name,phone,email,address) VALUES
('Demo Customer One','0771234567','demo1@example.com','Colombo'),
('Demo Customer Two','0712345678','demo2@example.com','Kandy');
INSERT INTO pets(customer_id,name,species,breed,birth_date) VALUES
(1,'Bruno','Dog','Labrador','2022-06-15'),(2,'Milo','Cat','Domestic shorthair','2023-01-20');
INSERT INTO services(name,price) VALUES ('Consultation',2500.00),('Vaccination',4500.00),('Wound dressing',1800.00);
INSERT INTO vets(name) VALUES ('Dr Demo Perera'),('Dr Demo Silva');
INSERT INTO appointments(pet_id,customer_id,service_id,vet_id,slot,fee,status) VALUES
(1,1,1,1,TIMESTAMP(CURRENT_DATE,'09:00:00'),2500.00,'COMPLETED'),
(2,2,2,2,TIMESTAMP(DATE_ADD(CURRENT_DATE,INTERVAL 1 DAY),'10:00:00'),4500.00,'BOOKED');
INSERT INTO treatments(appointment_id,diagnosis,medication) VALUES(1,'Routine wellness examination','No medication required. Return for annual checkup.');
INSERT INTO payments(appointment_id,amount) VALUES(1,1500.00);
