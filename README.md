# vaadin-admin

基于 **Spring Boot + Vaadin + MySQL + MyBatis-Plus + Sa-Token + Java 17** 的 RBAC 管理后台模板，开箱即用，适合作为业务系统的二次开发底座。

## 功能

- 登录 / 退出（Sa-Token 会话管理，密码 BCrypt 加密存储）
- 用户管理：搜索、新增、编辑、启停、删除、分配角色
- 角色管理：新增、编辑、删除、通过菜单树分配权限
- 菜单管理：树形展示，目录 / 菜单 / 按钮三级，维护路由、图标、权限标识
- 页面级权限控制：视图标注 `@RequiresPerm("xxx")`，无权限访问自动拦截
- 侧边导航按当前登录用户的菜单权限动态生成

## 技术栈

| 组件 | 版本 |
|---|---|
| Java | 17 |
| Spring Boot | 3.3.4 |
| Vaadin | 24.4.x |
| MyBatis-Plus | 3.5.7 |
| Sa-Token | 1.39.0 |
| Hutool | 5.8.32 |
| MySQL | 8.x |

## 快速开始

1. **初始化数据库**：本机安装 MySQL 8，执行脚本（会创建 `vaadin_admin` 库并写入种子数据）：

   ```bash
   mysql -uroot -p < sql/vaadin_admin.sql
   ```

2. **修改数据源**：编辑 `src/main/resources/application.yml` 中的 `spring.datasource` 用户名和密码。

3. **启动**：

   ```bash
   mvn spring-boot:run
   ```

4. **访问**：浏览器打开 <http://localhost:8080>，使用 `admin / 123456` 登录。

## 打包部署

```bash
mvn clean package -Pproduction
java -jar target/vaadin-admin-1.0.0.jar
```

## 目录结构

```
├── sql/vaadin_admin.sql        # 建库建表 + 种子数据
└── src/main/
    ├── java/com/example/admin/
    │   ├── config/             # MyBatis-Plus 分页、字段自动填充
    │   ├── security/           # Sa-Token 集成：登录、权限数据源、路由守卫、@RequiresPerm
    │   ├── system/
    │   │   ├── entity/         # sys_user / sys_role / sys_menu / sys_user_role / sys_role_menu
    │   │   ├── mapper/         # BaseMapper + 少量 @Select 连表查询
    │   │   └── service/        # 业务逻辑（关联保存先删后插）
    │   └── ui/                 # LoginView、MainLayout、HomeView、system/* 三个管理页
    └── resources/application.yml
```

## 关键设计说明

- **Sa-Token × Vaadin**：Vaadin 请求不经 Spring MVC，因此不使用 Sa-Token 的 MVC 拦截器；登录态通过在视图代码中直接调用 `StpUtil.login()` 建立（Cookie 写入），页面访问控制由 `SecurityServiceInitListener` 在 Vaadin 导航事件中完成。
- **权限模型**：经典五表 RBAC。角色编码为 `admin` 的用户拥有全部权限（`*` 通配），其余用户按角色关联的菜单 `perms` 判定。
- **按钮级权限**：`sys_menu.type = 2` 的按钮类型与 `StpInterfaceImpl` 权限源已支持，页面中可用 `StpUtil.hasPermission("xxx")` 控制按钮显隐，模板未做完整演示。
- **会话存储**：默认内存存储，重启后需重新登录；生产环境可引入 `sa-token-dao-redis` 持久化。
