-- DATABASE NAME: cardealership	

-- Drop table

-- DROP TABLE public.users;

CREATE TABLE public.users (
	username varchar(32) NOT NULL,
	"password" varchar(32) NOT NULL,
	first_name varchar(32) NOT NULL,
	last_name varchar(32) NOT NULL,
	"type" varchar(35) NULL,
	CONSTRAINT users_pkey PRIMARY KEY (username)
);

-- Drop table

-- DROP TABLE public.employees;

CREATE TABLE public.employees (
	username varchar(32) NOT NULL,
	CONSTRAINT employees_pkey PRIMARY KEY (username),
	CONSTRAINT fk_user_employee FOREIGN KEY (username) REFERENCES users(username)
);

-- Drop table

-- DROP TABLE public.customers;

CREATE TABLE public.customers (
	username varchar(32) NOT NULL,
	CONSTRAINT customers_pkey PRIMARY KEY (username),
	CONSTRAINT fk_users_customers FOREIGN KEY (username) REFERENCES users(username)
);

-- Drop table

-- DROP TABLE public.cars;

CREATE TABLE public.cars (
	vin varchar(20) NOT NULL,
	make varchar(25) NOT NULL,
	model varchar(25) NOT NULL,
	price float8 NOT NULL,
	is_sold bool NOT NULL,
	username varchar(32) NULL,
	active bool NULL,
	CONSTRAINT cars_pkey PRIMARY KEY (vin),
	CONSTRAINT fk_customer_car FOREIGN KEY (username) REFERENCES customers(username)
);

-- Drop table

-- DROP TABLE public.offerstatus;

CREATE TABLE public.offerstatus (
	status_id int4 NOT NULL,
	"name" varchar(20) NOT NULL,
	CONSTRAINT offerstatus_pkey PRIMARY KEY (status_id)
);

-- Drop table

-- DROP TABLE public.offers;

CREATE TABLE public.offers (
	offer_id varchar(32) NOT NULL,
	signed_date date NOT NULL,
	amount float8 NOT NULL,
	total_payment int4 NOT NULL,
	payments_made int4 NOT NULL,
	monthly_payment float8 NULL,
	username varchar(32) NOT NULL,
	vin varchar(25) NOT NULL,
	approved_by varchar(32) NULL,
	status_id int4 NOT NULL,
	CONSTRAINT contacts_pkey PRIMARY KEY (offer_id),
	CONSTRAINT fk_cars_contracts FOREIGN KEY (vin) REFERENCES cars(vin),
	CONSTRAINT fk_customers_contracts FOREIGN KEY (username) REFERENCES customers(username),
	CONSTRAINT fk_employees_offers FOREIGN KEY (approved_by) REFERENCES employees(username),
	CONSTRAINT fk_offerstatus_offers FOREIGN KEY (status_id) REFERENCES offerstatus(status_id)
);

-- Drop table

-- DROP TABLE public.payments;

CREATE TABLE public.payments (
	payment_id int4 NOT NULL,
	paid_date date NOT NULL,
	amount_paid float8 NOT NULL,
	vin varchar(25) NOT NULL,
	CONSTRAINT fk_cars_payments FOREIGN KEY (vin) REFERENCES cars(vin)
);

-- Trigger to update offers that were not accepted
create or replace function update_offer()
returns trigger as $$
declare
	offerId varchar(32);
	vinNum varchar(32);
	custUsername varchar(32);
begin
	offerId := new.offer_id;
	vinNum := new.vin;
	custUsername := new.username;

	update offers set status_id = 2 where offer_id != offerId and vin = vinNum and status_id = 3 and username != custUsername;

	raise info 'Updating offerings';

return new;
end;
$$ language plpgsql;

create trigger offer_update
	after update on offers
	for each row execute procedure update_offer();


create or replace function insert_car (carVin varchar, carMake varchar, carModel varchar, carPrice float) returns boolean as $$
	begin
		INSERT INTO public.cars (vin, make, model, price, is_sold, active)
		VALUES(carVin, carMake, carModel, carPrice, false, true);


		raise info 'Car added';
		return true;
	exception
  		when others then
   		raise info 'Rolling back transaction';
		rollback;

	return false;
end; $$ language 'plpgsql';

-- Insert customers
INSERT INTO users (username,password,first_name,last_name,type) 
VALUES ('micahg','s3cret','Micah','Garner','Customer'),
	   ('pandoraj','s3cret','Pandora','Jimenez','Customer'),
	   ('vaughanh','s3cret','Vaughan','Hill','Customer'),
	   ('lanab','s3cret','Lana','Bowman','Customer'),
	   ('salvadorb','s3cret','Salvador','Buchanan','Customer');

INSERT INTO public.customers (username)
VALUES('micahg'),
	  ('pandoraj'),
	  ('vaughanh'),
	  ('lanab'),
	  ('salvadorb');

	 
-- Insert employees
INSERT INTO users (username,password,first_name,last_name,type)
VALUES ('neroh','s3cret','Nero','Hull','Employee'),
	   ('libertye','s3cret','Liberty','Ellis','Employee'),
	   ('tobiasg','s3cret','Tobias','Gordon','Employee');

INSERT INTO public.employees (username)
VALUES('neroh'),
	  ('libertye'),
	  ('tobiasg');
	  
-- Insert cars
insert into cars (vin, make, model, price, is_sold, active) 
values  ('111', 'Nissan', 'Altima', 23000.00, false, true),
		('222', 'Nissan', 'Rogue', 35000.00, true, true),
		('333', 'Ford', 'Mustang', 34000.00, false, true),
		('444', 'Ford', 'Fusion', 20000.00, false, true),
		('555', 'Chevy', 'Silverado', 37000.00, false, true),
		('666', 'Toyota', 'Camry', 24000.00, false, true),
		('777', 'Toyota', 'Corolla', 15000.00, false, true),
		('888', 'Chevy', 'Malibu', 21000.00, false, true),
		('999', 'Toyota', 'Camry', 22000.00, false, true);
	 
-- Insert offerstatus
INSERT INTO public.offerstatus (status_id, "name") VALUES(1, 'ACCEPTED');
INSERT INTO public.offerstatus (status_id, "name") VALUES(2, 'REJECTED');
INSERT INTO public.offerstatus (status_id, "name") VALUES(3, 'PENDING');

--('micahg'),
--	  ('pandoraj'),
--	  ('vaughanh'),
--	  ('lanab'),
--	  ('salvadorb');
-- Insert offers
INSERT INTO public.offers
(offer_id, signed_date, amount, total_payment, payments_made, monthly_payment, username, vin, approved_by, status_id)
VALUES('111', '2019-06-14', 20000, 0, 0, 0, 'micahg', '111', null, 3);
INSERT INTO public.offers
(offer_id, signed_date, amount, total_payment, payments_made, monthly_payment, username, vin, approved_by, status_id)
VALUES('222', '2019-06-14', 20100, 0, 0, 0, 'pandoraj', '111', null, 3);
INSERT INTO public.offers
(offer_id, signed_date, amount, total_payment, payments_made, monthly_payment, username, vin, approved_by, status_id)
VALUES('333', '2019-06-14', 29000, 48, 4, 604.16, 'salvadorb', '222', null, 1);
INSERT INTO public.offers
(offer_id, signed_date, amount, total_payment, payments_made, monthly_payment, username, vin, approved_by, status_id)
VALUES('444', '2019-06-17', 15000, 0, 0, 0, 'salvadorb', '444', null, 3);
INSERT INTO public.offers
(offer_id, signed_date, amount, total_payment, payments_made, monthly_payment, username, vin, approved_by, status_id)
VALUES('555', '2019-06-17', 14000, 0, 0, 0, 'pandoraj', '444', null, 3);


-- Update username in cars which offers were accepted
update cars set username = 'salvadorb' where vin = '222';

-- Insert payments

INSERT INTO public.payments
(payment_id, paid_date, amount_paid, vin)
VALUES(1111, '2019-06-14', 604.16, '222');
INSERT INTO public.payments
(payment_id, paid_date, amount_paid, vin)
VALUES(2222, '2019-06-15', 604.16, '222');
INSERT INTO public.payments
(payment_id, paid_date, amount_paid, vin)
VALUES(3333, '2019-06-16', 604.16, '222');
INSERT INTO public.payments
(payment_id, paid_date, amount_paid, vin)
VALUES(4444, '2019-06-17', 604.16, '222');