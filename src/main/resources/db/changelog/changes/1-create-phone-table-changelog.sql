--liquibase formatted sql

--changeset naveen:1
CREATE TABLE phone (
  id BIGINT PRIMARY KEY,
  name VARCHAR(50) NOT NULL,
  available BOOLEAN NOT NULL DEFAULT TRUE,
  booked_on DATE,
  booked_by VARCHAR(50)
);
CREATE SEQUENCE "PHONE_SEQUENCE" MINVALUE 1 MAXVALUE 999999999 INCREMENT BY 1 START WITH 1 NOCACHE NOCYCLE;