/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.fatalix.app.view.profile;

import com.vaadin.cdi.CDIView;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.ExternalResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import de.fatalix.app.bl.model.AppUser;
import de.fatalix.app.view.AbstractView;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

/**
 *
 * @author felix.husse
 */
@CDIView(ProfileView.id)
public class ProfileView extends AbstractView implements View{
    
    @Inject private ProfilePresenter presenter;
    
    public static final String id = "profile";

    private Image profileImage;
    private TextField usernameField;
    private PasswordField passwordField;
    private TextField fullnameField;
    private TextField eMailField;

    
    @PostConstruct
    private void postInit() {
        Component leftComponent = createLeftLayout();
        Component rightComponent = createRightLayout();
        
        HorizontalLayout rootLayout = new HorizontalLayout();
        rootLayout.addStyleName(ValoTheme.LAYOUT_HORIZONTAL_WRAPPING);
        rootLayout.addComponents(leftComponent,rightComponent);
        rootLayout.setExpandRatio(rightComponent, 1.0f);
        //rootLayout.setWidth("100%");
        
        Label sectionLabel = new Label("My Account");
        sectionLabel.addStyleName(ValoTheme.LABEL_H2);
        VerticalLayout compositionRoot = new VerticalLayout();
        compositionRoot.addComponents(sectionLabel,rootLayout);
        compositionRoot.setMargin(true);
        this.setCompositionRoot(compositionRoot);
    }
    
    private Layout createHeader() {
        HorizontalLayout captionLayout = new HorizontalLayout();
        captionLayout.addStyleName("v-panel-caption");
        captionLayout.setWidth("100%");
        captionLayout.addComponents();
        
        return captionLayout;
    }
    
    private Component createLeftLayout() {
        profileImage = new Image();
        profileImage.setWidth(200, Unit.PIXELS);
        profileImage.setHeight(200, Unit.PIXELS);
        VerticalLayout layout = new VerticalLayout(profileImage);
        layout.setWidth(220, Unit.PIXELS);
        
        layout.setMargin(true);
        
        layout.setComponentAlignment(profileImage, Alignment.MIDDLE_CENTER);
        return layout;
    }
    
    private Component createRightLayout() {
        usernameField = new TextField("Username", "some.user");
        usernameField.setWidth("100%");
        
        passwordField = new PasswordField("Password", "somepassword");
        passwordField.setWidth("100%");
        
        fullnameField = new TextField("Fullname", "Some User");
        fullnameField.setWidth("100%");
        
        eMailField = new TextField("EMail", "user@some.de");
        eMailField.setWidth("100%");
        
        VerticalLayout userInfoLayout = new VerticalLayout(usernameField, passwordField, fullnameField, eMailField);
        userInfoLayout.setSpacing(true);
        userInfoLayout.setMargin(true);
        userInfoLayout.setWidth("100%");
        return userInfoLayout;
    }
    
    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        Subject loggedInUser = SecurityUtils.getSubject();
        AppUser user = presenter.getUser(loggedInUser.getPrincipal().toString());
        usernameField.setValue(user.getUsername());
        passwordField.setValue("NOPE%$NOPE");
        fullnameField.setValue(user.getFullname());
        eMailField.setValue(user.geteMail());
        profileImage.setSource(new ExternalResource(presenter.getProfileImage(user)));
    }
    
}
