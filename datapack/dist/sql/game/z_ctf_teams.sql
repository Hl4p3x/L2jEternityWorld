DROP TABLE IF EXISTS `z_ctf_teams`;
CREATE TABLE `z_ctf_teams` (
  `teamId` int(4) NOT NULL DEFAULT '0',
  `teamName` varchar(255) NOT NULL DEFAULT '',
  `teamX` int(11) NOT NULL DEFAULT '0',
  `teamY` int(11) NOT NULL DEFAULT '0',
  `teamZ` int(11) NOT NULL DEFAULT '0',
  `flagX` int(11) NOT NULL DEFAULT '0',
  `flagY` int(11) NOT NULL DEFAULT '0',
  `flagZ` int(11) NOT NULL DEFAULT '0',
  `teamColor` varchar(6) NOT NULL DEFAULT '0',
  PRIMARY KEY (`teamId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `z_ctf_teams` VALUES
('0', 'Blue', '109203', '-15352', '-996', '109990', '-14988', '-551', '0000FF'),
('1', 'Red', '113333', '-15344', '-996', '112600', '-15310', '-551', 'FF0000'),
('2', 'Green', '149496', '47826', '-3413', '149496', '47826', '-3413', '00FF00');