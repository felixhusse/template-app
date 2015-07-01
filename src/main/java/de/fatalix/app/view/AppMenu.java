/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.fatalix.app.view;

import com.vaadin.annotations.Title;
import com.vaadin.cdi.CDIView;
import com.vaadin.cdi.UIScoped;
import com.vaadin.cdi.internal.Conventions;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.ValoTheme;
import de.fatalix.app.bl.AppUserService;
import de.fatalix.app.view.profile.ProfileView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableObject;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.subject.Subject;
import org.vaadin.cdiviewmenu.ViewMenuItem;
import org.vaadin.viritin.button.MButton;
import org.vaadin.viritin.label.Header;

/**
 * 
 * @author felix.husse
 */
@UIScoped
public class AppMenu extends CssLayout{
    
    @Inject private BeanManager beanManager;
    @Inject private AppUserService userService;
    private CssLayout items;
    private final HashMap<String, Button> nameToButton = new HashMap<>();
    
    private MenuBar.MenuItem settingsItem;
    private Button selectedButton;
    private Button active;
    private Component secondaryComponent;
    
    private final Header header = new Header(null).setHeaderLevel(3);
    
    @PostConstruct
    private void postInit() {
        setPrimaryStyleName(ValoTheme.MENU_ROOT);
        addStyleName(ValoTheme.MENU_PART);
        addComponent(createHeader());
        final Button showMenu = new Button("Menu", new Button.ClickListener() {
            @Override
            public void buttonClick(final Button.ClickEvent event) {
                if (getStyleName().contains("valo-menu-visible")) {
                    removeStyleName("valo-menu-visible");
                } else {
                    addStyleName("valo-menu-visible");
                }
            }
        });
        showMenu.addStyleName(ValoTheme.BUTTON_PRIMARY);
        showMenu.addStyleName(ValoTheme.BUTTON_SMALL);
        showMenu.addStyleName("valo-menu-toggle");
        showMenu.setIcon(FontAwesome.LIST);
        addComponent(showMenu);        
        
        MenuBar settings = new MenuBar();
        settings.addStyleName("user-menu");
        settingsItem = settings.addItem("TEST USER",
                new ThemeResource("img/profile-pic-300px.jpg"),
                null);
        settingsItem.addItem("Edit Profile", new MenuBar.Command() {

            @Override
            public void menuSelected(MenuBar.MenuItem selectedItem) {
                UI.getCurrent().getNavigator().navigateTo(ProfileView.id);
            }
        });
        settingsItem.addItem("Preferences", null);
        settingsItem.addSeparator();
        settingsItem.addItem("Sign Out", new MenuBar.Command() {

            @Override
            public void menuSelected(MenuBar.MenuItem selectedItem) {
                SecurityUtils.getSubject().logout();
                VaadinSession.getCurrent().close();
                Page.getCurrent().setLocation("");
            }
        });
        addComponent(settings);
        
        addAttachListener(new AttachListener() {
            @Override
            public void attach(AttachEvent event) {
                getUI().addStyleName("valo-menu-responsive");
                if (getMenuTitle() == null) {
                    setMenuTitle(detectMenuTitle());
                }
                Navigator navigator = UI.getCurrent().getNavigator();
                if (navigator != null) {
                    String state = navigator.getState();
                    if (state == null) {
                        state = "";
                    }
                    Button b = nameToButton.get(state);
                    if (b != null) {
                        emphasisAsSelected(b);
                    }
                }
            }
        }
        );
    }
    
    public void loadMenu(Subject subject) {
        items = new CssLayout();
        settingsItem.setText(subject.getPrincipal().toString());
        settingsItem.setIcon(new ExternalResource(userService.getUserImage(userService.getAppUser(subject.getPrincipal().toString()))));
        items.addComponents(getAsLinkButtons(getAvailableViews(subject)));
        items.setPrimaryStyleName("valo-menuitems");
        addComponent(items);
    }
    
    private HorizontalLayout createHeader() {
        HorizontalLayout headerContent = new HorizontalLayout(header);
        headerContent.setMargin(false);
        headerContent.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
        headerContent.setStyleName(ValoTheme.MENU_TITLE);
        return headerContent;
    }
    
    public List<Bean<?>> getAvailableViews(Subject subject) {
        Set<Bean<?>> all = beanManager.getBeans(View.class,
                new AnnotationLiteral<Any>() {
                });

        final ArrayList<Bean<?>> list = new ArrayList<>();
        for (Bean<?> bean : all) {

            Class<?> beanClass = bean.getBeanClass();

            ViewMenuItem annotation = beanClass.
                    getAnnotation(ViewMenuItem.class);
            if (annotation != null) {
               
                RolesAllowed rolesAnnotation = beanClass.getAnnotation(RolesAllowed.class);
                if (rolesAnnotation != null) {
                    try {
                        subject.checkRoles(rolesAnnotation.value());
                        list.add(bean);
                    } catch (AuthorizationException ex) {
                        System.out.println("Subject " + subject.toString() + " is not allowed to see " + annotation.title() + " " + ex.getMessage());
                    }
                    
                }
                else {
                    list.add(bean);
                }
                
            }
        }

        Collections.sort(list, new Comparator<Bean<?>>() {

            @Override
            public int compare(Bean<?> o1, Bean<?> o2) {
                ViewMenuItem a1 = o1.getBeanClass().
                        getAnnotation(ViewMenuItem.class);
                ViewMenuItem a2 = o2.getBeanClass().
                        getAnnotation(ViewMenuItem.class);
                if (a1 == null && a2 == null) {
                    final String name1 = getNameFor(o1.getBeanClass());
                    final String name2 = getNameFor(o2.getBeanClass());
                    return name1.compareTo(name2); // just compare names
                } else {
                    int order1 = a1 == null ? ViewMenuItem.DEFAULT : a1.order();
                    int order2 = a2 == null ? ViewMenuItem.DEFAULT : a2.order();
                    if (order1 == order2) {
                        final String name1 = getNameFor(o1.getBeanClass());
                        final String name2 = getNameFor(o2.getBeanClass());
                        return name1.compareTo(name2); // just compare names
                    } else {
                        return order1 - order2;
                    }
                }
            }
        });
        
        return list;
    }
    
    private Component[] getAsLinkButtons(List<Bean<?>> availableViews) {

        Collections.sort(availableViews, new Comparator<Bean<?>>() {

            @Override
            public int compare(Bean<?> o1, Bean<?> o2) {
                return 0;
            }
        });

        ArrayList<Button> buttons = new ArrayList<>();
        for (Bean<?> viewBean : availableViews) {

            Class<?> beanClass = viewBean.getBeanClass();

            ViewMenuItem annotation = beanClass.
                    getAnnotation(ViewMenuItem.class
                    );
            if (annotation
                    != null && !annotation.enabled()) {
                continue;
            }

            if (beanClass.getAnnotation(CDIView.class
            ) != null) {
                MButton button = getButtonFor(beanClass);
                CDIView view = beanClass.getAnnotation(CDIView.class);
                String viewId = view.value();
                if (CDIView.USE_CONVENTIONS.equals(viewId)) {
                    viewId = Conventions.deriveMappingForView(beanClass);
                }

                nameToButton.put(viewId, button);
                buttons.add(button);
            }
        }

        return buttons.toArray(new Button[0]);
    }
    
    protected MButton getButtonFor(final Class<?> beanClass) {
        final MButton button = new MButton(getNameFor(beanClass));
        button.setPrimaryStyleName("valo-menu-item");
        button.setIcon(getIconFor(beanClass));
        button.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                navigateTo(beanClass);
            }
        });
        return button;
    }

    protected void emphasisAsSelected(Button button) {
        if (selectedButton != null) {
            selectedButton.removeStyleName("selected");
        }
        button.addStyleName("selected");
        selectedButton = button;
    }

    protected Resource getIconFor(Class<?> viewType) {
        ViewMenuItem annotation = viewType.getAnnotation(ViewMenuItem.class);
        if (annotation == null) {
            return FontAwesome.FILE;
        }
        return annotation.icon();
    }

    protected String getNameFor(Class<?> viewType) {
        ViewMenuItem annotation = viewType.getAnnotation(ViewMenuItem.class);
        if (annotation != null && !annotation.title().isEmpty()) {
            return annotation.title();
        }
        String simpleName = viewType.getSimpleName();
        // remove trailing view
        simpleName = simpleName.replaceAll("View$", "");
        // decamelcase
        simpleName = StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(
                simpleName), " ");
        return simpleName;
    }

    public void setActive(String viewId) {
        if (active != null) {
            active.setEnabled(true);
        }
        active = nameToButton.get(viewId);
        if (active != null) {
            active.setEnabled(false);
        }
    }

    public String getMenuTitle() {
        return header.getText();
    }

    public void setMenuTitle(String menuTitle) {
        this.header.setText(menuTitle);
    }

    private String detectMenuTitle() {
        // try to dig a sane default from Title annotation in UI or class name
        final Class<? extends UI> uiClass = getUI().getClass();
        Title title = uiClass.getAnnotation(Title.class);
        if (title != null) {
            return title.value();
        } else {
            String simpleName = uiClass.getSimpleName();
            return simpleName.replaceAll("UI", "");
        }
    }

    public View navigateTo(final Class<?> viewClass) {
        CDIView cdiview = viewClass.getAnnotation(CDIView.class);
        String viewId = cdiview.value();
        if (CDIView.USE_CONVENTIONS.equals(viewId)) {
            viewId = Conventions.deriveMappingForView(viewClass);
        }
        return navigateTo(viewId);
    }

    public View navigateTo(final String viewId) {
        removeStyleName("valo-menu-visible");
        Button button = nameToButton.get(viewId);
        if (button != null) {
            final Navigator navigator = UI.getCurrent().getNavigator();

            final MutableObject<View> view = new MutableObject<>();

            ViewChangeListener l = new ViewChangeListener() {

                @Override
                public boolean beforeViewChange(
                        ViewChangeListener.ViewChangeEvent event) {
                    return true;
                }

                @Override
                public void afterViewChange(
                        ViewChangeListener.ViewChangeEvent event) {
                    view.setValue(event.getNewView());
                }
            };

            navigator.addViewChangeListener(l);
            navigator.navigateTo(viewId);
            navigator.removeViewChangeListener(l);
            emphasisAsSelected(button);
            return view.getValue();
        }
        return null;
    }

    public void setSecondaryComponent(Component component) {
        if (secondaryComponent != component) {
            if (secondaryComponent != null) {
                removeComponent(secondaryComponent);
            }
            secondaryComponent = component;
            addComponent(component, 1);
        }
    }

    /**
     * Adds a custom button to the menu.
     *
     * @param button
     */
    public void addMenuItem(Button button) {
        button.setPrimaryStyleName("valo-menu-item");
        items.addComponent(button);
    }
    
}
