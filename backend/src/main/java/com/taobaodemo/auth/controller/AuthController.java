package com.taobaodemo.auth.controller;

import com.taobaodemo.auth.dto.LoginRequest;
import com.taobaodemo.auth.dto.RegisterRequest;
import com.taobaodemo.auth.entity.User;
import com.taobaodemo.auth.service.UserService;
import com.taobaodemo.common.util.ResponseUtil;
import com.taobaodemo.common.util.SessionUtil;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

/**
 * 用户认证控制器
 * 处理用户登录、注册、登出和获取当前用户信息
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private UserService userService;

    /**
     * 用户登录
     * @param request 登录请求（用户名和密码）
     * @param session HTTP会话
     * @return 登录成功返回用户信息，失败返回错误信息
     */
    @PostMapping("/login")
    public Object login(@Valid @RequestBody LoginRequest request, HttpSession session) {
        String username = request.getUsername();
        String password = request.getPassword();
        
        if (!userService.usernameExists(username)) {
            return ResponseUtil.error("用户名不存在");
        }
        
        Optional<User> userOpt = userService.login(username, password);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            session.setAttribute("currentUser", user);
            return ResponseUtil.success(user);
        } else {
            return ResponseUtil.error("密码错误");
        }
    }

    /**
     * 用户注册
     * @param request 注册请求（用户名、密码、用户类型）
     * @return 注册成功或失败信息
     */
    @PostMapping("/register")
    public Object register(@Valid @RequestBody RegisterRequest request) {
        String username = request.getUsername();
        
        if (userService.usernameExists(username)) {
            return ResponseUtil.error("用户名已存在，请使用其他用户名");
        }
        
        User user = new User();
        user.setUsername(username);
        user.setPassword(request.getPassword());
        user.setUserType(request.getUserType() != null ? request.getUserType() : User.TYPE_CUSTOMER);

        if (userService.register(user)) {
            return ResponseUtil.success(null, "注册成功");
        } else {
            return ResponseUtil.error("注册失败，请重试");
        }
    }

    /**
     * 用户登出
     * @param session HTTP会话
     * @return 登出成功信息
     */
    @PostMapping("/logout")
    public Object logout(HttpSession session) {
        session.invalidate();
        return ResponseUtil.success(null, "登出成功");
    }

    /**
     * 获取当前登录用户信息
     * @param session HTTP会话
     * @return 当前用户信息或未登录提示
     */
    @GetMapping("/current")
    public Object getCurrentUser(HttpSession session) {
        User user = SessionUtil.getCurrentUser(session);
        if (user != null) {
            return ResponseUtil.success(user);
        } else {
            return ResponseUtil.error("未登录");
        }
    }

    /**
     * 修改密码
     * @param request 修改密码请求（新密码）
     * @param session HTTP会话
     * @return 修改结果
     */
    @PostMapping("/change-password")
    public Object changePassword(@RequestBody Map<String, String> request, HttpSession session) {
        User user = SessionUtil.getCurrentUser(session);
        if (user == null) {
            return ResponseUtil.notLoggedIn();
        }
        
        String newPassword = request.get("newPassword");
        if (newPassword == null || newPassword.trim().isEmpty()) {
            return ResponseUtil.error("密码不能为空");
        }
        
        if (userService.changePassword(user.getUserId(), newPassword)) {
            return ResponseUtil.success(null, "密码修改成功");
        } else {
            return ResponseUtil.error("密码修改失败");
        }
    }

    /**
     * 更新用户信息（用户名）
     * @param request 更新请求（用户名）
     * @param session HTTP会话
     * @return 更新结果
     */
    @PutMapping("/profile")
    public Object updateProfile(@RequestBody Map<String, String> request, HttpSession session) {
        User user = SessionUtil.getCurrentUser(session);
        if (user == null) {
            return ResponseUtil.notLoggedIn();
        }
        
        String newUsername = request.get("username");
        if (newUsername == null || newUsername.trim().isEmpty()) {
            return ResponseUtil.error("用户名不能为空");
        }
        
        if (!newUsername.equals(user.getUsername()) && userService.usernameExists(newUsername)) {
            return ResponseUtil.error("用户名已存在，请使用其他用户名");
        }
        
        if (userService.updateUsername(user.getUserId(), newUsername)) {
            user.setUsername(newUsername);
            session.setAttribute("currentUser", user);
            return ResponseUtil.success(user, "用户名修改成功");
        } else {
            return ResponseUtil.error("用户名修改失败");
        }
    }
}

