package act.aaa;

import act.conf.AppConfig;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.osgl.aaa.*;
import org.osgl.aaa.impl.SimplePermission;
import org.osgl.aaa.impl.SimplePrivilege;
import org.osgl.aaa.impl.SimpleRole;
import org.osgl.util.IO;

import java.io.File;
import java.net.URL;
import java.util.List;

import static org.mockito.Mockito.mock;

public class YamlLoadTest extends TestBase {

    AAAPersistentService store;

    @Before
    public void setup() throws Exception {
        AAAConfig.ddl.update = false;
        store = new SimplePersistenceService();
    }

    @Test
    public void testSinglePrivilege() throws Exception {
        load("/single-privilege.yaml");
        Privilege p = privilege("admin");
        eq(100, p.getLevel());
    }

    @Test
    public void testSinglePermission() throws Exception {
        load("/single-permission.yaml");
        Permission p = permission("manage-my-profile");
        no(p.isDynamic());
    }

    @Test
    public void testSingleDynaPermission() throws Exception {
        load("/single-dyna-permission.yaml");
        Permission p = permission("manage-my-profile");
        yes(p.isDynamic());
    }

    @Test
    public void testSingleRoleTwoPerms() throws Exception {
        load("/single-role-two-perm.yaml");
        Role r = role("user-admin");
        List<Permission> perms = r.getPermissions();
        eq(2, perms.size());
        yes(r.hasPermission(new SimplePermission("manage-my-profile", true)));
        yes(r.hasPermission(new SimplePermission("manage-profile", false)));
    }

    @Test
    public void testFullAcl() throws Exception {
        AAAConfig.ddl.principal.create = true;
        load("/full-acl.yaml");
        Principal tom = principal("tom@abc.com");
        eq(tom.getPrivilege(), new SimplePrivilege("super-man", 9999));
        yes(tom.getPermissions().size() == 0);
        yes(tom.getRoles().size() == 0);

        Principal mary = principal("mary@abc.com");
        assertNull(mary.getPrivilege());

        List<Role> roles = mary.getRoles();
        yes(roles.contains(role("user")));
        yes(roles.contains(role("user-admin")));
        yes(mary.getPermissions().contains(permission("go-shopping")));
    }

    private void load(String path) throws Exception {
        URL url = getClass().getResource(path);
        String s = IO.readContentAsString(url.openStream());
        AAAService.loadYamlContent(s, store);
    }

    private Permission permission(String name) {
        return store.findByName(name, Permission.class);
    }

    private Privilege privilege(String name) {
        return store.findByName(name, Privilege.class);
    }

    private Role role(String name) {
        return store.findByName(name, Role.class);
    }

    private Principal principal(String name) {
        return store.findByName(name, Principal.class);
    }
}