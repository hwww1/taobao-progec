# 多阶段构建 - 前端Dockerfile
# 阶段1: 构建阶段
# 使用阿里云镜像（如果Docker镜像加速器不可用，可以取消注释使用）
# FROM registry.cn-hangzhou.aliyuncs.com/acs/node:18-alpine AS build
FROM node:18-alpine AS build

WORKDIR /app

# 配置npm使用淘宝镜像（加速依赖下载）
RUN npm config set registry https://registry.npmmirror.com

# 复制package文件并安装依赖（包括devDependencies用于构建）
COPY package*.json ./
RUN npm ci

# 复制源代码并构建
COPY . .
RUN npm run build

# 阶段2: 运行阶段（使用Nginx）
FROM nginx:alpine

# 复制构建产物到Nginx目录
COPY --from=build /app/dist /usr/share/nginx/html

# 复制Nginx配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 暴露端口
EXPOSE 80

# 启动Nginx
CMD ["nginx", "-g", "daemon off;"]

