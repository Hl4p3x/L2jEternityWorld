DROP TABLE IF EXISTS `character_colors`;
CREATE TABLE IF NOT EXISTS `character_colors`
(
    `char_name` varchar(35) NOT NULL,
    `color` int(8) NOT NULL,
    `reg_time` bigint(40) NOT NULL,
    `time` bigint(40) NOT NULL,
    PRIMARY KEY  (`char_name`)

) ENGINE=InnoDB DEFAULT CHARSET=utf8;