CREATE TABLE `users` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `username` char(50) NOT NULL DEFAULT '',
  `passwd` char(50) NOT NULL DEFAULT '',
  `webchat_id` char(50) NOT NULL DEFAULT '',
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8


alter table users add column create_time   DATETIME NOT NULL;


