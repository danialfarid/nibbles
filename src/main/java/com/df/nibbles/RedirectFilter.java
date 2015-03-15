package com.df.nibbles;

import com.google.api.server.spi.IoUtil;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

public class RedirectFilter implements Filter {

    public static String html;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletResponse resp = (HttpServletResponse) response;
        HttpServletRequest req = (HttpServletRequest) request;
        String[] requestURI = req.getRequestURI().split("/");
        if (isProjectId(requestURI)) {
            if (html == null || req.getParameter("no-cache") != null) {
                String path = req.getSession().getServletContext().getRealPath("/index.html");
                html = IoUtil.readFile(new File(path));
            }
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType("text/html");
            resp.getWriter().write(html);

            return;
        }
        chain.doFilter(req, resp);
    }

    protected boolean isProjectId(String[] parts) {
        return parts.length == 2 && parts[1].length() == 6;
    }

    @Override
    public void init(FilterConfig arg0) throws ServletException {
    }

    @Override
    public void destroy() {
    }
}