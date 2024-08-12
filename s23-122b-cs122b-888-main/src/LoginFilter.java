import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

import java.io.File;
import java.io.FileWriter;

/**
 * Servlet Filter implementation class LoginFilter
 */
@WebFilter(filterName = "LoginFilter", urlPatterns = "/*")
public class LoginFilter implements Filter {
    private final ArrayList<String> allowedURIs = new ArrayList<>();
    private final ArrayList<String> notAllowedUserURIs = new ArrayList<>();
    private final ArrayList<String> skippedURIs = new ArrayList<>();
    /**
     * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        System.out.println("LoginFilter: " + httpRequest.getRequestURI());

        // Check if this URL is allowed to access without logging in
        if (this.isUrlAllowedWithoutLogin(httpRequest.getRequestURI())) {
            // Keep default action: pass along the filter chain
            chain.doFilter(request, response);
            return;
        }

        // Redirect to login page if the "user" attribute doesn't exist in session
//        if (allowedURIs.stream().anyMatch(httpRequest.getRequestURI().toLowerCase()::endsWith)){
//            long startTime = System.nanoTime();
//            chain.doFilter(request, response);
//            long endTime = System.nanoTime();
//            long elapsedTime = endTime - startTime;
//            String path = request.getServletContext().getRealPath("/");
//            File file = new File(path+"\\log.txt");
//            FileWriter fr = new FileWriter(file, true);
//            fr.write("TS: " + elapsedTime + "\n");
//            fr.close();
//
//        }
//        else
        if (httpRequest.getSession().getAttribute("userType") == null){
            httpResponse.sendRedirect("login.html");
        }
        else if (httpRequest.getSession().getAttribute("user") == null && httpRequest.getSession().getAttribute("userType").equals("user")) {
            httpResponse.sendRedirect("login.html");
        }else if (httpRequest.getSession().getAttribute("employee") == null && httpRequest.getSession().getAttribute("userType").equals("employee")){
            httpResponse.sendRedirect("_dashboard.html");
        }
        else if(httpRequest.getSession().getAttribute("user") != null && httpRequest.getSession().getAttribute("userType").equals("user")){
            if (!this.isUrlNotAllowedByUser(httpRequest.getRequestURI())) {
                // Keep default action: pass along the filter chain
                chain.doFilter(request, response);
                return;
            }else{
                httpResponse.sendRedirect("main.html");

            }
//            chain.doFilter(request, response);
        }else{
            chain.doFilter(request, response);
        }
    }

    private boolean isUrlAllowedWithoutLogin(String requestURI) {
        /*
         Setup your own rules here to allow accessing some resources without logging in
         Always allow your own login related requests(html, js, servlet, etc..)
         You might also want to allow some CSS files, etc..
         */
        return allowedURIs.stream().anyMatch(requestURI.toLowerCase()::endsWith);
    }

    private boolean isUrlNotAllowedByUser(String requestURI) {
        /*
         Setup your own rules here to allow accessing some resources without logging in
         Always allow your own login related requests(html, js, servlet, etc..)
         You might also want to allow some CSS files, etc..
         */
        return notAllowedUserURIs.stream().anyMatch(requestURI.toLowerCase()::endsWith);
    }

    public void init(FilterConfig fConfig) {
        allowedURIs.add("login.html");
        allowedURIs.add("login.js");
        allowedURIs.add("login.css");
        allowedURIs.add("api/login");
        allowedURIs.add("_dashboard.html");
        allowedURIs.add("_dashboard.js");
        allowedURIs.add("_dashboard.css");
        allowedURIs.add("api/_dashboard");

        notAllowedUserURIs.add("metadata.html");
        notAllowedUserURIs.add("metadata.js");
        notAllowedUserURIs.add("api/metadata");

        skippedURIs.add("api/fulltext");
//        notAllowedUserURIs.add("_dashboard.html");
//        notAllowedUserURIs.add("_dashboard.js");
//        notAllowedUserURIs.add("_dashboard.css");
//        notAllowedUserURIs.add("api/_dashboard");

        notAllowedUserURIs.add("insert-movie.html");
        notAllowedUserURIs.add("insert-movie.js");
        notAllowedUserURIs.add("api/insert-movie");

        notAllowedUserURIs.add("insert-star.html");
        notAllowedUserURIs.add("insert-star.js");
        notAllowedUserURIs.add("api/insert-star");


    }

    public void destroy() {
        // ignored.
    }

}
