-- FUNCTION: public.test$arrayby(character varying[])

-- DROP FUNCTION IF EXISTS public."test$arrayby"(character varying[]);

CREATE TABLE IF NOT EXISTS public.craken_tblc
(
    wsname character varying NOT NULL,
    fqn character varying NOT NULL,
    jdata jsonb,
	parent character varying,
    CONSTRAINT craken_tblc_pkey PRIMARY KEY (wsname, fqn)
)



CREATE OR REPLACE FUNCTION public.craken$existBy(v_wsname varchar, v_fqn varchar)
    RETURNS refcursor
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
AS $BODY$
	DECLARE 
		rtn_cursor refcursor := 'rcursor';
	BEGIN
		OPEN rtn_cursor FOR
		Select fqn from craken_tblc where wsname = v_wsname and fqn = v_fqn 
		Union all
		Select fqn from craken_tblc where wsname = v_wsname and fqn like v_fqn || '%' limit 1 ;
		
		return rtn_cursor; 
	END 
$BODY$;


CREATE OR REPLACE FUNCTION public.craken$struBy(v_wsname varchar, v_fqn varchar)
    RETURNS refcursor
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
AS $BODY$
	DECLARE 
		rtn_cursor refcursor := 'rcursor';
	BEGIN
		OPEN rtn_cursor FOR
		Select fqn from craken_tblc where wsname = v_wsname and parent = v_fqn  ;
		
		return rtn_cursor; 
	END 
$BODY$;



CREATE OR REPLACE FUNCTION public.craken$childDataBy(v_wsname varchar, v_fqn varchar)
    RETURNS refcursor
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
AS $BODY$
	DECLARE 
		rtn_cursor refcursor := 'rcursor';
	BEGIN
		OPEN rtn_cursor FOR
		Select fqn, jdata from craken_tblc where wsname = v_wsname and parent = v_fqn  ;
		
		return rtn_cursor; 
	END 
$BODY$;



select * from craken_tblc where wsname = 'testworkspace'




CREATE OR REPLACE FUNCTION public.craken$dataBy(v_wsname varchar, v_fqn varchar)
    RETURNS refcursor
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
AS $BODY$
	DECLARE 
		rtn_cursor refcursor := 'rcursor';
	BEGIN
		OPEN rtn_cursor FOR
		Select util$nvl(max(jdata::varchar), '{}') as jdata
		from craken_tblc where wsname = v_wsname and fqn = v_fqn ;
		
		return rtn_cursor; 
	END 
$BODY$;


select craken$existBy('testworkspace', '/emp') ;
fetch all from rcursor

select * from craken_tblc


CREATE OR REPLACE FUNCTION public.craken$existBy(v_wsname varchar, v_fqn varchar)
    RETURNS refcursor
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
AS $BODY$
	DECLARE 
		rtn_cursor refcursor := 'rcursor';
	BEGIN
		OPEN rtn_cursor FOR
		Select jdata from craken_tblc where wsname = v_wsname and fqn = v_fqn  ;
		
		return rtn_cursor; 
	END 
$BODY$;



CREATE OR REPLACE FUNCTION public.craken$dataWith(v_wsname varchar, v_fqn varchar, v_jdata varchar, v_parent varchar)
    RETURNS integer
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
AS $BODY$
	BEGIN
		with upset as (update craken_tblc set jdata = v_jdata::jsonb where wsname = v_wsname and fqn = v_fqn returning *)
		insert into craken_tblc(wsname, fqn, jdata, parent)
		select v_wsname, v_fqn, v_jdata::jsonb, v_parent
		where not exists(select * from upset) ;
		
		insert into craken_tblc(wsname, fqn, jdata, parent)
		select v_wsname, fqn, '{}'::jsonb, COALESCE(lead(fqn) over(), '*') as parent
		from (
		SELECT util$nvl(array_to_string(trim_array(string_to_array(v_fqn, '/'), (row_number() over())::integer - 1), '/'), '/') fqn
		from (select unnest(string_to_array(v_fqn, '/')) ele) b
			) b
		where not exists(select 1 from craken_tblc where fqn = b.fqn) ;
		
		return 1; 
	END 
$BODY$;



CREATE OR REPLACE FUNCTION public.craken$removeSelfWith(v_wsname varchar, v_fqn varchar)
    RETURNS integer
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
AS $BODY$
	BEGIN
		delete from craken_tblc where wsname = v_wsname and fqn = v_fqn ;
		
		return 1; 
	END 
$BODY$;


CREATE OR REPLACE FUNCTION public.craken$removeChildWith(v_wsname varchar, v_fqn varchar)
    RETURNS integer
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
AS $BODY$
	DECLARE
		affectedRow integer ;
	BEGIN
		delete from craken_tblc where wsname = v_wsname and parent = v_fqn  ;
		
		GET DIAGNOSTICS affectedRow = row_count ;
		return affectedRow; 
	END 
$BODY$;

CREATE OR REPLACE FUNCTION public.util$nvl(character varying, character varying DEFAULT ''::character varying)
 RETURNS character varying
 LANGUAGE plpgsql
 IMMUTABLE COST 1
AS $function$
	BEGIN
		return case when $1 is null then $2 when $1 = '' then $2 else $1 end ;
	END $function$
;

select craken$struby('testworkspace', '/') ;
fetch all from rcursor ;

select craken$dataWith('my', '/', '{}')


select craken$existby('my', '/') ;
fetch all from rcursor ;

select fqn, COALESCE(lead(fqn) over(), '*') as parent
from (
SELECT util$nvl(array_to_string(trim_array(string_to_array('/a/b/a/d', '/'), (row_number() over())::integer - 1), '/'), '/') fqn
from (select unnest(string_to_array('/a/b/a/d', '/')) ele) b
	) b
where not exists(select 1 from craken_tblc where fqn = b.fqn)
	
select * from craken_tblc where parent like '/dscripts%'

delete from craken_tblc
	
create SEQUENCE test_seq

select nextval('test_seq')