/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.fatalix.app.bl;

import de.bripkens.gravatar.DefaultImage;
import de.bripkens.gravatar.Gravatar;
import de.bripkens.gravatar.Rating;
import de.fatalix.app.bl.dao.AppUserDAO;
import de.fatalix.app.bl.model.AppUser;
import java.util.List;
import javax.ejb.Stateless;
import javax.inject.Inject;

/**
 *
 * @author felix.husse
 */
@Stateless
public class AppUserService {

    @Inject
    private AppUserDAO appUserDao;

    public AppUser getAppUser(String username) {
        return appUserDao.findByUserName(username);
    }

    public List<AppUser> getAllAppUser() {
        return appUserDao.findAll();
    }

    public AppUser createUser(AppUser user) {

        return appUserDao.save(user);
    }

    public AppUser updateUserPassword(AppUser user, String password) {
        user.setPassword(password);
        return appUserDao.update(user);
    }

    public AppUser updateUser(AppUser user) {
        return appUserDao.update(user);
    }

    public void deleteUser(AppUser user) {
        appUserDao.delete(user.getId());
    }
    
    public String getUserImage(String username) {
        
        String eMail = appUserDao.findByUserName(username).geteMail();
        if (eMail == null || eMail.isEmpty()) {
            eMail = "someone@somewhere.com";
        }
        
        String gravatarImageURL = new Gravatar()
            .setSize(100)
            .setHttps(true)
            .setRating(Rating.GENERAL_AUDIENCE)
            .setStandardDefaultImage(DefaultImage.MONSTER)
            .getUrl(eMail);
        
        return gravatarImageURL;
    }
    
}
