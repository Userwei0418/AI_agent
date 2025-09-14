use ai_agent;

-- -------------------- RBAC 用户模块 --------------------
-- 用户表
create table if not exists user
(
    id           bigint auto_increment comment 'id' primary key,
    userAccount  varchar(256)                            not null comment '账号',
    userPassword varchar(512)                            not null comment '密码',
    salt         varchar(128)  default null comment '动态盐值',
    userName     varchar(256)                            null comment '用户昵称',
    userGender   varchar(20)                             null comment '性别',
    userAvatar   varchar(1024) default 'https://q6.itc.cn/q_70/images03/20240613/ee600a212edb436785cece5554261729.jpeg' COMMENT '用户头像',
    userPhone    varchar(20)                             null comment '电话',
    userEmail    varchar(255)                            null comment '邮箱',
    userProfile  varchar(512)                            null comment '用户简介',
    shareCode    varchar(20)                             null comment '邀请码',
    inviteUserId bigint                                  null comment '邀请用户ID',
    editTime     datetime      default CURRENT_TIMESTAMP not null comment '编辑时间',
    createTime   datetime      default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime      default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    userStatus   varchar(10)   default 'ACTIVE'          not null comment '用户状态',
    isDelete     tinyint       default 0                 not null comment '是否删除',
    UNIQUE KEY uk_userAccount (userAccount),
    UNIQUE KEY uk_shareCode (shareCode),
    INDEX idx_userName (userName),
    foreign key (inviteUserId) references user (id)
    ) comment '用户' collate = utf8mb4_unicode_ci;

-- 角色表
create table if not exists role
(
    id   bigint auto_increment comment '角色ID' primary key,
    name varchar(256) not null comment '角色名称'
    ) comment '角色' collate = utf8mb4_unicode_ci;

-- 权限表
create table if not exists permission
(
    id   bigint auto_increment comment '权限ID' primary key,
    name varchar(256) not null comment '权限名称',
    code varchar(256) not null comment '权限代码'
    ) comment '权限' collate = utf8mb4_unicode_ci;

-- 用户角色关联表
create table if not exists user_role
(
    userId bigint not null comment '用户ID',
    roleId bigint not null comment '角色ID',
    primary key (userId, roleId),
    foreign key (userId) references user (id),
    foreign key (roleId) references role (id)
    ) comment '用户角色关联' collate = utf8mb4_unicode_ci;

-- 角色权限关联表
create table if not exists role_permission
(
    roleId       bigint not null comment '角色ID',
    permissionId bigint not null comment '权限ID',
    primary key (roleId, permissionId),
    foreign key (roleId) references role (id),
    foreign key (permissionId) references permission (id)
    ) comment '角色权限关联' collate = utf8mb4_unicode_ci;

INSERT INTO role (name)
VALUES ('USER'),
       ('VIP'),
       ('ADMIN');

INSERT INTO user (userAccount, userPassword, salt, userName, userGender, userAvatar, userPhone, userEmail, userProfile,
                  shareCode, inviteUserId, userStatus, isDelete)
VALUES ('admin', '$2a$10$YrootwCQn62xD74FRcGYDOnGF3ZkYLPcXEkUhW5ggYPJbAI0xO4tS', 'eX1Ugn7t4Q8FzQsHhPaUWg==', 'admin',
        'MALE',
        'https://q6.itc.cn/q_70/images03/20240613/ee600a212edb436785cece5554261729.jpeg',
        '19807940898', 'zv041118@163.com', 'admin', 'hB2zPgfMR4', null, 'ACTIVE', 0);

INSERT INTO user_role (userId, roleId)
VALUES (1, 3);


-- 用户管理相关权限
INSERT INTO permission (name, code)
VALUES ('用户查询', 'system:user:query'),
       ('用户新增', 'system:user:add'),
       ('用户修改', 'system:user:edit'),
       ('用户删除', 'system:user:remove'),
       ('用户导出', 'system:user:export'),
       ('用户导入', 'system:user:import'),
       ('重置密码', 'system:user:resetPwd');

-- 角色管理相关权限
INSERT INTO permission (name, code)
VALUES ('角色查询', 'system:role:query'),
       ('角色新增', 'system:role:add'),
       ('角色修改', 'system:role:edit'),
       ('角色删除', 'system:role:remove'),
       ('角色导出', 'system:role:export');

-- 文章管理相关权限
INSERT INTO permission (name, code)
VALUES ('文章查询', 'blog:article:query'),
       ('文章新增', 'blog:article:add'),
       ('文章修改', 'blog:article:edit'),
       ('文章删除', 'blog:article:remove'),
       ('文章发布', 'blog:article:publish'),
       ('文章下架', 'blog:article:unpublish');

-- 标签管理相关权限
INSERT INTO permission (name, code)
VALUES ('标签查询', 'blog:tag:query'),
       ('标签新增', 'blog:tag:add'),
       ('标签修改', 'blog:tag:edit'),
       ('标签删除', 'blog:tag:remove');

-- 评论管理相关权限
INSERT INTO permission (name, code)
VALUES ('评论查询', 'blog:comment:query'),
       ('评论新增', 'blog:comment:add'),
       ('评论修改', 'blog:comment:edit'),
       ('评论删除', 'blog:comment:remove');

-- 点赞管理相关权限
INSERT INTO permission (name, code)
VALUES ('点赞查询', 'blog:like:query'),
       ('点赞新增', 'blog:like:add'),
       ('点赞取消', 'blog:like:remove');

-- USER角色
-- 分配用户查询、修改
INSERT INTO role_permission (roleId, permissionId)
SELECT 1, id
FROM permission
WHERE code IN (
               'system:user:query',
               'system:user:edit'
    );
-- 分配文章查询权限
INSERT INTO role_permission (roleId, permissionId)
SELECT 1, id
FROM permission
WHERE code IN (
               'blog:article:query',
               'blog:comment:add',
               'blog:like:add'
    );

-- VIP角色
-- 分配用户查询、修改、文章查询、新增、修改、评论查询、新增、点赞查询、新增权限
INSERT INTO role_permission (roleId, permissionId)
SELECT 2, id
FROM permission
WHERE code IN (
               'system:user:query',
               'system:user:edit',
               'blog:article:query',
               'blog:article:add',
               'blog:article:edit',
               'blog:comment:query',
               'blog:comment:add',
               'blog:like:query',
               'blog:like:add'
    );

-- ADMIN角色
-- 分配所有权限
INSERT INTO role_permission (roleId, permissionId)
SELECT 3, id
FROM permission;

-- 消息通知
CREATE TABLE if not exists message
(
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    userId     BIGINT       NOT NULL COMMENT '接收用户ID',
    title      VARCHAR(255) NOT NULL COMMENT '标题',
    content    TEXT         NOT NULL COMMENT '内容',
    type       INT          NOT NULL COMMENT '消息类型（0-审核通知，1-系统通知，2-其他通知）',
    isRead     TINYINT DEFAULT 0 COMMENT '是否已读（0-未读，1-已读）',
    createTime DATETIME     NOT NULL COMMENT '创建时间',
    KEY `idx_user_id` (`userId`, `createTime`, `isRead`) -- 优化按用户ID、时间范围、已读状态查询
) comment '消息' collate = utf8mb4_unicode_ci;

