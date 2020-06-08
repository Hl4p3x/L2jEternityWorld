DROP TABLE IF EXISTS `votes`;
CREATE TABLE `votes` (
  `IP` char(20) NOT NULL DEFAULT '',
  `time_voted` bigint(20) DEFAULT NULL,
  `time_rewarded` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`IP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
