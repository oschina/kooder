package com.gitee.search.server;

import io.netty.handler.codec.http.HttpHeaders;
import io.vertx.core.net.SocketAddress;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import org.apache.commons.lang3.NotImplementedException;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.net.URI;
import java.security.Principal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * HttpServletRequest wrapper over a vert.x {@link io.vertx.core.http.HttpServerRequest}
 */
public class VertxHttpServletRequest implements HttpServletRequest {

    private final RoutingContext context;
    private final URI requestUri;
    private final DateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH);

    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    public VertxHttpServletRequest(RoutingContext context) {
        this.context = context;
        this.requestUri = URI.create(context.request().absoluteURI());
    }


    @Override
    public String getAuthType() {
        // TODO: AUTH -- if supporting vertx-auth we would need to do something here, and other methods below (marked with TODO: AUTH)
        return null;
    }

    @Override
    public Cookie[] getCookies() {
        Map<String, io.vertx.core.http.Cookie> cookies = context.request().cookieMap();//.cookies();
        Cookie[] results = new Cookie[cookies.size()];
        int i = 0;
        for (io.vertx.core.http.Cookie oneCookie : cookies.values()) {
            results[i] = new Cookie(oneCookie.getName(), oneCookie.getValue());
            results[i].setDomain(oneCookie.getDomain());
            results[i].setPath(oneCookie.getPath());
        }
        return results;
    }

    @Override
    public long getDateHeader(String name) {
        String header = context.request().headers().get(name);
        if (header == null) {
            return -1;
        }
        synchronized (this) {
            try {
                return dateFormat.parse(header).getTime();
            } catch (ParseException e) {
                throw new IllegalArgumentException(e);
            }
        }
    }

    @Override
    public String getHeader(String name) {
        return context.request().headers().get(name);
    }


    @Override
    public Enumeration<String> getHeaders(String name) {
        return Collections.enumeration(context.request().headers().getAll(name));
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        return Collections.enumeration(context.request().headers().names());
    }

    @Override
    public int getIntHeader(String name) {
        String header = context.request().headers().get(name);
        if (header == null) {
            return -1;
        }
        return Integer.parseInt(header);
    }


    @Override
    public String getMethod() {
        return context.request().method().toString();
    }

    @Override
    public String getPathInfo() {
        return context.request().path();
    }

    @Override
    public String getPathTranslated() {
        // TODO: is this the same as return context.normalisedPath();
        throw new NotImplementedException();
    }


    @Override
    public String getContextPath() {
        // TODO: assuming we don't really mount a servlet context, root is ok
        return "/";
    }

    @Override
    public String getQueryString() {
        return context.request().query();
    }

    @Override
    public String getRemoteUser() {
        // TODO: AUTH -- we don't know what type of User we have from Vert.x so can't know the name
        throw new NotImplementedException();
    }

    @Override
    public boolean isUserInRole(String role) {
        // TODO: AUTH -- we could use context.user().isAuthorized(role, asyncCallback) to get the user and ask the role,
        // but sometimes people prefix roles or do other things so we can't be sure how this would
        return false;
    }


    @Override
    public Principal getUserPrincipal() {
        // TODO: AUTH -- would require conversion from context.user().principle() and convert it
        return null;
    }

    @Override
    public String getRequestedSessionId() {
        return context.session().id();
    }


    @Override
    public String getRequestURI() {
        if (requestUri == null) {
            return null;
        }
        return requestUri.getPath();
    }


    @Override
    public StringBuffer getRequestURL() {
        String uri = context.request().absoluteURI();
        if (uri == null) {
            return null;
        }
        int index = uri.indexOf("?");
        return new StringBuffer(index >= 0 ? uri.substring(0, index) : uri);
    }

    @Override
    public String getServletPath() {
        // TODO:  again, no real servlet, so this maybe could be context.currentRoute().getPath()
        throw new NotImplementedException();
    }

    @Override
    public HttpSession getSession(boolean create) {
        return new WrapSession(context.session());
    }

    private class WrapSession implements HttpSession {
        private final Session session;

        WrapSession(Session session) {
            this.session = session;
        }

        @Override
        public long getCreationTime() {
            throw new NotImplementedException();
        }

        @Override
        public String getId() {
            return session.id();
        }

        @Override
        public long getLastAccessedTime() {
            return session.lastAccessed();
        }

        @Override
        public ServletContext getServletContext() {
            throw new NotImplementedException();
        }

        @Override
        public void setMaxInactiveInterval(int interval) {
            throw new NotImplementedException();
        }

        @Override
        public int getMaxInactiveInterval() {
            throw new NotImplementedException();
        }

        @Override
        public HttpSessionContext getSessionContext() {
            throw new NotImplementedException();
        }

        @Override
        public Object getAttribute(String name) {
            return session.get(name);
        }

        @Override
        public Object getValue(String name) {
            return session.get(name);
        }

        @Override
        public Enumeration<String> getAttributeNames() {
            return Collections.enumeration(session.data().keySet());
        }

        @Override
        public String[] getValueNames() {
            return (String[]) session.data().keySet().toArray();
        }

        @Override
        public void setAttribute(String name, Object value) {
            if (value == null) {
                session.remove(name);
            } else {
                session.put(name, value);
            }
        }

        @Override
        public void putValue(String name, Object value) {
            if (value == null) {
                session.remove(name);
            } else {
                session.put(name, value);
            }
        }

        @Override
        public void removeAttribute(String name) {
            session.remove(name);
        }

        @Override
        public void removeValue(String name) {
            session.remove(name);
        }

        @Override
        public void invalidate() {
            session.destroy();
        }

        @Override
        public boolean isNew() {
            return false;
        }
    }


    @Override
    public HttpSession getSession() {
        return new WrapSession(context.session());
    }


    @Override
    public String changeSessionId() {
        throw new NotImplementedException();
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        throw new NotImplementedException();
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        throw new NotImplementedException();
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        throw new NotImplementedException();
    }

    @Override
    public boolean isRequestedSessionIdFromUrl() {
        throw new NotImplementedException();
    }

    @Override
    public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
        // TODO: AUTH
        throw new NotImplementedException();
    }

    @Override
    public void login(String username, String password) throws ServletException {
        // TODO: AUTH
        throw new NotImplementedException();
    }


    @Override
    public void logout() throws ServletException {
        context.clearUser();
        context.session().destroy();
    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        throw new NotImplementedException();
    }


    @Override
    public Part getPart(String name) throws IOException, ServletException {
        throw new NotImplementedException();
    }

    @Override
    public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) throws IOException, ServletException {
        throw new NotImplementedException();
    }

    @Override
    public Object getAttribute(String name) {
        return context.data().get(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return Collections.enumeration(context.data().keySet());
    }

    @Override
    public String getCharacterEncoding() {
        throw new NotImplementedException();
    }


    @Override
    public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
        throw new NotImplementedException();
    }

    @Override
    public int getContentLength() {
        return getIntHeader(HttpHeaders.Names.CONTENT_LENGTH);
    }

    @Override
    public long getContentLengthLong() {
        String header = context.request().headers().get(HttpHeaders.Names.CONTENT_LENGTH);
        if (header == null) {
            return -1;
        }
        return Long.parseLong(header);
    }


    @Override
    public String getContentType() {
        return context.request().headers().get(HttpHeaders.Names.CONTENT_TYPE);
    }


    @Override
    public ServletInputStream getInputStream() throws IOException {
        return new WrappedInputStream(new ByteArrayInputStream(context.getBodyAsString().getBytes()));
    }

    private class WrappedInputStream extends ServletInputStream {
        private final ByteArrayInputStream stream;

        WrappedInputStream(ByteArrayInputStream stream) {
            this.stream = stream;
        }

        @Override
        public boolean isFinished() {
            return false;
        }

        @Override
        public boolean isReady() {
            return stream.available() > 0;
        }

        @Override
        public void setReadListener(ReadListener readListener) {

        }

        @Override
        public int read() throws IOException {
            return stream.read();
        }
    }


    @Override
    public String getParameter(String name) {
        String value = context.request().params().get(name);
        if (value != null) {
            return value;
        }
        List<String> values = context.request().formAttributes().getAll(name);
        if (values != null && !values.isEmpty()) {
            return values.get(0);
        }
        return null;
    }

    @Override
    public Enumeration<String> getParameterNames() {
        List<String> names = new ArrayList<>(context.request().params().names());
        if (!context.request().formAttributes().isEmpty()) {
            names.addAll(context.request().formAttributes().names());
        }
        return Collections.enumeration(names);
    }

    @Override
    public String[] getParameterValues(String name) {

        List<String> values = context.request().params().getAll(name);
        if (!context.request().formAttributes().isEmpty()) {
            List<String> formValues = context.request().formAttributes().getAll(name);
            if (formValues != null && !formValues.isEmpty()) {
                values.addAll(formValues);
            }
        }

        if (values != null && !values.isEmpty()) {
            return values.toArray(new String[values.size()]);
        }

        return EMPTY_STRING_ARRAY;
    }


    @Override
    public Map<String, String[]> getParameterMap() {
        Map<String, List<String>> map = new HashMap<>();

        for (Map.Entry<String, String> e : context.request().params()) {
            List<String> values = map.get(e.getKey());
            if (values == null) {
                values = new ArrayList<>();
                map.put(e.getKey(), values);
            }
            values.add(e.getValue());
        }

        for (Map.Entry<String, String> e : context.request().formAttributes().entries()) {
            List<String> values = map.get(e.getKey());
            if (values == null) {
                values = new ArrayList<>();
                map.put(e.getKey(), values);
            }
            values.add(e.getValue());
        }

        Map<String, String[]> arrayMap = new HashMap<>();

        for (Map.Entry<String, List<String>> e : map.entrySet()) {
            arrayMap.put(e.getKey(), e.getValue().toArray(new String[e.getValue().size()]));
        }

        return arrayMap;
    }

    @Override
    public String getProtocol() {
        return context.request().version().name();
    }

    @Override
    public String getScheme() {
        return requestUri.getScheme();
    }

    @Override
    public String getServerName() {
        return requestUri.getHost();
    }

    @Override
    public int getServerPort() {
        int port = requestUri.getPort();
        if (port == 0) {
            return ("https".equals(getScheme())) ? 443 : 80;
        }
        return port;
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStream()));
    }

    @Override
    public String getRemoteAddr() {
        SocketAddress address = context.request().remoteAddress();
        if (address == null) {
            return null;
        }
        return address.toString();
    }


    @Override
    public String getRemoteHost() {
        return getRemoteAddr();
    }


    @Override
    public void setAttribute(String name, Object o) {
        context.put(name, o);
    }


    @Override
    public void removeAttribute(String name) {
        context.data().remove(name);
    }


    @Override
    public Locale getLocale() {
        String header = context.request().headers().get(HttpHeaders.Names.ACCEPT_LANGUAGE);
        if (header == null) {
            return Locale.US;
        }
        return new Locale(header);
    }


    @Override
    public Enumeration<Locale> getLocales() {
        List<Locale> list = new ArrayList<>();
        list.add(getLocale());
        return Collections.enumeration(list);
    }

    @Override
    public boolean isSecure() {
        // TODO: would be nice if this looked at the proxy / load balancer header too.  But I think servlet spec only talks about the local server itself
        return getScheme().equalsIgnoreCase("https");
    }


    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        throw new NotImplementedException();
    }


    @Override
    public String getRealPath(String path) {
        throw new NotImplementedException();
    }

    @Override
    public int getRemotePort() {
        // TODO: not important
        throw new NotImplementedException();
    }


    @Override
    public String getLocalName() {
        // TODO: we don't have the name handy, ip address works?
        return context.request().localAddress().host();
    }


    @Override
    public String getLocalAddr() {
        return context.request().localAddress().host();
    }

    @Override
    public int getLocalPort() {
        return context.request().localAddress().port();
    }

    @Override
    public ServletContext getServletContext() {
        // TODO: doing this means we never end bleeding and implement another billion parts of servlet spec
        throw new NotImplementedException();
    }


    @Override
    public AsyncContext startAsync() throws IllegalStateException {
        throw new NotImplementedException();
    }


    @Override
    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
        throw new NotImplementedException();
    }


    @Override
    public boolean isAsyncStarted() {
        return false;
    }


    @Override
    public boolean isAsyncSupported() {
        return false;
    }

    @Override
    public AsyncContext getAsyncContext() {
        throw new NotImplementedException();
    }

    @Override
    public DispatcherType getDispatcherType() {
        throw new NotImplementedException();
    }
}