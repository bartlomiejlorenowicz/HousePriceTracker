package com.scrapper.authservice.service;

import com.scrapper.authservice.entity.Role;
import com.scrapper.authservice.user.RoleType;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoleService {


    private final EntityManager entityManager;
    private final RoleIdCache roleIdCache;

    public RoleService(EntityManager entityManager, RoleIdCache roleIdCache) {
        this.entityManager = entityManager;
        this.roleIdCache = roleIdCache;
    }

    @Transactional(readOnly = true)
    public Role getRoleManaged(RoleType roleType) {
        Long id = roleIdCache.getRoleId(roleType);
        return entityManager.find(Role.class, id);
    }

    @Transactional(readOnly = true)
    public Role getRoleReference(RoleType roleType) {
        Long id = roleIdCache.getRoleId(roleType);
        return entityManager.getReference(Role.class, id);
    }

    // todo change registration service logic
}
