CREATE TABLE IF NOT EXISTS `custom_npcaidata` (
  `npcId` mediumint(7) unsigned NOT NULL,
  `minSkillChance` tinyint(3) unsigned NOT NULL DEFAULT '7',
  `maxSkillChance` tinyint(3) unsigned NOT NULL DEFAULT '15',
  `primarySkillId` smallint(5) unsigned DEFAULT '0',
  `agroRange` smallint(4) unsigned NOT NULL DEFAULT '0',
  `canMove` tinyint(1) unsigned NOT NULL DEFAULT '1',
  `targetable` tinyint(1) unsigned NOT NULL DEFAULT '1',
  `showName` tinyint(1) unsigned NOT NULL DEFAULT '1',
  `minRangeSkill` smallint(5) unsigned DEFAULT '0',
  `minRangeChance` tinyint(3) unsigned DEFAULT '0',
  `maxRangeSkill` smallint(5) unsigned DEFAULT '0',
  `maxRangeChance` tinyint(3) unsigned DEFAULT '0',
  `soulShot` smallint(4) unsigned DEFAULT '0',
  `spiritShot` smallint(4) unsigned DEFAULT '0',
  `spsChance` tinyint(3) unsigned DEFAULT '0',
  `ssChance` tinyint(3) unsigned DEFAULT '0',
  `aggro` smallint(4) unsigned NOT NULL DEFAULT '0',
  `isChaos` smallint(4) unsigned DEFAULT '0',
  `clan` varchar(40) DEFAULT NULL,
  `clanRange` smallint(4) unsigned DEFAULT '0',
  `enemyClan` varchar(40) DEFAULT NULL,
  `enemyRange` smallint(4) unsigned DEFAULT '0',
  `dodge` tinyint(3) unsigned DEFAULT '0',
  `aiType` varchar(8) NOT NULL DEFAULT 'fighter',
  PRIMARY KEY (`npcId`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

INSERT IGNORE INTO `custom_npcaidata` VALUES
(50007,7,15,0,1000,0,1,1,0,0,0,0,0,0,0,0,0,0,NULL,300,NULL,0,0,'fighter'),
(70010,7,15,0,1000,0,1,1,0,0,0,0,0,0,0,0,0,0,NULL,300,NULL,0,0,'fighter'),
(70011,7,15,0,1000,0,1,1,0,0,0,0,0,0,0,0,0,0,NULL,300,NULL,0,0,'fighter'),
(70012,7,15,0,1000,0,1,1,0,0,0,0,0,0,0,0,0,0,NULL,300,NULL,0,0,'fighter'),
(1000003,7,15,0,1000,0,1,1,0,0,0,0,0,0,0,0,0,0,NULL,300,NULL,0,0,'fighter'),
-- eventmod Elpies
(900100,7,15,0,1000,0,1,1,0,0,0,0,0,0,0,0,0,0,NULL,300,NULL,0,0,'fighter'),
-- eventmod Rabbits
(900101,7,15,0,1000,0,1,1,0,0,0,0,0,0,0,0,0,0,NULL,300,NULL,0,0,'fighter'),
(900102,7,15,0,1000,0,1,1,0,0,0,0,0,0,0,0,0,0,NULL,300,NULL,0,0,'fighter'),
-- eventmod Race
(900103,7,15,0,1000,0,1,1,0,0,0,0,0,0,0,0,0,0,NULL,300,NULL,0,0,'fighter'),
(900104,7,15,0,1000,0,1,1,0,0,0,0,0,0,0,0,0,0,NULL,300,NULL,0,0,'fighter'),
-- Top Npc
(8888,7,15,0,1000,0,1,1,0,0,0,0,0,0,0,0,0,0,NULL,300,NULL,0,0,'fighter'),
-- Fun Events
(50018,7,15,0,1000,0,1,1,0,0,0,0,0,0,0,0,0,0,NULL,300,NULL,0,0,'fighter'),
(50019,7,15,0,1000,0,1,1,0,0,0,0,0,0,0,0,0,0,NULL,300,NULL,0,0,'fighter'),
(97001,7,15,0,1000,0,1,1,0,0,0,0,0,0,0,0,0,0,NULL,300,NULL,0,0,'fighter'),
(97005,7,15,0,1000,0,1,1,0,0,0,0,0,0,0,0,0,0,NULL,300,NULL,0,0,'fighter'),
(97006,7,15,0,1000,0,1,1,0,0,0,0,0,0,0,0,0,0,NULL,300,NULL,0,0,'fighter'),
(97007,7,15,0,1000,0,1,1,0,0,0,0,0,0,0,0,0,0,NULL,300,NULL,0,0,'fighter'),
-- Scripts NPC
(50022,7,15,0,1000,0,1,1,0,0,0,0,0,0,0,0,0,0,NULL,300,NULL,0,0,'fighter'),
(50023,7,15,0,1000,0,1,1,0,0,0,0,0,0,0,0,0,0,NULL,300,NULL,0,0,'fighter'),
(50024,7,15,0,1000,0,1,1,0,0,0,0,0,0,0,0,0,0,NULL,300,NULL,0,0,'fighter'),
(50026,7,15,0,1000,0,1,1,0,0,0,0,0,0,0,0,0,0,NULL,300,NULL,0,0,'fighter'),
-- Hitman Event
(51,7,15,0,1000,0,1,1,0,0,0,0,0,0,0,0,0,0,NULL,300,NULL,0,0,'fighter'),
-- Last Hero
(77777,7,15,0,1000,0,1,1,0,0,0,0,0,0,0,0,0,0,NULL,300,NULL,0,0,'fighter'),
-- Castles Manager
(77778,7,15,0,1000,0,1,1,0,0,0,0,0,0,0,0,0,0,NULL,300,NULL,0,0,'fighter'),
-- Leprechaun
(7805,7,15,0,1000,0,1,1,0,0,0,0,0,0,0,0,0,0,NULL,300,NULL,0,0,'fighter'),
-- Monsters Rush
(40030,7,15,0,3000,1,1,1,0,0,0,0,0,0,0,0,100,0,'monster_rush_npc',300,'monster_rush',500,0,'fighter'),
(50000,7,15,0,3000,1,1,1,0,0,0,0,0,0,0,0,500,0,'monster_rush',600,'monster_rush_npc',4000,0,'fighter'),
(50001,7,15,0,3000,1,1,1,0,0,0,0,0,0,0,0,500,0,'monster_rush',600,'monster_rush_npc',4000,0,'fighter'),
(50002,7,15,0,3000,1,1,1,0,0,0,0,0,0,0,0,500,0,'monster_rush',600,'monster_rush_npc',4000,0,'fighter'),
(50003,7,15,0,3000,1,1,1,0,0,0,0,0,0,0,0,500,0,'monster_rush',600,'monster_rush_npc',4000,0,'fighter'),
(50004,7,15,0,3000,1,1,1,0,0,0,0,0,0,0,0,500,0,'monster_rush',600,'monster_rush_npc',4000,0,'fighter'),
(50005,7,15,0,3000,1,1,1,0,0,0,0,0,0,0,0,500,0,'monster_rush',600,'monster_rush_npc',4000,0,'fighter'),
(50006,7,15,0,3000,1,1,1,0,0,0,0,0,0,0,0,500,0,'monster_rush',600,'monster_rush_npc',4000,0,'fighter'),
(50008,7,15,0,3000,1,1,1,0,0,0,0,0,0,0,0,500,0,'monster_rush',600,'monster_rush_npc',4000,0,'fighter'),
-- Achievements NPC
(99999,7,15,0,1000,0,1,1,0,0,0,0,0,0,0,0,0,0,NULL,300,NULL,0,0,'fighter'),
-- NPC Buffer
(65535,7,15,0,1000,0,1,1,0,0,0,0,0,0,0,0,0,0,NULL,300,NULL,0,0,'fighter'),
-- Vote Manager
(99998,7,15,0,1000,0,1,1,0,0,0,0,0,0,0,0,0,0,NULL,300,NULL,0,0,'fighter'),
-- Phoenix Events NPC`s
(9101,7,15,0,1000,0,1,1,0,0,0,0,0,0,0,0,0,0,NULL,300,NULL,0,0,'fighter'),
(9102,7,15,0,1000,0,1,1,0,0,0,0,0,0,0,0,0,0,NULL,300,NULL,0,0,'fighter'),
(9103,7,15,0,1000,0,1,1,0,0,0,0,0,0,0,0,0,0,NULL,300,NULL,0,0,'fighter'),
(9108,7,15,0,1000,0,1,1,0,0,0,0,0,0,0,0,0,0,NULL,300,NULL,0,0,'fighter'),
(9109,7,15,0,1000,0,1,1,0,0,0,0,0,0,0,0,0,0,NULL,300,NULL,0,0,'fighter'),
(9110,7,15,0,1000,0,1,1,0,0,0,0,0,0,0,0,0,0,NULL,300,NULL,0,0,'fighter'),
(9999,7,15,0,1000,0,1,1,0,0,0,0,0,0,0,0,0,0,NULL,300,NULL,0,0,'fighter'),
-- AIO Manager
(9910,7,15,0,1000,0,1,1,0,0,0,0,0,0,0,0,0,0,NULL,300,NULL,0,0,'fighter');