-- ----------------------------
-- Table structure for fishing_championship
-- ----------------------------
CREATE TABLE IF NOT EXISTS `fishing_championship` (
  `PlayerName` varchar(35) CHARACTER SET utf8 NOT NULL,
  `fishLength` float(10,2) NOT NULL,
  `rewarded` int(1) NOT NULL
)DEFAULT CHARSET=utf8;
