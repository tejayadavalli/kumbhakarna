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

CREATE INDEX ON check_in(check_in_time);
CREATE INDEX ON check_in(check_out_time);