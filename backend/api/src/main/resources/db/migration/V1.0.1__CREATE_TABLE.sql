CREATE SCHEMA IF NOT EXISTS public;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "vector";

CREATE SEQUENCE IF NOT EXISTS pk_sequence_product
    start 1
  increment 1;

CREATE SEQUENCE IF NOT EXISTS pk_sequence_user
    start 1
  increment 1;

CREATE SEQUENCE IF NOT EXISTS pk_sequence_review
    start 1
  increment 1;

CREATE SEQUENCE IF NOT EXISTS pk_sequence_embedding
    start 1
  increment 1;

CREATE SEQUENCE IF NOT EXISTS pk_sequence_feedback
    start 1
  increment 1;

CREATE SEQUENCE IF NOT EXISTS pk_sequence_product_question
    start 1
  increment 1;

CREATE SEQUENCE IF NOT EXISTS pk_sequence_product_answer
    start 1
  increment 1;

CREATE TABLE IF NOT EXISTS public.user (
    id BIGSERIAL NOT NULL primary key,
    uid varchar(255),
    full_name varchar(255),
    first_name varchar(128),
    last_name varchar(128),
    email varchar(255),
    roles varchar(255)[],
    is_premium_user boolean not null default false,
    updated_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(uid)
);
ALTER TABLE public.user OWNER TO root;
comment on table public.user is 'Basic user information';

CREATE TABLE IF NOT EXISTS public.product (
    id BIGSERIAL NOT NULL primary key,
    product_sku varchar(255) not null,
    title varchar(255),
    seller varchar(255),
    product_url varchar(255),
    image_url varchar,
    description varchar,
    price numeric(8,2),
    currency varchar(8),
    num_reviews bigint default 0,
    num_questions_answered bigint default 0,
    star_rating numeric(2,1) not null default 0.0,
    updated_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(product_sku)
);
ALTER TABLE public.product OWNER TO root;

CREATE TABLE IF NOT EXISTS public.review (
    id BIGSERIAL NOT NULL,
    product_id bigint not null constraint product_id_fk references public.product (id) on delete cascade,
    title varchar(255),
    review_id varchar(255),
    merchant varchar(255),
    location varchar(255),
    verified_purchase boolean,
    likes bigint,
    star_rating bigint,
    reviewer_name varchar(255),
    reviewer_handle varchar(255),
    body varchar,
    submitted_at timestamptz,
    updated_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(review_id, product_id)
  );
ALTER TABLE public.review OWNER TO root;

CREATE TABLE IF NOT EXISTS public.product_question (
    id BIGSERIAL NOT NULL,
    product_id bigint not null constraint product_id_fk references public.product (id) on delete cascade,
    question_id varchar(255),
    question varchar not null,
    num_answers bigint,
    asked_at timestamptz,
    updated_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(question_id)
);
ALTER TABLE public.product_question OWNER TO root;

CREATE TABLE IF NOT EXISTS public.product_answer (
    id BIGSERIAL NOT NULL,
    product_id bigint not null constraint product_id_fk references public.product (id) on delete cascade,
    product_question_id bigint not null,
    answer varchar,
    answered_by_handle varchar(255),
    answered_by_url varchar(255),
    upvotes bigint,
    answered_at timestamptz,
    updated_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(product_question_id)
    );
ALTER TABLE public.product_answer OWNER TO root;




CREATE TABLE IF NOT EXISTS public.embedding (
    id BIGSERIAL NOT NULL,
    review_id bigint,
    product_id bigint not null,
    embedding vector(1536),
    updated_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP
);
ALTER TABLE public.embedding OWNER TO root;
comment on table public.embedding is 'Sentences embeddings';

CREATE TABLE IF NOT EXISTS public.feedback (
     id BIGSERIAL PRIMARY KEY NOT NULL,
     user_id bigint NOT NULL,
     subject varchar(255),
     body varchar,
     updated_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
     created_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP
);
ALTER TABLE public.feedback OWNER TO root;