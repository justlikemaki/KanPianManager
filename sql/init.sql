DROP DATABASE kanpian;
CREATE DATABASE kanpian
      WITH
      OWNER = postgres
      ENCODING = 'UTF8'
      LC_COLLATE = 'en_US.utf8'
      LC_CTYPE = 'en_US.utf8'
      TABLESPACE = pg_default
      template = template0
      CONNECTION LIMIT = -1;

use kanpian;       
       
CREATE TABLE public.errpage (
	id varchar(30) NOT NULL,
	"type" varchar(30) NULL,
	num varchar(30) NULL,
	errmsg text NULL,
	searchkey varchar(255) NULL,
	createtime timestamp NULL DEFAULT now(),
	CONSTRAINT errpage_pkey PRIMARY KEY (id)
)
WITH (
	OIDS=FALSE
) ;

CREATE TABLE public.javimg (
	id varchar(30) NOT NULL,
	srcid varchar(30) NULL,
	imgbase text NULL,
	CONSTRAINT javimg_pkey PRIMARY KEY (id)
)
WITH (
	OIDS=FALSE
) ;
CREATE INDEX imgsrcid ON public.javimg (srcid DESC) ;

-- DROP SEQUENCE public.javsrc__id__seq;

CREATE SEQUENCE public.javsrc__id__seq
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 9223372036854775807
	START 1;

-- Permissions

ALTER SEQUENCE public.javsrc__id__seq OWNER TO postgres;
GRANT ALL ON SEQUENCE public.javsrc__id__seq TO postgres;

CREATE TABLE public.javsrc (
	id int4 NOT NULL DEFAULT nextval('javsrc__id__seq'::regclass),
	mgid varchar(30) NOT NULL,
	title text NULL,
	times varchar(30) NULL,
	imgsrc text NULL,
	tabtype varchar(30) NULL,
	isdown varchar(1) NULL,
	tags text NULL,
	btfile text NULL,
	btname text NULL,
	isstar varchar(1) NULL,
	sbm text NULL,
	CONSTRAINT javsrc_pkey PRIMARY KEY ("_id_")
)
WITH (
	OIDS=FALSE
) ;
CREATE UNIQUE INDEX javsrc_id_tags_idx ON public.javsrc (id DESC) ;
CREATE INDEX javsrc_sbm_idx ON public.javsrc (sbm DESC) ;
CREATE INDEX javsrc_times_idx ON public.javsrc (times DESC) ;
CREATE INDEX javsrc_title_tags_idx ON public.javsrc (title DESC,tags DESC) ;

CREATE TABLE public.javtor (
	id varchar(30) NOT NULL,
	srcid varchar(30) NULL,
	torbase text NULL,
	CONSTRAINT javtor_pkey PRIMARY KEY (id)
)
WITH (
	OIDS=FALSE
) ;
CREATE INDEX torsrcid ON public.javtor (srcid DESC) ;