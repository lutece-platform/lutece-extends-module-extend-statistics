--
-- Init  table core_admin_right
--
INSERT INTO core_admin_right (id_right,name,level_right,admin_url,description,is_updatable,plugin_name,id_feature_group,icon_url, documentation_url) 
VALUES ('SOCIALHUB_STATISTICS','module.socialhub.statistics.adminFeature.statistics.name',2,'jsp/admin/plugins/socialhub/modules/statistics/ViewStats.jsp','module.socialhub.statistics.adminFeature.statistics.description',0,'socialhub-statistics','CONTENT','images/admin/skin/plugins/socialhub/modules/statistics/socialhub-statistics.png', 'jsp/admin/documentation/AdminDocumentation.jsp?doc=admin-socialhub-statistics');

--
-- Init  table core_user_right
--
INSERT INTO core_user_right (id_right,id_user) VALUES ('SOCIALHUB_STATISTICS',1);
INSERT INTO core_user_right (id_right,id_user) VALUES ('SOCIALHUB_STATISTICS',2);
