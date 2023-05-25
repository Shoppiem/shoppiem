CREATE SEQUENCE IF NOT EXISTS pk_email_campaign
    start 1
  increment 1;

CREATE SEQUENCE IF NOT EXISTS pk_email_tracking
    start 1
  increment 1;

CREATE TABLE IF NOT EXISTS public.email_campaign (
    id BIGSERIAL NOT NULL PRIMARY KEY,
    subject varchar(255),
    template varchar,
    campaign_id varchar(255) NOT NULL,
    num_emails_sent bigint NOT NULL default 0,
    personalized_by_name boolean NOT NULL default false,
    personalized_by_other boolean NOT NULL default false,
    updated_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(campaign_id)
);
ALTER TABLE public.email_campaign OWNER TO root;
comment on table public.email_campaign is 'Email Campaign';

CREATE TABLE IF NOT EXISTS public.email_tracking (
    id BIGSERIAL NOT NULL PRIMARY KEY,
    campaign_id varchar(255) not null constraint campaign_id_fk references public.email_campaign (campaign_id),
    email varchar(255),
    num_opened bigint NOT NULL DEFAULT 0,
    ip_address varchar(255),
    updated_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(email, campaign_id)
);
ALTER TABLE public.email_tracking OWNER TO root;
comment on table public.email_tracking is 'Email tracking data';


