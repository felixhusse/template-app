/*
 * Copyright (c) 2015 Felix Husse under MIT License
 * see LICENSE file
 */
package de.fatalix.app;

import de.fatalix.app.bl.AppUserService;
import de.fatalix.app.bl.model.AppUser;
import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

/**
 *
 * @author felix.husse
 */
@Startup
@Singleton
public class AppStartup {
    
    @Inject private AppUserService service;
    
    @PostConstruct
    private void init() {
        if (service.getAllAppUser().isEmpty()) {
            AppUser defaultAdminUser = new AppUser();
            defaultAdminUser.setUsername("admin");
            defaultAdminUser.setPassword("password");
            defaultAdminUser.setFullname("Administrator");
            defaultAdminUser.setRoles("admin,user,visitor");
            service.createUser(defaultAdminUser);
        }

    }
    
}
