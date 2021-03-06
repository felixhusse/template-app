/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.fatalix.app.view;

import com.vaadin.cdi.UIScoped;
import com.vaadin.server.Responsive;
import com.vaadin.ui.CssLayout;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import org.vaadin.viritin.layouts.MHorizontalLayout;

/**
 *
 * @author felix.husse
 */
@UIScoped
public class AppLayout extends MHorizontalLayout{
    @Inject private AppMenu appMenu;
    CssLayout content = new CssLayout();
    
    public CssLayout getMainContent() {
        return content;
    }
    
    @PostConstruct
    private void postInit() {
        setSpacing(false);
        setSizeFull();
        content.setPrimaryStyleName("valo-content");
        content.addStyleName("v-scrollable");
        content.setSizeFull();
        addComponents(appMenu, content);
        expand(content);
        addAttachListener(new AttachListener() {
            @Override
            public void attach(AttachEvent event) {
                Responsive.makeResponsive(getUI());
            }
        });
    }
    
    public AppMenu getAppMenu() {
        return appMenu;
    }
    
}
