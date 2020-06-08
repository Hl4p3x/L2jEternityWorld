DROP TABLE IF EXISTS `character_teleport`;
CREATE TABLE `character_teleport` (

  `TpId` int(11) NOT NULL AUTO_INCREMENT,
  
  `charId` int(11) DEFAULT NULL,
  
  `Xpos` text,

  `Ypos` text,

  `Zpos` text,

  `name` text,

  PRIMARY KEY (`TpId`)

) ENGINE=MyISAM AUTO_INCREMENT=321 DEFAULT CHARSET=cp1251;