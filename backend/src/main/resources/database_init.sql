-- ============================================
-- 淘宝Demo项目 - 完整数据库初始化脚本
-- 用途：创建数据库、表结构，并插入初始测试数据
-- 执行方法：mysql -u root -p < database_init.sql
-- ============================================

-- 1. 创建数据库
DROP DATABASE IF EXISTS taobaodemo;
CREATE DATABASE taobaodemo CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE taobaodemo;

-- ============================================
-- 2. 创建表结构
-- ============================================

-- 2.1 用户表 (user)
CREATE TABLE user (
    user_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) UNIQUE NOT NULL COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    user_type INT NOT NULL COMMENT '用户类型：1-运营商, 2-店铺, 3-顾客, 4-浏览者',
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否已删除（软删除标记）',
    INDEX idx_user_type (user_type),
    INDEX idx_is_deleted (is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 2.2 店铺表 (shop)
CREATE TABLE shop (
    shop_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '店铺ID',
    shop_name VARCHAR(100) UNIQUE NOT NULL COMMENT '店铺名称',
    user_id INT UNIQUE NOT NULL COMMENT '店铺所属用户ID',
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否已删除（软删除标记）',
    INDEX idx_user_id (user_id),
    INDEX idx_is_deleted (is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='店铺表';

-- 2.3 分类表 (category)
CREATE TABLE category (
    category_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',
    name VARCHAR(100) NOT NULL UNIQUE COMMENT '分类名称',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序号，越小越靠前',
    INDEX idx_sort_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品分类表';

-- 2.4 商品表 (product)
CREATE TABLE product (
    product_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '商品ID',
    shop_id INT NOT NULL COMMENT '所属店铺ID',
    name VARCHAR(200) NOT NULL COMMENT '商品名称',
    description TEXT COMMENT '商品描述',
    price DECIMAL(10, 2) NOT NULL COMMENT '商品价格',
    stock INT NOT NULL DEFAULT 0 COMMENT '库存数量',
    image_url VARCHAR(255) COMMENT '商品图片URL',
    is_on_sale BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否在售',
    category_id INT NOT NULL COMMENT '分类ID',
    INDEX idx_shop_id (shop_id),
    INDEX idx_category_id (category_id),
    INDEX idx_is_on_sale (is_on_sale)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品表';

-- 2.5 地址表 (address)
CREATE TABLE address (
    address_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '地址ID',
    user_id INT NOT NULL COMMENT '用户ID',
    receiver_name VARCHAR(50) NOT NULL COMMENT '收货人姓名',
    receiver_phone VARCHAR(20) NOT NULL COMMENT '收货人电话',
    province VARCHAR(50) NOT NULL COMMENT '省份',
    city VARCHAR(50) NOT NULL COMMENT '城市',
    district VARCHAR(50) NOT NULL COMMENT '区/县',
    detail_address VARCHAR(200) NOT NULL COMMENT '详细地址',
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户地址表';

-- 2.6 订单表 (ordermaster) - 合并了订单项信息
CREATE TABLE ordermaster (
    order_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '订单ID',
    customer_id INT NOT NULL COMMENT '顾客ID',
    shop_id INT NULL COMMENT '店铺ID（允许为NULL，店铺删除后设为NULL）',
    order_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '订单创建时间',
    total_amount DECIMAL(10, 2) NOT NULL COMMENT '订单总金额',
    status VARCHAR(50) NOT NULL COMMENT '订单状态：待支付, 待发货, 待收货, 已完成, 已取消',
    receiver_name VARCHAR(50) NOT NULL DEFAULT '' COMMENT '收货人姓名',
    receiver_phone VARCHAR(20) NOT NULL DEFAULT '' COMMENT '收货人电话',
    receiver_address VARCHAR(500) NOT NULL DEFAULT '' COMMENT '收货地址（完整地址字符串）',
    product_id INT NOT NULL COMMENT '商品ID',
    quantity INT NOT NULL COMMENT '购买数量',
    price_at_purchase DECIMAL(10, 2) NOT NULL COMMENT '购买时的商品价格（快照）',
    INDEX idx_customer_id (customer_id),
    INDEX idx_shop_id (shop_id),
    INDEX idx_status (status),
    INDEX idx_order_date (order_date),
    INDEX idx_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表（合并订单项）';

-- 2.7 评价表 (review)
CREATE TABLE review (
    review_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '评价ID',
    product_id INT NOT NULL COMMENT '商品ID',
    customer_id INT NOT NULL COMMENT '顾客ID',
    order_id INT NULL COMMENT '订单ID',
    rating INT NOT NULL COMMENT '评分（1-5）',
    content TEXT COMMENT '评价内容',
    review_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评价时间',
    INDEX idx_product_id (product_id),
    INDEX idx_customer_id (customer_id),
    INDEX idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品评价表';

-- ============================================
-- 3. 插入初始测试数据
-- ============================================

-- 3.1 插入运营商账号
INSERT INTO user (username, password, user_type, is_deleted) VALUES 
('admin', 'admin123', 1, FALSE);

-- 3.2 插入店铺账号和店铺
INSERT INTO user (username, password, user_type, is_deleted) VALUES 
('shop001', '123456', 2, FALSE);

-- 创建店铺（关联到shop001用户）
INSERT INTO shop (shop_name, user_id, is_deleted) VALUES 
('测试店铺', (SELECT user_id FROM user WHERE username = 'shop001'), FALSE);

-- 3.3 插入商品分类（8个）
INSERT INTO category (name, sort_order) VALUES
('女装', 1),
('男装', 2),
('鞋靴', 3),
('美妆', 4),
('数码', 5),
('家电', 6),
('家居', 7),
('食品', 8);

-- 3.4 插入顾客账号
INSERT INTO user (username, password, user_type, is_deleted) VALUES 
('customer001', '123456', 3, FALSE),
('customer002', '123456', 3, FALSE);

-- 3.5 插入测试商品（每个分类都有商品）
INSERT INTO product (shop_id, name, description, price, stock, image_url, is_on_sale, category_id) VALUES 
-- 女装分类
((SELECT shop_id FROM shop WHERE shop_name = '测试店铺'), 
 '时尚连衣裙', '春季新款连衣裙，舒适透气', 299.00, 50, NULL, TRUE, (SELECT category_id FROM category WHERE name='女装')),
((SELECT shop_id FROM shop WHERE shop_name = '测试店铺'), 
 '休闲T恤', '纯棉T恤，多色可选', 89.00, 100, NULL, TRUE, (SELECT category_id FROM category WHERE name='女装')),

-- 男装分类
((SELECT shop_id FROM shop WHERE shop_name = '测试店铺'), 
 '商务衬衫', '正装衬衫，适合职场', 199.00, 80, NULL, TRUE, (SELECT category_id FROM category WHERE name='男装')),
((SELECT shop_id FROM shop WHERE shop_name = '测试店铺'), 
 '休闲牛仔裤', '经典款牛仔裤，百搭', 159.00, 120, NULL, TRUE, (SELECT category_id FROM category WHERE name='男装')),

-- 鞋靴分类
((SELECT shop_id FROM shop WHERE shop_name = '测试店铺'), 
 '运动鞋', '舒适运动鞋，适合跑步', 399.00, 60, NULL, TRUE, (SELECT category_id FROM category WHERE name='鞋靴')),
((SELECT shop_id FROM shop WHERE shop_name = '测试店铺'), 
 '休闲皮鞋', '商务休闲皮鞋', 299.00, 40, NULL, TRUE, (SELECT category_id FROM category WHERE name='鞋靴')),

-- 美妆分类
((SELECT shop_id FROM shop WHERE shop_name = '测试店铺'), 
 '口红套装', '多色口红套装，持久不脱色', 199.00, 30, NULL, TRUE, (SELECT category_id FROM category WHERE name='美妆')),
((SELECT shop_id FROM shop WHERE shop_name = '测试店铺'), 
 '护肤套装', '补水保湿护肤套装', 299.00, 50, NULL, TRUE, (SELECT category_id FROM category WHERE name='美妆')),

-- 数码分类
((SELECT shop_id FROM shop WHERE shop_name = '测试店铺'), 
 'iPhone 15 Pro', '最新款iPhone，性能强劲，拍照出色', 7999.00, 100, NULL, TRUE, (SELECT category_id FROM category WHERE name='数码')),
((SELECT shop_id FROM shop WHERE shop_name = '测试店铺'), 
 '华为Mate 60', '华为旗舰手机，支持5G', 5999.00, 50, NULL, TRUE, (SELECT category_id FROM category WHERE name='数码')),
((SELECT shop_id FROM shop WHERE shop_name = '测试店铺'), 
 'MacBook Pro', '苹果笔记本电脑，M3芯片', 12999.00, 30, NULL, TRUE, (SELECT category_id FROM category WHERE name='数码')),

-- 家电分类
((SELECT shop_id FROM shop WHERE shop_name = '测试店铺'), 
 '智能电视', '55寸4K智能电视', 3999.00, 20, NULL, TRUE, (SELECT category_id FROM category WHERE name='家电')),
((SELECT shop_id FROM shop WHERE shop_name = '测试店铺'), 
 '洗衣机', '全自动洗衣机，10公斤', 2999.00, 25, NULL, TRUE, (SELECT category_id FROM category WHERE name='家电')),

-- 家居分类
((SELECT shop_id FROM shop WHERE shop_name = '测试店铺'), 
 '床上四件套', '纯棉四件套，舒适透气', 299.00, 80, NULL, TRUE, (SELECT category_id FROM category WHERE name='家居')),
((SELECT shop_id FROM shop WHERE shop_name = '测试店铺'), 
 '台灯', '护眼台灯，可调光', 199.00, 100, NULL, TRUE, (SELECT category_id FROM category WHERE name='家居')),

-- 食品分类
((SELECT shop_id FROM shop WHERE shop_name = '测试店铺'), 
 '进口零食大礼包', '多种进口零食组合', 199.00, 200, NULL, TRUE, (SELECT category_id FROM category WHERE name='食品')),
((SELECT shop_id FROM shop WHERE shop_name = '测试店铺'), 
 '有机茶叶', '高山有机茶叶，500g装', 299.00, 150, NULL, TRUE, (SELECT category_id FROM category WHERE name='食品'));

-- 3.6 插入测试地址数据
-- 为 customer001 添加地址
INSERT INTO address (user_id, receiver_name, receiver_phone, province, city, district, detail_address) VALUES
((SELECT user_id FROM user WHERE username = 'customer001'), 
 '张三', '13800138001', '广东', '广州市', '天河区', '天河路123号'),
((SELECT user_id FROM user WHERE username = 'customer001'), 
 '张三', '13800138001', '北京', '北京市', '朝阳区', '建国路88号');

-- 为 customer002 添加地址
INSERT INTO address (user_id, receiver_name, receiver_phone, province, city, district, detail_address) VALUES
((SELECT user_id FROM user WHERE username = 'customer002'), 
 '李四', '13900139002', '上海', '上海市', '浦东新区', '陆家嘴金融街100号'),
((SELECT user_id FROM user WHERE username = 'customer002'), 
 '李四', '13900139002', '江苏', '南京市', '鼓楼区', '中山路200号');

-- ============================================
-- 4. 验证数据
-- ============================================
SELECT '数据库初始化完成！' AS result;
SELECT COUNT(*) AS total_users FROM user WHERE is_deleted = FALSE;
SELECT COUNT(*) AS total_shops FROM shop WHERE is_deleted = FALSE;
SELECT COUNT(*) AS total_products FROM product;
SELECT COUNT(*) AS total_addresses FROM address;
SELECT COUNT(*) AS total_categories FROM category;
