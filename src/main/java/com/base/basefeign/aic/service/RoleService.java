package com.base.basefeign.aic.service;

import com.base.basefeign.aic.model.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author ：sunrise
 * @description ：
 * @copyright ：	Copyright 2019 yowits Corporation. All rights reserved.
 * @create ：2019/8/2 21:54
 */
@FeignClient(name = "roleService", url = "${anyi.url}")
public interface RoleService {
    @GetMapping(value = "/v2/get")
    User getRole();
}
