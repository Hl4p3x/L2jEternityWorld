ALTER TABLE `character_recipebook` DROP PRIMARY KEY;
ALTER TABLE `character_recipebook` ADD PRIMARY KEY (`id`,`charId`,`classIndex`);