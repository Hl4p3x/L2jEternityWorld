CREATE TABLE IF NOT EXISTS `grandboss_data` (
  `boss_id` smallint(5) unsigned NOT NULL DEFAULT '0',
  `loc_x` mediumint(6) NOT NULL DEFAULT '0',
  `loc_y` mediumint(6) NOT NULL DEFAULT '0',
  `loc_z` mediumint(6) NOT NULL DEFAULT '0',
  `heading` mediumint(6) NOT NULL DEFAULT '0',
  `respawn_time` bigint(13) unsigned NOT NULL DEFAULT '0',
  `currentHP` decimal(30,15) NOT NULL,
  `currentMP` decimal(30,15) NOT NULL,
  `status` tinyint(1) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`boss_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT IGNORE INTO `grandboss_data` VALUES 
(29001, -21610, 181594, -5734, 0, 0, 229898.48, 667.776, 0), 				-- Queen Ant (40)
(29006, 17726, 108915, -6480, 0, 0, 622493.58388, 3793.536, 0), 			-- Core (50)
(29014, 55024, 17368, -5412, 10126, 0, 622493.58388, 3793.536, 0), 			-- Orfen (50)
(29019, 185708, 114298, -8221, 32768, 0, 17850000, 39960, 0), 				-- Antharas (79)
(29020, 116033, 17447, 10107, -25348, 0, 4068372, 39960, 0), 				-- Baium (75)
(29028, -105200, -253104, -15264, 0, 0, 62041918, 2248572, 0), 				-- Valakas (85)
(29062, -16397, -53308, -10448, 16384, 0, 400000, 9999, 0),      		  	-- Andreas Van Halter (80)
(29066, 185708, 114298, -8221,32768, 0, 14518000, 3996000, 0), 				-- Antharas Weak (79)
(29067, 185708, 114298, -8221,32768, 0, 16184000, 3996000, 0), 				-- Antharas Normal (79)
(29068, 185708, 114298, -8221,32768, 0, 62802301, 1998000, 0), 				-- Antharas Strong (85)
(29118, 0, 0, 0, 0, 0, 4109288, 1220547, 0), 						-- Beleth (83)
(29150, -179484, 208692, -15501, 0, 0, 2804633, 409990, 0), 			  	-- Ekimus (85)
(29163, -250403, 207040, -11957, 16285, 0, 8727677, 204995, 0); 		  	-- Tiat (87)