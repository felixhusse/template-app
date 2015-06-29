/*
 * Copyright (c) 2015 Felix Husse under MIT License
 * see LICENSE file
 */
package de.fatalix.app.view.admin;

import com.vaadin.cdi.UIScoped;
import de.fatalix.app.bl.AppUserService;
import de.fatalix.app.bl.model.AppUser;
import java.io.IOException;
import java.util.List;
import javax.inject.Inject;
import javax.mail.MessagingException;


/**
 *
 * @author felix
 */
@UIScoped
public class AdminPresenter {
    
    @Inject private AppUserService service;

    
    public List<AppUser> loadUserList() {
        return service.getAllAppUser();
    }
    
    public AppUser updateUser(AppUser user) {
        return service.updateUser(user);
    }
    
    public AppUser updatePassword(AppUser user, String password) {
        return service.updateUserPassword(user, password);
    }
    
    public AppUser createNewUser() {
        AppUser user = new AppUser();
        user.setUsername("newuser");
        user.setPassword("password");
        return service.createUser(user);
    }
    
    public void deleteUser(AppUser user) {
        service.deleteUser(user);
    }

   
}
