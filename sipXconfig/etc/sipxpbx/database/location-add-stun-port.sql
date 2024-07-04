alter table location drop column if exists stun_port;
alter table location add column stun_port integer default 3478;
