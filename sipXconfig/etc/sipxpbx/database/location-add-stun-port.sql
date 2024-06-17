do $$ 
    begin
        begin
            alter table location add column stun_port integer;
            update location set stun_port=3478;
            alter table location alter column stun_port set not null;
            alter table location alter column stun_port set default 3478;
        exception
            when duplicate_column then raise notice 'column stun_port already exists in table location.';
        end;
    end;
$$



