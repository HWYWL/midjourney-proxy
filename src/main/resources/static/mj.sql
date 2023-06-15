/*
 Navicat Premium Data Transfer

 Source Server         : MySQL-本地
 Source Server Type    : MySQL
 Source Server Version : 80032 (8.0.32)
 Source Host           : 172.16.10.90:3306
 Source Schema         : mj

 Target Server Type    : MySQL
 Target Server Version : 80032 (8.0.32)
 File Encoding         : 65001

 Date: 15/06/2023 15:14:27
*/

SET NAMES utf8mb4;

-- ----------------------------
-- Table structure for task_log
-- ----------------------------
CREATE TABLE IF NOT EXISTS `task_log`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `action` enum('IMAGINE','UPSCALE','VARIATION','RESET') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '需要执行的任务',
  `task_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '任务id',
  `prompt` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '描述词',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '执行的描述词',
  `user_id` int NOT NULL COMMENT '执行任务的用户id',
  `submit_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '任务提交时间',
  `finish_time` datetime NULL DEFAULT NULL COMMENT '任务完成时间',
  `image_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'ai生成的图片url',
  `task_status` enum('NOT_START','IN_PROGRESS','FAILURE','SUCCESS') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'NOT_START' COMMENT '任务执行状态',
  `task_progress` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '任务进度',
  `notify_hook` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '回调接口',
  `message_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '聊天的id',
  `message_hash` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '消息的hash',
  `final_prompt` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '最终的执行关键词',
  `related_task_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '关联的任务id',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `task_id_index`(`task_id` ASC) USING BTREE COMMENT '任务索引，id唯一',
  INDEX `user_id_index`(`user_id` ASC) USING BTREE COMMENT '执行任务的用户id',
  INDEX `related_task_id_index`(`related_task_id` ASC) USING BTREE COMMENT '关联任务id'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '任务表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user_info
-- ----------------------------
CREATE TABLE IF NOT EXISTS `user_info`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '用户id',
  `user_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户名',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '密码',
  `state` int NOT NULL DEFAULT 1 COMMENT '用户状态，1：正常、2：封禁',
  `grounds_prohibitio` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '封禁原因',
  `vip_start_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'vip开通时间',
  `vip_end_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'vip结束时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `user_name_index`(`user_name` ASC) USING BTREE COMMENT '唯一约束'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

