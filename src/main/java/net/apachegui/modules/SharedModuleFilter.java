package net.apachegui.modules;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;

import net.apachegui.db.SettingsDao;

import net.apachegui.global.Constants;
import org.apache.log4j.Logger;

/**
 * Servlet Filter implementation class SharedModuleFilter
 */
@WebFilter("/SharedModuleFilter")
public class SharedModuleFilter implements Filter {
    Logger log = Logger.getLogger(SharedModuleFilter.class);

    /**
     * Default constructor.
     */
    public SharedModuleFilter() {
    }

    /**
     * @see Filter#destroy()
     */
    public void destroy() {
    }

    /**
     * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            if (SettingsDao.getInstance().getSetting(Constants.INIT) != null) {
                SharedModuleHandler.updateSharedModules();
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        chain.doFilter(request, response);
    }

    /**
     * @see Filter#init(FilterConfig)
     */
    public void init(FilterConfig fConfig) throws ServletException {
    }

}
