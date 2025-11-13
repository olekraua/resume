CREATE TABLE account(
    id    integer                NOT NULL,
    name  character varying(60)  NOT NULL,
    email character varying(100) NOT NULL
)
WITH (oids = false);

CREATE TABLE category(
    id            integer               NOT NULL,
    name          character varying(60) NOT NULL,
    url           character varying(60) NOT NULL,
    product_count integer DEFAULT 0     NOT NULL
)
WITH (oids = false);

CREATE TABLE "order"(
    id         bigint                                       NOT NULL,
    id_account integer                                      NOT NULL,
    created    timestamp(0) without time zone DEFAULT now() NOT NULL
)
WITH (oids = false);

CREATE TABLE order_item(
    id         bigint  NOT NULL,
    id_order   bigint  NOT NULL,
    id_product integer NOT NULL,
    count      integer NOT NULL
)
WITH (oids = false);

CREATE TABLE producer(
    id            integer               NOT NULL,
    name          character varying(60) NOT NULL,
    product_count integer DEFAULT 0     NOT NULL
)
WITH (oids = false);

CREATE TABLE product(
    id          integer                NOT NULL,
    name        character varying(255) NOT NULL,
    description text                   NOT NULL,
    image_link  character varying(255) NOT NULL,
    price       numeric(8, 2)          NOT NULL,
    id_category integer                NOT NULL,
    id_producer integer                NOT NULL
)
WITH (oids = false);

CREATE SEQUENCE order_item_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE SEQUENCE order_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE SEQUENCE account_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER TABLE ONLY account
    ADD CONSTRAINT account_email_key UNIQUE (email);

ALTER TABLE ONLY account
    ADD CONSTRAINT account_pkey PRIMARY KEY (id);

ALTER TABLE ONLY category
    ADD CONSTRAINT category_pkey PRIMARY KEY (id);

ALTER TABLE ONLY category
    ADD CONSTRAINT category_url_key UNIQUE (url);

ALTER TABLE ONLY order_item
    ADD CONSTRAINT order_item_pkey PRIMARY KEY (id);

ALTER TABLE ONLY "order"
    ADD CONSTRAINT order_pkey PRIMARY KEY (id);

ALTER TABLE ONLY producer
    ADD CONSTRAINT producer_pkey PRIMARY KEY (id);

ALTER TABLE ONLY product
    ADD CONSTRAINT product_pkey PRIMARY KEY (id);

ALTER TABLE ONLY "order"
    ADD CONSTRAINT order_fk FOREIGN KEY (id_account) REFERENCES account (id) ON UPDATE CASCADE ON DELETE RESTRICT;

ALTER TABLE ONLY order_item
    ADD CONSTRAINT order_item_fk FOREIGN KEY (id_order) REFERENCES "order" (id) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE ONLY order_item
    ADD CONSTRAINT order_item_fk1 FOREIGN KEY (id_product) REFERENCES product (id) ON UPDATE CASCADE ON DELETE RESTRICT;

ALTER TABLE ONLY product
    ADD CONSTRAINT product_fk FOREIGN KEY (id_category) REFERENCES category (id) ON UPDATE CASCADE ON DELETE RESTRICT;

ALTER TABLE ONLY product
    ADD CONSTRAINT product_fk1 FOREIGN KEY (id_producer) REFERENCES producer (id) ON UPDATE CASCADE ON DELETE RESTRICT;