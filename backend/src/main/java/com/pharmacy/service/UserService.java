package com.pharmacy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pharmacy.common.PageResult;
import com.pharmacy.dto.LoginDTO;
import com.pharmacy.dto.PasswordDTO;
import com.pharmacy.dto.UserDTO;
import com.pharmacy.entity.User;
import com.pharmacy.vo.LoginVO;
import com.pharmacy.vo.UserVO;

import java.util.List;

public interface UserService extends IService<User> {

    LoginVO login(LoginDTO dto);

    UserVO getUserInfo(Long id);

    PageResult<UserVO> listUsers(int page, int size, String keyword);

    void addUser(UserDTO dto);

    void updateUser(Long id, UserDTO dto);

    void deleteUser(Long id);

    void changePassword(Long userId, PasswordDTO dto);

    List<UserVO> getAllUsers();
}
