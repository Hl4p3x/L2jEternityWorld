CREATE TABLE IF NOT EXISTS `character_advent_bonus` (
  `charId` int(10) unsigned NOT NULL,
  `advent_time` int(10) unsigned NOT NULL DEFAULT '0',
  `advent_points` int(10) unsigned NOT NULL DEFAULT '0',
  UNIQUE KEY `charId` (`charId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
