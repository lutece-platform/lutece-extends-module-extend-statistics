-- liquibase formatted sql
-- changeset extend-statistics:update_db_extend_statistics-2.0.0-2.0.1.sql
-- preconditions onFail:MARK_RAN onError:WARN
-- precondition-sql-check expectedResult:1 SELECT COUNT(*) FROM core_admin_right WHERE id_right = 'EXTEND_STATISTICS' AND icon_url = 'images/admin/skin/plugins/extend/modules/statistics/extend-statistics.png'
UPDATE core_admin_right SET icon_url = 'ti ti-chart-bar' WHERE id_right = 'EXTEND_STATISTICS' AND icon_url = 'images/admin/skin/plugins/extend/modules/statistics/extend-statistics.png';
