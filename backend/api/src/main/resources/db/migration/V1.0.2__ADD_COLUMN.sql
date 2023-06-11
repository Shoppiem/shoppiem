ALTER TABLE public.product ADD COLUMN has_embedding boolean NOT NULL default false;
ALTER TABLE public.review ADD COLUMN has_embedding boolean NOT NULL default false;
ALTER TABLE public.product_question ADD COLUMN has_embedding boolean NOT NULL default false;
ALTER TABLE public.product_answer ADD COLUMN has_embedding boolean NOT NULL default false;
ALTER TABLE public.embedding
    ADD COLUMN text varchar NOT NULL,
    ADD COLUMN question_id bigint,
    ADD COLUMN answer_id bigint;