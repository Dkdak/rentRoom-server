package com.mteam.sleerenthome.service;

import com.mteam.sleerenthome.model.User;

import java.util.List;

public interface IUserService {
    User resisterUser(User user);

    List<User> getUsers();

    void deleteUser(String email);

    User getUser(String email);
}
