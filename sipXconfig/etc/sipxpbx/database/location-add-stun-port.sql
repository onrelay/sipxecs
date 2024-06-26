alter table location add column stun_port integer;
update location set stun_port=3478;
alter table location alter column stun_port set not null;
alter table location alter column stun_port set default 5061;
