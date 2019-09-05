-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT ,
  `age` tinyint(4)  ,
  `weight` float  ,
  `salary` double ,
  `login` varchar(20) ,
  `create_time` datetime(3)  ,
  `is_male` tinyint(4) ,
  `is_del` tinyint(4)  ,
  PRIMARY KEY (`id`, `create_time`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8;
