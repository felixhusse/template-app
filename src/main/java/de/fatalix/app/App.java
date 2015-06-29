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
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Button;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
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
@Theme("mytheme")
public class App extends UI{
    
    @Inject
    protected CDIViewProvider viewProvider;
    @Inject
    protected AppLayout appLayout;
    
    private Button logout;
    
    private final Button.ClickListener logoutClickListener = new Button.ClickListener() {
        private static final long serialVersionUID = -1545988729141348821L;

        @Override
        public void buttonClick(Button.ClickEvent event) {
            SecurityUtils.getSubject().logout();
            VaadinSession.getCurrent().close();
            Page.getCurrent().setLocation("");
        }
    };

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
        logout = new Button("Logout", logoutClickListener);
        logout.setIcon(FontAwesome.SIGN_OUT);
        logout.addStyleName("user-menu");
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
            getMenu().addMenuItem(logout);
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
        getMenu().navigateTo(HomeView.id);
        getMenu().addMenuItem(logout);
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
