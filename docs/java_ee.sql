-- --------------------------------------------------------
-- 主机:                           127.0.0.1
-- 服务器版本:                        10.11.5-MariaDB - mariadb.org binary distribution
-- 服务器操作系统:                      Win64
-- HeidiSQL 版本:                  12.14.0.7165
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


-- 导出 java_ee_db 的数据库结构
DROP DATABASE IF EXISTS `java_ee_db`;
CREATE DATABASE IF NOT EXISTS `java_ee_db` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */;
USE `java_ee_db`;

-- 导出  表 java_ee_db.activity 结构
DROP TABLE IF EXISTS `activity`;
CREATE TABLE IF NOT EXISTS `activity` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `title` varchar(100) NOT NULL,
  `description` text DEFAULT NULL,
  `location` varchar(100) DEFAULT NULL,
  `start_time` datetime DEFAULT NULL,
  `end_time` datetime DEFAULT NULL,
  `publisher_id` bigint(20) unsigned DEFAULT NULL,
  `create_time` datetime DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  KEY `FK_activity_user` (`publisher_id`),
  CONSTRAINT `FK_activity_user` FOREIGN KEY (`publisher_id`) REFERENCES `user` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 正在导出表  java_ee_db.activity 的数据：~2 rows (大约)
DELETE FROM `activity`;
INSERT INTO `activity` (`id`, `title`, `description`, `location`, `start_time`, `end_time`, `publisher_id`, `create_time`) VALUES
	(1, '迎新晚会', '2025年迎新晚会，欢迎新同学！', '大礼堂', '2025-09-01 19:00:00', '2025-09-01 21:00:00', 1, '2025-12-22 11:07:51'),
	(2, '编程马拉松', '24小时编程挑战赛', '实验楼A101', '2025-10-15 08:00:00', '2025-10-16 08:00:00', 1, '2025-12-22 11:07:51');

-- 导出  表 java_ee_db.attendance 结构
DROP TABLE IF EXISTS `attendance`;
CREATE TABLE IF NOT EXISTS `attendance` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) unsigned NOT NULL,
  `activity_id` bigint(20) unsigned NOT NULL,
  `sign_time` datetime DEFAULT current_timestamp(),
  `sign_type` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_attendance_user` (`user_id`),
  KEY `FK_attendance_activity` (`activity_id`),
  CONSTRAINT `FK_attendance_activity` FOREIGN KEY (`activity_id`) REFERENCES `activity` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `FK_attendance_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 正在导出表  java_ee_db.attendance 的数据：~2 rows (大约)
DELETE FROM `attendance`;
INSERT INTO `attendance` (`id`, `user_id`, `activity_id`, `sign_time`, `sign_type`) VALUES
	(1, 2, 1, '2025-12-26 16:59:55', 'MANUAL'),
	(2, 1, 1, '2025-12-26 17:01:05', 'MANUAL');

-- 导出  表 java_ee_db.feedback 结构
DROP TABLE IF EXISTS `feedback`;
CREATE TABLE IF NOT EXISTS `feedback` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) unsigned NOT NULL,
  `activity_id` bigint(20) unsigned DEFAULT NULL,
  `content` text DEFAULT NULL,
  `rating` int(11) DEFAULT NULL,
  `create_time` datetime DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  KEY `FK_feedback_activity` (`activity_id`),
  KEY `FK_feedback_user` (`user_id`),
  CONSTRAINT `FK_feedback_activity` FOREIGN KEY (`activity_id`) REFERENCES `activity` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `FK_feedback_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 正在导出表  java_ee_db.feedback 的数据：~3 rows (大约)
DELETE FROM `feedback`;
INSERT INTO `feedback` (`id`, `user_id`, `activity_id`, `content`, `rating`, `create_time`) VALUES
	(1, 2, 1, '活动很精彩，组织有序！', 5, '2025-09-02 10:00:00'),
	(2, 3, 1, '希望明年能有更多互动环节。', 4, '2025-09-02 11:00:00'),
	(3, 2, 2, '编程马拉松很有挑战性，收获很大。', 5, '2025-10-16 09:00:00');

-- 导出  表 java_ee_db.hello 结构
DROP TABLE IF EXISTS `hello`;
CREATE TABLE IF NOT EXISTS `hello` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `content` text NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=201 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 正在导出表  java_ee_db.hello 的数据：~200 rows (大约)
DELETE FROM `hello`;
INSERT INTO `hello` (`id`, `content`) VALUES
	(1, 'cdaqoxaadhi\nrgl'),
	(2, 'vvp\nbokllyrfdhT'),
	(3, 'ebOs\nIeduteulse'),
	(4, 'enlrwqrrqypRwgw'),
	(5, 'cccVthkbrsvpkal'),
	(6, 'dkbpoqtmucgnVco'),
	(7, 'vaaJgviChhqpjyk'),
	(8, 'UskWWsuavggArwp'),
	(9, 'lgQbg'),
	(10, 'DeqscilvckvaiCl'),
	(11, 'hwqIpydvmndosch'),
	(12, 'qs ikhveadjMrfs'),
	(13, 'ebquyppActwfelo'),
	(14, 'janrEyvoPyycrpl'),
	(15, 'tyjQ shopwOqcgc'),
	(16, 'n gdqgiv   jjbW'),
	(17, 'mxctqxuwiljqDnB'),
	(18, 'fgkuuuufotrrrmd'),
	(19, 'rBByiocagiqihwN'),
	(20, 'hpxfrkrrkkv vip'),
	(21, 'erpfmedthTqlvbs'),
	(22, 'ccmbAkrqgnoplUm'),
	(23, 'yfpbKmmhlhuilxu'),
	(24, 'owvdw ovklxlItq'),
	(25, 'upctwDgvAvlpyjf'),
	(26, 'dn  Anvcoewjjek'),
	(27, 'mdbiwyo kiewuKv'),
	(28, 'pgNmcysmbmnpsnu'),
	(29, 'bpubttFprmbbnex'),
	(30, 'tggcqivs ptnew '),
	(31, 'gxqNvbldHj qpup'),
	(32, 'jxs\nuk\n  WsoTqH'),
	(33, 'fmubbhmhend eas'),
	(34, 'ektsdjjegdnh ke'),
	(35, 'hbquaana tnghpS'),
	(36, 'imQrouSw ru\nEha'),
	(37, 'Qvpi mLeUPbavqu'),
	(38, 'LpbQqwyvtphpjoD'),
	(39, 'vrpcdfKAuajyiaf'),
	(40, 'tfesjwjdoltd gu'),
	(41, 'vmdprnlarive\nqi'),
	(42, 'jLxxnvdi bYkymx'),
	(43, 'trLmpj ldCmeiKt'),
	(44, 'odxbLyt\nmy l ja'),
	(45, 'h r liS gqf\nwdr'),
	(46, 'Frlhimxhoylojls'),
	(47, 'jwfVocfTuWxiwfx'),
	(48, 'd antghsi xongh'),
	(49, 'jjtodcOigoqtcwc'),
	(50, 'necijbrwhr  smq'),
	(51, 'cLfMg'),
	(52, 'bwAddjfpiwfuxbH'),
	(53, 'euithaebflhmcjY'),
	(54, 'qurfvdU epfGtdc'),
	(55, 'uwxeudsvvbo  fq'),
	(56, 'q srenlUibuiXmq'),
	(57, 'lXexvhrojfpbepu'),
	(58, 'i k ugcRYyru qw'),
	(59, 'pd\nnbHKn  rclgL'),
	(60, 'fnmagswoakKtsvk'),
	(61, 'jYj dclljhLflem'),
	(62, 'fqbiktqvyoqn mj'),
	(63, 'oibydiNkyjouvbg'),
	(64, 'isvgwvXacJkejt '),
	(65, 'ovuR ylldqwyyw '),
	(66, 'wsua tpxdfxdi m'),
	(67, 'fmiqProtsyiryng'),
	(68, 'itgdmyfk\npcocyr'),
	(69, 'ef BiLysof\nxblh'),
	(70, 'oyHkafkybnwrukr'),
	(71, 'xMaccatkbB wylt'),
	(72, 'aw kjwuiwaquecu'),
	(73, 'nt oobdkcLkgtwn'),
	(74, 'soq oejmbm tvmg'),
	(75, 'nltjbklx xmiuwR'),
	(76, 'yfe hula uqq\nfl'),
	(77, 'qeMvox coxDigeJ'),
	(78, 'dxlkvetpulmoEci'),
	(79, 'lgvhb waVvisvfy'),
	(80, 'dHeapsoNThxueeu'),
	(81, 'iwftnsrWuygbdli'),
	(82, 'ocqlTiegph'),
	(83, 'bhYhfeqfoph\n om'),
	(84, 'dwJ aakuXovcuno'),
	(85, 'c ggebyqtuftydl'),
	(86, 'xfaixx yklncnoe'),
	(87, 'pudesxsocjclcxu'),
	(88, 'fbaetxqJxrrdkjw'),
	(89, 'nffotdxrub cfyv'),
	(90, 'klrgcfdqfu ctcf'),
	(91, 'oekpcuLugrnrvlv'),
	(92, 'fnXmRxpqikdGxvn'),
	(93, 'leyffuq i qtaoW'),
	(94, 'lD AxcraghkyQtc'),
	(95, 'fsvmQdWLuDhXelb'),
	(96, 'hoh glhdasfPrdK'),
	(97, 'nfshoJoosdyjchk'),
	(98, 'Hnko vDmmmgKlnp'),
	(99, 'xwopexknedphic '),
	(100, 'splrhNkmwwlsdkd'),
	(101, 'qoyylhv utvBrny'),
	(102, 'nKfiHrtx yowera'),
	(103, 'kxPsNnmoosowbja'),
	(104, 'peebBG\nmmdlxil '),
	(105, 'eyw bphtqXUcobd'),
	(106, 'nsdoacwikkuyrkC'),
	(107, 'NRvDtjedujvdt q'),
	(108, 'dkcftaroywlpdoc'),
	(109, 'rnftKecmP csvhD'),
	(110, 'eiousUexhiovowC'),
	(111, 'hucvgpmndbqokdv'),
	(112, 'upMlvxxulirbylj'),
	(113, 'plf urwsEnx'),
	(114, 's\nSitgpo aokfWk'),
	(115, 'pmvurobhykWiqgv'),
	(116, 'Vvffpbwioxysivh'),
	(117, 'lyHphyramwdexve'),
	(118, 'n lnbmdli vyfdI'),
	(119, 'jENUjewkrbumawk'),
	(120, 'jyyg lyknAonmfr'),
	(121, 'cndim\nwwyqnxryf'),
	(122, 'ow vyUnrtUjkbk '),
	(123, 'pgg wxicsKwc\nlo'),
	(124, 'cfMryjd FjrImpj'),
	(125, 'mnbtlYapwHqrefd'),
	(126, 'peslaOdvnegwury'),
	(127, 'pjRjxl nq DRfry'),
	(128, 'iljdd ynphkeqga'),
	(129, 'el rplnjoxiuXg '),
	(130, 'rgnCvejuopiYxon'),
	(131, 'liuuEunjvoavfat'),
	(132, 'gmcyoffvteBlhEO'),
	(133, 'rcekYGjN QnbvvO'),
	(134, 'ipbixFvao lrcno'),
	(135, 'ycjveiAt tmpLfQ'),
	(136, 'Eqh\n rQao dcron'),
	(137, 'waoglqhYqKqofdl'),
	(138, 'wdRjIjmwxkeyruq'),
	(139, 'djxnl\nsvtjrweug'),
	(140, 'sdbrlwx nrnkvpw'),
	(141, 'ninpnsp h odUbd'),
	(142, 'tamvcqgqlBgjdsc'),
	(143, 'qRyewq\nLnqmewra'),
	(144, 'jstkblkenpuvaqu'),
	(145, 'bnmArhr\nbsalrhq'),
	(146, 'uqpef ahklwucml'),
	(147, 'Jfit oghqAqaaxw'),
	(148, 'IyVMkjvdmll\nI f'),
	(149, 'dguldmcgvpvbken'),
	(150, 'p\nsxoVhyvbls pd'),
	(151, 'kMjxmpcPjomljlg'),
	(152, 'pu dxhvhgfysfrh'),
	(153, 'thkPgrptrUeroHl'),
	(154, 'g cSHlddjsvieoj'),
	(155, 'hsSfUc oucWAssj'),
	(156, 'r\niyjomcwgjmljj'),
	(157, 'jvvjr\netqevdlwd'),
	(158, 'OfcexsWnty\nqlpw'),
	(159, 'ljTahpsenxdnpcm'),
	(160, 'CoBkioqciywaord'),
	(161, 'vmrt iwYidvivSo'),
	(162, 'v  ljmdmmsyHfka'),
	(163, 'upjlFsTsudt dl\n'),
	(164, 'Glxfrfw nlxfyrW'),
	(165, 'iinQQdjvwndjygo'),
	(166, 'iDablGV pkiwfdr'),
	(167, 'ixioyiSirtjsnyq'),
	(168, 'blQjelgqfa omci'),
	(169, 'jAybrcjr qo n  '),
	(170, 'wmiq\ncqt ojokxu'),
	(171, 'diawstgxaarwtfd'),
	(172, 'npdqummufeuWgSl'),
	(173, 'grtgxkoxqxicbt '),
	(174, 'pPfkssjBadS xBx'),
	(175, 'hYrgvaf \noaDv n'),
	(176, 'mvuiwagnbndag  '),
	(177, 'hrjnmInehxv ruw'),
	(178, 'rmdksuw kqsstbC'),
	(179, 'oe s\npHxOa  oue'),
	(180, 'lwhsylubkwbbbih'),
	(181, 'jDuxsqvmi FyQeg'),
	(182, 'fbktyiChyuarcxy'),
	(183, 'Lcqxowuuy bs hq'),
	(184, 'lYw gvMhlsvnlsv'),
	(185, 'Dx sknkMwjdvavd'),
	(186, 'mGlvcht nlfgcvR'),
	(187, 'xfnqpsyEpvSqgvm'),
	(188, 'icyccinReg bbsg'),
	(189, 'GtAcnwuqbu\niptu'),
	(190, 'Snihaldetguimkt'),
	(191, 'xdQfhijdjhriHwq'),
	(192, 'ctrbodnikvmDVwa'),
	(193, 'ilkeorg muwlwgs'),
	(194, 'krStvjpiwpjkcdG'),
	(195, 'hpfbexeo\nonjdsa'),
	(196, 'iakq wxbOpqldlk'),
	(197, 'jadvqnshm fcakr'),
	(198, 'yyawrleqmurcurl'),
	(199, 'Rwwqhotnwl wXxb'),
	(200, 'ohp mppxucwaENl');

-- 导出  表 java_ee_db.images 结构
DROP TABLE IF EXISTS `images`;
CREATE TABLE IF NOT EXISTS `images` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `path` varchar(128) NOT NULL,
  `uploader_uid` bigint(20) unsigned NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `FK_images_user` (`uploader_uid`),
  CONSTRAINT `FK_images_user` FOREIGN KEY (`uploader_uid`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 正在导出表  java_ee_db.images 的数据：~0 rows (大约)
DELETE FROM `images`;

-- 导出  表 java_ee_db.operation_log 结构
DROP TABLE IF EXISTS `operation_log`;
CREATE TABLE IF NOT EXISTS `operation_log` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint(20) unsigned NOT NULL COMMENT '操作用户ID',
  `operation` text NOT NULL COMMENT '操作类型',
  `target_id` bigint(20) DEFAULT NULL COMMENT '目标对象ID',
  `target_type` varchar(50) DEFAULT NULL COMMENT '目标类型',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP地址',
  `create_time` datetime DEFAULT current_timestamp() COMMENT '操作时间',
  PRIMARY KEY (`id`),
  KEY `FK_operation_log_user` (`user_id`),
  CONSTRAINT `FK_operation_log_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 正在导出表  java_ee_db.operation_log 的数据：~0 rows (大约)
DELETE FROM `operation_log`;

-- 导出  表 java_ee_db.user 结构
DROP TABLE IF EXISTS `user`;
CREATE TABLE IF NOT EXISTS `user` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `password` varchar(100) NOT NULL,
  `real_name` varchar(50) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `avatar` varchar(50) DEFAULT NULL,
  `role` tinyint(4) DEFAULT NULL,
  `status` tinyint(4) DEFAULT 1,
  `create_time` datetime DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  KEY `FK_user_user_role` (`role`),
  KEY `FK_user_user_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 正在导出表  java_ee_db.user 的数据：~3 rows (大约)
DELETE FROM `user`;
INSERT INTO `user` (`id`, `username`, `password`, `real_name`, `email`, `phone`, `avatar`, `role`, `status`, `create_time`) VALUES
	(1, 'admin', '$2a$10$mX0hX83WiKdOCxswb8V9.OE3Q2jNgXLeeoy0itfQWYZzoxHZJ9MSa', '管理员', 'admin@school.edu', '13800000001', NULL, 1, 1, '2025-12-22 11:07:51'),
	(2, 'alice', '$2a$10$LN5PaAo5adLAsY2M13TTGufih0jQxZ5dJdIOHgECARnen.zoLpThy', '张三', 'alice@school.edu', '13800000002', NULL, 2, 1, '2025-12-22 11:07:51'),
	(3, 'bob', '$2a$10$Txw5V33vB/Z1E1Lg.fKVJeAk7.VtMsgbDAi/zPEgW6PQn.CxkiJaa', '李四', 'bob@school.edu', '13800000003', NULL, 2, 1, '2025-12-22 11:07:51');

-- 导出  表 java_ee_db.user_role 结构
DROP TABLE IF EXISTS `user_role`;
CREATE TABLE IF NOT EXISTS `user_role` (
  `id` tinyint(4) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  `description` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 正在导出表  java_ee_db.user_role 的数据：~2 rows (大约)
DELETE FROM `user_role`;
INSERT INTO `user_role` (`id`, `name`, `description`) VALUES
	(1, '管理员', '系统管理员'),
	(2, '学生', '普通学生用户');

-- 导出  表 java_ee_db.user_status 结构
DROP TABLE IF EXISTS `user_status`;
CREATE TABLE IF NOT EXISTS `user_status` (
  `id` tinyint(4) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(10) DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 正在导出表  java_ee_db.user_status 的数据：~2 rows (大约)
DELETE FROM `user_status`;
INSERT INTO `user_status` (`id`, `name`) VALUES
	(1, '正常'),
	(2, '停用');

-- 导出  触发器 java_ee_db.prevent_admin_delete 结构
DROP TRIGGER IF EXISTS `prevent_admin_delete`;
SET @OLDTMP_SQL_MODE=@@SQL_MODE, SQL_MODE='STRICT_TRANS_TABLES,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION';
DELIMITER //
CREATE TRIGGER prevent_admin_delete
BEFORE DELETE ON user
FOR EACH ROW
BEGIN
  IF OLD.username = 'admin' THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '不能删除 admin 用户';
  END IF;
END//
DELIMITER ;
SET SQL_MODE=@OLDTMP_SQL_MODE;

-- 导出  触发器 java_ee_db.prevent_admin_update 结构
DROP TRIGGER IF EXISTS `prevent_admin_update`;
SET @OLDTMP_SQL_MODE=@@SQL_MODE, SQL_MODE='STRICT_TRANS_TABLES,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION';
DELIMITER //
CREATE TRIGGER prevent_admin_update
BEFORE UPDATE ON user
FOR EACH ROW
BEGIN
  IF OLD.username = 'admin' THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '不能修改 admin 用户';
  END IF;
END//
DELIMITER ;
SET SQL_MODE=@OLDTMP_SQL_MODE;

/*!40103 SET TIME_ZONE=IFNULL(@OLD_TIME_ZONE, 'system') */;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IFNULL(@OLD_FOREIGN_KEY_CHECKS, 1) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40111 SET SQL_NOTES=IFNULL(@OLD_SQL_NOTES, 1) */;
