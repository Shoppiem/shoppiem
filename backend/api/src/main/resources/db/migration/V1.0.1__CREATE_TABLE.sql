CREATE SCHEMA IF NOT EXISTS public;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE SEQUENCE IF NOT EXISTS pk_sequence_project
    start 1
  increment 1;

CREATE SEQUENCE IF NOT EXISTS pk_sequence_sampled_image
    start 1
  increment 1;

CREATE SEQUENCE IF NOT EXISTS pk_sequence_media_content
    start 1
  increment 1;
 
CREATE SEQUENCE IF NOT EXISTS pk_sequence_user
    start 1
  increment 1;

CREATE SEQUENCE IF NOT EXISTS pk_sequence_cookie_jar
    start 1
  increment 1;


CREATE TABLE IF NOT EXISTS public.user (
    firebase_id varchar(255) NOT NULL PRIMARY KEY,
    full_name varchar(255),
    first_name varchar(128),
    last_name varchar(128),
    email varchar(255),
    roles varchar(255)[],
    is_premium_user boolean not null default false,
    updated_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(firebase_id)
);
ALTER TABLE public.user OWNER TO root;
comment on table public.user is 'Basic user information';


CREATE TABLE IF NOT EXISTS public.project (
    id BIGSERIAL NOT NULL constraint project_id_fk primary key,
    user_id varchar(255) not null constraint user_id_fk references public.user (firebase_id),
    content varchar,
    project_name varchar not null default 'Untitled',
    content_link varchar,
    paraphrase boolean not null default true,
    embed_images boolean not null default false,
    content_type varchar(255),
    processed boolean not null default false,
    failed boolean not null default false,
    reason_failed varchar(255),
    updated_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP
);
ALTER TABLE public.project OWNER TO root;
comment on table public.project is 'User project';


CREATE TABLE IF NOT EXISTS public.sampled_image (
  id BIGSERIAL NOT NULL,
  project_id bigint not null constraint project_id_fk references public.project (id) on delete cascade,
  image_key varchar(255),
  updated_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
  created_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP
);
ALTER TABLE public.sampled_image OWNER TO root;
comment on table public.sampled_image is 'Image sampled from video frames';

CREATE TABLE IF NOT EXISTS public.media_content (
    id BIGSERIAL NOT NULL,
    project_id bigint not null constraint project_id_fk references public.project (id) on delete cascade,
    media_id varchar(255) not null,
    scraped_title varchar,
    scraped_description varchar,
    raw_transcript varchar,
    permalink varchar,
    on_screen_text varchar,
    creator_handle varchar,
    is_audio boolean not null default false,
    updated_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(media_id, project_id)
  );
ALTER TABLE public.media_content OWNER TO root;
comment on table public.media_content is 'Audio/video metadata';

CREATE TABLE IF NOT EXISTS public.cookie_jar (
     id BIGSERIAL PRIMARY KEY NOT NULL,
     c_key varchar,
     c_path varchar,
     c_domain varchar,
     c_name varchar,
     c_value varchar,
     c_username varchar,
     c_host_only boolean,
     c_http_only boolean,
     c_persistent boolean,
     c_secure boolean,
     c_expires_at bigint,
     updated_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
     created_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP
);
ALTER TABLE public.cookie_jar OWNER TO root;
comment on table public.cookie_jar is 'Persists request cookies';

-- Create table indices
CREATE INDEX IF NOT EXISTS cookie_jar_idx
    ON cookie_jar (c_key, c_username);
