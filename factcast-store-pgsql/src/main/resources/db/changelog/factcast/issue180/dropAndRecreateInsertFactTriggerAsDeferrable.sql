--liquibase formatted sql
--changeset usr:issue180
DROP TRIGGER IF EXISTS tr_fact_insert on fact;
CREATE CONSTRAINT TRIGGER tr_deferred_fact_insert AFTER INSERT ON fact DEFERRABLE FOR EACH ROW EXECUTE PROCEDURE notifyFactInsert();

