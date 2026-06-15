create table student
(
    id uuid not null
        constraint student_pk primary key,
    iin varchar(12) not null,
    first_name varchar(255) not null,
    last_name varchar(255) not null,
    phone varchar(255) not null,
    created_at timestamp without time zone not null,
    updated_at timestamp without time zone not null,
    constraint student_iin_digits_check check (iin ~ '^[0-9]{12}$')
);

create unique index student_iin_uindex on student (iin);
create index student_last_name_index on student (last_name);

create table exam_reservation
(
    id uuid not null
        constraint exam_reservation_pk primary key,
    student_id uuid not null,
    exam_type varchar(32) not null,
    exam_date_time timestamp without time zone not null,
    status varchar(32) not null,
    created_at timestamp without time zone not null,
    updated_at timestamp without time zone not null,
    constraint exam_reservation_student_fk
        foreign key (student_id) references student (id) on delete cascade,
    constraint exam_reservation_exam_type_check
        check (exam_type in ('THEORY', 'PRACTICE')),
    constraint exam_reservation_status_check
        check (status in ('ACTIVE', 'CANCELLED', 'COMPLETED'))
);

create index exam_reservation_student_id_index on exam_reservation (student_id);
create index exam_reservation_status_index on exam_reservation (status);
create index exam_reservation_exam_date_time_index on exam_reservation (exam_date_time);
create unique index exam_reservation_one_active_per_student_uindex
    on exam_reservation (student_id)
    where status = 'ACTIVE';
