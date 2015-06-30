/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.fatalix.app.view.profile;

import com.vaadin.cdi.UIScoped;
import de.fatalix.app.bl.AppUserService;
import de.fatalix.app.bl.model.AppUser;
import javax.inject.Inject;

/**
 *
 * @author felix.husse
 */
@UIScoped
public class ProfilePresenter {
    
    @Inject private AppUserService userService;
    
    public AppUser getUser(String username) {
        return userService.getAppUser(username);
    }
    
    public String getProfileImage(AppUser user) {
        return userService.getUserImage(user);
    }
    
}
