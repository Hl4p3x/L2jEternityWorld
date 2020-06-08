DROP TABLE IF EXISTS `aio_buffs`;
CREATE TABLE `aio_buffs` (
  `category` varchar(45) DEFAULT NULL,
  `buff_name` varchar(45) DEFAULT NULL,
  `buff_id` int(10) DEFAULT NULL,
  `buff_lvl` int(10) DEFAULT NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

INSERT INTO `aio_buffs` VALUES 
('Prophet', 'Invigor', '1032', '315'),
('Prophet', 'Resist Poison', '1033', '330'),
('Prophet', 'Resist Aqua', '1182', '330'),
('Prophet', 'Resist Wind', '1189', '330'),
('Prophet', 'Resist Fire', '1191', '330'),
('Prophet', 'Resist Holy', '1392', '330'),
('Prophet', 'Resist Dark', '1392', '130'),
('Prophet', 'Resist Shock', '1393', '130'),
('Prophet', 'Resist Shock', '1259', '330'),
('Prophet', 'Chant of Victory', '1363', '315'),
('Prophet', 'Blazing Skin', '1232', '330'),
('Prophet', 'Pa\'agrio\'s Fist', '1416', '115'),
('Prophet', 'Clarity', '1397', '130');