CREATE SEQUENCE IF NOT EXISTS pk_leads
    start 1
  increment 1;

CREATE TABLE IF NOT EXISTS public.leads (
    id BIGSERIAL NOT NULL PRIMARY KEY,
    first_name varchar(255),
    last_name varchar(255),
    email varchar(256),
    description varchar,
    keywords varchar,
    channel_url varchar(255),
    channel_name varchar(255),
    channel_handle varchar(255),
    subscribers varchar(255),
    subscribers_value bigint,
    facebook varchar(255),
    instagram varchar(255),
    twitter varchar(255),
    website varchar(255),
    blog varchar(255),
    snap_chat varchar(255),
    discord varchar(255),
    tiktok varchar(255),
    pinterest varchar(255),
    query varchar(255) NOT NULL,
    updated_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(email)
);
ALTER TABLE public.leads OWNER TO root;
comment on table public.leads is 'Leads for user acquisition';
