ALTER TABLE `characters` ADD `lastVoteHopzone` bigint(20) unsigned DEFAULT NULL;
ALTER TABLE `characters` ADD `lastVoteTopzone` bigint(20) unsigned DEFAULT NULL;
ALTER TABLE `characters` ADD `hasVotedHop` int(10) unsigned DEFAULT '0';
ALTER TABLE `characters` ADD `hasVotedTop` int(10) unsigned DEFAULT '0';
ALTER TABLE `characters` ADD `monthVotes` int(10) unsigned DEFAULT '0';
ALTER TABLE `characters` ADD `totalVotes` int(10) unsigned DEFAULT '0';
ALTER TABLE `characters` ADD `tries` int(10) unsigned DEFAULT '3';