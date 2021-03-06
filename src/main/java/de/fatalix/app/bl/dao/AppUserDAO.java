/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.fatalix.app.bl.dao;

import de.fatalix.app.bl.DAOBean;
import de.fatalix.app.bl.model.AppUser;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author felix.husse
 */
@Stateless
public class AppUserDAO extends DAOBean<AppUser>{
    @PersistenceContext(unitName = "app-pu")
    private EntityManager entityManager;
    
    @SuppressWarnings("unused")
    @PostConstruct
    private void init() {
        super.init(entityManager, AppUser.class);
    }
    
    public AppUser findByUserName(String username) {
        Map<String, Object> params = new HashMap<>();
        params.put("username", username);
        return getFirstEntity(params, AppUser.FIND_BY_USERNAME);
    }
    
}
