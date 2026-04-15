package com.gepardec.mega.hexagon.user.domain.services;

import com.gepardec.mega.hexagon.shared.domain.model.Role;
import com.gepardec.mega.hexagon.user.domain.model.OfficeManagementEmails;
import com.gepardec.mega.hexagon.user.domain.model.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

@ApplicationScoped
public class UserRolePolicyService {

    private final OfficeManagementEmails officeManagementEmails;

    @Inject
    public UserRolePolicyService(OfficeManagementEmails officeManagementEmails) {
        this.officeManagementEmails = officeManagementEmails;
    }

    public Set<Role> determineRoles(String email, User existingUser) {
        Set<Role> roles = EnumSet.of(Role.EMPLOYEE);
        if (officeManagementEmails.contains(email)) {
            roles.add(Role.OFFICE_MANAGEMENT);
        }
        if (existingUser != null && existingUser.roles().contains(Role.PROJECT_LEAD)) {
            roles.add(Role.PROJECT_LEAD);
        }
        return roles;
    }

    public Optional<User> updateProjectLeadRoleIfNeeded(User user, boolean shouldBeLead) {
        boolean hasProjectLeadRole = user.roles().contains(Role.PROJECT_LEAD);
        if (shouldBeLead == hasProjectLeadRole) {
            return Optional.empty();
        }

        return Optional.of(shouldBeLead
                ? user.grantProjectLeadRole()
                : user.revokeProjectLeadRole());
    }
}
