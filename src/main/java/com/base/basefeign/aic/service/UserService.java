package com.base.basefeign.aic.service;

import com.base.basefeign.aic.model.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * @author ：sunrise
 * @description ：动态url远程调用
 * @copyright ：	Copyright 2019 yowits Corporation. All rights reserved.
 * @create ：2019/8/2 21:23
 */
@FeignClient(name = "UserService")
public interface UserService {

//    @RequestLine("GET")
//    @RequestMapping(method = RequestMethod.GET)
    @GetMapping
    User getUser(@RequestParam("name") String name);

    @GetMapping
    User getUser(@ModelAttribute User user);//参数无效

    @PostMapping
    User createUser(@RequestBody User user);

    @PutMapping
    void updateUser(@RequestBody User user);
}
