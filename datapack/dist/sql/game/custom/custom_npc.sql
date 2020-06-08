CREATE TABLE IF NOT EXISTS `custom_npc`(
  `id` mediumint(7) unsigned NOT NULL,
  `idTemplate` smallint(5) unsigned NOT NULL,
  `name` varchar(200) NOT NULL DEFAULT '',
  `serverSideName` tinyint(1) NOT NULL DEFAULT '1',
  `title` varchar(45) NOT NULL DEFAULT '',
  `serverSideTitle` tinyint(1) NOT NULL DEFAULT '1',
  `class` varchar(200) DEFAULT NULL,
  `collision_radius` decimal(6,2) DEFAULT NULL,
  `collision_height` decimal(6,2) DEFAULT NULL,
  `level` tinyint(2) DEFAULT NULL,
  `sex` enum('etc','female','male') NOT NULL DEFAULT 'etc',
  `type` varchar(22) DEFAULT NULL,
  `attackrange` smallint(4) DEFAULT NULL,
  `hp` decimal(30,15) DEFAULT NULL,
  `mp` decimal(30,15) DEFAULT NULL,
  `hpreg` decimal(30,15) DEFAULT NULL,
  `mpreg` decimal(30,15) DEFAULT NULL,
  `str` tinyint(2) NOT NULL DEFAULT '40',
  `con` tinyint(2) NOT NULL DEFAULT '43',
  `dex` tinyint(2) NOT NULL DEFAULT '30',
  `int` tinyint(2) NOT NULL DEFAULT '21',
  `wit` tinyint(2) NOT NULL DEFAULT '20',
  `men` tinyint(2) NOT NULL DEFAULT '20',
  `exp` int(9) NOT NULL DEFAULT '0',
  `sp` int(9) NOT NULL DEFAULT '0',
  `patk` decimal(12,5) DEFAULT NULL,
  `pdef` decimal(12,5) DEFAULT NULL,
  `matk` decimal(12,5) DEFAULT NULL,
  `mdef` decimal(12,5) DEFAULT NULL,
  `atkspd` smallint(4) NOT NULL DEFAULT '230',
  `critical` tinyint(1) NOT NULL DEFAULT '1',
  `matkspd` smallint(4) NOT NULL DEFAULT '333',
  `rhand` smallint(5) unsigned NOT NULL DEFAULT '0',
  `lhand` smallint(5) unsigned NOT NULL DEFAULT '0',
  `enchant` tinyint(1) NOT NULL DEFAULT '0',
  `walkspd` decimal(10,5) NOT NULL DEFAULT '60',
  `runspd` decimal(10,5) NOT NULL DEFAULT '120',
  `dropHerbGroup` tinyint(1) NOT NULL DEFAULT '0',
  `basestats` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT IGNORE INTO `custom_npc` VALUES
(50007,31324,'Andromeda',1,'Wedding Manager',1,'NPC.a_casino_FDarkElf',8.00,23.00,70,'female','L2WeddingManager',40,2444,2444,0.00,0.00,10,10,10,10,10,10,0,0,500,500,500,500,278,1,333,0,0,0,28,120,0,0),
(70010,31606,'Catrina',1,'TvT Event Manager',1,'Monster2.queen_of_cat',8.00,15.00,70,'female','L2TvTEventNpc',40,2444,2444,0.00,0.00,10,10,10,10,10,10,0,0,500,500,500,500,278,1,333,0,0,0,28,120,0,0),
(70011,31606,'Sara',1,'Premium Manager',1,'Monster2.queen_of_cat',8.00,15.00,70,'female','L2Npc',40,2444,2444,0.00,0.00,10,10,10,10,10,10,0,0,500,500,500,500,278,1,333,0,0,0,28,120,0,0),
(70012,31606,'Catrina',1,'TvTRound Event Manager',1,'Monster2.queen_of_cat',8.00,15.00,70,'female','L2TvTRoundEventNpc',40,2444,2444,0.00,0.00,10,10,10,10,10,10,0,0,500,500,500,500,278,1,333,0,0,0,28,120,0,0),
(1000003,32226,'Shiela',1,'L2J NPC Buffer',1,'LineageNPC2.K_F1_grand',11.00,22.25,70,'male','L2NpcBuffer',40,2444,2444,0.00,0.00,10,10,10,10,10,10,0,0,500,500,500,500,278,1,333,0,0,0,28,120,0,0),
-- eventmod Elpies
(900100,20432,'Elpy',1,'',1,'LineageMonster.elpy',5.00,4.50,1,'male','L2EventMonster',40,40,36,3.16,0.91,40,43,30,21,20,20,35,2,8,40,7,25,230,1,333,0,0,0,50,80,0,0),
-- eventmod Rabbits
(900101,32365,'Snow',1,'Event Manager',1,'LineageNPC2.TP_game_staff',5.00,12.50,70,'male','L2Npc',40,2444,1225,0.00,0.00,40,43,30,21,20,20,0,0,1086,471,749,313,230,1,333,0,0,0,68,109,0,0),
(900102,13098,'Event Treasure Chest',1,'',1,'LineageMonster.mimic_even',8.50,8.50,80,'male','L2EventChest',40,2880,1524,0.00,0.00,40,43,30,21,20,20,0,0,1499,577,1035,384,230,1,253,0,0,0,1,1,0,0),
-- eventmod Race
(900103,32365,'Start',1,'Event Manager',1,'LineageNPC2.TP_game_staff',5.00,12.50,70,'male','L2Npc',40,2444,1225,0.00,0.00,40,43,30,21,20,20,0,0,1086,471,749,313,230,1,333,0,0,0,68,109,0,0),
(900104,32365,'Finish',1,'Event Manager',1,'LineageNPC2.TP_game_staff',5.00,12.50,70,'male','L2Npc',40,2444,1225,0.00,0.00,40,43,30,21,20,20,0,0,1086,471,749,313,230,1,333,0,0,0,68,109,0,0),
-- Top Npc
(8888,31646,'Rank Stone',1,'Top Players',1,'LineageNPC.codex_stone',7.00,24.00,70,'male','L2Npc',40,2444,2444,0.00,0.00,10,10,10,10,10,10,0,0,500,500,500,500,278,1,333,0,0,0,28,120,0,0),
-- Fun Events
(50018,16046,'CTF Manager',1,'',1,'LineageMonster4.fairy_princess_pet',8.00,24.00,55,'male','L2Npc',40,2444,2444,0.00,0.00,10,10,10,10,10,10,0,0,500,500,500,500,278,0,333,0,0,0,60,60,0,0),
(50019,16046,'DM Manager',1,'',1,'LineageMonster4.fairy_princess_pet',8.00,24.00,55,'male','L2Npc',40,2444,2444,0.00,0.00,10,10,10,10,10,10,0,0,500,500,500,500,278,0,333,0,0,0,60,60,0,0),
(97001,31774,'Event Manager',1,'',1,'Monster.cat_the_cat',9.00,16.00,70,'male','L2Npc',40,3862,1493,11.85,2.78,40,43,30,21,20,10,490,10,1335,470,780,382,278,0,333,0,0,0,88,132,0,0),
(97005,35062,'Zone Flag',1,'',0,'Deco.flag_a',21.00,82.00,80,'etc','L2Npc',40,689694,667,495.04,2.14,60,57,73,76,70,80,2179536,140740,3290,2420,77,3000,278,0,3819,0,0,0,129,372,0,0),
(97006,32027,'CTF Throne',1,'',0,'NpcEV.grail_brazier_b',9.50,29.00,80,'etc','L2Npc',40,689694,667,495.04,2.14,60,57,73,76,70,80,2179536,140740,3290,2420,77,3000,278,0,3819,0,0,0,129,372,0,0),
(97007,35233,'BW Base',0,'',0,'NPC.castle_kent_statue_jewel',25.00,98.00,80,'etc','L2Npc',40,689694,667,495.04,2.14,60,57,73,76,70,80,2179536,140740,3290,2420,77,3000,278,0,3819,0,0,0,129,372,0,0),
-- Scripts NPC
(50022,16045,'Deleveler',1,'',1,'LineageMonster4.ferret_pet',8.00,11.00,55,'male','L2Merchant',40,2444,2444,0.00,0.00,10,10,10,10,10,10,0,0,500,500,500,500,278,0,333,0,0,0,60,60,0,0),
(50023,32075,'Color Name Manager',1,'',1,'Monster2.queen_of_cat',8.00,15.00,70,'female','L2Npc',40,2444,2444,0.00,0.00,10,10,10,10,10,10,0,0,500,500,500,500,278,0,333,0,0,0,60,60,0,0),
(50024,32071,'Rename Manager',1,'',1,'NPC2.a_child_people_Fhuman',5.00,12.30,70,'female','L2Npc',40,3862,1494,0.00,0.00,40,43,30,21,20,20,0,0,1303,471,607,382,253,0,333,0,0,0,24,50,0,0),
(50026,32379,'Server Info',1,'',1,'LineageNPC2.TP_flag',12.00,12.00,90,'male','L2Npc',40,1000,1000,1.00,1.00,40,40,40,40,40,40,0,0,1314,500,500,500,500,0,333,0,0,0,60,60,0,0),
-- Hitman Event
(51,36288,'Agent Guy',1,'Hitman Manager',1,'LineageNPC2.garrison_of_insurgents',10,23.75,75,'female','L2Hitman',40,3862,1494,0.00,0.00,40,43,30,21,20,20,0,0,1303,471,607,382,253,0,333,0,0,0,30,120,0,0),
-- Last Hero
(77777,32130,'Lucifer',1,'Last Hero Manager',1,'LineageMonster3.king_of_cat',6.00,16.01,80,'male','L2Npc',40,2444,2444,0.00,0.00,10,10,10,10,10,10,0,0,500,500,500,500,278,1,333,0,0,0,28,120,0,0),
-- Castles Manager
(77778,32226,'Sandra',1,'Castles Manager',1,'LineageNPC2.K_F1_grand',11.00,22.25,70,'male','L2CastleManage',40,2444,2444,0.00,0.00,10,10,10,10,10,10,0,0,500,500,500,500,278,1,333,0,0,0,28,120,0,0),
-- Leprechaun
(7805,32033,'Leprechaun',1,'Event Manager',1,'LineageNPC2.doctor_chaos','9','15.6',70,'male','L2Npc',40,'2444.46819','1345.8','7.5','2.7',40,43,30,21,20,20,0,0,'688.86373','295.91597','470.40463','216.53847',253,4,333,0,0,0,60,120,0,1),
-- Monsters Rush
(40030, 25603, 'Darion', 1, 'Dion Protector', 1, 'LineageMonster4.darion', 50.00, 81.22, 87, 'male', 'L2Guard',80,2308600,2128,123.48,9.81,60,57,73,76,70,80,0,0,6491,1950,1778,1056,253,4,278,0,0,0,60,70,0,0),
(50000, 22526, 'Morrano', 1, 'Hero of Moria', 1, 'LineageMonster4.Death_slayer_080p', 16.00, 27.20, 83, 'male', 'L2Monster',80,440658,1680,0.00,0.00,60,57,73,76,70,80,3100452,410692,3112,1889,1835,839,230,4,278,0,0,0,73,109,0,1),
(50001, 22526, 'Uruk', 0, '', 0, 'LineageMonster4.Death_slayer_080p', 16.00, 27.20, 83, 'male', 'L2Monster',80,440658,1680,0.00,0.00,60,57,73,76,70,80,3100452,410692,3112,1889,1835,839,230,4,278,0,0,0,73,109,0,1),
(50002, 22526, 'Warg', 0, '', 0, 'LineageMonster4.Death_slayer_080p', 16.00, 27.20, 83, 'male', 'L2Monster',80,440658,1680,0.00,0.00,60,57,73,76,70,80,3100452,410692,3112,1889,1835,839,230,4,278,0,0,0,73,109,0,1),
(50003, 22526, 'Emissary of Death', 0, '', 0, 'LineageMonster4.Death_slayer_080p', 16.00, 27.20, 83, 'male', 'L2Monster',80,440658,1680,0.00,0.00,60,57,73,76,70,80,3100452,410692,3112,1889,1835,839,230,4,278,0,0,0,73,109,0,1),
(50004, 22526, 'Emissary of Hell', 0, '', 0, 'LineageMonster4.Death_slayer_080p', 16.00, 27.20, 83, 'male', 'L2Monster',80,440658,1680,0.00,0.00,60,57,73,76,70,80,3100452,410692,3112,1889,1835,839,230,4,278,0,0,0,73,109,0,1),
(50005, 22536, 'Royal Guard Captain', 0, '', 0, 'LineageMonster4.Dragon_eliteguard_075p', 40.00, 55.00, 84, 'male', 'L2Monster',80,440658,1680,0.00,0.00,60,57,73,76,70,80,3100452,410692,3112,1889,1835,839,230,4,278,0,0,0,73,109,0,1),
(50006, 22538, 'Dragon Steed Troop Commander', 0, '', 0, 'LineageMonster4.Dragon_centurion_135p', 28.00, 46.50, 83, 'male', 'L2Monster',80,440658,1680,0.00,0.00,60,57,73,76,70,80,3100452,410692,3112,1889,1835,839,230,4,278,0,0,0,73,109,0,1),
(50008, 25648, 'Awakened Ancient Fighter', 0, 'Raid Boss', 0, 'LineageMonster4.death_knight_n_156p', 34.32, 62.40, 86, 'male', 'L2Monster',80,440658,1680,0.00,0.00,60,57,73,76,70,80,3100452,410692,3112,1889,1835,839,230,4,278,0,0,0,73,109,0,1),
-- Achievements NPC
(99999,32033,'Locky',1,'Achievements Manager',1,'LineageNPC2.doctor_chaos','9','15.6',70,'male','L2Achievements',40,'2444.46819','1345.8','7.5','2.7',40,43,30,21,20,20,0,0,'688.86373','295.91597','470.40463','216.53847',253,4,333,0,0,0,60,120,0,1),
-- NPC Buffer
(65535,32226,'Shiela',1,'NPC Buffer',1,'LineageNPC2.K_F1_grand',11.00,22.25,70,'male','L2Npc',40,2444,2444,0.00,0.00,10,10,10,10,10,10,0,0,500,500,500,500,278,1,333,0,0,0,28,120,0,0),
-- Vote Manager
(99998,32226,'Silla',1,'Vote Manager',1,'LineageNPC2.K_F1_grand',11.00,22.25,70,'male','L2VoteManager',40,2444,2444,0.00,0.00,10,10,10,10,10,10,0,0,500,500,500,500,278,1,333,0,0,0,28,120,0,0),
-- Phoenix Events NPC`s
(9101,30026,'Simon',1,'Event',1,'NPC.a_fighterguild_master_Mhuman',8.00,23.50,70,'male','L2Npc',40,3862,1493,11.85,2.78,40,43,30,21,20,10,0,0,1314,470,780,382,278,0,333,0,0,0,55,132,0,0),
(9102,18284,'Treasure Chest',0,'',0,'LineageMonster.mimic',8.50,8.50,78,'male','L2Npc',40,4428,1784,13.43,3.09,40,43,30,21,20,10,7300,799,1715,555,1069,451,278,0,333,0,0,0,88,181,0,0),
(9103,31772,'Zone',1,'',0,'LineageNPC.heroes_obelisk_dwarf',23.00,80.00,70,'etc','L2Npc',40,3862,1493,11.85,2.78,40,43,30,21,20,10,0,0,1314,470,780,382,278,0,333,0,0,0,50,120,0,0),
(9108,18284,'Russian',1,'',0,'LineageMonster.mimic',8.50,8.50,78,'male','L2Npc',40,4428,1784,13.43,3.09,40,43,30,21,20,10,7300,799,1715,555,1069,451,278,0,333,0,0,0,88,181,0,0),
(9109,18284,'Bomb',1,'',0,'LineageMonster.mimic',8.50,8.50,78,'male','L2Npc',40,4428,1784,13.43,3.09,40,43,30,21,20,10,7300,799,1715,555,1069,451,278,0,333,0,0,0,88,181,0,0),
(9110,35062,'Base',1,'',0,'LineageDeco.flag_a',21.00,82.00,1,'etc','L2Npc',40,158000,989,3.16,0.91,40,43,30,21,20,10,0.00,0.00,652,753,358,295,423,0,333,0,0,0,55,132,0,0),
(9999,31606,'Tally',1,'Phoenix Events Manager',1,'Monster2.queen_of_cat',8.00,15.00,70,'female','L2Npc',40,2444,2444,0.00,0.00,10,10,10,10,10,10,0,0,500,500,500,500,278,1,333,0,0,0,28,120,0,0),
-- AIO Manager
(9910,36288,'Sage',1,'AIO Manager',1,'LineageNPC2.garrison_of_insurgents',10,23.75,75,'female','L2AioNpc',40,3862,1494,0.00,0.00,40,43,30,21,20,20,0,0,1303,471,607,382,253,0,333,0,0,0,30,120,0,0);