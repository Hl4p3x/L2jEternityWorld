CREATE TABLE IF NOT EXISTS `siegable_clanhall` (
  `id` int(10) NOT NULL DEFAULT '0',
  `name` varchar(45) DEFAULT NULL,
  `ownerId` int(10) DEFAULT NULL,
  `desc` varchar(100) DEFAULT NULL,
  `location` varchar(100) DEFAULT NULL,
  `Grade` decimal(1,0) NOT NULL default '0',
  `nextSiege` bigint(20) DEFAULT NULL,
  `siegeLenght` int(10) DEFAULT NULL,
  `schedule_config` varchar(20) DEFAULT NULL, 
  PRIMARY KEY (`id`),
  KEY `ownerId` (`ownerId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT IGNORE INTO `siegable_clanhall` (`id`, `name`, `ownerId`, `desc`, `location`, `Grade`, `nextSiege`, `siegeLenght`, `schedule_config`) VALUES
(21, 'Fortress of Resistance', 0, 'Contestable Clan Hall', 'Dion', 3, 0, 3600000, '14;0;0;12;00'),
(34, 'Devastated Castle', 0, 'Contestable Clan Hall', 'Aden', 3, 0, 3600000, '14;0;0;12;00'),
(35, 'Bandit StrongHold', 0, 'Contestable Clan Hall', 'Oren', 3, 0, 3600000, '14;0;0;12;00'),
(62, 'Rainbow Springs', 0, 'Contestable Clan Hall', 'Goddard', 3, 0, 3600000, '14;0;0;12;00'),
(63, 'Beast Farm', 0, 'Contestable Clan Hall', 'Rune', 3, 0, 3600000, '14;0;0;12;00'),
(64, 'Fortresss of the Dead', 0, 'Contestable Clan Hall', 'Rune', 3, 0, 3600000, '14;0;0;12;00');