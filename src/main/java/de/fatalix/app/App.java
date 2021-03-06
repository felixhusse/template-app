/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.fatalix.app;

import com.vaadin.annotations.Theme;
import com.vaadin.cdi.CDIUI;
import com.vaadin.cdi.CDIViewProvider;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.ValoTheme;
import de.fatalix.app.view.AppLayout;
import de.fatalix.app.view.AppMenu;
import de.fatalix.app.view.home.HomeView;
import de.fatalix.app.view.login.LoginView;
import de.fatalix.app.view.login.UserLoggedInEvent;
import javax.enterprise.event.Observes;
import javax.enterprise.event.Reception;
import javax.inject.Inject;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

 /*
 *
 * @author Fatalix
 */
@CDIUI("")
@Theme("gray-theme")
public class App extends UI{
    
    @Inject
    protected CDIViewProvider viewProvider;
    @Inject
    protected AppLayout appLayout;

    @Override
    protected void init(VaadinRequest request) {

        Navigator navigator = new Navigator(this, appLayout.
                getMainContent()) {
                    @Override
                    public void navigateTo(String navigationState) {
                        try {
                            
                            super.navigateTo(navigationState);
                        } catch (Exception e) {
                            handleNavigationError(navigationState, e);
                        }
                    }

                };
        navigator.addProvider(viewProvider);
        setContent(appLayout);

        getNavigator().addViewChangeListener(new ViewChangeListener() {

            @Override
            public boolean beforeViewChange(ViewChangeListener.ViewChangeEvent event) {
                getMenu().setVisible(isLoggedIn());
                if (isLoggedIn()) {
                    if (event.getNewView() instanceof LoginView) {
                        getNavigator().navigateTo(HomeView.id);
                        return false;
                    }
                    return true;
                }
                else {
                    if (!(event.getNewView() instanceof LoginView)) {
                        getNavigator().navigateTo(LoginView.id);
                        return false;
                    }
                    return true;
                }
            }

            @Override
            public void afterViewChange(ViewChangeListener.ViewChangeEvent event) {
                
            }
        });
        
        if (!isLoggedIn()) {
            getMenu().setVisible(false);
            getNavigator().navigateTo(LoginView.id);
        } else {
            getMenu().setVisible(isLoggedIn());
            getMenu().loadMenu(SecurityUtils.getSubject());
            if(getNavigator().getState().isEmpty()) {
                getNavigator().navigateTo(HomeView.id);
            }
        }

    }

    private boolean isLoggedIn() {
        Subject subject = SecurityUtils.getSubject();
        if (subject == null) {
            System.err.println("Could not find subject");
            return false;
        }

        return subject.isAuthenticated();
    }

    public void userLoggedIn(@Observes(notifyObserver = Reception.IF_EXISTS) UserLoggedInEvent event) {
        Notification.show("Welcome back " + event.getUsername());
        getMenu().loadMenu(SecurityUtils.getSubject());
        getMenu().navigateTo(HomeView.id);
        getMenu().setVisible(isLoggedIn());
    }
    
    public AppLayout getAppLayout() {
        return appLayout;
    }

    public CssLayout getContentLayout() {
        return appLayout.getMainContent();
    }

    public static AppMenu getMenu() {
        return ((App) UI.getCurrent()).getAppLayout().getAppMenu();
    }

    /**
     * Workaround for issue 1, related to vaadin issues: 13566, 14884
     *
     * @param navigationState the view id that was requested
     * @param e the exception thrown by Navigator
     */
    protected void handleNavigationError(String navigationState, Exception e) {
        Notification.show(
                "The requested view (" + navigationState + ") was not available, "
                + "entering default screen.", Notification.Type.WARNING_MESSAGE);
        if (navigationState != null && !navigationState.isEmpty()) {
            getNavigator().navigateTo("");
        }
        getSession().getErrorHandler().error(new com.vaadin.server.ErrorEvent(e));
    }
    
    
}
