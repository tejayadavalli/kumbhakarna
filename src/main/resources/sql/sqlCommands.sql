CREATE TABLE room_status (
    room text NOT NULL PRIMARY KEY,
    status text  NOT NULL,
    linked_check_in text
);

Alter table room_status add column meta text;

Create table guest(
    phone text PRIMARY KEY,
    name text
);

Alter table guest add column check_in_sms bigint;
Alter table guest add column check_out_sms bigint;

Create Table check_in(
    id text PRIMARY KEY,
    phone text,
    room text,
    guest_count int,
    extra_bed boolean,
    plan text,
    tariff int,
    remark text,
    check_in_time text,
    check_out_time text,
    FOREIGN KEY(phone)
REFERENCES guest(phone)
);



CREATE INDEX ON check_in(check_in_time);
CREATE INDEX ON check_in(check_out_time);

Alter table check_in add column days int;
Alter table check_in add column room_bill int;
Alter table check_in add column food_bill int;
Alter table check_in add column cash int;
Alter table check_in add column card int;
Alter table check_in add column upi int;
Alter table check_in add column ingommt int;
Alter table check_in add column gst_number text;
Alter table check_in add column s3_bill_key text;
Alter table check_in add column bill_number text;
Alter table check_in add column is_deleted boolean default false;
Alter table check_in add column water_bottles int;
Alter table check_in add column combos int;
Alter table check_in add column cooldrinks int;



Create Table bookings(
    id text PRIMARY KEY,
    phone text,
    name text,
    d_rooms int,
    d_tariff int,
    e_rooms int,
    e_tariff int,
    sp_rooms int,
    sp_tariff int,
    b_rooms int,
    b_tariff int,
    su_rooms int,
    su_tariff int,
    days int,
    plan text,
    extra_bed boolean,
    mode text,
    check_in_time text,
    advance int,
    remark text
);

CREATE INDEX ON bookings(check_in_time);
Alter table bookings add  column deleted boolean default false;

create table cron_job (
    id text PRIMARY KEY,
    assigned_to  text,
    cron_expression text,
    flock_message text,
    portal_message text,
    differentiate_rooms boolean
);

create table tasks(
    id text PRIMARY KEY,
    task_info text,
    created_at text,
    task_status  text,
    assigned_to  text,
    remark     text,
    updated_at  text
);

CREATE INDEX ON tasks(task_status);

CREATE FUNCTION delete_tasks() RETURNS trigger AS $emp_stamp$
$emp_stamp$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION delete_tasks() RETURNS TRIGGER AS $_$
BEGIN
            Delete from tasks where task_status != 'CREATED';
    RETURN OLD;
END $_$ LANGUAGE 'plpgsql';


CREATE TRIGGER emp_stamp After INSERT ON tasks
FOR EACH ROW EXECUTE PROCEDURE delete_tasks();


create table cash_out(
id text PRIMARY KEY,
date text,
amount int
);

CREATE INDEX ON cash_out(date);
