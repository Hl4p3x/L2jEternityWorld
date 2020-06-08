DROP TABLE IF EXISTS `hwid_bans`;
CREATE TABLE `hwid_bans` (
  `HWID` VARCHAR( 32 ) default NULL ,
  `expiretime` int(11) NOT NULL default '0',
  `comments` varchar(255) default NULL,
  UNIQUE (`HWID` )
) ENGINE = MYISAM;