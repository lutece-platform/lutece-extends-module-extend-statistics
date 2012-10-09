--
-- Init  table core_admin_right
--
INSERT INTO core_admin_right (id_right,name,level_right,admin_url,description,is_updatable,plugin_name,id_feature_group,icon_url, documentation_url) 
VALUES ('EXTEND_STATISTICS','module.extend.statistics.adminFeature.statistics.name',2,'jsp/admin/plugins/extend/modules/statistics/ViewStats.jsp','module.extend.statistics.adminFeature.statistics.description',0,'extend-statistics','CONTENT','images/admin/skin/plugins/extend/modules/statistics/extend-statistics.png', 'jsp/admin/documentation/AdminDocumentation.jsp?doc=admin-extend-statistics');

--
-- Init  table core_user_right
--
INSERT INTO core_user_right (id_right,id_user) VALUES ('EXTEND_STATISTICS',1);
INSERT INTO core_user_right (id_right,id_user) VALUES ('EXTEND_STATISTICS',2);
