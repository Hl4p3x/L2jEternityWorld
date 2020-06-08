DROP TABLE IF EXISTS `character_schemes`;
CREATE TABLE `character_schemes` (
  `ownerId` int(10) unsigned NOT NULL DEFAULT '0',
  `id` int(10) unsigned NOT NULL DEFAULT '0',
  `level` int(10) unsigned NOT NULL DEFAULT '0',
  `scheme` varchar(20) CHARACTER SET utf8 NOT NULL DEFAULT 'default'
) ENGINE=MyISAM DEFAULT CHARSET=utf8;