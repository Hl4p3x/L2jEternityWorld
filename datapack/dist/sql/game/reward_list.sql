DROP TABLE IF EXISTS `reward_list`;
CREATE TABLE `reward_list` (
  `charid` int(11) NOT NULL default '0',
  `itemId` int(11) NOT NULL default '0',
  `count` int(22) NOT NULL default '0',
  `castle_name` varchar(50) NOT NULL default '',
  `rewarded` int(2) NOT NULL default '0',
  PRIMARY KEY  (`charid`,`itemId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;