ALTER TABLE `plug_uris_uri` CHANGE
  `mime_type` `mime_type` VARCHAR( 50 ) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL;

ALTER TABLE `plug_uris_uri` CHANGE
  `err_msg` `err_msg` LONGTEXT CHARACTER SET utf8 COLLATE utf8_bin NOT NULL;
