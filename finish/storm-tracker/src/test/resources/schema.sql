create sequence storms_id_generator start 10 increment 1;

create table storms (id int8 not null, end_date varchar(255), end_location varchar(255), intensity int4 not null, start_date varchar(255), start_location varchar(255), type varchar(255), primary key (id));