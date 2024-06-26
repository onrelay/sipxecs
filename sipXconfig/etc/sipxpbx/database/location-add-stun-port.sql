alter table location drop if exists column stun_port;
alter table location add column stun_port integer default 3478;


