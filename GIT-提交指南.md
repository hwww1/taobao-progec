# Git 提交到 GitHub 指南

## 📋 提交前检查

### 1. 确认哪些文件会被提交

```bash
cd 新建文件夹
git status
```

**重要：** 确认以下文件**不会**出现在列表中：
- `.env` （包含敏感信息）
- `backend/target/` （编译产物）
- `frontend/node_modules/` （依赖包）
- `uploads/` （上传的文件）

### 2. 如果看到不应该提交的文件

如果 `.env` 或其他敏感文件出现在列表中，说明它们已经被Git跟踪了，需要移除：

```bash
# 从Git跟踪中移除（但保留本地文件）
git rm --cached .env

# 或者移除整个目录
git rm -r --cached backend/target/
git rm -r --cached frontend/node_modules/
```

## 🚀 提交步骤

### 步骤1：初始化Git仓库（如果还没有）

```bash
cd 新建文件夹
git init
```

### 步骤2：添加文件到暂存区

```bash
# 添加所有文件（.gitignore会自动排除敏感文件）
git add .

# 或者只添加特定文件
git add backend/
git add frontend/
git add docker-compose.yml
git add .gitignore
git add env.example
```

### 步骤3：检查将要提交的文件

```bash
# 查看暂存区的文件列表
git status

# 或者查看详细的文件列表
git ls-files --cached
```

**再次确认：** `.env`、`target/`、`node_modules/` 不应该出现在列表中！

### 步骤4：提交代码

```bash
git commit -m "Initial commit: Taobao Demo Project"
```

### 步骤5：创建GitHub仓库

1. 登录 GitHub (https://github.com)
2. 点击右上角 **"+"** → **"New repository"**
3. 填写仓库信息：
   - Repository name: `taobao-demo` （或您喜欢的名字）
   - Description: `淘宝Demo项目 - Spring Boot + Vue.js`
   - 选择 **Public** 或 **Private**
   - **不要**勾选 "Initialize this repository with a README"
   - **不要**添加 .gitignore 或 license（我们已经有了）
4. 点击 **"Create repository"**

### 步骤6：连接本地仓库到GitHub

```bash
# 添加远程仓库（替换 YOUR_USERNAME 和 YOUR_REPO_NAME）
git remote add origin https://github.com/YOUR_USERNAME/YOUR_REPO_NAME.git

# 例如：
# git remote add origin https://github.com/zhangsan/taobao-demo.git
```

### 步骤7：推送代码到GitHub

```bash
# 设置主分支为 main
git branch -M main

# 推送代码
git push -u origin main
```

如果提示输入用户名和密码：
- **用户名**：您的GitHub用户名
- **密码**：使用 **Personal Access Token**（不是GitHub密码）

### 如何获取 Personal Access Token

1. GitHub → Settings → Developer settings → Personal access tokens → Tokens (classic)
2. 点击 "Generate new token (classic)"
3. 填写 Note（如：`taobao-demo`）
4. 选择过期时间
5. 勾选 `repo` 权限
6. 点击 "Generate token"
7. **复制token**（只显示一次！）
8. 推送时使用这个token作为密码

## ✅ 提交后验证

### 检查GitHub仓库

1. 访问您的GitHub仓库
2. 确认文件已上传
3. **确认 `.env` 文件不存在**（重要！）

### 如果发现提交了敏感文件

**立即处理：**

1. **修改密码/密钥**（如果密码已泄露）
2. **从Git历史中删除敏感文件：**

```bash
# 使用 git filter-branch（Git < 2.24）
git filter-branch --force --index-filter \
  "git rm --cached --ignore-unmatch .env" \
  --prune-empty --tag-name-filter cat -- --all

# 或使用 git filter-repo（推荐，需要先安装）
git filter-repo --path .env --invert-paths

# 强制推送（危险操作，会覆盖远程历史）
git push origin --force --all
```

## 📝 后续更新代码

```bash
# 1. 查看修改
git status

# 2. 添加修改的文件
git add .

# 3. 提交
git commit -m "描述您的修改"

# 4. 推送到GitHub
git push
```

## ⚠️ 重要提醒

1. **永远不要提交 `.env` 文件**
2. **定期检查 `git status` 确认没有敏感文件**
3. **如果使用SSH方式，需要配置SSH密钥**
4. **建议使用 `git status` 检查后再 `git add`**

## 🔐 使用SSH方式（可选）

如果您配置了SSH密钥，可以使用SSH URL：

```bash
git remote add origin git@github.com:YOUR_USERNAME/YOUR_REPO_NAME.git
```

## 📞 常见问题

### Q: 提示 "remote origin already exists"
```bash
# 删除现有远程仓库
git remote remove origin

# 重新添加
git remote add origin https://github.com/YOUR_USERNAME/YOUR_REPO_NAME.git
```

### Q: 推送时提示认证失败
- 确认使用 Personal Access Token 而不是密码
- 或配置SSH密钥

### Q: 想忽略某个文件但已经提交了
```bash
# 从Git中移除但保留本地文件
git rm --cached 文件名

# 提交这个更改
git commit -m "Remove sensitive file from Git"

# 推送
git push
```

