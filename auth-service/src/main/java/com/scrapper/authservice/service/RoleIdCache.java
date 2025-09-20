package com.scrapper.authservice.service;

import com.scrapper.authservice.exception.DefaultRoleMissingException;
import com.scrapper.authservice.exception.UserErrorType;
import com.scrapper.authservice.repository.RoleRepository;
import com.scrapper.authservice.user.RoleType;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class RoleIdCache {

    private final RoleRepository roleRepository;

    public RoleIdCache(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Cacheable(value = "roleIds", key="#roleType")
    public Long getRoleId(RoleType roleType) {
        return roleRepository.findIdByRole(roleType)
                .orElseThrow(() -> new DefaultRoleMissingException(UserErrorType.DEFAULT_ROLE_MISSING, "Role not found: " + roleType));
    }
}
