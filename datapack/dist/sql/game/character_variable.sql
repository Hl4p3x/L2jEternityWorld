CREATE TABLE IF NOT EXISTS `character_variable` (
  `charId` int(10) UNSIGNED NOT NULL,
  `var` varchar(255) NOT NULL,
  `val` text NOT NULL 
) ENGINE=InnoDB DEFAULT CHARSET=utf8;