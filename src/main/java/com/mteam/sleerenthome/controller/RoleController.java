package com.mteam.sleerenthome.controller;

import com.mteam.sleerenthome.exception.RoleAlreadyExistsException;
import com.mteam.sleerenthome.model.Role;
import com.mteam.sleerenthome.model.User;
import com.mteam.sleerenthome.service.IRoleService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.FOUND;

@Controller
@RequestMapping("/roles")
@RequiredArgsConstructor
public class RoleController {

    private static final Logger logger = LogManager.getLogger(RoomController.class);

    private final IRoleService roleService;

    @GetMapping("/all-roles")
    public ResponseEntity<List<Role>> getAllRoles() {
        return new ResponseEntity<>(roleService.getRoles(), FOUND);

    }

    @PostMapping("/create-new-row")
    public ResponseEntity<String> createRole(@RequestBody Role theRole) {
        logger.info("createRole...{}", theRole.getName());
        try {
            roleService.createRole(theRole);
            return ResponseEntity.ok("new role creates successfullty");
        } catch (RoleAlreadyExistsException re) {

            return ResponseEntity.status(HttpStatus.CONFLICT).body(re.getMessage());
        }
    }

    @DeleteMapping("/delete/{roleId}")
    public void deleteRole(@PathVariable("roleId") Long roleId) {
        roleService.deleteRole(roleId);
    }

    @PostMapping("/remove-all-users-from-role/{roleId}")
    public Role removeAllUsersFromRole(@PathVariable("roleId") Long roleId) {
        return roleService.removeAllUsersFromRole(roleId);
    }

    @PostMapping("remove-user-from-role")
    public User removeUserFromRole(
            @RequestParam("userId") Long userId,
            @RequestParam("roleId") Long roleId) {
        return roleService.removeUserFromRole(userId, roleId);
    }

    @PostMapping("assign-user-to-role")
    public User assignUserToRole(
            @RequestParam("userId") Long userId,
            @RequestParam("roleId") Long roleId) {
        return roleService.assignRoleToUser(userId, roleId);
    }


}
