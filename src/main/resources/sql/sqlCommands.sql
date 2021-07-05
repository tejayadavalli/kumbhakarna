CREATE TABLE room_status (
    room text NOT NULL PRIMARY KEY,
    status text  NOT NULL,
    linked_check_in text
);

Create table guest(
    phone text PRIMARY KEY,
    name text
);

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

cd

CREATE INDEX ON check_in(check_in_time);
CREATE INDEX ON check_in(check_out_time);

Alter table check_in add column days int;
Alter table check_in add column room_bill int;
Alter table check_in add column food_bill int;
