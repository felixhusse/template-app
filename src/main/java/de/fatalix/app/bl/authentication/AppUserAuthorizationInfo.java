/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.fatalix.app.bl.authentication;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.Permission;

/**
 *
 * @author felix.husse
 */
public class AppUserAuthorizationInfo implements AuthorizationInfo{
    
    
    
    @Override
    public Collection<String> getRoles() {
        return Collections.unmodifiableCollection(Arrays.asList(new String[]{"admin","user"}));
    }

    @Override
    public Collection<String> getStringPermissions() {
        return Collections.emptySet();
    }

    @Override
    public Collection<Permission> getObjectPermissions() {
        return Collections.emptySet();
    }
    
}