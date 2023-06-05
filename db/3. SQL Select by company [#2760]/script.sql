CREATE TABLE company
(
    id integer NOT NULL,
    name character varying,
    CONSTRAINT company_pkey PRIMARY KEY (id)
);

CREATE TABLE person
(
    id integer NOT NULL,
    name character varying,
    company_id integer references company(id),
    CONSTRAINT person_pkey PRIMARY KEY (id)
);


insert into company(id, name) values(1,'company_1');
insert into company(id, name) values(2,'company_2');
insert into company(id, name) values(3,'company_3');
insert into company(id, name) values(4,'company_4');
insert into company(id, name) values(5,'company_5');

insert into person(id, name, company_id) values(1, 'Ivanov Ivan', 1);
insert into person(id, name, company_id) values(2, 'Petrov Igor', 1);

insert into person(id, name, company_id) values(3, 'Stenkin Oleg', 2);
insert into person(id, name, company_id) values(4, 'Schors Matvey', 2 );

insert into person(id, name, company_id) values(5, 'Sidorov Igor', 3);
insert into person(id, name, company_id) values(6, 'Kshnyakin Maksim', 3);

insert into person(id, name, company_id) values(7, 'Jhon Jhonson', 4);
insert into person(id, name, company_id) values(8, 'Gzyba Andrey', 4);
insert into person(id, name, company_id) values(12, 'Zotin Ilya', 4);

insert into person(id, name, company_id) values(9, 'Malinin Nikita', 5);
insert into person(id, name, company_id) values(10, 'Driz Pavel', 5);
insert into person(id, name, company_id) values(11, 'Pyatkin Ilya', 5);

------------Ex1-----------------------------------------------------------------------------------------

select name from person where company_id != 5;
select p.name as Сотрудник, c.name as Компания from person as p join company as c on p.company_id = c.id;
--------------------------------------------------------------------------------------------------------


------------Ex2-----------------------------------------------------------------------------------------

select c.name as Компания, count(p.company_id) as "Количество сотрудников"
from person as p 
join company as c
on p.company_id  = c.id
group by c.name
having count(p.company_id)=(
select max(person_count) from(
select c.name, count(p.company_id) as person_count
from person as p 
join company as c
on p.company_id  = c.id
group by c.name) as max);

--------------------------------------------------------------------------------------------------------
